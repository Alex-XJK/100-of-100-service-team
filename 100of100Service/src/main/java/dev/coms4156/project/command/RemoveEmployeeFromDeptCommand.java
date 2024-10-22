package dev.coms4156.project.command;

import dev.coms4156.project.Department;
import dev.coms4156.project.Employee;
import dev.coms4156.project.HrDatabaseFacade;
import dev.coms4156.project.exception.NotFoundException;

/**
 * A command to remove an employee from given department.
 */
public class RemoveEmployeeFromDeptCommand implements Command {
  private final int clientId;
  private final int departmentId;
  private final int employeeId;

  /**
   * Constructs a command to remove an employee from a given department.
   *
   * @param clientId     the ID of the client organization
   * @param departmentId the ID of the department from which the employee will be removed
   * @param employeeId   the ID of the employee to be removed
   */
  public RemoveEmployeeFromDeptCommand(int clientId, int departmentId, int employeeId) {
    this.clientId = clientId;
    this.departmentId = departmentId;
    this.employeeId = employeeId;
  }

  @Override
  public Object execute() {
    HrDatabaseFacade dbFacade = HrDatabaseFacade.getInstance(clientId);

    // Get the department from the database
    Department department = dbFacade.getDepartment(departmentId);
    if (department == null) {
      throw new NotFoundException("Department [" + this.departmentId + "] not found");
    }

    // Find the employee to remove
    Employee employeeToRemove = department.getEmployees().stream()
        .filter(e -> e.getId() == employeeId)
        .findFirst()
        .orElseThrow(() ->
          new IllegalArgumentException("Employee not found with ID: " + employeeId));

    // Remove the employee and update the department
    department.removeEmployee(employeeToRemove);
    dbFacade.updateDepartment(department);  // Sync with the database

    return "Employee " + employeeToRemove.getName() + "(ID = " +
            employeeId + ")" + " removed from department: " + department.getName();
  }
}
