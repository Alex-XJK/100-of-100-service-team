package dev.coms4156.project;

import dev.coms4156.project.exception.NotFoundException;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A test class for the HrDatabaseFacade class.
 */
public class HrDatabaseFacadeTest {

  private DatabaseConnection dbConnection;
  private HrDatabaseFacade facade;
  private int testOrganizationId = 1;

  /**
   * Sets up the test environment by initializing the DatabaseConnection and HrDatabaseFacade instance.
   */
  @BeforeEach
  public void setup() {
    dbConnection = InmemConnection.getInstance();
    HrDatabaseFacade.setConnection(dbConnection);
    facade = HrDatabaseFacade.getInstance(testOrganizationId);
  }

  @Test
  @Order(1)
  public void testGetEmployee_employeeNotInCache() throws Exception {
    Field employeesField = HrDatabaseFacade.class.getDeclaredField("employees");
    employeesField.setAccessible(true);
    employeesField.set(facade, new ArrayList<>());

    Employee employee = facade.getEmployee(1);
    assertNotNull(employee, "Employee should be fetched from database when not in cache");

    // Verify cache is updated
    List<Employee> employeesCache = (List<Employee>) employeesField.get(facade);
    assertFalse(employeesCache.isEmpty(), "Employees cache should be updated");
    assertTrue(employeesCache.contains(employee), "Employees cache should contain the fetched employee");
  }

  @Test
  @Order(2)
  public void testGetDepartment_departmentNotInCache() throws Exception {
    Field departmentsField = HrDatabaseFacade.class.getDeclaredField("departments");
    departmentsField.setAccessible(true);
    departmentsField.set(facade, new ArrayList<>());

    Department department = facade.getDepartment(1);
    assertNotNull(department, "Department should be fetched from database when not in cache");
  }

  @Test
  @Order(3)
  public void testRemoveEmployeeFromDepartment_DepartmentWithMultipleEmployees() {
    // Add multiple employees to a department
    Employee employee1 = new Employee(0, "Employee1", new Date());
    Employee employee2 = new Employee(0, "Employee2", new Date());
    Employee employee3 = new Employee(0, "Employee3", new Date());

    Employee addedEmployee1 = facade.addEmployeeToDepartment(1, employee1);
    Employee addedEmployee2 = facade.addEmployeeToDepartment(1, employee2);
    Employee addedEmployee3 = facade.addEmployeeToDepartment(1, employee3);

    boolean removed = facade.removeEmployeeFromDepartment(1, addedEmployee2.getId());
    assertTrue(removed, "Employee should be removed successfully");

    facade.removeEmployeeFromDepartment(1, addedEmployee1.getId());
    facade.removeEmployeeFromDepartment(1, addedEmployee3.getId());
  }

  @Test
  @Order(4)
  public void testRemoveDepartment_Successful() throws Exception {
    // Create a mock DatabaseConnection that returns true for removeDepartment
    DatabaseConnection mockDbConnection = new InmemConnection() {
      @Override
      public boolean removeDepartment(int orgId, int deptId) {
        return true;
      }
    };

    HrDatabaseFacade.setConnection(mockDbConnection);
    facade = HrDatabaseFacade.getInstance(testOrganizationId);

    Department departmentToRemove = new Department(99, "Dept to Remove", new ArrayList<>());
    facade.departments.add(departmentToRemove);

    boolean removed = facade.removeDepartment(99);
    assertTrue(removed, "Department should be removed successfully");

    assertFalse(facade.departments.stream().anyMatch(dept -> dept.getId() == 99),
            "Department should be removed from the cache");
  }

  @Test
  @Order(5)
  public void testRemoveOrganization_Successful() {
    // Create a mock DatabaseConnection that returns true for removeOrganization
    DatabaseConnection mockDbConnection = new InmemConnection() {
      @Override
      public boolean removeOrganization(int orgId) {
        return true; // Simulate successful removal
      }
    };

    HrDatabaseFacade.setConnection(mockDbConnection);

    HrDatabaseFacade facadeInstance = HrDatabaseFacade.getInstance(testOrganizationId);
    assertNotNull(facadeInstance, "Facade instance should exist");

    boolean removed = HrDatabaseFacade.removeOrganization(testOrganizationId);
    assertTrue(removed, "Organization should be removed successfully");

    Map<Integer, HrDatabaseFacade> instancesMap = getInstancesMapViaReflection();
    assertFalse(instancesMap.containsKey(testOrganizationId),
            "Facade instance should be removed from instances map");
  }

