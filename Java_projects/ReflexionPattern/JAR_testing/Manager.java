import java.util.List;
import java.util.ArrayList;

public class Manager extends Employee implements Displayable {
    private List<Employee> team;

    public Manager(String name, int age, double salary, String department) {
        super(name, age, salary, department);
        this.team = new ArrayList<>();
    }

    public void addTeamMember(Employee employee) {
        team.add(employee);
    }

    @Override
    public void display() {
        System.out.println("Manager: " + getName() + ", Department: " + getDepartment());
    }
}