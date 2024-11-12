package dev.coms4156.project;

import dev.coms4156.project.command.AddEmpToDeptCmd;
import dev.coms4156.project.command.Command;
import dev.coms4156.project.command.GetDeptInfoCmd;
import dev.coms4156.project.command.GetEmpInfoCmd;
import dev.coms4156.project.command.GetOrgInfoCmd;
import dev.coms4156.project.command.RemoveEmpFromDeptCmd;
import dev.coms4156.project.command.SetDeptHeadCmd;
import dev.coms4156.project.command.SetEmpPerfCmd;
import dev.coms4156.project.command.SetEmpPosiCmd;
import dev.coms4156.project.command.SetEmpSalCmd;
import dev.coms4156.project.command.StatDeptBudgCmd;
import dev.coms4156.project.command.StatDeptPerfCmd;
import dev.coms4156.project.command.StatDeptPosiCmd;
import dev.coms4156.project.exception.NotFoundException;
import dev.coms4156.project.utils.CodecUtils;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * This class contains all the API routes for the system.
 */
@RestController
public class RouteController {

  /**
   * Redirects to the homepage.
   */
  @GetMapping({"/", "/index", "/home"})
  public String index() {
    return "Welcome, in order to make an API call direct your browser or Postman to an endpoint.";
  }

  /* ***** GET METHODS ***** */

