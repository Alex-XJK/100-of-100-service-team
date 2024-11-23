package dev.coms4156.project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents a department in the organization.
 * Designed under the Composite Design Pattern.
 */
public class Department extends OrganizationComposite {
  private Employee head;
  private final List<Employee> employees;

  /**
   * Constructs a department with the given ID and name.
   *
   * @param id the ID of the department (external ID)
   * @param name the name of the department
   */
  public Department(int id, String name) {
    super(id, name);
    this.typeName = "Department";
    this.employees = new ArrayList<>();
    this.head = null;
  }

  /**
   * Constructs a department with the given ID, name, and list of employees.
   *
   * @param id   the ID of the department (external ID)
   * @param name the name of the department
   * @param employees the list of existing employees in the department
   */
  public Department(int id, String name, List<Employee> employees) {
    super(id, name);
    this.typeName = "Department";
    this.employees = employees;
    this.head = null;
  }

  /**
   * Adds an employee to the department.
   *
   * @param employee the employee to be added
   * @return true if the employee is added successfully, false otherwise
   */
  public boolean addEmployee(Employee employee) {
    this.employees.add(employee); // Add to employee list
    return this.add(employee);
  }

  /**
   * Removes an employee from the department.
   *
   * @param employee the employee to be removed
   * @return true if the employee is removed successfully, false otherwise
   */
  public boolean removeEmployee(Employee employee) {
    this.employees.remove(employee); // Remove from employee list
    return this.remove(employee);
  }

  /**
   * Returns the list of employees in the department.
   *
   * @return the list of employees
   */
  public List<Employee> getEmployees() {
    // We choose to return the list itself, not a copy, for performance reasons
    // caller can make the decision to copy the list if needed
    return this.employees;
  }

  /**
   * Returns the head of the department.
   *
   * @return the head of the department
   */
  public Employee getHead() {
    return this.head;
  }

  /**
   * Sets the head of the department.
   *
   * @param head the head of the department
   * @return true if the validation passes, false otherwise
   */
  public boolean setHead(Employee head) {
    // TODO: Any validation? Manager must in the department?
    this.head = head;
    return true;
  }

  /**
   * Returns a statistic of the employees' positions in the department.
   *
   * @return a Map of the statistic that can be easily converted to JSON
   */
  public Map<String, Integer> getEmployeePositionStatisticMap() {
    Map<String, Integer> result = new HashMap<>();
    for (Employee e : this.employees) {
      String position = e.getPosition();
      position = (position == null || position.trim().isEmpty())
          ? "unassigned" : position.trim().toLowerCase();
      result.put(position, result.getOrDefault(position, 0) + 1);
    }
    return result;
  }

  /**
   * Returns a statistic of the employees' salaries in the department.
   *
   * @return a Map of the statistic that can be easily converted to JSON
   */
  public Map<String, Object> getEmployeeSalaryStatisticMap() {
    if (this.employees.isEmpty()) {
      Map<String, Object> emptyResult = new HashMap<>();
      emptyResult.put("total", 0.0);
      emptyResult.put("average", 0.0);
      emptyResult.put("highest", 0.0);
      emptyResult.put("lowest", 0.0);
      emptyResult.put("highestEmployee", null);
      emptyResult.put("lowestEmployee", null);
      return emptyResult;
    }

    double totalSalary = 0.0;
    double highestSalary = Double.MIN_VALUE;
    double lowestSalary = Double.MAX_VALUE;
    Employee highestEmployee = null;
    Employee lowestEmployee = null;

    for (Employee e : this.employees) {
      double salary = e.getSalary();

      totalSalary += salary;

      if (salary > highestSalary) {
        highestSalary = salary;
        highestEmployee = e;
      }

      if (salary < lowestSalary) {
        lowestSalary = salary;
        lowestEmployee = e;
      }
    }

    double averageSalary = totalSalary / this.employees.size();

    Map<String, Object> result = new HashMap<>();
    result.put("total", totalSalary);
    result.put("average", averageSalary);
    result.put("highest", highestSalary);
    result.put("lowest", lowestSalary);
    result.put("highestEmployee", highestEmployee != null ? highestEmployee.getId() : null);
    result.put("lowestEmployee", lowestEmployee != null ? lowestEmployee.getId() : null);
    return result;
  }

