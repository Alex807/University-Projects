package unzipping;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipReader {
	
	public static String removeExtensionFromFileName (File file) { 
		String completeFileName = file.getName(); 
		
		// obtain name of file without '.txt' extension for example
        int dotIndex = completeFileName.lastIndexOf('.');
        
        return (dotIndex > 0) ? completeFileName.substring(0, dotIndex) : completeFileName;
	}

	public static File extractZipContent (File zipArchive, File placeToExtractContent) throws IOException { 
		if (!zipArchive.exists() || zipArchive == null) {//check if path really exists on disk, an is not a link or something
			throw new IllegalArgumentException(String.format(
					"Path to zip archive <%s> is NOT existing on disk. Please check method inputs!", zipArchive.getAbsolutePath()));
		
		} else if (!placeToExtractContent.exists() || placeToExtractContent == null) { 
			throw new IllegalArgumentException(String.format(
					"Path for place to save Zip archive <%s> is NOT existing on disk. Please check method inputs!", placeToExtractContent.getAbsolutePath()));
		}
		
		if (!zipArchive.isFile() ) { 
			throw new IllegalArgumentException(String.format(
					"Path to zip archive <%s> is a directory, NOT a file. Please check method inputs !"));
		}
		
		String extractionDirName = ZipReader.removeExtensionFromFileName(zipArchive);
		File outputDir = new File(placeToExtractContent, extractionDirName); 		
		 
		FileInputStream inputStream = new FileInputStream(zipArchive);
		ZipInputStream zipIn = new ZipInputStream(inputStream);  
		
		ZipEntry entry; //for reaching every entry in zip's archive
		while ((entry = zipIn.getNextEntry()) != null) {
			String fileFromZipName = entry.getName();
			File outputFile = new File(outputDir, fileFromZipName); 
			
			if (entry.isDirectory()) { 
				if (!outputFile.exists()) { 
					outputFile.mkdirs();
				}
				
			} else {
				File parent = outputFile.getParentFile(); 
				if (!parent.exists()) { 
					parent.mkdirs();
				}
				
				FileOutputStream out = new FileOutputStream(outputFile);				
				try { 
					byte[] buffer = new byte[1024];  
					int length; 
					while ((length = zipIn.read(buffer)) > 0) { 
						out.write(buffer, 0, length);
					}		
				} 
				finally {
				    out.close();
				}
			}
		}
		zipIn.close();

		return zipArchive; 
	}

	
	
	public static void main(String args[]) { 
		String zipToReadPath = "E:\\Github\\Personal-Projects\\Java_projects\\JeanWorkspace\\ZipHandling\\ZipTest.zip";  
		String placeToExtractContent = "E:\\Github\\Personal-Projects\\Java_projects\\JeanWorkspace\\ZipHandling";
		
		if (args.length == 1) { 
			zipToReadPath = args[0];
		}
		File zipArchive = new File(zipToReadPath);
		File extractToDirectory = new File(placeToExtractContent);
		
		File zipContent;
		try { 
			zipContent = ZipReader.extractZipContent(zipArchive, extractToDirectory);
		
		} 
		catch (IllegalArgumentException exception1) { 
			exception1.printStackTrace();
		}
		catch (IOException exception2) {
			exception2.printStackTrace();
		}
	}	
}
