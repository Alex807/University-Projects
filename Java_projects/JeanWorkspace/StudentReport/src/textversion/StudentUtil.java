package textversion;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

public abstract class StudentUtil {

	
	public static String dateToString(Student student) { 
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("d/M/yyyy");
		LocalDate date = student.getBirthDate();
		String birthdateAsString = dateFormat.format(date); 
		
		return birthdateAsString;
	}

	public static int calculateStudentAge(Student student) { 
		LocalDate currentDate = LocalDate.now(); 
		LocalDate date = student.getBirthDate();
		int age = Period.between(date, currentDate).getYears();
		
		return  age;
	}
	
	public static String aboutStudent(Student student) {
		String birthDateAsString  = dateToString(student);
		int age = calculateStudentAge(student);
		
		String fullName = student.getFullName();
		String className = student.getClassName(); 
		String homeLocation = student.getHomeLocation(); 
		double grades = student.getGrades();
		
		return String.format("Nume: '%s', Varsta: %d ani, DataNastere: %s, Clasa: %s, Localitate: %s, Media: %.2f \n", fullName, age, birthDateAsString, className, homeLocation, grades); 
	}
}
