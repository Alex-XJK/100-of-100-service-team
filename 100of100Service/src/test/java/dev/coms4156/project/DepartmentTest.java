package dev.coms4156.project;

import java.util.Date;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * A unit test class for the Department class.
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DepartmentTest {
  private static Department department;
  private static Employee employee1;
  private static Employee employee2;

  /**
   * Set up the test environment.
   */
  @BeforeAll
  public static void setUp() {
    employee1 = new Employee(1, "John", new Date());
    employee1.setPosition("ProductManager");
    employee1.setSalary(100);
    employee1.setPerformance(80);
    employee2 = new Employee(2, "Jake", new Date());
    employee2.setPosition("SoftwareEngineer");
    employee2.setSalary(50);
    employee2.setPerformance(90);
  }

  @Test
  @Order(1)
  public void testCreateDepartment() {
    department = new Department(1, "Teaching");
    Assertions.assertNotNull(department);
  }

  @Test
  @Order(2)
  public void testAddEmployee() {
    boolean result1 = department.addEmployee(employee1);
    Assertions.assertTrue(result1);
    Assertions.assertEquals(1, department.getEmployees().size());
    boolean result2 = department.addEmployee(employee2);
    Assertions.assertTrue(result2);
    Assertions.assertEquals(2, department.getEmployees().size());
  }

  @Test
  @Order(3)
  public void testRemoveEmployee() {
    boolean result = department.removeEmployee(employee1);
    Assertions.assertTrue(result);
    Assertions.assertEquals(1, department.getEmployees().size());
  }

  @Test
  @Order(4)
  public void testRemoveEmployeeNotInDepartment() {
    boolean result = department.removeEmployee(employee1);
    Assertions.assertFalse(result);
    Assertions.assertEquals(1, department.getEmployees().size());
  }

  @Test
  @Order(5)
  public void testRemoveEmployeeFromEmptyDepartment() {
    Department emptyDepartment = new Department(2, "Empty");
    boolean result = emptyDepartment.removeEmployee(employee2);
    Assertions.assertFalse(result);
    Assertions.assertEquals(0, emptyDepartment.getEmployees().size());
  }

  @Test
  @Order(6)
  public void testSetHead() {
    boolean result = department.setHead(employee2);
    Assertions.assertTrue(result);
    Assertions.assertEquals(employee2, department.getHead());
  }

  @Test
  @Order(7)
  public void testSetHeadToNull() {
    boolean result = department.setHead(null);
    Assertions.assertTrue(result);
    Assertions.assertNull(department.getHead());
  }

  @Test
  @Order(8)
  public void testToString1() {
    String expected = "Department: Teaching (ID: 1)\n  Employees:\n    - Jake (ID: 2)";
    Assertions.assertEquals(expected, department.toString());
  }

  @Test
  @Order(9)
  public void testToString2() {
    Department emptyDepartment = new Department(3, "Empty");
    String expected = "Department: Empty (ID: 3)\n  No employees in this department.";
    Assertions.assertEquals(expected, emptyDepartment.toString());
  }

  @Test
  @Order(10)
  public void testToString3() {
    department.setHead(employee2);
    String expected = "Department: Teaching (ID: 1) Head: Jake\n  Employees:\n    - Jake (ID: 2)";
    Assertions.assertEquals(expected, department.toString());
  }

  @Test
  @Order(11)
  public void testGetEmployeePositionStatistic() {
    Map<String, Integer> ac = department.getEmployeePositionStatisticMap();
    System.out.println(ac);
    Assertions.assertTrue(ac.containsKey(employee2.getPosition().trim().toLowerCase()));
  }

  @Test
  @Order(12)
  public void testGetEmployeePositionStatisticEmpty() {
    Department emptyDepartment = new Department(4, "Empty");
    Map<String, Integer> ac = emptyDepartment.getEmployeePositionStatisticMap();
    Assertions.assertTrue(ac.isEmpty());
  }

  @Test
  @Order(13)
  public void testGetEmployeeSalaryStatistic() {
    Map<String, Object> ac = department.getEmployeeSalaryStatisticMap();
    System.out.println(ac);
    Assertions.assertTrue(ac.containsKey("total"));
    Assertions.assertEquals(50.0, ac.get("total"));
    Assertions.assertTrue(ac.containsKey("average"));
    Assertions.assertTrue(ac.containsKey("highest"));
    Assertions.assertTrue(ac.containsKey("lowest"));
    Assertions.assertTrue(ac.containsKey("highestEmployee"));
    Assertions.assertEquals(employee2.getId(), ac.get("highestEmployee"));
    Assertions.assertTrue(ac.containsKey("lowestEmployee"));
  }

  @Test
  @Order(14)
  public void testGetEmployeePerformanceStatistic() {
    Employee e1 = new Employee(1, "A", new Date(), "DataScientist", 10.5, 100);
    Employee e2 = new Employee(2, "B", new Date(), "DataScientist", 20.5, 90);
    Employee e3 = new Employee(3, "C", new Date(), "DataScientist", 30.5, 80);
    Employee e4 = new Employee(4, "D", new Date(), "DataScientist", 40.5, 70);
    Employee e5 = new Employee(5, "E", new Date());
    Department d1 = new Department(1, "D1", List.of(e1, e2, e3, e4, e5));

    Map<String, Object> ac = d1.getEmployeePerformanceStatisticMap();
    System.out.println(ac);
    Assertions.assertTrue(ac.containsKey("average"));
    Assertions.assertTrue(ac.containsKey("highest"));
    Assertions.assertEquals(100.0, ac.get("highest"));
    Assertions.assertTrue(ac.containsKey("percentile25"));
    Assertions.assertTrue(ac.containsKey("median"));
    Assertions.assertTrue(ac.containsKey("percentile75"));
    Assertions.assertTrue(ac.containsKey("lowest"));
    Assertions.assertTrue(ac.containsKey("sortedEmployeeIds"));
    Assertions.assertArrayEquals(new int[]{1, 2, 3, 4, 5}, (int[]) ac.get("sortedEmployeeIds"));
  }

}
