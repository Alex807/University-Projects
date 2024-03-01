package proiect_Java;

import java.io.File; //used to import File class for a new "file" object 
import java.io.FileNotFoundException; //used for handling file exceptions 
import java.util.*;

final class DefineConstants{ //this class can NOT be inherited for preventing possible changes
	public static final String FILE_NAME = "resurse.txt";  //can't be modifying in program, they are constants
	public static final String inLine_SEPARATOR = ",";
}

//pune aici si o interfata sa ai bine definit tipul unui asset
abstract class LibraryAsset{ 
	private String title; 
	private String author; 
	private int numberOfCopies; 
	
	protected LibraryAsset(String title, String author, int copies) { 
		this.title = title; 
		this.author = author; 
		this.numberOfCopies = copies;
	}
	
	public String getTitle() { 
		return title;
	}
	
	public String getAuthor() { 
		return author;
	}
	
	public int getNumberOfCopies() { 
		return numberOfCopies;
	}
	
	public abstract String showDetailes();
}

class Book extends LibraryAsset{  
	private String bookDescription;
	
	public Book(String title, String author, String description, int copies) { 
		super(title, author, copies); 
		this.bookDescription = description;
	}
	
	//@Overriding 
	public String showDetailes() { 
		return "  (Book) \tTitle: '" + getTitle() + "', Author: " + getAuthor() + ", Description: '" + bookDescription + "', Copies: " + getNumberOfCopies() ;
	}
}

class Magazine extends LibraryAsset{  
	private int uniqueSerialNumber; 
	
	public Magazine(String title, String author, int uniqueNumber, int copies) { 
		super(title, author, copies); 
		this.uniqueSerialNumber = uniqueNumber;
	}
	
	//@Overriding 
	public String showDetailes() { 
		return "(Magazine)\tTitle: '" + getTitle() + "', Author: " + getAuthor() + ", SerialNumber: " + uniqueSerialNumber + ", Copies: " + getNumberOfCopies() ;
	}
	
}

class Library{ 
	private String libraryName;
	private ArrayList<LibraryAsset> listOfBooks; 
	
	public Library(String libraryName) { 
		this.libraryName = libraryName; 
		listOfBooks = new ArrayList<LibraryAsset>();
	}
	
	public void addAssetInLibraryManually(LibraryAsset asset) { 
		listOfBooks.add(asset);
	}
	
	public void populateLibrary_FromFile(String fileName) {
		int countLinesReaded = 1; //used for count at which line occurred an exception
		
		try { 
			File myFileObject = new File(fileName);  //save files who you want to read in project folder, NOT in src
			Scanner myReader = new Scanner(myFileObject);
			
			while (myReader.hasNextLine()) { 
				boolean isABook = false; //we check if current library asset is a book or a magazine
				String currentLineInfos = myReader.nextLine(); //we read from file line by line, after with StringTokenizer read  specific values 
				
				String inLineSeparator = DefineConstants.inLine_SEPARATOR;
				StringTokenizer parsser = new StringTokenizer(currentLineInfos, inLineSeparator);
				//we don't verify with a 'while' loop if we have enough tokens for instances because we catch exception if format is not respected
				
				String title = parsser.nextToken().trim(); 
				String author = parsser.nextToken().trim();  
			
				String descriptionString = parsser.nextToken().trim(); //with 'trim' we remove spaces to can parse string to int if it's possible
				int descriptionForMagazines = 0;
				try { 
					descriptionForMagazines = Integer.parseInt(descriptionString); //if is description of a book throw an exception
					
				}catch(NumberFormatException e) {//here we know asset is a book, because books have string description and can't be parsed
					isABook = true;
				}
				
				
				String numberOfCopiesAssString = parsser.nextToken().trim(); //for removing possible spaces at the end of line
				int numberOfCopies = Integer.parseInt(numberOfCopiesAssString);
				
				if (isABook) { 
					this.addAssetInLibraryManually(new Book(title, author, descriptionString, numberOfCopies));
				} else {
					this.addAssetInLibraryManually(new Magazine(title, author, descriptionForMagazines, numberOfCopies));
				}	
				countLinesReaded++;
			}
			myReader.close(); //in this state reading is over and we close this resource
			
		} catch (FileNotFoundException exception1) { 
			System.out.println("An error occured, are you sure this is your file name ??"); 
			exception1.printStackTrace(); //used to see the root of the exception
		
		} catch (NumberFormatException exception2) { //case when number of files(int at end of a line is not actually an int)
			System.out.println("Number of copies is invalid, check out file at line " + countLinesReaded + " !!");
			exception2.printStackTrace();
			
		} catch (NoSuchElementException exception3) { //case when format of line is not respected and "parsser.nextToken()" hasn't enough tokens
			System.out.println("In file at line " + countLinesReaded + " format of a line(string, string, int/string, int) is NOT respected !!"); 
			exception3.printStackTrace();
		}
} 
	
	public String showLibraryAssets() { 
		String result = "'" + libraryName + "' Library has in his collection: \n"; 
		for (LibraryAsset currentAsset : listOfBooks) { 
			result += currentAsset.showDetailes() + "\n";
		}
		
		return result;
	}
}

public class Main {

	public static void main(String[] args) {
		Library library = new Library("UPT"); 
		String fileNameToRead = DefineConstants.FILE_NAME;
		library.populateLibrary_FromFile(fileNameToRead); 
		
		System.out.println(library.showLibraryAssets());

	}

}
