package dev.coms4156.project;

import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OrganizationTest {

  private static Organization organization;
  private static Employee employee1;
  private static Employee employee2;
  private static Department department1;
  private static Department departmentWithNullName;
  private static Department departmentWithNullEmployees;

  @BeforeAll
  public static void setUp() {
    organization = new Organization(1, "Test Organization");
    employee1 = new Employee(1, "Alice", new Date());
    employee2 = new Employee(2, "Bob", new Date());
    department1 = new Department(1, "HR");
  }

  @Test
  @Order(1)
  public void testAddEmployee() {
    boolean added = organization.addEmployee(employee1);
    Assertions.assertTrue(added, "Employee should be added successfully");
    int numEmployees = organization.getNumEmployees();
    Assertions.assertEquals(1, numEmployees, "Organization should have 1 employee");
  }

  @Test
  @Order(2)
  public void testRemoveEmployee_Present() {
    // Employee1 is in the organization
    boolean removed = organization.removeEmployee(employee1);
    Assertions.assertTrue(removed, "Employee should be removed successfully");
    int numEmployees = organization.getNumEmployees();
    Assertions.assertEquals(0, numEmployees, "Organization should have 0 employees");
  }

  @Test
  @Order(3)
  public void testRemoveEmployee_NotPresent() {
    // Employee1 has already been removed
    boolean removed = organization.removeEmployee(employee1);
    Assertions.assertFalse(removed, "Removing a non-existent employee should return false");
    int numEmployees = organization.getNumEmployees();
    Assertions.assertEquals(0, numEmployees, "Organization should still have 0 employees");
  }

  @Test
  @Order(4)
  public void testGetNumEmployees_Empty() {
    int numEmployees = organization.getNumEmployees();
    Assertions.assertEquals(0, numEmployees, "Organization should have 0 employees");
  }

  @Test
  @Order(5)
  public void testGetNumEmployees_NonEmpty() {
    organization.addEmployee(employee2);
    int numEmployees = organization.getNumEmployees();
    Assertions.assertEquals(1, numEmployees, "Organization should have 1 employee");
  }

  @Test
  @Order(6)
  public void testToString() {
    String expected = "Organization: Test Organization (ID: 1)";
    Assertions.assertEquals(expected, organization.toString(), "toString() should return the correct string");
  }

  @Test
  @Order(7)
  public void testToJson_DepartmentWithNullName() {
    // Create a department with null name
    departmentWithNullName = new Department(2, null);
    organization.addDepartment(departmentWithNullName);

    Map<String, Object> orgJson = organization.toJson();
    Assertions.assertNotNull(orgJson.get("departments"), "Departments list should not be null");

    List<Map<String, Object>> departments = (List<Map<String, Object>>) orgJson.get("departments");

    boolean foundNullNameDept = false;
    for (Map<String, Object> deptInfo : departments) {
      if (deptInfo.get("id").equals(2)) {
        foundNullNameDept = true;
        Assertions.assertEquals("", deptInfo.get("name"), "Department name should be empty string");
      }
    }
    Assertions.assertTrue(foundNullNameDept, "Department with null name should be present");
  }

  @Test
  @Order(8)
  public void testToJson_DepartmentWithNullEmployees() throws Exception {
    // Create a department with null employees list
    departmentWithNullEmployees = new DepartmentWithNullEmployees(3, "IT");
    organization.addDepartment(departmentWithNullEmployees);

    Map<String, Object> orgJson = organization.toJson();
    Assertions.assertNotNull(orgJson.get("departments"), "Departments list should not be null");

    List<Map<String, Object>> departments = (List<Map<String, Object>>) orgJson.get("departments");

    boolean foundNullEmployeesDept = false;
    for (Map<String, Object> deptInfo : departments) {
      if (deptInfo.get("id").equals(3)) {
        foundNullEmployeesDept = true;
        Assertions.assertEquals(0, deptInfo.get("employeeCount"), "Employee count should be 0 when employees list is null");
      }
    }
    Assertions.assertTrue(foundNullEmployeesDept, "Department with null employees should be present");
  }

  // Helper class to simulate a Department with null employees list
  private static class DepartmentWithNullEmployees extends Department {
    public DepartmentWithNullEmployees(int id, String name) {
      super(id, name);
    }

    @Override
    public List<Employee> getEmployees() {
      return null;
    }
  }
}