  @Test
  @Order(6)
  public void testGetInstance_DoubleCheckedLocking() {
    Map<Integer, HrDatabaseFacade> instancesMap = getInstancesMapViaReflection();
    instancesMap.clear();

    HrDatabaseFacade facadeInstance = HrDatabaseFacade.getInstance(testOrganizationId);
    assertNotNull(facadeInstance, "Facade instance should be created");

    assertTrue(instancesMap.containsKey(testOrganizationId), "Instances map should contain the organization ID");
  }

  private DatabaseConnection getDbConnectionViaReflection() {
    try {
      Field dbConnectionField = HrDatabaseFacade.class.getDeclaredField("dbConnection");
      dbConnectionField.setAccessible(true);
      return (DatabaseConnection) dbConnectionField.get(null);
    } catch (Exception e) {
      throw new RuntimeException("Failed to access dbConnection via reflection", e);
    }
  }

  private Map<Integer, HrDatabaseFacade> getInstancesMapViaReflection() {
    try {
      Field instancesField = HrDatabaseFacade.class.getDeclaredField("instances");
      instancesField.setAccessible(true);
      return (Map<Integer, HrDatabaseFacade>) instancesField.get(null);
    } catch (Exception e) {
      throw new RuntimeException("Failed to access instances map via reflection", e);
    }
  }

  @Test
  @Order(7)
  public void testGetOrganization() {
    Organization org = facade.getOrganization();
    assertNotNull(org, "Organization should not be null");
    System.out.println("Successfully retrieved: " + org.getName());
    System.out.println(Organization.displayStructure(org, 0));
  }

  @Test
  @Order(8)
  public void testGetNonexistentOrganization() {
    Exception exception = assertThrows(NotFoundException.class, () -> {
      HrDatabaseFacade.getInstance(-1);
    });
    assertEquals("Organization not found", exception.getMessage());
  }

  @Test
  @Order(9)
  public void testGetEmployee() {
    Employee employee = facade.getEmployee(1);
    assertNotNull(employee, "Employee should not be null");
    System.out.println("Retrieved employee: " + employee.getName());
  }

  @Test
  @Order(10)
  public void testGetNonexistentEmployee() {
    Employee employee = facade.getEmployee(-1);
    assertNull(employee, "Employee should be null for nonexistent ID");
  }

  @Test
  @Order(11)
  public void testGetDepartment() {
    Department department = facade.getDepartment(1);
    assertNotNull(department, "Department should not be null");
    System.out.println("Retrieved department: " + department.getName());
  }

  @Test
  @Order(12)
  public void testGetNonexistentDepartment() {
    Department department = facade.getDepartment(-1);
    assertNull(department, "Department should be null for nonexistent ID");
  }

  @Test
  @Order(12)
  public void testAddEmployeeToDepartment() {
    Employee newEmployee = new Employee(0, "Test Employee", new Date());
    newEmployee.setPosition("Tester");
    newEmployee.setSalary(50000);
    newEmployee.setPerformance(80);

    Employee addedEmployee = facade.addEmployeeToDepartment(1, newEmployee);
    assertNotNull(addedEmployee, "Added employee should not be null");
    assertTrue(addedEmployee.getId() > 0, "Employee ID should be assigned");

    // Verify the employee is in the department
    Department department = facade.getDepartment(1);
    assertTrue(department.getEmployees().contains(addedEmployee), "Department should contain the new employee");
  }

  @Test
  @Order(13)
  public void testAddEmployeeToNonexistentDepartment() {
    Employee newEmployee = new Employee(0, "Test Employee", new Date());

    Employee addedEmployee = facade.addEmployeeToDepartment(-1, newEmployee);
    assertNull(addedEmployee, "Adding to a nonexistent department should return null");
  }

