package writeAFile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import alex.FileReader;
import alex.Person;  
import alex.TextFileParser;

public class ZipWriter {
	
	public static File createZipFile (File fileToRead, File destinationParentDir, String zipName, String inLineSeparator) throws IOException { 	
		
		if (!fileToRead.exists() || fileToRead == null) {//check if path really exists on disk, an is not a link or something
			throw new IllegalArgumentException(String.format(
					"Path for readedFile <%s> is NOT existing on disk. Please check method inputs!", fileToRead.getAbsolutePath()));
		
		} else if (!destinationParentDir.exists() || destinationParentDir == null) { 
			throw new IllegalArgumentException(String.format(
					"Path for place to save Zip archive <%s> is NOT existing on disk. Please check method inputs!", destinationParentDir.getAbsolutePath()));
		}
		
		if (!fileToRead.isFile()) {//check if given readFile is something that can be read
			throw new IllegalArgumentException(
					"Recieved parameter 'fileToRead' is NOT a file. Please check method inputs !");
		}
		 
		if (!zipName.endsWith(".zip")) { //make sure we have correct zip's archive name
			zipName += ".zip";
		}
		
		List<Person> borascuCitizens = FileReader.readDataFromFile(fileToRead.getAbsolutePath(), inLineSeparator); 
							
		File zipArchive = new File(destinationParentDir, zipName); //create an object to return which contains path to resulted zip's archive
		
		FileOutputStream fileOutStream = new FileOutputStream(zipArchive);
		ZipOutputStream zipOut = new ZipOutputStream(fileOutStream);
		
		OutputStreamWriter  outStreamWriter = new OutputStreamWriter(zipOut);
		BufferedWriter writer = new BufferedWriter(outStreamWriter); 
		
		try { 
			String outputFileName = fileToRead.getName();
			zipOut.putNextEntry(new ZipEntry(outputFileName)); 
			String placeToRead = "\t Datele au fost citite de la sursa < " + fileToRead.getAbsolutePath() + " >\n\n";
			writer.write(placeToRead); 
			
			for(Person currentPerson : borascuCitizens) { 
				writer.write(TextFileParser.showData(currentPerson));
			}
		}
		finally { 
			if (writer != null) { //if was created successfully, we can close it properly	 
				writer.close();		
			} 
			if (zipOut != null) {
				zipOut.close();
			}			
		}
		return zipArchive;
	}
	
	public static void main(String args[]) { 
		String filePath = "E:\\Github\\Personal-Projects\\Java_projects\\JeanWorkspace\\ZipHandling\\Testing.txt"; 
		if (args.length == 1) { 
			filePath = args[0];
		}
		File fileToRead = new File(filePath);
		
		String inLineSeparator = ",";
		String zipName = "Output.zip";  //make SURE you put '.zip' extension in given name
		
		String absolutOutputPath = "E:\\Github\\Personal-Projects"; 
		File placeToCreateZip = new File(absolutOutputPath);
		
		File zipArchive;
		try { 
			zipArchive = ZipWriter.createZipFile(fileToRead, placeToCreateZip, zipName, inLineSeparator);
		} 
		catch (IllegalArgumentException exception1) { 
			exception1.printStackTrace();
		}
		catch (IOException exception2) { 
			exception2.printStackTrace();
		}
	}
}