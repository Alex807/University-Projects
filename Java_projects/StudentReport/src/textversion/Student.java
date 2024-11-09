package textversion;

import java.time.LocalDate;

public class Student implements Person {
	
		private String fullName; 
		private LocalDate birthDate; 
		private String className;
		private String homeLocation; 
		private double grades;
		
		public Student(String name, LocalDate date, String className, String home, double grades) {
			this.fullName = name; 
			this.birthDate = date; 
			this.className = className; 
			this.homeLocation = home; 
			this.grades = grades;
		}
		
		@Override 
		public String getFullName() {
			return fullName;
		}
		
		public LocalDate getBirthDate() { 
			return birthDate;
		}

		public String getClassName() {
			return className;
		}

		public double getGrades() {
			return grades;
		}

		public String getHomeLocation() {
			return homeLocation;
		}		
}