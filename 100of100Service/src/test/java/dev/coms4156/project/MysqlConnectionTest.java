package dev.coms4156.project;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * A unit test class for the real MysqlConnection class.
 */
public class MysqlConnectionTest {

  private DatabaseConnection realConnection;
  private int testOrganizationId = 1;

  /**
   * Sets up the test environment by initializing the (real) DatabaseConnection instance.
   */
  @BeforeEach
  public void setup() {
    try {
      realConnection = MysqlConnection.getInstance();
    } catch (Exception e) {
      // If the database connection fails, skip the tests.
      assumeTrue(false, "Database connection failed, skipping tests.");
    }
  }

  @Test
  public void testGetOrganization() {
    Organization org = realConnection.getOrganization(testOrganizationId);
    assertNotNull(org, "Organization should not be null");
    System.out.println("Successfully retrieved organization: " + org.getName());
  }

  @Test
  public void testGetNonexistentOrganization() {
    Organization org = realConnection.getOrganization(-1);
    assertNull(org, "Organization should be null for nonexistent ID");
  }

  @Test
  public void testGetEmployees() {
    List<Employee> employees = realConnection.getEmployees(testOrganizationId);
    assertNotNull(employees, "Employees list should not be null");
    assertFalse(employees.isEmpty(), "Employees list should not be empty");
    System.out.println("Retrieved " + employees.size() + " employees");
  }

  @Test
  public void testGetEmployeesForNonexistentOrganization() {
    List<Employee> employees = realConnection.getEmployees(-1);
    assertNotNull(employees, "Employees list should not be null");
    assertTrue(employees.isEmpty(), "Employees list should be empty for nonexistent organization");
  }

  @Test
  public void testGetEmployee() {
    Employee employee = realConnection.getEmployee(testOrganizationId, 1);
    assertNotNull(employee, "Employee should not be null");
    System.out.println("Retrieved employee: " + employee.getName());
  }

  @Test
  public void testGetNonexistentEmployee() {
    Employee employee = realConnection.getEmployee(testOrganizationId, -1);
    assertNull(employee, "Employee should be null for nonexistent ID");
  }

  @Test
  public void testGetDepartments() {
    List<Department> departments = realConnection.getDepartments(testOrganizationId);
    assertNotNull(departments, "Departments list should not be null");
    assertFalse(departments.isEmpty(), "Departments list should not be empty");
    System.out.println("Retrieved " + departments.size() + " departments");
  }

  @Test
  public void testGetDepartmentsForNonexistentOrganization() {
    List<Department> departments = realConnection.getDepartments(-1);
    assertNotNull(departments, "Departments list should not be null");
    assertTrue(departments.isEmpty(), "Departments list should be empty for nonexistent organization");
  }

  @Test
  public void testGetDepartment() {
    Department department = realConnection.getDepartment(testOrganizationId, 1);
    assertNotNull(department, "Department should not be null");
    System.out.println("Retrieved department: " + department.getName());
  }

  @Test
  public void testGetNonexistentDepartment() {
    Department department = realConnection.getDepartment(testOrganizationId, -1);
    assertNull(department, "Department should be null for nonexistent ID");
  }

  @Test
  public void testAddEmployeeToDepartment() {
    Employee newEmployee = new Employee(0, "Test Employee", new Date());
    newEmployee.setPosition("Tester");
    newEmployee.setSalary(50000);
    newEmployee.setPerformance(80);

    int departmentId = 1; // External department ID
    int internalDeptId = testOrganizationId * 10000 + departmentId;

    int newEmployeeId = realConnection.addEmployeeToDepartment(testOrganizationId, internalDeptId, newEmployee);
    assertTrue(newEmployeeId > 0, "New employee ID should be positive");

    // Verify the employee was added
    Employee addedEmployee = realConnection.getEmployee(testOrganizationId, newEmployeeId % 10000);
    assertNotNull(addedEmployee, "Added employee should not be null");
    assertEquals("Test Employee", addedEmployee.getName(), "Employee name should match");

    // Cleanup: Remove the added employee
    boolean removed = realConnection.removeEmployeeFromDepartment(testOrganizationId, internalDeptId, newEmployeeId);
    assertTrue(removed, "Employee should be removed successfully");
  }

  @Test
  public void testAddEmployeeToNonexistentDepartment() {
    Employee newEmployee = new Employee(0, "Test Employee", new Date());
    int nonExistentDeptId = -1;
    int newEmployeeId = realConnection.addEmployeeToDepartment(testOrganizationId, nonExistentDeptId, newEmployee);
    assertEquals(-1, newEmployeeId, "Should return -1 when adding to a nonexistent department");
  }

