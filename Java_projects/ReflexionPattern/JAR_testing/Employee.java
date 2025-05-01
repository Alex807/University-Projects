public class Employee extends Person {
    private double salary;
    private String department;
    private Person[] arrayRandom;

    public Employee(String name, int age, double salary, String department) {
        super(name, age);
        this.salary = salary;
        this.department = department;
        this.arrayRandom = new Person[100];
    }

    public double getSalary() {
        return salary;
    }

    public String getDepartment() {
        return department;
    }
}