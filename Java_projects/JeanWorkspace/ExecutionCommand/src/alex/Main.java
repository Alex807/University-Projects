package alex;

import report.Person;
import ro.report.Car;

public class Main {
	public static void main(String args[]) { 
		Car car = new Car();
		Person p = new Person(car);
		System.out.println("Hello World p = " + p + ", c=" + car);
	}
}