  @Test
  public void testRemoveEmployeeFromDepartment() {
    // First, add an employee to remove
    Employee newEmployee = new Employee(0, "Employee to Remove", new Date());
    newEmployee.setPosition("Temp");
    newEmployee.setSalary(40000);
    newEmployee.setPerformance(70);

    int departmentId = 1;
    int internalDeptId = testOrganizationId * 10000 + departmentId;

    int newEmployeeId = realConnection.addEmployeeToDepartment(testOrganizationId, internalDeptId, newEmployee);
    assertTrue(newEmployeeId > 0, "New employee ID should be positive");

    // Now, remove the employee
    boolean removed = realConnection.removeEmployeeFromDepartment(testOrganizationId, internalDeptId, newEmployeeId);
    assertTrue(removed, "Employee should be removed successfully");

    // Verify the employee was removed
    Employee removedEmployee = realConnection.getEmployee(testOrganizationId, newEmployeeId % 10000);
    assertNull(removedEmployee, "Employee should be null after removal");
  }

  @Test
  public void testRemoveNonexistentEmployeeFromDepartment() {
    int departmentId = 1;
    int internalDeptId = testOrganizationId * 10000 + departmentId;
    int nonExistentEmployeeId = -1;

    boolean removed = realConnection.removeEmployeeFromDepartment(testOrganizationId, internalDeptId, nonExistentEmployeeId);
    assertFalse(removed, "Removing a nonexistent employee should return false");
  }

  @Test
  public void testUpdateEmployee() {
    Employee employee = realConnection.getEmployee(testOrganizationId, 1);
    assertNotNull(employee, "Employee should not be null");

    // Update employee details
    String originalPosition = employee.getPosition();
    employee.setPosition("Updated Position");

    boolean updated = realConnection.updateEmployee(testOrganizationId, employee);
    assertTrue(updated, "Employee should be updated successfully");

    // Verify the update
    Employee updatedEmployee = realConnection.getEmployee(testOrganizationId, 1);
    assertEquals("Updated Position", updatedEmployee.getPosition(), "Employee position should be updated");

    // Restore original position
    employee.setPosition(originalPosition);
    realConnection.updateEmployee(testOrganizationId, employee);
  }

  @Test
  public void testUpdateNonexistentEmployee() {
    Employee employee = new Employee(-1, "Nonexistent Employee", new Date());
    boolean updated = realConnection.updateEmployee(testOrganizationId, employee);
    assertFalse(updated, "Updating a nonexistent employee should return false");
  }

//  @Test
//  public void testUpdateDepartment() {
//    Department department = realConnection.getDepartment(testOrganizationId, 1);
//    assertNotNull(department, "Department should not be null");
//
//    // Update department name
//    String originalName = department.getName();
//    department.setName("Updated Department Name");
//
//    boolean updated = realConnection.updateDepartment(testOrganizationId, department);
//    assertTrue(updated, "Department should be updated successfully");
//
//    // Verify the update
//    Department updatedDepartment = realConnection.getDepartment(testOrganizationId, 1);
//    assertEquals("Updated Department Name", updatedDepartment.getName(), "Department name should be updated");
//
//    // Restore original name
//    department.setName(originalName);
//    realConnection.updateDepartment(testOrganizationId, department);
//  }

  @Test
  public void testUpdateDepartmentWithInvalidHead() {
    Department department = realConnection.getDepartment(testOrganizationId, 1);
    assertNotNull(department, "Department should not be null");

    // Set an invalid head (nonexistent employee)
    Employee invalidHead = new Employee(-1, "Invalid Head", new Date());
    department.setHead(invalidHead);

    boolean updated = realConnection.updateDepartment(testOrganizationId, department);
    assertFalse(updated, "Updating department with invalid head should return false");
  }

  @Test
  public void testUpdateDepartmentWithNullHead() {
    Department department = realConnection.getDepartment(testOrganizationId, 1);
    assertNotNull(department, "Department should not be null");

    // Set head to null
    department.setHead(null);

    boolean updated = realConnection.updateDepartment(testOrganizationId, department);
    assertTrue(updated, "Updating department with null head should be successful");
  }

  @Test
  public void testUpdateDepartmentWithValidHead() {
    Department department = realConnection.getDepartment(testOrganizationId, 1);
    assertNotNull(department, "Department should not be null");

    // Set a valid head
    Employee validHead = realConnection.getEmployee(testOrganizationId, 1);
    department.setHead(validHead);

    boolean updated = realConnection.updateDepartment(testOrganizationId, department);
    assertTrue(updated, "Updating department with valid head should be successful");
  }

//  @Test
//  public void testUpdateOrganization() {
//    Organization organization = realConnection.getOrganization(testOrganizationId);
//    assertNotNull(organization, "Organization should not be null");
//
//    // Update organization name
//    String originalName = organization.getName();
//    organization.setName("Updated Organization Name");
//
//    boolean updated = realConnection.updateOrganization(organization);
//    assertTrue(updated, "Organization should be updated successfully");
//
//    // Verify the update
//    Organization updatedOrganization = realConnection.getOrganization(testOrganizationId);
//    assertEquals("Updated Organization Name", updatedOrganization.getName(), "Organization name should be updated");
//
//    // Restore original name
//    organization.setName(originalName);
//    realConnection.updateOrganization(organization);
//  }

