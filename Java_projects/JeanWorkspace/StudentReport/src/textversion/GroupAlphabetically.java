package textversion;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class GroupAlphabetically {

	public static void writeStudentsAlphabetically(List<Student> listOfStudents, File destinationFile, boolean sortAscending) throws IOException{ 
		if (destinationFile == null ) { 
			throw new IllegalArgumentException("Report for alphabetical sort has NO valid path !"); 
		}
		
		File dir = destinationFile.getParentFile(); 
		if (!dir.exists()) { //ensure we have all directories created to can create destinationFile for output
			dir.mkdirs();
		}
		
		Comparator<Student> comp = new Comparator<Student>() { //use this interface to can compare 2 students

			@Override
			public int compare(Student o1, Student o2) {
				int number1 = GroupByGrades.getNumberFromClassName(o1.getClassName(), ' '); 
				int number2 = GroupByGrades.getNumberFromClassName(o2.getClassName(), ' ');
				
				int result = Integer.compare(number1, number2);
				if (!sortAscending) { 
					result = -result;  //use '-' at result to switch in descending order
				}
				if (result == 0) { 
					String letter1 = GroupByGrades.getLetterFromClassName(o1.getClassName(), ' '); 
					String letter2 = GroupByGrades.getLetterFromClassName(o2.getClassName(), ' '); 
					
					result = letter1.compareTo(letter2);
					if (result == 0) {
						String name1 = o1.getFullName();
						String name2 = o2.getFullName();
						
						result = name1.compareTo(name2); 
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
			 
			@Override
			public String buildeDisplayText(Student item) {
				// TODO Auto-generated method stub
				return StudentUtil.aboutStudent(item);
			}

			@Override
			public String getGroupByKey(Student item) {
				// TODO Auto-generated method stub
				return item.getClassName();
			}
		};
		writeInTxtFileNew(auxiliarList, destinationFile, "Elevii sunt sortati pe clase in mod alfabetic", item);
//		writeInTxtFile(auxiliarList, destinationFile);
	}
	
//	private static void writeInTxtFile(List<Student> students, File destinationFile) throws IOException{ 
//		FileWriter fileWriter = new FileWriter(destinationFile); 
//		try { 
//			
//			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
//			try { 
//				String lastClassName = ""; //initial start with current because we have not a previous one
//				String currentClassName;
//				
//				bufferedWriter.write("Elevii sunt sortati pe clase in mod alfabetic");
//				for (Student currentStudent : students) {  
//					currentClassName = currentStudent.getClassName();
//					
//					if (!lastClassName.equals(currentClassName)) { 
//						bufferedWriter.write(String.format("\n\n \tClasa %s: \n", currentClassName));
//						lastClassName = currentClassName;
//					}
//					bufferedWriter.write("\t\t" + StudentUtil.aboutStudent(currentStudent)); 						
//				}
//			} finally { 
//				bufferedWriter.close();
//			}
//		} finally { 
//			fileWriter.close();
//		}
//	}
//	
	public static <Type> void writeInTxtFileNew(List<Type> itemToDisplay, File destinationFile, String header, ItemRenderer<Type> rendererCallback) throws IOException{ 
		FileWriter fileWriter = new FileWriter(destinationFile); 
		try { 
			
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			try { 
				String lastClassName = ""; //initial start with current because we have not a previous one
				String currentClassName;
				
				bufferedWriter.write(header);
				for (Type item : itemToDisplay) {  
					currentClassName = rendererCallback.getGroupByKey(item);
//					
					if (!lastClassName.equals(currentClassName)) { 
						bufferedWriter.write(String.format("\n\n \tClasa %s: \n", currentClassName)); //alta metoda in interfata ce iti stie linia de criteriu
						lastClassName = currentClassName;
					}
					String line = rendererCallback.buildeDisplayText(item);
					bufferedWriter.write("\t\t" + line); 						
				}
			} finally { 
				bufferedWriter.close();
			}
		} finally { 
			fileWriter.close();
		}
	}
}
