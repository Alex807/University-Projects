package textversion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public abstract class FileParser {
	
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

	public static List<Student> readDataFromFile(File sourceFile, String inLineSeparator) throws IOException { 
		if (sourceFile == null || !sourceFile.isFile()) { 
			throw new RuntimeException("Source data is NOT a file !");
		}
		
		List<Student> dataSource = new ArrayList<Student>();

		FileReader fileReader = new FileReader(sourceFile);
		try { 
			
			BufferedReader bufferIn = new BufferedReader(fileReader);
			try {
				String currentLine; 
				while ((currentLine = bufferIn.readLine()) != null) { 
					
					StringTokenizer token = new StringTokenizer(currentLine, inLineSeparator);
					int inLineTokens = token.countTokens(); 
					if (inLineTokens != 5) { 
						throw new RuntimeException("Line has NOT enough tokens !!");
					}
					
					String name = token.nextToken().trim(); 
					String birthdateAsString = token.nextToken().trim();
					LocalDate birthdate = FileParser.stringToDate(birthdateAsString); 
					
					String className = token.nextToken().trim();
					String home = token.nextToken().trim(); 
					
					String gradesAsString = token.nextToken().trim();
					double grades = Double.parseDouble(gradesAsString);
					
					Student currentStudent = new Student(name, birthdate, className, home, grades);
					dataSource.add(currentStudent); 
				}
			} finally { 
				bufferIn.close();
			} 
		} finally { 
			fileReader.close();
		}

		return dataSource;
	}
}
