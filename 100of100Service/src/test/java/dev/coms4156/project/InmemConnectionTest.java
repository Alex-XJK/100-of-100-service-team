package dev.coms4156.project;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * A unit test class for the InmemConnection class.
 */
public class InmemConnectionTest {

    private InmemConnection inmemConnection;
    private int testOrganizationId = 1;

    /**
     * Sets up the test environment by initializing the InmemConnection instance.
     */
    @BeforeEach
    public void setup() {
        inmemConnection = InmemConnection.getInstance();
        inmemConnection.resetTestData(); // Ensure a clean state before each test
    }

    @Test
    public void testGetOrganization() {
        Organization org = inmemConnection.getOrganization(testOrganizationId);
        assertNotNull(org, "Organization should not be null");
        System.out.println("Successfully retrieved organization: " + org.getName());
    }

    @Test
    public void testGetNonexistentOrganization() {
        Organization org = inmemConnection.getOrganization(-1);
        assertNull(org, "Organization should be null for nonexistent ID");
    }

    @Test
    public void testGetEmployees() {
        List<Employee> employees = inmemConnection.getEmployees(testOrganizationId);
        assertNotNull(employees, "Employees list should not be null");
        assertFalse(employees.isEmpty(), "Employees list should not be empty");
        System.out.println("Retrieved " + employees.size() + " employees");
    }

    @Test
    public void testGetEmployeesForNonexistentOrganization() {
        List<Employee> employees = inmemConnection.getEmployees(-1);
        assertNotNull(employees, "Employees list should not be null");
        assertTrue(employees.isEmpty(), "Employees list should be empty for nonexistent organization");
    }

    @Test
    public void testGetEmployee() {
        Employee employee = inmemConnection.getEmployee(testOrganizationId, 1);
        assertNotNull(employee, "Employee should not be null");
        System.out.println("Retrieved employee: " + employee.getName());
    }

    @Test
    public void testGetNonexistentEmployee() {
        Employee employee = inmemConnection.getEmployee(testOrganizationId, -1);
        assertNull(employee, "Employee should be null for nonexistent ID");
    }

    @Test
    public void testGetDepartments() {
        List<Department> departments = inmemConnection.getDepartments(testOrganizationId);
        assertNotNull(departments, "Departments list should not be null");
        assertFalse(departments.isEmpty(), "Departments list should not be empty");
        System.out.println("Retrieved " + departments.size() + " departments");
    }

    @Test
    public void testGetDepartmentsForNonexistentOrganization() {
        List<Department> departments = inmemConnection.getDepartments(-1);
        assertNotNull(departments, "Departments list should not be null");
        assertTrue(departments.isEmpty(), "Departments list should be empty for nonexistent organization");
    }

    @Test
    public void testGetDepartment() {
        Department department = inmemConnection.getDepartment(testOrganizationId, 1);
        assertNotNull(department, "Department should not be null");
        System.out.println("Retrieved department: " + department.getName());
    }

    @Test
    public void testGetNonexistentDepartment() {
        Department department = inmemConnection.getDepartment(testOrganizationId, -1);
        assertNull(department, "Department should be null for nonexistent ID");
    }

//    @Test
//    public void testAddEmployeeToDepartment() {
//        Employee newEmployee = new Employee(0, "Test Employee", new Date());
//        newEmployee.setPosition("Tester");
//        newEmployee.setSalary(50000);
//        newEmployee.setPerformance(80);
//
//        int departmentId = 1; // Department ID 1 exists in test data
//
//        int newEmployeeId = inmemConnection.addEmployeeToDepartment(testOrganizationId, departmentId, newEmployee);
//        assertTrue(newEmployeeId > 0, "New employee ID should be positive");
//
//        // Verify the employee was added
//        Employee addedEmployee = inmemConnection.getEmployee(testOrganizationId, newEmployeeId);
//        assertNotNull(addedEmployee, "Added employee should not be null");
//        assertEquals("Test Employee", addedEmployee.getName(), "Employee name should match");
//
//        // Verify the employee is in the department
//        Department department = inmemConnection.getDepartment(testOrganizationId, departmentId);
//        assertTrue(department.getEmployees().contains(addedEmployee), "Department should contain the new employee");
//    }

    @Test
    public void testAddEmployeeToNonexistentDepartment() {
        Employee newEmployee = new Employee(0, "Test Employee", new Date());
        int nonExistentDeptId = -1;

        int newEmployeeId = inmemConnection.addEmployeeToDepartment(testOrganizationId, nonExistentDeptId, newEmployee);
        assertEquals(-1, newEmployeeId, "Should return -1 when adding to a nonexistent department");
    }

