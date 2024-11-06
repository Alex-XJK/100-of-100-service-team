package dev.coms4156.project.command;

import dev.coms4156.project.Employee;
import dev.coms4156.project.HrDatabaseFacade;
import dev.coms4156.project.Position;
import dev.coms4156.project.exception.InternalServerErrorException;
import dev.coms4156.project.exception.NotFoundException;

public class SetEmpPosiCommand implements Command {
  private final int clientId;
  private final int employeeId;
  private final Position position;

  /**
   * Constructs a command to set the position of an employee.
   *
   * @param clientId the client ID
   * @param employeeId the employee ID
   * @param position the position to set
   */
  public SetEmpPosiCommand(int clientId, int employeeId, String position) {
    this.clientId = clientId;
    this.employeeId = employeeId;
    this.position = parsePosition(position);
  }

  /**
   * Parses the position from the given string.
   *
   * @param position the position string
   * @return the position
   * @throws IllegalArgumentException if the position is invalid
   */
  private Position parsePosition(String position) {
    try {
      return Position.valueOf(position);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid position: " + position);
    }
  }

  /**
   * Executes the command.
   *
   * @return the result of the command
   */
  @Override
  public Object execute() {
    HrDatabaseFacade db = HrDatabaseFacade.getInstance(this.clientId);
    Employee emp = db.getEmployee(this.employeeId);
    if (emp == null) {
      throw new NotFoundException("Employee [" + this.employeeId + "] not found");
    }
    emp.setPosition(this.position);
    boolean result = db.updateEmployee(emp);
    if (!result) {
      throw new InternalServerErrorException("Failed to update employee [" + this.employeeId + "]");
    }

    return "Successfully set position of employee [" + this.employeeId + "] to " + this.position;
  }
}