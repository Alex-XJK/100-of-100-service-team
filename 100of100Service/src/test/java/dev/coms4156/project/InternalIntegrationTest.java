package dev.coms4156.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Internal integration tests for the HR system.
 * These tests verify the interactions between the core classes:
 * `Employee`, `Department`, `Organization`, `HrDatabaseFacade`, and `DatabaseConnection`.
 * Each test method specifies the classes being integrated.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InternalIntegrationTest {

  // Fields for Employee, Department, Organization tests
  private Organization organization;
  private Department department1;
  private Department department2;
  private Employee employee1;
  private Employee employee2;

  // Fields for HrDatabaseFacade and DatabaseConnection tests
  private HrDatabaseFacade facade;
  private DatabaseConnection mockDbConnection;
  private static int testOrganizationId;

  @BeforeAll
  public static void setupAll() {
    testOrganizationId = 1;
  }

  /**
   * Sets up the test environment.
   */
  @BeforeEach
  public void setup() throws Exception {
    // Initialize organization, departments, employees
    organization = new Organization(testOrganizationId, "Test Organization");

    department1 = new Department(1, "Engineering");
    department2 = new Department(2, "Marketing");

    employee1 = new Employee(1, "John Doe", new Date());
    employee2 = new Employee(2, "Jane Smith", new Date());

    organization.addDepartment(department1);
    organization.addDepartment(department2);

    // Set up HrDatabaseFacade and DatabaseConnection mocks
    mockDbConnection = mock(DatabaseConnection.class);

    // Mock Organization
    when(mockDbConnection.getOrganization(testOrganizationId))
        .thenReturn(organization);

    // Mock departments and employees
    List<Department> mockDepartments = Arrays.asList(department1, department2);
    List<Employee> mockEmployees = Arrays.asList(employee1, employee2);

    when(mockDbConnection.getDepartments(testOrganizationId))
        .thenReturn(mockDepartments);
    when(mockDbConnection.getEmployees(testOrganizationId))
        .thenReturn(mockEmployees);

    // Set the mock DatabaseConnection in HrDatabaseFacade
    HrDatabaseFacade.setConnection(mockDbConnection);

    // Clear the singleton instances using reflection
    clearHrDatabaseFacadeInstances();

    // Initialize HrDatabaseFacade with the test organization ID
    facade = HrDatabaseFacade.getInstance(testOrganizationId);
  }

  @AfterEach
  public void tearDown() throws Exception {
    clearHrDatabaseFacadeInstances();
  }

  /**
   * Clears the singleton instances of HrDatabaseFacade using reflection.
   *
   * @throws Exception if an error occurs during reflection
   */
  private void clearHrDatabaseFacadeInstances() throws Exception {
    Field instancesField = HrDatabaseFacade.class.getDeclaredField("instances");
    instancesField.setAccessible(true);
    ((Map<Integer, HrDatabaseFacade>) instancesField.get(null)).clear();
  }

  /**
   * Test 1: Verifies that adding an employee to a department associates the employee
   * with that department.
   * Classes integrated: `Employee`, `Department`
   */
  @Test
  @Order(1)
  public void testAddEmployeeToDepartment() {
    department1.addEmployee(employee1);
    assertTrue(department1.getEmployees().contains(employee1));
  }

  /**
   * Test 2: Verifies that assigning a head to a department correctly updates the department's head.
   * Classes integrated: `Employee`, `Department`
   */
  @Test
  @Order(2)
  public void testAssignDepartmentHead() {
    department1.setHead(employee1);
    assertEquals(employee1, department1.getHead());
  }

  /**
   * Test 3: Verifies that transferring an employee from one department to another
   * updates the departments' employee lists.
   * Classes integrated: `Employee`, `Department`
   */
  @Test
  @Order(3)
  public void testTransferEmployeeBetweenDepartments() {
    department1.addEmployee(employee1);
    department1.removeEmployee(employee1);
    department2.addEmployee(employee1);
    assertFalse(department1.getEmployees().contains(employee1));
    assertTrue(department2.getEmployees().contains(employee1));
  }

  /**
   * Test 4: Verifies that removing an employee from a department updates
   * the department's employee list.
   * Classes integrated: `Employee`, `Department`
   */
  @Test
  @Order(4)
  public void testRemoveEmployeeFromDepartment() {
    department1.addEmployee(employee1);
    department1.removeEmployee(employee1);
    assertFalse(department1.getEmployees().contains(employee1));
  }

  /**
   * Test 5: Verifies that the department's employee count reflects the number of employees added.
   * Classes integrated: `Employee`, `Department`
   */
  @Test
  @Order(5)
  public void testDepartmentEmployeeCount() {
    department1.addEmployee(employee1);
    department1.addEmployee(employee2);
    assertEquals(2, department1.getEmployees().size());
  }

  /**
   * Test 6: Verifies that department position statistics are calculated correctly.
   * Classes integrated: `Employee`, `Department`
   */
  @Test
  @Order(6)
  public void testDepartmentEmployeePositionStatistic() {
    employee1.setPosition("Engineer");
    employee2.setPosition("Engineer");
    department1.addEmployee(employee1);
    department1.addEmployee(employee2);
    Map<String, Integer> positionStats = department1.getEmployeePositionStatisticMap();
    assertEquals(1, positionStats.size());
    assertTrue(positionStats.containsKey("engineer"));
    assertEquals(2, positionStats.get("engineer"));
  }

  /**
   * Test 7: Verifies that department salary statistics are calculated correctly.
   * Classes integrated: `Employee`, `Department`
   */
  @Test
  @Order(7)
  public void testDepartmentEmployeeSalaryStatistic() {
    employee1.setSalary(50000);
    employee2.setSalary(60000);
    department1.addEmployee(employee1);
    department1.addEmployee(employee2);
    Map<String, Object> salaryStats = department1.getEmployeeSalaryStatisticMap();
    assertEquals(110000.0, (double) salaryStats.get("total"));
    assertEquals(55000.0, (double) salaryStats.get("average"));
    assertEquals(60000.0, (double) salaryStats.get("highest"));
    assertEquals(50000.0, (double) salaryStats.get("lowest"));
  }

  /**
   * Test 8: Verifies that department performance statistics are calculated correctly.
   * Classes integrated: `Employee`, `Department`
   */
  @Test
  @Order(8)
  public void testDepartmentEmployeePerformanceStatistic() {
    employee1.setPerformance(85.0);
    employee2.setPerformance(90.0);
    department1.addEmployee(employee1);
    department1.addEmployee(employee2);
    Map<String, Object> performanceStats = department1.getEmployeePerformanceStatisticMap();
    assertEquals(90.0, (double) performanceStats.get("highest"));
    assertEquals(85.0, (double) performanceStats.get("lowest"));
    assertEquals(87.5, (double) performanceStats.get("average"));
  }

  /**
   * Test 9: Verifies that the organization contains the added departments.
   * Classes integrated: `Organization`, `Department`
   */
  @Test
  @Order(9)
  public void testOrganizationContainsDepartments() {
    List<OrganizationComponent> components = organization.getChildren();
    assertTrue(components.contains(department1));
    assertTrue(components.contains(department2));
  }

  /**
   * Test 10: Verifies that the organization can be converted to JSON correctly.
   * Classes integrated: `Organization`
   */
  @Test
  @Order(10)
  public void testOrganizationToJson() {
    Map<String, Object> orgJson = organization.toJson();
    assertEquals(testOrganizationId, orgJson.get("id"));
    assertEquals("Test Organization", orgJson.get("name"));
  }

  /**
   * Test 11: Verifies that adding an employee through `HrDatabaseFacade` correctly delegates
   * to `DatabaseConnection`.
   * Classes integrated: `HrDatabaseFacade`, `DatabaseConnection`, `Employee`, `Department`
   */
  @Test
  @Order(11)
  public void testAddEmployeeDelegation() {
    int departmentId = department1.getId();
    Employee newEmployee = new Employee(0, "New Employee", new Date());

    int internalEmpId = testOrganizationId * 10000 + 3;
    when(mockDbConnection.addEmployeeToDepartment(
        eq(testOrganizationId),
        eq(testOrganizationId * 10000 + departmentId),
        any(Employee.class)))
        .thenReturn(internalEmpId);

    Employee addedEmployee = new Employee(3, "New Employee", new Date());
    List<Employee> updatedEmployees = new ArrayList<>(
        Arrays.asList(employee1, employee2, addedEmployee)
    );
    when(mockDbConnection.getEmployees(testOrganizationId))
        .thenReturn(updatedEmployees);

    Employee result = facade.addEmployeeToDepartment(departmentId, newEmployee);

    verify(mockDbConnection, times(1))
        .addEmployeeToDepartment(
            eq(testOrganizationId),
            eq(testOrganizationId * 10000 + departmentId),
            eq(newEmployee)
        );

    assertNotNull(result);
    assertEquals(3, result.getId());
    assertEquals("New Employee", result.getName());
  }

  /**
   * Test 12: Verifies that getting the organization from `HrDatabaseFacade` returns
   * the correct organization.
   * Classes integrated: `HrDatabaseFacade`, `Organization`
   */
  @Test
  @Order(12)
  public void testGetOrganizationFromFacade() {
    Organization result = facade.getOrganization();

    assertNotNull(result);
    assertEquals(testOrganizationId, result.getId());
    assertEquals("Test Organization", result.getName());
  }

  /**
   * Test 13: Verifies that updating an employee through `HrDatabaseFacade` correctly delegates
   * to `DatabaseConnection`.
   * Classes integrated: `HrDatabaseFacade`, `DatabaseConnection`, `Employee`
   */
  @Test
  @Order(13)
  public void testUpdateEmployeeDelegation() {
    Employee updatedEmployee = new Employee(1, "John Doe", new Date());
    updatedEmployee.setPosition("Senior Engineer");
    updatedEmployee.setSalary(80000);

    when(mockDbConnection.updateEmployee(testOrganizationId, updatedEmployee))
        .thenReturn(true);

    boolean result = facade.updateEmployee(updatedEmployee);

    verify(mockDbConnection, times(1))
        .updateEmployee(testOrganizationId, updatedEmployee);

    assertTrue(result);
  }

  /**
   * Test 14: Verifies that getting an employee from `HrDatabaseFacade`
   * retrieves the correct employee.
   * lasses integrated: `HrDatabaseFacade`, `Employee`
   */
  @Test
  @Order(14)
  public void testGetEmployee() {
    Employee result = facade.getEmployee(employee1.getId());

    assertNotNull(result);
    assertEquals(employee1.getName(), result.getName());
  }

  /**
   * Test 15: Verifies that getting a department from `HrDatabaseFacade`
   * retrieves the correct department.
   * Classes integrated: `HrDatabaseFacade`, `Department`
   */
  @Test
  @Order(15)
  public void testGetDepartment() {
    Department result = facade.getDepartment(department1.getId());

    assertNotNull(result);
    assertEquals(department1.getName(), result.getName());
  }
}
