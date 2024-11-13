package dev.coms4156.project;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A singleton class of HR database facade.
 * This class is responsible for creating and managing the connection to the HR database.
 * Designed under the Singleton Design Pattern.
 */
public class HrDatabaseFacade {
  private static final Map<Integer, HrDatabaseFacade> instances = new HashMap<>();
  // This boolean is used to switch between the real database and the test database
  private static boolean isTestMode = false;
  private static DatabaseConnection dbConnectionStub = null;

  private final DatabaseConnection dbConnection;
  private final int organizationId;
  private List<Employee> employees;
  private List<Department> departments;
  private Organization organization;

  /**
   * Constructs a HR database facade instance for a specific organization.
   *
   * @param organizationId the organization id
   */
  private HrDatabaseFacade(int organizationId) {
    this.dbConnection = isTestMode ? dbConnectionStub : DatabaseConnection.getInstance();
    this.organizationId = organizationId;
    // Initialize the in-memory cache
    this.employees = dbConnection.getEmployees(organizationId);
    this.departments = dbConnection.getDepartments(organizationId);
    this.organization = dbConnection.getOrganization(organizationId);
    // TODO: What if this organization does not exist in the database?
    // TODO: Should we throw a 403 exception here?
  }

  /**
   * Returns the employee with the specified ID.
   *
   * @param employeeId the employee ID
   * @return the employee
   */
  public Employee getEmployee(int employeeId) {
    // Check the in-memory cache
    Employee employee = employees
            .stream()
            .filter(e -> e.getId() == employeeId)
            .findFirst()
            .orElse(null);

    if (employee == null) {
      // If not found in cache, query the database
      List<Employee> updatedEmployees = dbConnection.getEmployees(this.organizationId);
      employee = updatedEmployees
              .stream()
              .filter(e -> e.getId() == employeeId)
              .findFirst()
              .orElse(null);

      if (employee != null) {
        // Update the cache
        this.employees = updatedEmployees;
      }
    }

    return employee;
  }

  /**
   * Returns the department with the specified ID.
   *
   * @param departmentId the department ID
   * @return the department
   */
  public Department getDepartment(int departmentId) {
    // Check the in-memory cache
    Department department = departments
            .stream()
            .filter(d -> d.getId() == departmentId)
            .findFirst()
            .orElse(null);

    if (department == null) {
      // If not found in cache, query the database
      List<Department> updatedDepartments = dbConnection.getDepartments(this.organizationId);
      department = updatedDepartments
              .stream()
              .filter(d -> d.getId() == departmentId)
              .findFirst()
              .orElse(null);

      if (department != null) {
        // Update the cache
        this.departments = updatedDepartments;
      }
    }

    return department;
  }

  /**
   * Returns the organization of the client.
   *
   * @return the organization
   */
  public Organization getOrganization() {
    // Check the in-memory cache (this.organization was initialized in constructor)
    return organization;
  }

  /**
   * Updates the employee information.
   *
   * @param employee the updated employee object
   * @return true if the employee is updated successfully, false otherwise
   */
  public boolean updateEmployee(Employee employee) {
    boolean success = dbConnection.updateEmployee(this.organizationId, employee);
    if (success) {
      this.employees = dbConnection.getEmployees(this.organizationId);
    }
    return success;
  }

  /**
   * Updates the department information.
   *
   * @param department the updated department object
   * @return true if the department is updated successfully, false otherwise
   */
  public boolean updateDepartment(Department department) {
    boolean success = dbConnection.updateDepartment(this.organizationId, department);
    if (success) {
      List<Department> updatedDepartments = dbConnection.getDepartments(this.organizationId);
      this.departments = updatedDepartments;
    }
    return success;
  }

    /**
     * Updates the organization information.
     *
     * @param organization the updated organization object
     * @return true if the organization is updated successfully, false otherwise
     */
  public boolean updateOrganization(Organization organization) {
    boolean success = dbConnection.updateOrganization(organization);
    if (success) {
      // Update the in-memory cache
      this.organization = organization;
    }
    return success;
  }

