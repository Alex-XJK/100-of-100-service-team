package dev.coms4156.project.command;

import dev.coms4156.project.Department;
import dev.coms4156.project.Employee;
import dev.coms4156.project.HrDatabaseFacade;
import dev.coms4156.project.exception.BadRequestException;
import dev.coms4156.project.exception.InternalServerErrorException;
import dev.coms4156.project.exception.NotFoundException;
import java.util.HashMap;
import java.util.Map;

/**
 * A command to set the head of a department.
 */
public class SetDeptHeadCmd implements Command {
  private final int clientId;
  private final int departmentId;
  private final int employeeId;

  /**
   * Constructs a command to set the head of a department.
   *
   * @param clientId the client ID
   * @param departmentId the department ID
   * @param employeeId the employee ID
   */
  public SetDeptHeadCmd(int clientId, int departmentId, int employeeId) {
    this.clientId = clientId;
    this.departmentId = departmentId;
    this.employeeId = employeeId;
  }

  @Override
  public Object execute() {
    HrDatabaseFacade db = HrDatabaseFacade.getInstance(this.clientId);
    Department department = db.getDepartment(this.departmentId);
    if (department == null) {
      throw new NotFoundException("Department [" + this.departmentId + "] not found");
    }
    Employee employee = db.getEmployee(this.employeeId);
    if (employee == null) {
      throw new NotFoundException("Employee [" + this.employeeId + "] not found");
    }
    boolean success = department.setHead(employee);
    if (!success) {
      throw new BadRequestException("Failed to set head of department [" + this.departmentId + "]");
    }
    boolean updated = db.updateDepartment(department);
    if (!updated) {
      throw new InternalServerErrorException(
          "Failed to update department [" + this.departmentId + "]"
      );
    }

    Map<String, Object> response = new HashMap<>();
    response.put("status", 200);
    response.put("message", "Successfully set head of department [" + this.departmentId + "] "
        + "to employee [" + this.employeeId + "]");
    return response;
  }
}