  @Test
  public void testUpdateNonexistentOrganization() {
    Organization organization = new Organization(-1, "Nonexistent Organization");
    boolean updated = realConnection.updateOrganization(organization);
    assertFalse(updated, "Updating a nonexistent organization should return false");
  }

  @Test
  public void testInsertDepartment() {
    Department newDepartment = new Department(0, "New Department", new ArrayList<>());

    Department insertedDepartment = realConnection.insertDepartment(testOrganizationId, newDepartment);
    assertNotNull(insertedDepartment, "Inserted department should not be null");
    assertTrue(insertedDepartment.getId() > 0, "Inserted department ID should be positive");

    // Cleanup: Remove the inserted department
    boolean removed = realConnection.removeDepartment(testOrganizationId, insertedDepartment.getId());
    assertTrue(removed, "Inserted department should be removed successfully");
  }

  @Test
  public void testInsertDepartmentWithExistingName() {
    // Attempt to insert a department with a name that already exists
    Department newDepartment = new Department(0, "Engineering", new ArrayList<>());

    Department insertedDepartment = realConnection.insertDepartment(testOrganizationId, newDepartment);
    assertNotNull(insertedDepartment, "Inserted department should not be null");

    // Cleanup: Remove the inserted department
    boolean removed = realConnection.removeDepartment(testOrganizationId, insertedDepartment.getId());
    assertTrue(removed, "Inserted department should be removed successfully");
  }

  @Test
  public void testRemoveDepartment() {
    // First, insert a department to remove
    Department newDepartment = new Department(0, "Department to Remove", new ArrayList<>());
    Department insertedDepartment = realConnection.insertDepartment(testOrganizationId, newDepartment);
    assertNotNull(insertedDepartment, "Inserted department should not be null");

    // Now, remove the department
    boolean removed = realConnection.removeDepartment(testOrganizationId, insertedDepartment.getId());
    assertTrue(removed, "Department should be removed successfully");

    // Verify the removal
    Department removedDepartment = realConnection.getDepartment(testOrganizationId, insertedDepartment.getId());
    assertNull(removedDepartment, "Department should be null after removal");
  }

  @Test
  public void testRemoveNonexistentDepartment() {
    boolean removed = realConnection.removeDepartment(testOrganizationId, -1);
    assertFalse(removed, "Removing a nonexistent department should return false");
  }

  @Test
  public void testInsertOrganization() {
    Organization newOrg = new Organization(0, "New Organization");
    Organization insertedOrg = realConnection.insertOrganization(newOrg);
    assertNotNull(insertedOrg, "Inserted organization should not be null");
    assertTrue(insertedOrg.getId() > 0, "Inserted organization ID should be positive");

    // Cleanup: Remove the inserted organization
    boolean removed = realConnection.removeOrganization(insertedOrg.getId());
    assertTrue(removed, "Inserted organization should be removed successfully");
  }

  @Test
  public void testInsertOrganizationWithExistingName() {
    // Attempt to insert an organization with a name that might already exist
    Organization newOrg = new Organization(0, "Acme Corp");
    Organization insertedOrg = realConnection.insertOrganization(newOrg);
    assertNotNull(insertedOrg, "Inserted organization should not be null");

    // Cleanup: Remove the inserted organization
    boolean removed = realConnection.removeOrganization(insertedOrg.getId());
    assertTrue(removed, "Inserted organization should be removed successfully");
  }

  @Test
  public void testRemoveOrganization() {
    // First, insert an organization to remove
    Organization newOrg = new Organization(0, "Organization to Remove");
    Organization insertedOrg = realConnection.insertOrganization(newOrg);
    assertNotNull(insertedOrg, "Inserted organization should not be null");

    // Now, remove the organization
    boolean removed = realConnection.removeOrganization(insertedOrg.getId());
    assertTrue(removed, "Organization should be removed successfully");

    // Verify the removal
    Organization removedOrg = realConnection.getOrganization(insertedOrg.getId());
    assertNull(removedOrg, "Organization should be null after removal");
  }

  @Test
  public void testRemoveNonexistentOrganization() {
    boolean removed = realConnection.removeOrganization(-1);
    assertFalse(removed, "Removing a nonexistent organization should return false");
  }

  @Test
  public void testMysqlConnectionConstructor() {
    // Since the constructor is private, we'll test the singleton instance
    MysqlConnection connection1 = MysqlConnection.getInstance();
    MysqlConnection connection2 = MysqlConnection.getInstance();
    assertSame(connection1, connection2, "Both instances should be the same (singleton)");
  }

  @Test
  public void testConnectionName() {
    String name = realConnection.connectionName();
    assertNotNull(name, "Connection name should not be null");
    System.out.println("Connection name: " + name);
  }
}