    @Test
    public void testAddEmployeeToDepartmentWithNullEmployees() {
        // Remove the departments to simulate null employees list
        inmemConnection.getTestDepartments().remove(testOrganizationId);

        Employee newEmployee = new Employee(0, "Test Employee", new Date());
        int departmentId = 1;

        int newEmployeeId = inmemConnection.addEmployeeToDepartment(testOrganizationId, departmentId, newEmployee);
        assertEquals(-1, newEmployeeId, "Should return -1 when departments list is null");
    }

    @Test
    public void testRemoveEmployeeFromDepartment() {
        // Add an employee to remove
        Employee newEmployee = new Employee(0, "Employee to Remove", new Date());
        newEmployee.setPosition("Temp");
        newEmployee.setSalary(40000);
        newEmployee.setPerformance(70);

        int departmentId = 1;

        int newEmployeeId = inmemConnection.addEmployeeToDepartment(testOrganizationId, departmentId, newEmployee);
        assertTrue(newEmployeeId > 0, "New employee ID should be positive");

        // Now, remove the employee
        boolean removed = inmemConnection.removeEmployeeFromDepartment(testOrganizationId, departmentId, newEmployeeId);
        assertTrue(removed, "Employee should be removed successfully");

        // Verify the employee was removed
        Employee removedEmployee = inmemConnection.getEmployee(testOrganizationId, newEmployeeId);
        assertNull(removedEmployee, "Employee should be null after removal");

        // Verify the employee is removed from the department
        Department department = inmemConnection.getDepartment(testOrganizationId, departmentId);
        assertFalse(department.getEmployees().stream().anyMatch(emp -> emp.getId() == newEmployeeId),
                "Employee should be removed from the department");
    }

    @Test
    public void testRemoveNonexistentEmployeeFromDepartment() {
        int departmentId = 1;
        int nonExistentEmployeeId = -1;

        boolean removed = inmemConnection.removeEmployeeFromDepartment(testOrganizationId, departmentId, nonExistentEmployeeId);
        assertFalse(removed, "Removing a nonexistent employee should return false");
    }

    @Test
    public void testRemoveEmployeeFromNonexistentDepartment() {
        int nonExistentDeptId = -1;
        int employeeId = 1;

        boolean removed = inmemConnection.removeEmployeeFromDepartment(testOrganizationId, nonExistentDeptId, employeeId);
        assertFalse(removed, "Removing from a nonexistent department should return false");
    }

    @Test
    public void testUpdateEmployee() {
        Employee employee = inmemConnection.getEmployee(testOrganizationId, 1);
        assertNotNull(employee, "Employee should not be null");

        // Update employee details
        String originalPosition = employee.getPosition();
        employee.setPosition("Updated Position");

        boolean updated = inmemConnection.updateEmployee(testOrganizationId, employee);
        assertTrue(updated, "Employee should be updated successfully");

        // Verify the update
        Employee updatedEmployee = inmemConnection.getEmployee(testOrganizationId, 1);
        assertEquals("Updated Position", updatedEmployee.getPosition(), "Employee position should be updated");

        // Restore original position
        employee.setPosition(originalPosition);
        inmemConnection.updateEmployee(testOrganizationId, employee);
    }

    @Test
    public void testUpdateNonexistentEmployee() {
        Employee employee = new Employee(-1, "Nonexistent Employee", new Date());
        boolean updated = inmemConnection.updateEmployee(testOrganizationId, employee);
        assertFalse(updated, "Updating a nonexistent employee should return false");
    }

    @Test
    public void testUpdateEmployeeWithNullEmployees() {
        // Remove the employees list to simulate null
        inmemConnection.getTestEmployees().remove(testOrganizationId);

        Employee employee = new Employee(1, "Test Employee", new Date());
        boolean updated = inmemConnection.updateEmployee(testOrganizationId, employee);
        assertFalse(updated, "Updating when employees list is null should return false");
    }

