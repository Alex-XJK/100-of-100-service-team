package dev.coms4156.project;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import dev.coms4156.project.exception.InternalServerErrorException;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A unit test class for the real MysqlConnection class.
 */
public class MysqlConnectionTest {

  private static final Logger logger = LoggerFactory.getLogger(MysqlConnectionTest.class);

  private MysqlConnection realConnection;
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

  @AfterEach
  public void tearDown() throws Exception {
    // Clear any system properties set during tests
    System.clearProperty("db.url");
    System.clearProperty("db.user");
    System.clearProperty("db.password");

    // Reset the MysqlConnection instance to ensure a fresh connection for each test
    resetMysqlConnectionInstance();
  }

  private void resetMysqlConnectionInstance() throws Exception {
    Field instanceField = MysqlConnection.class.getDeclaredField("instance");
    instanceField.setAccessible(true);
    instanceField.set(null, null);
    // Do not re-initialize realConnection here; let setup() handle it
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
    List<Employee> employees = realConnection.getEmployees(testOrganizationId);
    assumeTrue(!employees.isEmpty(), "No employees found in the organization to test");
    int employeeId = employees.get(0).getId();

    Employee employee = realConnection.getEmployee(testOrganizationId, employeeId);
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
    List<Department> departments = realConnection.getDepartments(testOrganizationId);
    assumeTrue(!departments.isEmpty(), "No departments found in the organization to test");
    int departmentId = departments.get(0).getId();

    Department department = realConnection.getDepartment(testOrganizationId, departmentId);
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

    List<Department> departments = realConnection.getDepartments(testOrganizationId);
    assumeTrue(!departments.isEmpty(), "No departments found in the organization to test");
    int departmentId = departments.get(0).getId();
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

    List<Department> departments = realConnection.getDepartments(testOrganizationId);
    assumeTrue(!departments.isEmpty(), "No departments found in the organization to test");
    int departmentId = departments.get(0).getId();
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
    List<Department> departments = realConnection.getDepartments(testOrganizationId);
    assumeTrue(!departments.isEmpty(), "No departments found in the organization to test");
    int departmentId = departments.get(0).getId();
    int internalDeptId = testOrganizationId * 10000 + departmentId;
    int nonExistentEmployeeId = -1;

    boolean removed = realConnection.removeEmployeeFromDepartment(testOrganizationId, internalDeptId, nonExistentEmployeeId);
    assertFalse(removed, "Removing a nonexistent employee should return false");
  }

