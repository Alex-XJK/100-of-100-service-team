package dev.coms4156.project;

import java.util.ArrayList;
import java.util.List;

/**
 * An organization in the HR system.
 * An organization is a composite of employees and departments.
 */
public class Organization extends OrganizationComposite {
  private final List<Employee> employees;
  private final List<Department> departments;

  /**
   * Constructs an organization with the given ID and name.
   *
   * @param id the ID of the organization
   * @param name the name of the organization
   */
  public Organization(int id, String name) {
    super(id, name);
    this.typeName = "Organization";
    this.employees = new ArrayList<>();
    this.departments = new ArrayList<>();
  }

  /**
   * Onboarding an employee to the organization.
   *
   * @param employee the employee to be onboarded
   * @return true if the employee is onboarded, false otherwise
   */
  public boolean addEmployee(Employee employee) {
    this.employees.add(employee);
    return this.add(employee);
  }

  /**
   * Offboarding an employee from the organization.
   *
   * @param employee the employee to be offboarded
   * @return true if the employee is offboarded, false otherwise
   */
  public boolean removeEmployee(Employee employee) {
    return this.employees.remove(employee);
  }

  /**
   * Add a new department from the organization.
   *
   * @param department department to be added to this organization
   * @return true if the department is added, false otherwise
   */
  public boolean addDepartment(Department department) {
    this.departments.add(department);
    return this.add(department);
  }

  /**
   * Returns the number of employees in the organization.
   *
   * @return the number of employees in the organization
   */
  public int getNumEmployees() {
    return this.employees.size();
  }

  /**
   * Return the string representation of the organization.
   *
   * @return the string representation of the organization
   */
  @Override
  public String toString() {
    return "Organization: " + this.name + " (ID: " + this.id + ")";
  }

  /**
   * Display the hierarchical structure of the organization.
   * Class Static Method
   *
   * @return a string representation of the hierarchical structure of the organization
   */
  public static String displayStructure(OrganizationComponent component, int depth) {
    // Tested - add clarification to Dept or Employee
    StringBuilder sb = new StringBuilder();
    sb.append(" ".repeat(depth * 2)).append("- ").append(component.getTypeName())
        .append(": ").append(component.getName()).append("\n");
    for (OrganizationComponent child : component.getChildren()) {
      sb.append(displayStructure(child, depth + 1));
    }
    return sb.toString();
  }
}
