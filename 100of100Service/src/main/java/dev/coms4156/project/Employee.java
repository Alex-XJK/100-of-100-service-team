package dev.coms4156.project;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This class represents an employee in the organization.
 * Designed under the Composite Design Pattern.
 */
public class Employee implements OrganizationComponent {
  private final int id;
  private final String name;
  private final Date hireDate;

  /**
   * Constructs an employee with the given ID, name, and hire date.
   *
   * @param id the ID of the employee (external ID)
   * @param name the name of the employee
   * @param hireDate the hire date of the employee
   */
  public Employee(int id, String name, Date hireDate) {
    this.id = id;
    this.name = name;
    if (hireDate == null) {
      this.hireDate = new Date();
    } else {
      this.hireDate = new Date(hireDate.getTime());
    }
  }

  /**
   * Returns the ID of the employee.
   *
   * @return the ID of the employee (external ID)
   */
  @Override
  public int getId() {
    return this.id;
  }

  /**
   * Returns the name of the employee.
   *
   * @return the name of the employee
   */
  @Override
  public String getName() {
    return this.name;
  }

  /**
   * Returns the type name of the employee.
   *
   * @return the type name of the employee
   */
  @Override
  public String getTypeName() {
    return "Employee";
  }

  /**
   * Returns the child structure of the employee.
   *
   * @return an empty list
   */
  @Override
  public List<OrganizationComponent> getChildren() {
    return new ArrayList<>();
  }

  /**
   * Returns the hire date of the employee.
   *
   * @return the hire date of the employee
   */
  public Date getHireDate() {
    return new Date(this.hireDate.getTime());
  }

  /**
   * Returns the basic information of the employee, including the name and ID.
   *
   * @return the string representation of the employee
   */
  @Override
  public String toString() {
    return "Employee: " + this.name + " (ID: " + this.id + ")" + " Hired at: " + this.hireDate;
  }
}
