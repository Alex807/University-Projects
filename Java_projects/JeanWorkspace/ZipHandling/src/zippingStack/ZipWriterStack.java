package zippingStack;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipWriterStack {

	public static File archiveResorcesToZip(File sourceData, File destinationDir, String zipName, boolean startFromItemItself) throws IOException { 
		if (sourceData == null || !sourceData.exists()) {//check if path really exists on disk, an is not a link or something
			throw new IllegalArgumentException(String.format(
					"Path for resources to read <%s> is NOT existing on disk. Please check method inputs!", sourceData.getAbsolutePath()));
		
		} else if (destinationDir == null || !destinationDir.exists() ) { 
			throw new IllegalArgumentException(String.format(
					"Path for place to save Zip archive <%s> is NOT existing on disk. Please check method inputs!", destinationDir.getAbsolutePath()));
		}
		
		if(!sourceData.isFile() && !sourceData.isDirectory()) { 
			throw new IllegalArgumentException(
					"Recieved parameter 'toZipResources' is NOT a file or a directory. Please check method inputs !");
		}
		
		if (!zipName.toLowerCase().endsWith(".zip")) { //make sure we have correct zip's archive name
			zipName += ".zip";
		}
		
		File zipArchive = new File(destinationDir, zipName);
		
		FileOutputStream fileOutStream = new FileOutputStream(zipArchive);
		try {
			
			BufferedOutputStream bufferedOut = new BufferedOutputStream(fileOutStream);
			try { 
				
				ZipOutputStream zipOutStream = new ZipOutputStream(bufferedOut);
				try { 
					
					Stack<StackItem> stack = new Stack<StackItem>();
					
					if (startFromItemItself || sourceData.isFile()) {
						StackItem rootItem = new StackItem(sourceData, ""); //first level of items in read directory
						stack.push(rootItem);
					
					} else {
						File[] children = sourceData.listFiles();						
						for (File f : children) { //put all items in stack to can be processed later
							StackItem rootItem = new StackItem(f, "");
							stack.push(rootItem);
						}						
					}
					
					byte[] buffer = new byte[1024];
					while (!stack.isEmpty()) { //process all items from stack, until none is left
						
						StackItem stackItem = stack.pop(); 
						File currentItem = stackItem.getItem();
						String currentPath = stackItem.getParentItemPath();
						
						if (currentItem.isDirectory()) { //put again all items. until only files are left to be processed
							File[] children = currentItem.listFiles();	
							
							String newCurrentPath; 
							if (currentPath.equals("")) { 
								newCurrentPath =  currentItem.getName(); 
							} else { 
								newCurrentPath = currentPath + "/" + currentItem.getName(); 
							}
							
							zipOutStream.putNextEntry(new ZipEntry(newCurrentPath + "/")); //used to create empty directories, before populate them
							zipOutStream.closeEntry(); 
							
							for (File f : children) { 
								StackItem childStackItem = new StackItem(f, newCurrentPath);
								stack.push(childStackItem);
							}						
						} else if (currentItem.isFile()) { // add the file to the zip's archive
							
							FileInputStream fileInStream = new FileInputStream(currentItem); //create an input stream to can read current file	
							try {  
								
								BufferedInputStream bufferedInput = new BufferedInputStream(fileInStream);
								try {
									
									String zipEntryName; 
									if (currentPath.equals("")) { //first level in zip's archive
										zipEntryName = currentItem.getName(); 
									} else { 
										zipEntryName = currentPath + "/" + currentItem.getName();
									}
									
									zipOutStream.putNextEntry(new ZipEntry(zipEntryName)); //create a zip's archive entry for this specific file
									
									int length; 
									while ((length = bufferedInput.read(buffer)) > 0) { //ensure that we read something
										zipOutStream.write(buffer, 0, length);
									}
									zipOutStream.closeEntry(); //close only writing in this specific file in zip's archive
								
								} finally { 
									bufferedInput.close();
								}
								
							} finally {
								fileInStream.close();
							}
							
						} else { ///case when link or something is stored in directory
							throw new IllegalStateException("Unknon type of item!!");
						}
					} 
				} finally { //close stream opened above for writing in zip's archive
					zipOutStream.close();
				}
				
			} finally { //here is closed stream's opened, not above
				bufferedOut.close();
			}
		} finally { //a try-finally for each opened stream, keep in mind
			fileOutStream.close();
		}
		
		return zipArchive;
	}
	
	public static void main(String[] args) {
		String sourcePath = "E:\\Github\\Personal-Projects\\Java_projects\\JeanWorkspace\\ZipHandling\\testZIP";
		String zipName = "Archive.zip"; //make sure you give this parameter with '.zip' extension
		String destinationPath = "E:\\Github\\Personal-Projects\\Java_projects\\JeanWorkspace\\ZipHandling"; 
	
		if (args.length == 1) { 
			sourcePath = args[0];
		}
		File toZipResources = new File(sourcePath); 
		File destinationZipArchive = new File(destinationPath);
		boolean startFromItemItself = false; //if it's 'true' will put all sub-directories saved in zip's archive in one large directory, if it's 'false' will pe spill 
		
		try { 
			 ZipWriterStack.archiveResorcesToZip(toZipResources, destinationZipArchive, zipName, startFromItemItself);
		}
		catch (Exception exception1){ 
			exception1.printStackTrace();
		}
	}
}
