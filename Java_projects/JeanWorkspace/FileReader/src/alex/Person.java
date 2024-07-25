package alex;

import java.time.format.DateTimeFormatter; //used to switch birthday from 'LocalDate' type in 'String'
import java.time.LocalDate; 
import java.time.Period;

public class Person {
	
	private String surename; 
	private String lastname; 
	private LocalDate birthdate; 
	private String profession;
	
	public Person (String surename, String lastname, LocalDate birthdate, String profession) { 
		this.surename = surename; 
		this.lastname = lastname; 
		this.birthdate = birthdate;
		this.profession = profession;
	}
	
	public String dateToString() { 
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("d/M/yyyy"); 
		String birthdateAsString = dateFormat.format(birthdate); 
		
		return birthdateAsString;
	}
	
	public int calculateAge() { 
		LocalDate currentDate = LocalDate.now(); 
		int age = Period.between(birthdate, currentDate).getYears();
		
		return  age;
	}
	
	public String aboutPerson() { 
		String birthdateAsString = this.dateToString();
		int age = this.calculateAge();
		
		return String.format("Nume: '%s', Prenume: '%s', DataNastere: %s, Varsta: %d ani, Profesie: %s \n", surename, lastname, birthdateAsString, age, profession); 
	}
}
