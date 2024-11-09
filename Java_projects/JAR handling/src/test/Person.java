package test; 

public class Person { 
	private String message;
	
	public Person(String message) { 
		this.message = message;
	}
	
	public String showDetails() { 
		return message;
	}
}