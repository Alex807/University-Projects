package textversion;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class GroupByGrades {
	
	public static int getNumberFromClassName(String className, char separator) { 
		int indexToStop = className.indexOf(separator);
		String numberAsString = className.substring(0, indexToStop);
		
		return Integer.parseInt(numberAsString);
	}
	
	public static String getLetterFromClassName(String className, char separator) { 
		int indexToStart = className.indexOf(separator);
		
		return className.substring(indexToStart).trim(); //remove all spaces remained
	}
	
	public static void writeStudentsByGrades(List<Student> listOfStudents, File destinationFile, boolean sortAscending) throws IOException{ 
		if (destinationFile == null)  { 
			throw new IllegalArgumentException("Report for ascendent sort has NO valid path !"); //it has not been created
		}
		
		File dir = destinationFile.getParentFile(); 
		if (!dir.exists()) { //ensure we have all directories created to can create destinationFile for output
			dir.mkdirs();
		}
		
		Comparator<Student> comp = new Comparator<Student>() { //use this interface to can compare 2 students
			
			@Override
			public int compare(Student o1, Student o2) {
				int number1 = getNumberFromClassName(o1.getClassName(), ' ');
				int number2 = getNumberFromClassName(o2.getClassName(), ' ');
				
				int result = Integer.compare(number1, number2); //to can be descending with using '-'
				if (!sortAscending) { 
					result = -result;
				}
				if (result == 0) { 
					String letter1 = getLetterFromClassName(o1.getClassName(), ' '); 
					String letter2 = getLetterFromClassName(o2.getClassName(), ' '); 
					
 					result = letter1.compareTo(letter2); //equals just says if it's equal or not, not know who is bigger to can sort
					if (result == 0) { 
						
						result = Double.compare(o1.getGrades(), o2.getGrades()); 
						if (!sortAscending) { 
							result = -result;  //use '-' at result to switch in descending order
						}
					}
				}
				return result;
			}
		};
		List<Student> auxiliarList = new ArrayList<Student>(listOfStudents); //create a new list to NOT modify original one
		auxiliarList.sort(comp);
		
		ItemRenderer<Student> item = new ItemRenderer<Student>() {
			private int count = 0;
			@Override
			public String buildeDisplayText(Student item) {
				count++;
				return StudentUtil.aboutStudent(item);
			}
			@Override
			public String getGroupByKey(Student item) {
				// TODO Auto-generated method stub
				return item.getClassName();
			}
		};
		
		GroupAlphabetically.writeInTxtFileNew(auxiliarList, destinationFile, "bbdbd", item);
		
	}
}
