package alex;

import java.io.File; //used to check if given 'fileName' exist on disk
import java.io.IOException;
import java.util.ArrayList;

public class Main {
	
	public static String showData(ArrayList<Person> dataSource, String dataSourceName) { 
		String result = String.format("\t In baza de date '%s' au fost identificate %d persoane: \n\n", dataSourceName, dataSource.size());
		int index = 1;
		for (Person currentPerson : dataSource) { 
			result += index + ". " + currentPerson.aboutPerson(); 
			index++;
		}
		return result;
	}
	
	public static void main(String args[]) { 
		String fileName = "Person.txt";  
		String inLineSeparator = ",";
		String dataSourceName = "Cetateni Borascu";
		
		File file = new File(fileName); //create a file with given name
		
		if (file.exists() && !file.isDirectory()) { 
			
			try { 
				List<Person> borascuCitizens = FileReader.readDataFromFile(fileName, inLineSeparator);
				System.out.print(showData(borascuCitizens, dataSourceName));
			}
			catch (IOException exception2){ 
				System.out.print("An error occurred in parsing file process !! \n");
				exception2.getStackTrace();
			}
		} else { 
			System.out.println(String.format("Given file name '%s' is NOT on disk or a file !! ", fileName));
		}
		
		
		
	}
}