  @Test
  public void testUpdateEmployee() {
    List<Employee> employees = realConnection.getEmployees(testOrganizationId);
    assumeTrue(!employees.isEmpty(), "No employees found in the organization to test");
    Employee employee = employees.get(0);

    // Update employee details
    String originalPosition = employee.getPosition();
    employee.setPosition("Updated Position");

    boolean updated = realConnection.updateEmployee(testOrganizationId, employee);
    assertTrue(updated, "Employee should be updated successfully");

    // Verify the update
    Employee updatedEmployee = realConnection.getEmployee(testOrganizationId, employee.getId());
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

  @Test
  public void testUpdateDepartmentWithInvalidHead() {
    List<Department> departments = realConnection.getDepartments(testOrganizationId);
    assumeTrue(!departments.isEmpty(), "No departments found in the organization to test");
    Department department = departments.get(0);

    // Set an invalid head (nonexistent employee)
    Employee invalidHead = new Employee(-1, "Invalid Head", new Date());
    department.setHead(invalidHead);

    boolean updated = realConnection.updateDepartment(testOrganizationId, department);
    assertFalse(updated, "Updating department with invalid head should return false");
  }

  @Test
  public void testUpdateDepartmentWithNullHead() {
    List<Department> departments = realConnection.getDepartments(testOrganizationId);
    assumeTrue(!departments.isEmpty(), "No departments found in the organization to test");
    Department department = departments.get(0);

    // Set head to null
    department.setHead(null);

    boolean updated = realConnection.updateDepartment(testOrganizationId, department);
    assertTrue(updated, "Updating department with null head should be successful");
  }

  @Test
  public void testUpdateDepartmentWithValidHead() {
    List<Department> departments = realConnection.getDepartments(testOrganizationId);
    assumeTrue(!departments.isEmpty(), "No departments found in the organization to test");
    Department department = departments.get(0);

    List<Employee> employees = realConnection.getEmployees(testOrganizationId);
    assumeTrue(!employees.isEmpty(), "No employees found in the organization to test");
    Employee validHead = employees.get(0);

    department.setHead(validHead);

    boolean updated = realConnection.updateDepartment(testOrganizationId, department);
    assertTrue(updated, "Updating department with valid head should be successful");
  }

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
  public void testMysqlConnectionSingleton() {
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

  // New test to cover the catch block in the constructor
  @Test
  public void testMysqlConnectionConstructor_SQLException() throws Exception {
    // Set invalid database URL to induce SQLException
    System.setProperty("db.url", "jdbc:mysql://invalid-host:3306/invalid_db");
    System.setProperty("db.user", "invalid_user");
    System.setProperty("db.password", "invalid_password");

    // Reset the MysqlConnection instance to force the constructor to run again
    resetMysqlConnectionInstance();

    Exception exception = assertThrows(InternalServerErrorException.class, () -> {
      MysqlConnection.getInstance();
    });

    assertEquals("Failed to connect to the database.", exception.getMessage());
    // The catch block in the constructor should now be covered
  }

  // Tests that induce SQLException in methods by closing the connection

  @Test
  public void testGetEmployee_SQLException() throws Exception {
    // Use reflection to close the connection and induce SQLException
    closeConnection();

    Employee employee = realConnection.getEmployee(testOrganizationId, 1);
    assertNull(employee, "Employee should be null due to SQLException");
  }

  @Test
  public void testGetDepartment_SQLException() throws Exception {
    // Use reflection to close the connection and induce SQLException
    closeConnection();

    Department department = realConnection.getDepartment(testOrganizationId, 1);
    assertNull(department, "Department should be null due to SQLException");
  }

  @Test
  public void testGetEmployees_SQLException() throws Exception {
    // Use reflection to close the connection and induce SQLException
    closeConnection();

    List<Employee> employees = realConnection.getEmployees(testOrganizationId);
    assertNotNull(employees, "Employees list should not be null even after SQLException");
    assertTrue(employees.isEmpty(), "Employees list should be empty due to SQLException");
  }

  @Test
  public void testGetDepartments_SQLException() throws Exception {
    // Use reflection to close the connection and induce SQLException
    closeConnection();

    List<Department> departments = realConnection.getDepartments(testOrganizationId);
    assertNotNull(departments, "Departments list should not be null even after SQLException");
    assertTrue(departments.isEmpty(), "Departments list should be empty due to SQLException");
  }

  @Test
  public void testGetOrganization_SQLException() throws Exception {
    // Use reflection to close the connection and induce SQLException
    closeConnection();

    Organization org = realConnection.getOrganization(testOrganizationId);
    assertNull(org, "Organization should be null due to SQLException");
  }

  @Test
  public void testAddEmployeeToDepartment_SQLException() throws Exception {
    // Use reflection to close the connection and induce SQLException
    closeConnection();

    Employee newEmployee = new Employee(0, "Test Employee", new Date());
    int departmentId = 1;
    int internalDeptId = testOrganizationId * 10000 + departmentId;

    int result = realConnection.addEmployeeToDepartment(testOrganizationId, internalDeptId, newEmployee);
    assertEquals(-1, result, "Method should return -1 due to SQLException");
  }

  @Test
  public void testRemoveEmployeeFromDepartment_SQLException() throws Exception {
    // Use reflection to close the connection and induce SQLException
    closeConnection();

    boolean result = realConnection.removeEmployeeFromDepartment(testOrganizationId, 1, 1);
    assertFalse(result, "Method should return false due to SQLException");
  }

  @Test
  public void testUpdateEmployee_SQLException() throws Exception {
    // Use reflection to close the connection and induce SQLException
    closeConnection();

    Employee employee = new Employee(1, "Test Employee", new Date());
    boolean result = realConnection.updateEmployee(testOrganizationId, employee);
    assertFalse(result, "Method should return false due to SQLException");
  }

  @Test
  public void testUpdateDepartment_SQLException() throws Exception {
    // Use reflection to close the connection and induce SQLException
    closeConnection();

    Department department = new Department(1, "Test Department", new ArrayList<>());
    boolean result = realConnection.updateDepartment(testOrganizationId, department);
    assertFalse(result, "Method should return false due to SQLException");
  }

  @Test
  public void testUpdateOrganization_SQLException() throws Exception {
    // Use reflection to close the connection and induce SQLException
    closeConnection();

    Organization organization = new Organization(testOrganizationId, "Test Org");
    boolean result = realConnection.updateOrganization(organization);
    assertFalse(result, "Method should return false due to SQLException");
  }

  @Test
  public void testInsertDepartment_SQLException() throws Exception {
    // Use reflection to close the connection and induce SQLException
    closeConnection();

    Department newDepartment = new Department(0, "New Department", new ArrayList<>());
    Department result = realConnection.insertDepartment(testOrganizationId, newDepartment);
    assertNull(result, "Method should return null due to SQLException");
  }

  @Test
  public void testRemoveDepartment_SQLException() throws Exception {
    // Use reflection to close the connection and induce SQLException
    closeConnection();

    boolean result = realConnection.removeDepartment(testOrganizationId, 1);
    assertFalse(result, "Method should return false due to SQLException");
  }

  @Test
  public void testInsertOrganization_SQLException() throws Exception {
    // Use reflection to close the connection and induce SQLException
    closeConnection();

    Organization newOrg = new Organization(0, "New Organization");
    Organization result = realConnection.insertOrganization(newOrg);
    assertNull(result, "Method should return null due to SQLException");
  }

  @Test
  public void testRemoveOrganization_SQLException() throws Exception {
    // Use reflection to close the connection and induce SQLException
    closeConnection();

    boolean result = realConnection.removeOrganization(testOrganizationId);
    assertFalse(result, "Method should return false due to SQLException");
  }

  @Test
  @Order(1)
  public void testAddEmployeeToDepartment_NoExistingEmployees() throws Exception {
    // Create a new organization
    Organization newOrg = new Organization(0, "New Org for Testing");
    Organization insertedOrg = realConnection.insertOrganization(newOrg);
    assertNotNull(insertedOrg, "Inserted organization should not be null");
    int newOrgId = insertedOrg.getId();

    // Insert a new department in the new organization
    Department newDepartment = new Department(0, "New Department", new ArrayList<>());
    Department insertedDepartment = realConnection.insertDepartment(newOrgId, newDepartment);
    assertNotNull(insertedDepartment, "Inserted department should not be null");
    int newDeptId = insertedDepartment.getId();
    int internalDeptId = newOrgId * 10000 + newDeptId;

    // Add an employee to the new department
    Employee newEmployee = new Employee(0, "First Employee", new Date());
    newEmployee.setPosition("Tester");
    newEmployee.setSalary(50000);
    newEmployee.setPerformance(80);

    int newEmployeeId = realConnection.addEmployeeToDepartment(newOrgId, internalDeptId, newEmployee);
    assertTrue(newEmployeeId > 0, "New employee ID should be positive");

    // Verify the employee was added
    Employee addedEmployee = realConnection.getEmployee(newOrgId, newEmployeeId % 10000);
    assertNotNull(addedEmployee, "Added employee should not be null");
    assertEquals("First Employee", addedEmployee.getName(), "Employee name should match");

    // Cleanup
    boolean removedEmployee = realConnection.removeEmployeeFromDepartment(newOrgId, internalDeptId, newEmployeeId);
    assertTrue(removedEmployee, "Employee should be removed successfully");

    boolean removedDepartment = realConnection.removeDepartment(newOrgId, newDeptId);
    assertTrue(removedDepartment, "Department should be removed successfully");

    boolean removedOrganization = realConnection.removeOrganization(newOrgId);
    assertTrue(removedOrganization, "Organization should be removed successfully");
  }

  @Test
  @Order(2)
  public void testRemoveEmployeeFromDepartment_RemovingDepartmentHead() throws Exception {
    // First, add a new employee
    Employee newEmployee = new Employee(0, "Department Head", new Date());
    newEmployee.setPosition("Manager");
    newEmployee.setSalary(60000);
    newEmployee.setPerformance(90);

    List<Department> departments = realConnection.getDepartments(testOrganizationId);
    assumeTrue(!departments.isEmpty(), "No departments found in the organization to test");
    int departmentId = departments.get(0).getId();
    int internalDeptId = testOrganizationId * 10000 + departmentId;

    int newEmployeeId = realConnection.addEmployeeToDepartment(testOrganizationId, internalDeptId, newEmployee);
    assertTrue(newEmployeeId > 0, "New employee ID should be positive");

    // Set the new employee as the head of the department
    Department department = realConnection.getDepartment(testOrganizationId, departmentId);
    Employee addedEmployee = realConnection.getEmployee(testOrganizationId, newEmployeeId % 10000);
    department.setHead(addedEmployee);
    boolean updated = realConnection.updateDepartment(testOrganizationId, department);
    assertTrue(updated, "Department should be updated successfully");

    // Now, remove the employee
    boolean removed = realConnection.removeEmployeeFromDepartment(testOrganizationId, internalDeptId, newEmployeeId);
    assertTrue(removed, "Employee should be removed successfully");

    // Verify that the department head is now null
    Department updatedDepartment = realConnection.getDepartment(testOrganizationId, departmentId);
    assertNull(updatedDepartment.getHead(), "Department head should be null after removing the head");
  }

  @Test
  @Order(3)
  public void testGetInstance_DoubleCheckedLocking() throws Exception {
    // Use reflection to set instance to null
    Field instanceField = MysqlConnection.class.getDeclaredField("instance");
    instanceField.setAccessible(true);

    // Save the original instance
    MysqlConnection originalInstance = (MysqlConnection) instanceField.get(null);

    // Set instance to null
    instanceField.set(null, null);

    // Call getInstance, which should create a new instance
    MysqlConnection newInstance = MysqlConnection.getInstance();
    assertNotNull(newInstance, "New instance should be created");

    // Verify that the new instance is different from the original (if original was not null)
    if (originalInstance != null) {
      assertNotSame(originalInstance, newInstance, "New instance should not be the same as the original");
    }

    // Restore the original instance
    instanceField.set(null, originalInstance);
  }

  @Test
  @Order(4)
  public void testUpdateNonexistentOrganization_ReturnsFalse() {
    Organization organization = new Organization(-1, "Nonexistent Organization");
    boolean updated = realConnection.updateOrganization(organization);
    assertFalse(updated, "Updating a nonexistent organization should return false");
  }

  @Test
  @Order(5)
  public void testUpdateNonexistentDepartment_ReturnsFalse() {
    Department department = new Department(-1, "Nonexistent Department", new ArrayList<>());
    boolean updated = realConnection.updateDepartment(testOrganizationId, department);
    assertFalse(updated, "Updating a nonexistent department should return false");
  }

  @Test
  @Order(6)
  public void testAddEmployeeToNonexistentDepartment_NoRowsAffected() {
    Employee newEmployee = new Employee(0, "Test Employee", new Date());
    int nonExistentDeptId = -1;
    int newEmployeeId = realConnection.addEmployeeToDepartment(testOrganizationId, nonExistentDeptId, newEmployee);
    assertEquals(-1, newEmployeeId, "Should return -1 when adding to a nonexistent department");
  }

  private void closeConnection() throws Exception {
    Field connectionField = MysqlConnection.class.getDeclaredField("connection");
    connectionField.setAccessible(true);
    Connection connection = (Connection) connectionField.get(realConnection);
    connection.close();

    // Also reset the instance so that subsequent tests get a fresh connection
    Field instanceField = MysqlConnection.class.getDeclaredField("instance");
    instanceField.setAccessible(true);
    instanceField.set(null, null);
  }
}
