package dev.coms4156.project;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * A singleton class of database connection.
 * This class is responsible for creating and managing the connection to the database.
 * Designed under the Singleton Design Pattern.
 */
public class DatabaseConnection {
  private static volatile DatabaseConnection instance;
  private Connection connection;

  protected DatabaseConnection() {
    try {
      String url = "jdbc:mysql://database-100-team.c7mqy28ys9uq.us-east-1.rds.amazonaws.com:3306/"
              + "organization_management";
      String user = "admin";
      String password = "sxy6cJEmv6iLT61qs7DO";
      this.connection = DriverManager.getConnection(url, user, password);
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  /**
   * Returns an employee in a given organization by external ID.
   *
   * @param organizationId the organization id (clientId)
   * @param externalEmployeeId the external employee id
   * @return the employee if found, null otherwise
   */
  public Employee getEmployee(int organizationId, int externalEmployeeId) {
    int internalEmployeeId = organizationId * 10000 + externalEmployeeId;
    String query = "SELECT * FROM employees WHERE organization_id = ? AND employee_id = ?";
    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
      pstmt.setInt(1, organizationId);
      pstmt.setInt(2, internalEmployeeId);
      ResultSet rs = pstmt.executeQuery();
      if (rs.next()) {
        return new Employee(
                externalEmployeeId,
                rs.getString("name"),
                rs.getDate("hire_date") // Assuming this field exists
        );
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Returns a department in a given organization by external ID.
   *
   * @param organizationId the organization id (clientId)
   * @param externalDepartmentId the external department id
   * @return the department if found, null otherwise
   */
  public Department getDepartment(int organizationId, int externalDepartmentId) {
    int internalDepartmentId = organizationId * 10000 + externalDepartmentId;
    String query = "SELECT * FROM departments WHERE organization_id = ? AND department_id = ?";
    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
      pstmt.setInt(1, organizationId);
      pstmt.setInt(2, internalDepartmentId);
      ResultSet rs = pstmt.executeQuery();
      if (rs.next()) {
        List<Employee> employees = getEmployeesForDepartment(internalDepartmentId);
        return new Department(
                externalDepartmentId,
                rs.getString("name"),
                employees
        );
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Returns a list of employees in a given organization.
   *
   * @param organizationId the organization id
   * @return a list of employees in the organization
   */
  public List<Employee> getEmployees(int organizationId) {
    List<Employee> employees = new ArrayList<>();
    String query = "SELECT * FROM employees WHERE organization_id = ?";
    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
      pstmt.setInt(1, organizationId);
      ResultSet rs = pstmt.executeQuery();
      while (rs.next()) {
        int internalId = rs.getInt("employee_id");
        int externalId = internalId % 10000;
        Employee employee = new Employee(
                externalId,
                rs.getString("name"),
                rs.getDate("hire_date") // Assuming this field exists
        );
        employees.add(employee);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return employees;
  }

  /**
   * Returns a list of departments in a given organization.
   *
   * @param organizationId the organization id
   * @return a list of departments in the organization
   */
  public List<Department> getDepartments(int organizationId) {
    List<Department> departments = new ArrayList<>();
    String query = "SELECT * FROM departments WHERE organization_id = ?";
    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
      pstmt.setInt(1, organizationId);
      ResultSet rs = pstmt.executeQuery();
      while (rs.next()) {
        int internalId = rs.getInt("department_id");
        int externalId = internalId % 10000;
        List<Employee> employees = getEmployeesForDepartment(internalId);
        Department department = new Department(
                externalId,
                rs.getString("name"),
                employees
        );
        departments.add(department);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return departments;
  }

  /**
   * Returns an organization with the given organization id.
   *
   * @param organizationId the organization id
   * @return the organization with the given organization id
   */
  public Organization getOrganization(int organizationId) {
    String query = "SELECT * FROM organizations WHERE organization_id = ?";
    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
      pstmt.setInt(1, organizationId);
      ResultSet rs = pstmt.executeQuery();
      if (rs.next()) {
        return new Organization(
                rs.getInt("organization_id"),
                rs.getString("name")
        );
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Returns a list of employees in a given department.
   *
   * @param internalDepartmentId the internal department id
   * @return a list of employees in the department
   */
  private List<Employee> getEmployeesForDepartment(int internalDepartmentId) {
    List<Employee> employees = new ArrayList<>();
    String query = "SELECT * FROM employees WHERE department_id = ?";

    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
      pstmt.setInt(1, internalDepartmentId);
      ResultSet rs = pstmt.executeQuery();

      while (rs.next()) {
        int internalId = rs.getInt("employee_id");
        int externalId = internalId % 10000;
        Employee employee = new Employee(
                externalId,
                rs.getString("name"),
                rs.getDate("hire_date")
        );
        employees.add(employee);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return employees;
  }

  /**
   * Adds a new employee to the database.
   *
   * @param organizationId the organization ID
   * @param employee       the employee to add
   * @return true if the employee was added successfully, false otherwise
   */
  public boolean addEmployee(int organizationId, Employee employee) {
    int internalEmployeeId = organizationId * 10000 + employee.getId();
    Integer internalDepartmentId = null;
    if (employee.getDepartmentId() != null) {
      internalDepartmentId = organizationId * 10000 + employee.getDepartmentId();
    }

    String query = "INSERT INTO employees "
            + "(employee_id, organization_id, department_id, name, hire_date) "
            + "VALUES (?, ?, ?, ?, ?)";
    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
      pstmt.setInt(1, internalEmployeeId);
      pstmt.setInt(2, organizationId);

      if (internalDepartmentId != null) {
        pstmt.setInt(3, internalDepartmentId);
      } else {
        pstmt.setNull(3, Types.INTEGER);
      }

      pstmt.setString(4, employee.getName());
      if (employee.getHireDate() != null) {
        pstmt.setDate(5, new java.sql.Date(employee.getHireDate().getTime()));
      } else {
        pstmt.setNull(5, Types.DATE);
      }

      pstmt.executeUpdate();
      return true;
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return false;
  }

  /**
   * Updates the department information in the database.
   *
   * @param organizationId the organization ID
   * @param department     the department to update
   * @return true if the update was successful, false otherwise
   */
  public boolean updateDepartment(int organizationId, Department department) {
    int internalDepartmentId = organizationId * 10000 + department.getId();

    try {
      // Update department details
      String updateDeptQuery = "UPDATE departments SET name = ? WHERE organization_id = ? "
              + "AND department_id = ?";
      try (PreparedStatement pstmt = connection.prepareStatement(updateDeptQuery)) {
        pstmt.setString(1, department.getName());
        pstmt.setInt(2, organizationId);
        pstmt.setInt(3, internalDepartmentId);
        pstmt.executeUpdate();
      }

      // Clear existing employees from the department
      String clearEmployeesQuery = "UPDATE employees SET department_id = NULL "
              + "WHERE department_id = ?";
      try (PreparedStatement pstmt = connection.prepareStatement(clearEmployeesQuery)) {
        pstmt.setInt(1, internalDepartmentId);
        pstmt.executeUpdate();
      }

      // Assign employees to the department
      String assignEmployeeQuery = "UPDATE employees SET department_id = ? WHERE employee_id = ?";
      try (PreparedStatement pstmt = connection.prepareStatement(assignEmployeeQuery)) {
        for (Employee employee : department.getEmployees()) {
          int internalEmployeeId = organizationId * 10000 + employee.getId();
          pstmt.setInt(1, internalDepartmentId);
          pstmt.setInt(2, internalEmployeeId);
          pstmt.addBatch();
        }
        pstmt.executeBatch();
      }

      return true;
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return false;
  }

  /**
   * Generates a new unique external employee ID for the organization.
   *
   * @param organizationId the organization ID
   * @return a new unique external employee ID
   */
  public int generateNewEmployeeId(int organizationId) {
    String query = "SELECT MAX(employee_id) AS max_id FROM employees WHERE organization_id = ?";
    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
      pstmt.setInt(1, organizationId);
      ResultSet rs = pstmt.executeQuery();
      if (rs.next()) {
        int maxInternalId = rs.getInt("max_id");
        int maxExternalId = maxInternalId % 10000;
        return maxExternalId + 1;
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    // Start from 1 if no employees exist
    return 1;
  }

  /**
   * Returns the unique instance of the database connection.
   * Designed with "double-checked locking" mechanism to ensure thread safety.
   *
   * @return the database connection instance
   */
  public static DatabaseConnection getInstance() {
    if (instance == null) {
      synchronized (DatabaseConnection.class) {
        if (instance == null) {
          instance = new DatabaseConnection();
        }
      }
    }
    return instance;
  }
}