  /**
   * Gets the information of a department.
   *
   * @param clientId the client ID
   * @param departmentId the department ID
   * @return the information of the department
   */
  @GetMapping(value = "/getDeptInfo", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> getDepartment(
      @RequestAttribute("cid") int clientId,
      @RequestParam("did") int departmentId
  ) {
    Command command = new GetDeptInfoCmd(clientId, departmentId);
    return new ResponseEntity<>(command.execute(), HttpStatus.OK);
  }

  /**
   * Gets the information of an employee.
   *
   * @param clientId the client ID
   * @param employeeId the employee ID
   * @return the information of the employee
   */
  @GetMapping(value = "/getEmpInfo", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> getEmployee(
      @RequestAttribute("cid") int clientId,
      @RequestParam("eid") int employeeId
  ) {
    Command command = new GetEmpInfoCmd(clientId, employeeId);
    return new ResponseEntity<>(command.execute(), HttpStatus.OK);
  }

  /**
   * Gets the information of an organization.
   *
   * @param clientId the client ID
   * @return the information of the organization
   */
  @GetMapping(value = "/getOrgInfo", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> getOrganization(
      @RequestAttribute("cid") int clientId
  ) {
    Command command = new GetOrgInfoCmd(clientId);
    return new ResponseEntity<>(command.execute(), HttpStatus.OK);
  }

  /**
   * Gets the budget statistics of a department.
   *
   * @param clientId the client ID
   * @param departmentId the department ID
   * @return the statistics of the department
   */
  @GetMapping(value = "/statDeptBudget", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> getDepartmentBudgetStatistic(
      @RequestAttribute("cid") int clientId,
      @RequestParam("did") int departmentId
  ) {
    Command command = new StatDeptBudgCmd(clientId, departmentId);
    return new ResponseEntity<>(command.execute(), HttpStatus.OK);
  }

  /**
   * Gets the performance statistics of a department.
   *
   * @param clientId the client ID
   * @param departmentId the department ID
   * @return the statistics of the department
   */
  @GetMapping(value = "/statDeptPerf", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> getDepartmentPerformanceStatistic(
      @RequestAttribute("cid") int clientId,
      @RequestParam("did") int departmentId
  ) {
    Command command = new StatDeptPerfCmd(clientId, departmentId);
    return new ResponseEntity<>(command.execute(), HttpStatus.OK);
  }

  /**
   * Gets the position statistics of a department.
   *
   * @param clientId the client ID
   * @param departmentId the department ID
   * @return the statistics of the department
   */
  @GetMapping(value = "/statDeptPos", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> getDepartmentPositionStatistic(
      @RequestAttribute("cid") int clientId,
      @RequestParam("did") int departmentId
  ) {
    Command command = new StatDeptPosiCmd(clientId, departmentId);
    return new ResponseEntity<>(command.execute(), HttpStatus.OK);
  }

  /* ***** PATCH METHODS ***** */

  /**
   * Sets the head of a department.
   *
   * @param clientId the client ID
   * @param departmentId the department ID
   * @param employeeId the employee ID
   * @return true if the head is set, false otherwise
   */
  @PatchMapping(value = "/setDeptHead", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> setDepartmentHead(
      @RequestAttribute("cid") int clientId,
      @RequestParam("did") int departmentId,
      @RequestParam("eid") int employeeId
  ) {
    Command command = new SetDeptHeadCmd(clientId, departmentId, employeeId);
    return new ResponseEntity<>(command.execute(), HttpStatus.OK);
  }

  /**
   * Set the performance of an employee.
   *
   * @param clientId the client ID
   * @param employeeId the employee ID
   * @param performance the performance value to set
   */
  @PatchMapping(value = "/setEmpPerf", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> setEmployeePerformance(
      @RequestAttribute("cid") int clientId,
      @RequestParam("eid") int employeeId,
      @RequestParam("performance") double performance
  ) {
    Command command = new SetEmpPerfCmd(clientId, employeeId, performance);
    return new ResponseEntity<>(command.execute(), HttpStatus.OK);
  }

  /**
   * Set the position of an employee.
   *
   * @param clientId the client ID
   * @param employeeId the employee ID
   * @param position the position to set
   */
  @PatchMapping(value = "/setEmpPos", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> setEmployeePosition(
      @RequestAttribute("cid") int clientId,
      @RequestParam("eid") int employeeId,
      @RequestParam("position") String position
  ) {
    Command command = new SetEmpPosiCmd(clientId, employeeId, position);
    return new ResponseEntity<>(command.execute(), HttpStatus.OK);
  }

  /**
   * Set the salary of an employee.
   *
   * @param clientId the client ID
   * @param employeeId the employee ID
   * @param salary the salary to set
   */
  @PatchMapping(value = "/setEmpSalary", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> setEmployeeSalary(
      @RequestAttribute("cid") int clientId,
      @RequestParam("eid") int employeeId,
      @RequestParam("salary") double salary
  ) {
    Command command = new SetEmpSalCmd(clientId, employeeId, salary);
    return new ResponseEntity<>(command.execute(), HttpStatus.OK);
  }

  /**
   * Update all the information of an employee.
   * The provided information will overwrite the existing information, if not null.
   * If the information is null, the existing information will be retained.
   * Notice: No associated command for this method.
   *
   * @param clientId the client ID
   * @param employeeId the employee ID
   * @param position (optional) the position to set
   * @param salary (optional) the salary to set
   * @param performance (optional) the performance to set
   * @return a success message if the employee is successfully updated.
   */
  @PatchMapping(value = "/updateEmpInfo", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> updateEmployeeInfo(
      @RequestAttribute("cid") int clientId,
      @RequestParam("eid") int employeeId,
      @RequestParam(value = "position", required = false) String position,
      @RequestParam(value = "salary", required = false) Double salary,
      @RequestParam(value = "performance", required = false) Double performance
  ) {
    String component = " ";
    if (position != null) {
      Command command = new SetEmpPosiCmd(clientId, employeeId, position);
      command.execute();
      component += "position ";
    }
    if (salary != null) {
      Command command = new SetEmpSalCmd(clientId, employeeId, salary);
      command.execute();
      component += "salary ";
    }
    if (performance != null) {
      Command command = new SetEmpPerfCmd(clientId, employeeId, performance);
      command.execute();
      component += "performance ";
    }
    String response = "Employee" + component + "information updated successfully";
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  /* ***** POST METHODS ***** */

  /**
   * Add an employee to the given department.
   *
   * @param clientId the client ID
   * @param departmentId the department ID
   * @param name the employee name
   * @param hireDate the hire date of the employee
   * @return a success message if the employee is successfully added,
   *         or throws an exception if the operation fails
   */
  @PostMapping(value = "/addEmpToDept", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> addEmployeeToDepartment(
      @RequestAttribute("cid") int clientId,
      @RequestParam("did") int departmentId,
      @RequestParam("name") String name,
      @RequestParam("hireDate") String hireDate // this need to be in format of "yyyy-MM-dd"
  ) {
    Command command = new AddEmpToDeptCmd(clientId, departmentId, name, hireDate);
    return new ResponseEntity<>(command.execute(), HttpStatus.CREATED);
  }

  /**
   * Login as a client by validating the client ID.
   * Notice: No associated command for this method.
   *
   * @param clientId the client ID
   * @return a success message if the client ID is valid,
   *        or throws a 401 exception if the operation fails
   */
  @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> login(
      @RequestParam("cid") String clientId
  ) {
    Map<String, String> response = new HashMap<>();
    try {
      int cid = Integer.parseInt(CodecUtils.decode(clientId));
      Command command = new GetOrgInfoCmd(cid);
      Map<String, Object> orgResponse = (Map<String, Object>) command.execute();
      String orgName = (String) orgResponse.get("name");
      response.put("status", "success");
      response.put("message", "Logged in as " + orgName);
      return new ResponseEntity<>(response, HttpStatus.OK);
    } catch (NotFoundException | IllegalArgumentException e) {
      response.put("status", "failed");
      response.put("message", "Invalid client ID");
      return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }
  }

  /* ***** DELETE METHODS ***** */

  /**
   * Remove an employee from the given department.
   *
   * @param clientId the client ID
   * @param departmentId the department ID
   * @param employeeId the employee ID
   * @return a success message if the employee is successfully removed,
   *         or throws an exception if the operation fails
   */
  @DeleteMapping(value = "/removeEmpFromDept", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> removeEmployeeFromDept(
      @RequestAttribute("cid") int clientId,
      @RequestParam("did") int departmentId,
      @RequestParam("eid") int employeeId
  ) {
    Command command = new RemoveEmpFromDeptCmd(clientId, departmentId, employeeId);
    return new ResponseEntity<>(command.execute(), HttpStatus.OK);
  }

  /**
   * Handles any exceptions that occur in controller.
   *
   * @param e the exception that occurred
   * @return the response entity
   * @deprecated This method is replaced by the global exception handler after 80586a8
   */
  @Deprecated
  private ResponseEntity<?> handleException(Exception e) {
    System.out.println(e.toString());
    return new ResponseEntity<>("An Error has occurred", HttpStatus.INTERNAL_SERVER_ERROR);
  }

}