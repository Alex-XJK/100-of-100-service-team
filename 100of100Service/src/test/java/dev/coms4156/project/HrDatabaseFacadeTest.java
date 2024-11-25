package dev.coms4156.project;

import dev.coms4156.project.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Date;

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
    // Initialize the database connection (InmemConnection)
    dbConnection = InmemConnection.getInstance();
    HrDatabaseFacade.setConnection(dbConnection);
    facade = HrDatabaseFacade.getInstance(testOrganizationId);
  }

  @Test
  public void testGetOrganization() {
    Organization org = facade.getOrganization();
    assertNotNull(org, "Organization should not be null");
    System.out.println("Successfully retrieved: " + org.getName());
    System.out.println(Organization.displayStructure(org, 0));
  }

  @Test
  public void testGetNonexistentOrganization() {
    Exception exception = assertThrows(NotFoundException.class, () -> {
      HrDatabaseFacade.getInstance(-1);
    });
    assertEquals("Organization not found", exception.getMessage());
  }

  @Test
  public void testGetEmployee() {
    Employee employee = facade.getEmployee(1);
    assertNotNull(employee, "Employee should not be null");
    System.out.println("Retrieved employee: " + employee.getName());
  }

  @Test
  public void testGetNonexistentEmployee() {
    Employee employee = facade.getEmployee(-1);
    assertNull(employee, "Employee should be null for nonexistent ID");
  }

  @Test
  public void testGetDepartment() {
    Department department = facade.getDepartment(1);
    assertNotNull(department, "Department should not be null");
    System.out.println("Retrieved department: " + department.getName());
  }

  @Test
  public void testGetNonexistentDepartment() {
    Department department = facade.getDepartment(-1);
    assertNull(department, "Department should be null for nonexistent ID");
  }

  @Test
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
  public void testAddEmployeeToNonexistentDepartment() {
    Employee newEmployee = new Employee(0, "Test Employee", new Date());

    Employee addedEmployee = facade.addEmployeeToDepartment(-1, newEmployee);
    assertNull(addedEmployee, "Adding to a nonexistent department should return null");
  }

  @Test
  public void testRemoveEmployeeFromDepartment() {
    // Add an employee to remove
    Employee newEmployee = new Employee(0, "Employee to Remove", new Date());
    newEmployee.setPosition("Temp");
    newEmployee.setSalary(40000);
    newEmployee.setPerformance(70);

    Employee addedEmployee = facade.addEmployeeToDepartment(1, newEmployee);
    assertNotNull(addedEmployee, "Added employee should not be null");

    // Now, remove the employee
    boolean removed = facade.removeEmployeeFromDepartment(1, addedEmployee.getId());
    assertTrue(removed, "Employee should be removed successfully");

    // Verify the employee was removed
    Employee removedEmployee = facade.getEmployee(addedEmployee.getId());
    assertNull(removedEmployee, "Employee should be null after removal");

//    // Verify the employee is removed from the department
//    Department department = facade.getDepartment(1);
//    assertFalse(department.getEmployees().stream().anyMatch(emp -> emp.getId() == addedEmployee.getId()),
//            "Employee should be removed from the department");
  }

  @Test
  public void testRemoveNonexistentEmployeeFromDepartment() {
    boolean removed = facade.removeEmployeeFromDepartment(1, -1);
    assertFalse(removed, "Removing a nonexistent employee should return false");
  }

  @Test
  public void testRemoveEmployeeFromNonexistentDepartment() {
    boolean removed = facade.removeEmployeeFromDepartment(-1, 1);
    assertFalse(removed, "Removing from a nonexistent department should return false");
  }

  @Test
  public void testUpdateEmployee() {
    Employee employee = facade.getEmployee(1);
    assertNotNull(employee, "Employee should not be null");

    // Update employee details
    String originalPosition = employee.getPosition();
    employee.setPosition("Updated Position");

    boolean updated = facade.updateEmployee(employee);
    assertTrue(updated, "Employee should be updated successfully");

    // Verify the update
    Employee updatedEmployee = facade.getEmployee(1);
    assertEquals("Updated Position", updatedEmployee.getPosition(), "Employee position should be updated");

    // Restore original position
    employee.setPosition(originalPosition);
    facade.updateEmployee(employee);
  }

  @Test
  public void testUpdateNonexistentEmployee() {
    Employee employee = new Employee(-1, "Nonexistent Employee", new Date());
    boolean updated = facade.updateEmployee(employee);
    assertFalse(updated, "Updating a nonexistent employee should return false");
  }

  @Test
  public void testUpdateDepartment() {
    Department department = facade.getDepartment(1);
    assertNotNull(department, "Department should not be null");

    // Update department name
    String originalName = department.getName();
    department.setName("Updated Department Name");

    boolean updated = facade.updateDepartment(department);
    assertTrue(updated, "Department should be updated successfully");

    // Verify the update
    Department updatedDepartment = facade.getDepartment(1);
    assertEquals("Updated Department Name", updatedDepartment.getName(), "Department name should be updated");

    // Restore original name
    department.setName(originalName);
    facade.updateDepartment(department);
  }

  @Test
  public void testUpdateNonexistentDepartment() {
    Department department = new Department(-1, "Nonexistent Department", new ArrayList<>());
    boolean updated = facade.updateDepartment(department);
    assertFalse(updated, "Updating a nonexistent department should return false");
  }

  @Test
  public void testInsertDepartment() {
    Department newDepartment = new Department(0, "Research", new ArrayList<>());

    Department insertedDepartment = facade.insertDepartment(newDepartment);
    assertNull(insertedDepartment, "insertDepartment should return null as per InmemConnection implementation");
  }

  @Test
  public void testRemoveDepartment() {
    boolean removed = facade.removeDepartment(1);
    assertFalse(removed, "removeDepartment should return false as per InmemConnection implementation");
  }

  @Test
  public void testUpdateOrganization() {
    Organization organization = facade.getOrganization();
    organization.setName("Updated Org Name");

    boolean updated = facade.updateOrganization(organization);
    assertFalse(updated, "updateOrganization should return false as per InmemConnection implementation");
  }

  @Test
  public void testInsertOrganization() {
    Organization newOrg = new Organization(0, "New Organization");

    Organization insertedOrg = HrDatabaseFacade.insertOrganization(newOrg);
    assertNotNull(insertedOrg, "Inserted organization should not be null");
    assertTrue(insertedOrg.getId() > 0, "Inserted organization ID should be positive");

    // Verify that the instance is created
    HrDatabaseFacade newFacade = HrDatabaseFacade.getInstance(insertedOrg.getId());
    assertNotNull(newFacade, "HrDatabaseFacade instance should be created for the new organization");
  }

