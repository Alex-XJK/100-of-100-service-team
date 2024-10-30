package dev.coms4156.project;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
        List<Employee> employees = getEmployeesForDepartment(internalDepartmentId, organizationId);
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
        List<Employee> employees = getEmployeesForDepartment(internalId, organizationId);
        Department department = new Department(
                externalId,
                rs.getString("name"),
                employees
        );

        // Get and set head if exists
        Integer headEmployeeId = rs.getInt("head_employee_id");
        if (!rs.wasNull()) {
          Employee head = getEmployee(organizationId, headEmployeeId % 10000);
          if (head != null) {
            department.setHead(head);
          }
        }

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
   * @param organizationId the organization id
   * @return a list of employees in the department
   */
  private List<Employee> getEmployeesForDepartment(int internalDepartmentId, int organizationId) {
    List<Employee> employees = new ArrayList<>();
    String query = "SELECT * FROM employees WHERE department_id = ? AND organization_id = ?";

    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
      pstmt.setInt(1, internalDepartmentId);
      pstmt.setInt(2, organizationId);
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
   * Adds a new employee to a department in the database.
   *
   * @param organizationId the organization id
   * @param departmentId the internal department id
   * @param employee the employee to add
   * @return the internal employee ID if successful, -1 if failed
   */
  public int addEmployeeToDepartment(int organizationId, int departmentId, Employee employee) {
    // First get the next available employee ID for this organization
    String maxIdQuery =
            "SELECT MAX(employee_id) as max_id "
                    + "FROM employees "
                    + "WHERE organization_id = ?";
    int newEmployeeId;

    try (PreparedStatement pstmt = connection.prepareStatement(maxIdQuery)) {
      pstmt.setInt(1, organizationId);
      ResultSet rs = pstmt.executeQuery();
      if (rs.next()) {
        int maxId = rs.getInt("max_id");
        if (rs.wasNull()) {
          newEmployeeId = organizationId * 10000 + 1;
        } else {
          newEmployeeId = maxId + 1;
        }
      } else {
        newEmployeeId = organizationId * 10000 + 1;
      }
    } catch (SQLException e) {
      e.printStackTrace();
      return -1;
    }

    // Insert the new employee
    String insertEmployeeQuery =
            "INSERT INTO employees "
                    + "(employee_id, organization_id, "
                    + "department_id, name, hire_date, position, salary) "
                    + "VALUES (?, ?, ?, ?, ?, 'New Employee', 50000.00)";

    try (PreparedStatement pstmt = connection.prepareStatement(insertEmployeeQuery)) {
      pstmt.setInt(1, newEmployeeId);
      pstmt.setInt(2, organizationId);
      pstmt.setInt(3, departmentId);
      pstmt.setString(4, employee.getName());
      pstmt.setDate(5, new java.sql.Date(employee.getHireDate().getTime()));

      int rowsAffected = pstmt.executeUpdate();
      if (rowsAffected > 0) {
        return newEmployeeId;
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return -1;
  }

  /**
   * Removes an employee from a department in the database.
   *
   * @param organizationId the organization id
   * @param departmentId the internal department id
   * @param employeeId the internal employee id
   * @return true if removal successful, false otherwise
   */
  public boolean removeEmployeeFromDepartment(int organizationId,
                                              int departmentId, int employeeId) {
    String checkHeadQuery =
            "SELECT head_employee_id FROM departments "
                    + "WHERE department_id = ? AND organization_id = ?";

    try (PreparedStatement checkStmt = connection.prepareStatement(checkHeadQuery)) {
      checkStmt.setInt(1, departmentId);
      checkStmt.setInt(2, organizationId);

      ResultSet rs = checkStmt.executeQuery();
      if (rs.next() && rs.getInt("head_employee_id") == employeeId) {
        String updateHeadQuery =
                "UPDATE departments SET head_employee_id = NULL "
                        + "WHERE department_id = ? AND organization_id = ?";
        try (PreparedStatement updateStmt = connection.prepareStatement(updateHeadQuery)) {
          updateStmt.setInt(1, departmentId);
          updateStmt.setInt(2, organizationId);
          updateStmt.executeUpdate();
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }

    String deleteQuery =
            "DELETE FROM employees "
                    + "WHERE employee_id = ? AND department_id = ? AND organization_id = ?";

    try (PreparedStatement pstmt = connection.prepareStatement(deleteQuery)) {
      pstmt.setInt(1, employeeId);
      pstmt.setInt(2, departmentId);
      pstmt.setInt(3, organizationId);

      int rowsAffected = pstmt.executeUpdate();
      return rowsAffected > 0;
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Updates a department's information in the database.
   * This method handles all department updates including setting department head.
   *
   * @param organizationId the organization id
   * @param department the department to update
   * @return true if update successful, false otherwise
   */
  public boolean updateDepartment(int organizationId, Department department) {
    int internalDepartmentId = organizationId * 10000 + department.getId();
    Employee head = department.getHead();
    int headEmployeeId = head != null ? (organizationId * 10000 + head.getId()) : 0;

    if (head != null) {
      String verifyQuery =
              "SELECT 1 FROM employees "
                      + "WHERE employee_id = ? AND organization_id = ? AND department_id = ?";

      try (PreparedStatement verifyStmt = connection.prepareStatement(verifyQuery)) {
        verifyStmt.setInt(1, headEmployeeId);
        verifyStmt.setInt(2, organizationId);
        verifyStmt.setInt(3, internalDepartmentId);

        if (!verifyStmt.executeQuery().next()) {
          return false;
        }
      } catch (SQLException e) {
        e.printStackTrace();
        return false;
      }
    }

    String query = "UPDATE departments SET name = ?, head_employee_id = ? "
            + "WHERE organization_id = ? AND department_id = ?";

    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
      pstmt.setString(1, department.getName());
      if (head != null) {
        pstmt.setInt(2, headEmployeeId);
      } else {
        pstmt.setNull(2, java.sql.Types.INTEGER);
      }
      pstmt.setInt(3, organizationId);
      pstmt.setInt(4, internalDepartmentId);

      int rowsAffected = pstmt.executeUpdate();
      return rowsAffected > 0;
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
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
