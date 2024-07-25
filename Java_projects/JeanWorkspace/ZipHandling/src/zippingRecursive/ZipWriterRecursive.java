package zippingRecursive;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipWriterRecursive {
	
	private static void zipDirectory(File readDirectory, String parentFolderName, ZipOutputStream zipOutStream, byte[] buffer) throws IOException { 
		File[] allItems = readDirectory.listFiles();
	    if (allItems == null) {  //base case for recursion
	        return;
	    }
	    
	    if (parentFolderName.endsWith("/")) { //check to NOT have in path double '/' between directories
	    	int indexOfLastCharacter = parentFolderName.length() - 1;
	    	parentFolderName = parentFolderName.substring(0, indexOfLastCharacter); //remove last '/' because new one is added below
	    }
		
	    for (File currentItem : allItems) {
	        if (currentItem.isDirectory()) {
	        	String folderName = parentFolderName + "/" + currentItem.getName() + "/";
	            
	        	try {	            	// create path for directory
		            zipOutStream.putNextEntry(new ZipEntry(folderName)); //create dir in zip
	            }
	        	finally { 
		            zipOutStream.closeEntry(); //close entry, we wanted only to create it
	        	}
	            
	            //use only this separator for 'in zip path entry'
	            zipDirectory(currentItem, folderName, zipOutStream, buffer);
	        } else {
	            zipFile(currentItem, parentFolderName + "/" + currentItem.getName(), zipOutStream, buffer);
	        } 
	    }
	}

	
	private static void zipFile(File readFile, String zipEntryName, ZipOutputStream zipOutStream, byte[] buffer) throws IOException{ 
		FileInputStream fileInStream = new FileInputStream(readFile); //create an input stream to can read received file
		try {
			BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInStream);
			try {
				zipOutStream.putNextEntry(new ZipEntry(zipEntryName)); //create a zip's entry in archive for this specific file
				int length; 
				while ((length = bufferedInputStream.read(buffer)) > 0) { //ensure that we read something
					zipOutStream.write(buffer, 0, length);
				}
				
				zipOutStream.closeEntry();
			
			} finally {
				bufferedInputStream.close();
			}
		} finally {
			fileInStream.close();
		}
	}

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
		byte[] buffer = new byte[1024];  //create a buffer to read files from given resource 
		
		FileOutputStream fileOutStream = new FileOutputStream(zipArchive);
		try {
			ZipOutputStream zipOutStream = new ZipOutputStream(fileOutStream);
			
			try {
				if (startFromItemItself || sourceData.isFile()) { //we add recursively files or directories in one large item that we received
					if (sourceData.isDirectory()) { 
						zipDirectory(sourceData, sourceData.getName(), zipOutStream, buffer);				
					
					} else if (sourceData.isFile()) { 
						zipFile(sourceData, sourceData.getName(), zipOutStream, buffer);
					}
					
				} else { //case when we want all resources of main directory to be spill in zip's archive
					
					File[] allItems = sourceData.listFiles();
				    if (allItems == null) {  
				        return zipArchive; //case when we have only a empty directory to archive
				    }
				    
				    for (File currentItem : allItems) {
				    	String folderName = currentItem.getName(); //we add item directly in zip's archive
				        if (currentItem.isDirectory()) {    
				            zipDirectory(currentItem, folderName + "/", zipOutStream, buffer); //use only this separator for 'in zip path entry' to create dir(have '/' last character)
				        
				        } else {
				            zipFile(currentItem, folderName, zipOutStream, buffer);
				        } 
				    }
				}
				
			} finally { //close stream's opened above
				zipOutStream.close();
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
		boolean startFromItemItself = false; //if it's 'true' will put all sub-directories saved in zip's archive in one large directory, if it's 'false' will be spill
		
		try { 
			 ZipWriterRecursive.archiveResorcesToZip(toZipResources, destinationZipArchive, zipName, startFromItemItself);
		}
		catch (Exception exception1){ 
			exception1.printStackTrace();
		}
	}

}