    @Test
    public void testUpdateDepartment() {
        Department department = inmemConnection.getDepartment(testOrganizationId, 1);
        assertNotNull(department, "Department should not be null");

        // Update department name
        String originalName = department.getName();
        department.setName("Updated Department Name");

        boolean updated = inmemConnection.updateDepartment(testOrganizationId, department);
        assertTrue(updated, "Department should be updated successfully");

        // Verify the update
        Department updatedDepartment = inmemConnection.getDepartment(testOrganizationId, 1);
        assertEquals("Updated Department Name", updatedDepartment.getName(), "Department name should be updated");

        // Restore original name
        department.setName(originalName);
        inmemConnection.updateDepartment(testOrganizationId, department);
    }

    @Test
    public void testUpdateNonexistentDepartment() {
        Department department = new Department(-1, "Nonexistent Department", new ArrayList<>());
        boolean updated = inmemConnection.updateDepartment(testOrganizationId, department);
        assertFalse(updated, "Updating a nonexistent department should return false");
    }

    @Test
    public void testUpdateDepartmentWithInvalidHead() {
        Department department = inmemConnection.getDepartment(testOrganizationId, 1);
        assertNotNull(department, "Department should not be null");

        // Set an invalid head (nonexistent employee)
        Employee invalidHead = new Employee(-1, "Invalid Head", new Date());
        department.setHead(invalidHead);

        boolean updated = inmemConnection.updateDepartment(testOrganizationId, department);
        assertFalse(updated, "Updating department with invalid head should return false");
    }

    @Test
    public void testUpdateDepartmentWithNullDepartments() {
        // Remove the departments list to simulate null
        inmemConnection.getTestDepartments().remove(testOrganizationId);

        Department department = new Department(1, "Test Department", new ArrayList<>());
        boolean updated = inmemConnection.updateDepartment(testOrganizationId, department);
        assertFalse(updated, "Updating when departments list is null should return false");
    }

    @Test
    public void testInsertOrganization() {
        Organization newOrg = new Organization(0, "New Organization");
        Organization insertedOrg = inmemConnection.insertOrganization(newOrg);
        assertNotNull(insertedOrg, "Inserted organization should not be null");
        assertTrue(insertedOrg.getId() > 0, "Inserted organization ID should be positive");

        // Verify the insertion
        Organization retrievedOrg = inmemConnection.getOrganization(insertedOrg.getId());
        assertNotNull(retrievedOrg, "Retrieved organization should not be null");
        assertEquals("New Organization", retrievedOrg.getName(), "Organization name should match");
    }

    @Test
    public void testUpdateOrganization() {
        // Since updateOrganization returns false, test this behavior
        Organization organization = inmemConnection.getOrganization(testOrganizationId);
        assertNotNull(organization, "Organization should not be null");

        boolean updated = inmemConnection.updateOrganization(organization);
        assertFalse(updated, "updateOrganization should return false as per implementation");
    }

    @Test
    public void testRemoveOrganization() {
        // Since removeOrganization returns false, test this behavior
        boolean removed = inmemConnection.removeOrganization(testOrganizationId);
        assertFalse(removed, "removeOrganization should return false as per implementation");
    }

    @Test
    public void testInsertDepartment() {
        // Since insertDepartment returns null, test this behavior
        Department newDepartment = new Department(0, "New Department", new ArrayList<>());
        Department insertedDepartment = inmemConnection.insertDepartment(testOrganizationId, newDepartment);
        assertNull(insertedDepartment, "insertDepartment should return null as per implementation");
    }

    @Test
    public void testRemoveDepartment() {
        // Since removeDepartment returns false, test this behavior
        boolean removed = inmemConnection.removeDepartment(testOrganizationId, 1);
        assertFalse(removed, "removeDepartment should return false as per implementation");
    }

    @Test
    public void testResetTestData() {
        // Modify test data
        Organization org = inmemConnection.getOrganization(testOrganizationId);
        org.setName("Modified Name");

        // Reset test data
        inmemConnection.resetTestData();

        // Verify that data is reset
        Organization resetOrg = inmemConnection.getOrganization(testOrganizationId);
        assertNotNull(resetOrg, "Organization should not be null after reset");
        assertNotEquals("Modified Name", resetOrg.getName(), "Organization name should be reset");
    }

    @Test
    public void testSingletonInstance() {
        InmemConnection instance1 = InmemConnection.getInstance();
        InmemConnection instance2 = InmemConnection.getInstance();
        assertSame(instance1, instance2, "Instances should be the same (singleton pattern)");
    }

    @Test
    public void testConnectionName() {
        String name = inmemConnection.connectionName();
        assertNotNull(name, "Connection name should not be null");
        System.out.println("Connection name: " + name);
    }
}
