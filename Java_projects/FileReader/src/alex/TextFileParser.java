package alex;

import java.io.BufferedWriter;
import java.io.File; //used to check if given 'fileName' exist on disk
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class TextFileParser {
	
	public static String showData(Person person) { 
		if (person == null) { //case when we add 'null' in list from fileReader
			return "\t Linia NU respecta formatul dat !!\n";  
		}
	
		return person.aboutPerson();
	}
	
	public static void main(String args[]) {  
		String filePath = "E:\\Github\\Personal-Projects\\Java_projects\\JeanWorkspace\\FileReader\\Person.txt";
		if(args.length == 1) { 
			filePath = args[0];
		} 
		String inLineSeparator = ",";
		
		File file = new File(filePath); //create a file with given name  
	
		
		if (file.isFile()) { 
			
			try { 
				List<Person> borascuCitizens = FileReader.readDataFromFile(filePath, inLineSeparator); 
				String placeToRead = "\t Datele au fost citite de la sursa < " + file.getAbsolutePath() + " >\n\n";
				
				//create output file 
				File outputFile = new File(file.getParent(), "Output.txt"); 
				PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(outputFile)));
				try { 
					writer.print(placeToRead); 
					
					for (Person currentPerson : borascuCitizens) { 
						writer.print(TextFileParser.showData(currentPerson));
					}
				}
				catch (NullPointerException exception) {
					writer.print("Process was ended unusually !!");
					exception.printStackTrace();
				}
				finally { 
					if (writer != null) { //if was created successfully, we can close it properly	 
						writer.close();		
					}
				}
			}
			catch (IOException exception){ 
				System.out.print("An error occurred in parsing file process !! \n");
				exception.getStackTrace();
			}
		} else { 
			System.out.println(String.format("Given file path < %s > can NOT be founded in this spot OR is NOT a file !! ", filePath));
		}		
		
	}
}