  /**
   * Adds a new employee to a department.
   *
   * @param departmentId the department ID
   * @param employee the employee to add
   * @return the added employee with assigned ID, or null if failed
   */
  public Employee addEmployeeToDepartment(int departmentId, Employee employee) {
    int internalDeptId = this.organizationId * 10000 + departmentId;
    int internalEmpId = dbConnection
            .addEmployeeToDepartment(this.organizationId, internalDeptId, employee);

    if (internalEmpId != -1) {
      int externalEmpId = internalEmpId % 10000;

      Employee newEmployee = new Employee(
              externalEmpId,
              employee.getName(),
              employee.getHireDate()
      );

      // Update the in-memory cache
      this.employees = dbConnection.getEmployees(this.organizationId);
      this.departments = dbConnection.getDepartments(this.organizationId);

      return newEmployee;
    }
    return null;
  }


    /**
     * Inserts a new department into the database.
     *
     * @param department the partially filled department object
     * @return the real department object with the ID assigned
     */
  public Department insertDepartment(Department department) {
    Department newDepartment = dbConnection.insertDepartment(this.organizationId, department);
    if (newDepartment != null) {
      // Update the in-memory cache
      this.departments.add(newDepartment);
    }
    return newDepartment;
  }

  /**
   * Removes an employee from a department.
   *
   * @param departmentId the department ID
   * @param employeeId the employee ID
   * @return true if the employee is removed successfully, false otherwise
   */
  public boolean removeEmployeeFromDepartment(int departmentId, int employeeId) {
    int internalDeptId = this.organizationId * 10000 + departmentId;
    int internalEmpId = this.organizationId * 10000 + employeeId;

    boolean success = dbConnection.removeEmployeeFromDepartment(
            this.organizationId,
            internalDeptId,
            internalEmpId
    );

    if (success) {
      // Update the in-memory cache
      this.employees = dbConnection.getEmployees(this.organizationId);
      this.departments = dbConnection.getDepartments(this.organizationId);
    }

    return success;
  }

    /**
     * Removes a department from the database.
     *
     * @param departmentId the department ID
     * @return true if the department is removed successfully, false otherwise
     */
  public boolean removeDepartment(int departmentId) {
    boolean success = dbConnection.removeDepartment(this.organizationId, departmentId);
    if (success) {
      // Update the in-memory cache
      this.departments.removeIf(dept -> dept.getId() == departmentId);
    }
    return success;
  }


  // TODO: How to insert(register) / remove(deregister) an organization?

  public static Organization insertOrganization(Organization organization) {
    DatabaseConnection dbConnection = isTestMode ? dbConnectionStub : DatabaseConnection.getInstance();
    Organization newOrganization = dbConnection.insertOrganization(organization);
    if (newOrganization != null) {
      // Create a new instance of HrDatabaseFacade for the new organization
      synchronized (HrDatabaseFacade.class) {
        instances.put(newOrganization.getId(), new HrDatabaseFacade(newOrganization.getId()));
      }
    }
    return newOrganization;
  }


  public static boolean removeOrganization(int organizationId) {
    DatabaseConnection dbConnection = isTestMode ? dbConnectionStub : DatabaseConnection.getInstance();
    boolean success = dbConnection.removeOrganization(organizationId);
    if (success) {
      // Remove the HrDatabaseFacade instance for the organization
      synchronized (HrDatabaseFacade.class) {
        instances.remove(organizationId);
      }
    }
    return success;
  }


  /**
   * Returns the unique instance of the HR database facade for a specific organization.
   * Designed with "double-checked locking" mechanism to ensure thread safety.
   *
   * @param organizationId the organization id
   * @return the HR database facade instance
   */
  public static HrDatabaseFacade getInstance(int organizationId) {
    if (!instances.containsKey(organizationId)) {
      synchronized (HrDatabaseFacade.class) {
        if (!instances.containsKey(organizationId)) {
          instances.put(organizationId, new HrDatabaseFacade(organizationId));
        }
      }
    }
    return instances.get(organizationId);
  }

  /**
   * Sets the test mode and test database for the HR database facade.
   *
   * @param testDatabaseConnection the test database connection
   *                               (null to disable the test mode)
   */
  public static void setTestMode(DatabaseConnection testDatabaseConnection) {
    if (testDatabaseConnection != null) {
      isTestMode = true;
      HrDatabaseFacade.dbConnectionStub = testDatabaseConnection;
    } else {
      isTestMode = false;
      HrDatabaseFacade.dbConnectionStub = null;
    }
  }
}