//  @Test
//  public void testInsertOrganization_dbConnectionNull() {
//    HrDatabaseFacade.setConnection(null);
//    Organization org = new Organization(0, "Test Organization");
//
//    Exception exception = assertThrows(IllegalStateException.class, () -> {
//      HrDatabaseFacade.insertOrganization(org);
//    });
//
//    assertEquals("Database connection is not initialized", exception.getMessage());
//  }

  @Test
  public void testRemoveOrganization() {
    boolean removed = HrDatabaseFacade.removeOrganization(testOrganizationId);
    assertFalse(removed, "removeOrganization should return false as per InmemConnection implementation");
  }

//  @Test
//  public void testRemoveOrganization_dbConnectionNull() {
//    HrDatabaseFacade.setConnection(null);
//
//    Exception exception = assertThrows(IllegalStateException.class, () -> {
//      HrDatabaseFacade.removeOrganization(testOrganizationId);
//    });
//
//    assertEquals("Database connection is not initialized", exception.getMessage());
//  }
//
//  @Test
//  public void testConstructor_dbConnectionNull() {
//    HrDatabaseFacade.setConnection(null);
//
//    Exception exception = assertThrows(IllegalStateException.class, () -> {
//      HrDatabaseFacade.getInstance(testOrganizationId);
//    });
//
//    assertEquals("Database connection is not initialized", exception.getMessage());
//  }

//  @Test
//  public void testConstructor_organizationNotFound() {
//    // Create a mock DatabaseConnection where getOrganization returns null
//    DatabaseConnection mockDbConnection = new InmemConnection() {
//      @Override
//      public Organization getOrganization(int organizationId) {
//        return null;
//      }
//
//      @Override
//      public List<Department> getDepartments(int organizationId) {
//        return new ArrayList<>();
//      }
//
//      @Override
//      public List<Employee> getEmployees(int organizationId) {
//        return new ArrayList<>();
//      }
//    };
//
//    HrDatabaseFacade.setConnection(mockDbConnection);
//
//    Exception exception = assertThrows(NotFoundException.class, () -> {
//      HrDatabaseFacade.getInstance(testOrganizationId);
//    });
//
//    assertEquals("Organization not found", exception.getMessage());
//  }

//  @Test
//  public void testGetEmployee_employeeNotInCache() {
//    // Remove employee from cache
//    facade.employees.clear();
//
//    Employee employee = facade.getEmployee(1);
//    assertNotNull(employee, "Employee should be fetched from database when not in cache");
//    assertEquals(1, employee.getId(), "Employee ID should match");
//  }

//  @Test
//  public void testGetDepartment_departmentNotInCache() {
//    // Remove department from cache
//    facade.departments.clear();
//
//    Department department = facade.getDepartment(1);
//    assertNotNull(department, "Department should be fetched from database when not in cache");
//    assertEquals(1, department.getId(), "Department ID should match");
//  }
}