  @Test
  @Order(14)
  public void testRemoveEmployeeFromDepartment() {
    Employee newEmployee = new Employee(0, "Employee to Remove", new Date());
    newEmployee.setPosition("Temp");
    newEmployee.setSalary(40000);
    newEmployee.setPerformance(70);

    Employee addedEmployee = facade.addEmployeeToDepartment(1, newEmployee);
    assertNotNull(addedEmployee, "Added employee should not be null");

    boolean removed = facade.removeEmployeeFromDepartment(1, addedEmployee.getId());
    assertTrue(removed, "Employee should be removed successfully");

    Employee removedEmployee = facade.getEmployee(addedEmployee.getId());
    assertNull(removedEmployee, "Employee should be null after removal");
  }

  @Test
  @Order(15)
  public void testRemoveNonexistentEmployeeFromDepartment() {
    boolean removed = facade.removeEmployeeFromDepartment(1, -1);
    assertFalse(removed, "Removing a nonexistent employee should return false");
  }

  @Test
  @Order(16)
  public void testRemoveEmployeeFromNonexistentDepartment() {
    boolean removed = facade.removeEmployeeFromDepartment(-1, 1);
    assertFalse(removed, "Removing from a nonexistent department should return false");
  }

  @Test
  @Order(17)
  public void testUpdateEmployee() {
    Employee employee = facade.getEmployee(1);
    assertNotNull(employee, "Employee should not be null");

    String originalPosition = employee.getPosition();
    employee.setPosition("Updated Position");

    boolean updated = facade.updateEmployee(employee);
    assertTrue(updated, "Employee should be updated successfully");

    Employee updatedEmployee = facade.getEmployee(1);
    assertEquals("Updated Position", updatedEmployee.getPosition(), "Employee position should be updated");

    employee.setPosition(originalPosition);
    facade.updateEmployee(employee);
  }

  @Test
  @Order(18)
  public void testUpdateNonexistentEmployee() {
    Employee employee = new Employee(-1, "Nonexistent Employee", new Date());
    boolean updated = facade.updateEmployee(employee);
    assertFalse(updated, "Updating a nonexistent employee should return false");
  }

  @Test
  @Order(19)
  public void testUpdateDepartment() {
    Department department = facade.getDepartment(1);
    assertNotNull(department, "Department should not be null");

    String originalName = department.getName();
    department.setName("Updated Department Name");

    boolean updated = facade.updateDepartment(department);
    assertTrue(updated, "Department should be updated successfully");

    Department updatedDepartment = facade.getDepartment(1);
    assertEquals("Updated Department Name", updatedDepartment.getName(), "Department name should be updated");

    department.setName(originalName);
    facade.updateDepartment(department);
  }

  @Test
  @Order(20)
  public void testUpdateNonexistentDepartment() {
    Department department = new Department(-1, "Nonexistent Department", new ArrayList<>());
    boolean updated = facade.updateDepartment(department);
    assertFalse(updated, "Updating a nonexistent department should return false");
  }

  @Test
  @Order(21)
  public void testInsertDepartment() {
    Department newDepartment = new Department(0, "Research", new ArrayList<>());

    Department insertedDepartment = facade.insertDepartment(newDepartment);
    assertNull(insertedDepartment, "insertDepartment should return null as per InmemConnection implementation");
  }

  @Test
  @Order(22)
  public void testRemoveDepartment() {
    boolean removed = facade.removeDepartment(1);
    assertFalse(removed, "removeDepartment should return false as per InmemConnection implementation");
  }

  @Test
  @Order(23)
  public void testUpdateOrganization() {
    Organization organization = facade.getOrganization();
    organization.setName("Updated Org Name");

    boolean updated = facade.updateOrganization(organization);
    assertFalse(updated, "updateOrganization should return false as per InmemConnection implementation");
  }

  @Test
  @Order(24)
  public void testInsertOrganization() {
    Organization newOrg = new Organization(0, "New Organization");

    Organization insertedOrg = HrDatabaseFacade.insertOrganization(newOrg);
    assertNotNull(insertedOrg, "Inserted organization should not be null");
    assertTrue(insertedOrg.getId() > 0, "Inserted organization ID should be positive");

    HrDatabaseFacade newFacade = HrDatabaseFacade.getInstance(insertedOrg.getId());
    assertNotNull(newFacade, "HrDatabaseFacade instance should be created for the new organization");
  }

  @Test
  @Order(25)
  public void testRemoveOrganization() {
    boolean removed = HrDatabaseFacade.removeOrganization(testOrganizationId);
    assertFalse(removed, "removeOrganization should return false as per InmemConnection implementation");
  }
}
