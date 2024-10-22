package dev.coms4156.project.command;

import dev.coms4156.project.Department;
import dev.coms4156.project.Employee;
import dev.coms4156.project.HrDatabaseFacade;
import dev.coms4156.project.exception.NotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A command to add an employee to a given department.
 */
public class AddEmployeeToDeptCommand implements Command {
  private final int clientId;
  private final int departmentId;
  private final String name;
  private final String hireDate;

  /**
   * Constructs a command to add an employee to a given department.
   *
   * @param clientId     the ID of the client organization
   * @param departmentId the ID of the department where the employee will be added
   * @param name         the name of the employee
   * @param hireDate     the hire date of the employee in "yyyy-MM-dd" format
   */
  public AddEmployeeToDeptCommand(int clientId, int departmentId, String name, String hireDate) {
    this.clientId = clientId;
    this.departmentId = departmentId;
    this.name = name;
    this.hireDate = hireDate;
  }

  @Override
  public Object execute() {
    HrDatabaseFacade dbFacade = HrDatabaseFacade.getInstance(clientId);

    // Fetch the department from the in-memory cache or DB
    Department department = dbFacade.getDepartment(departmentId);
    if (department == null) {
      throw new NotFoundException("Department [" + this.departmentId + "] not found");
    }

    // Parse the hireDate string into a Date object
    Date parsedHireDate;
    try {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
      parsedHireDate = sdf.parse(hireDate);
    } catch (ParseException e) {
      throw new IllegalArgumentException("Invalid date format. Expected yyyy-MM-dd.", e);
    }

    // Generate a new external employee ID (e.g., max existing ID + 1)
    int newEmployeeId = dbFacade.generateNewEmployeeId();

    // Create the new employee
    Employee newEmployee = new Employee(newEmployeeId, name, parsedHireDate, departmentId);

    // Add the employee to the department
    department.addEmployee(newEmployee);

    // Add the employee to the organization's employee list
    dbFacade.addEmployeeToOrganization(newEmployee);

    // Sync changes with the database
    boolean success = dbFacade.updateDepartment(department);
    success = success && dbFacade.addEmployeeToDatabase(newEmployee);

    if (!success) {
      throw new IllegalStateException("Failed to add employee to the database.");
    }

    return "Employee " + name + " added to department: " + department.getName();
  }
}
