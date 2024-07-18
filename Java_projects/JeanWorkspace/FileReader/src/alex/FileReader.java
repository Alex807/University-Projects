package alex;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

public abstract class FileReader {
	
	private static LocalDate stringToDate(String dateAsString) { 
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy"); 
		LocalDate date = null;
		
		try { 
			date = LocalDate.parse(dateAsString, formatter); 
		}
		catch (DateTimeParseException exception) {
			System.out.println("Date format is invalid !!"); 
			exception.printStackTrace();
		}
		return date;
	}
	
	public static ArrayList<Person> readDataFromFile(String fileName, String inLineSeparator) throws IOException{ 
		ArrayList<Person> dataSource = new ArrayList<Person>();
		BufferedReader fileCharStream = null;
		
		try { 
			FileInputStream inputFile = new FileInputStream(fileName);
			fileCharStream = new BufferedReader(new InputStreamReader(inputFile));
			
			String currentLine; 
			String surename, lastname, profession; 
			LocalDate birthdate;
			while ((currentLine = fileCharStream.readLine()) != null) { 
				try {
					StringTokenizer token = new StringTokenizer(currentLine, inLineSeparator);
					
					surename = token.nextToken().trim(); 
					lastname = token.nextToken().trim(); 
					String birthdateAsString = token.nextToken().trim();
					birthdate = FileReader.stringToDate(birthdateAsString); 
					profession = token.nextToken().trim();
					
					Person currentPerson = new Person(surename, lastname, birthdate, profession);
					dataSource.add(currentPerson); 
				} 
				catch (NoSuchElementException | DateTimeParseException exception) { //case when a line has NOT given format, we ignore it and keep reading
					int lineIndex = dataSource.size() + 1;
					System.err.println(String.format("Line format is incorrect in file '%s' at line %d !\n", fileName, lineIndex));
					dataSource.add(null); 
				}
			}
		}
		finally { 
			if (fileCharStream != null) { //if was created successfully, we can close it properly	 
				fileCharStream.close();		
			}
		}
		return dataSource;
	}
}