  /**
   * Returns a statistic of the employees' performance in the department.
   *
   * @return a Map of the statistic that can be easily converted to JSON
   */
  public Map<String, Object> getEmployeePerformanceStatisticMap() {
    Map<String, Object> result = new HashMap<>();

    if (this.employees.isEmpty()) {
      result.put("highest", 0.0);
      result.put("percentile25", 0.0);
      result.put("median", 0.0);
      result.put("percentile75", 0.0);
      result.put("lowest", 0.0);
      result.put("average", 0.0);
      result.put("sortedEmployeeIds", new int[0]);
      return result;
    }

    List<Double> performances = new ArrayList<>();
    for (Employee e : this.employees) {
      performances.add(e.getPerformance());
    }
    performances.sort(Double::compareTo);

    int size = performances.size();
    double highest = performances.get(size - 1);
    double lowest = performances.get(0);
    double median = size % 2 == 0
            ? (performances.get(size / 2 - 1) + performances.get(size / 2)) / 2
            : performances.get(size / 2);

    // Calculate quartiles safely
    double q1 = size < 4 ? lowest : performances.get(Math.max(0, (size - 1) / 4));
    double q3 = size < 4 ? highest : performances.get(Math.min(size - 1, (size - 1) * 3 / 4));

    // Calculate average
    double average = performances.stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0);

    result.put("highest", highest);
    result.put("percentile25", q1);
    result.put("median", median);
    result.put("percentile75", q3);
    result.put("lowest", lowest);
    result.put("average", average);

    // Sort employees by performance (descending) and get their IDs
    int[] sortedIds = this.employees.stream()
            .sorted((e1, e2) -> Double.compare(e2.getPerformance(), e1.getPerformance()))
            .mapToInt(Employee::getId)
            .toArray();
    result.put("sortedEmployeeIds", sortedIds);

    return result;
  }

  /**
   * Report all the information of the department in a JSON format.
   *
   * @return a Map of the information that can be easily converted to JSON
   */
  public Map<String, Object> toJson() {
    Map<String, Object> result = new HashMap<>();
    result.put("id", this.id);
    result.put("name", this.name);
    result.put("head", this.head != null ? this.head.getName() : "");
    result.put("headId", this.head != null ? this.head.getId() : "");
    result.put("employeeCount", this.employees.size());

    // Add detailed employee information if needed
    List<Map<String, Object>> employeesList = this.employees.stream().map(emp -> {
      Map<String, Object> empInfo = new HashMap<>();
      empInfo.put("id", emp.getId());
      empInfo.put("name", emp.getName());
      empInfo.put("position", emp.getPosition());
      empInfo.put("performance", emp.getPerformance());
      empInfo.put("salary", emp.getSalary());
      return empInfo;
    }).toList();
    result.put("employees", employeesList);

    return result;
  }

  /**
   * Returns the basic information of the Department,
   * including the name, ID, list of employees, and head.
   *
   * @return the string representation of the department
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    // add employee infos in the toString()
    sb.append("Department: ").append(this.name).append(" (ID: ").append(this.id).append(")");
    if (this.head != null) {
      sb.append(" Head: ").append(this.head.getName());
    }

    if (!this.employees.isEmpty()) {
      sb.append("\n  Employees:");  // Indent employees section
      for (Employee employee : this.employees) {
        sb.append("\n    - ").append(employee.getName())
          .append(" (ID: ").append(employee.getId()).append(")");
      }
    } else {
      sb.append("\n  No employees in this department.");
    }

    return sb.toString();
  }
}
