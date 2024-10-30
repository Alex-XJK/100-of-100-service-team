package dev.coms4156.project.stubs;

import dev.coms4156.project.DatabaseConnection;
import dev.coms4156.project.Department;
import dev.coms4156.project.Employee;
import dev.coms4156.project.Organization;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A test stub for the DatabaseConnection class.
 * This stub provides in-memory data for testing purposes without connecting to a real database.
 */
public class DatabaseConnectionStub extends DatabaseConnection {
  private static volatile DatabaseConnection instance;

  private final Map<Integer, List<Employee>> testEmployees = new HashMap<>();
  private final Map<Integer, List<Department>> testDepartments = new HashMap<>();
  private final Map<Integer, Organization> testOrganizations = new HashMap<>();

  /** Constructs a DatabaseConnectionStub and initializes test data. */
  private DatabaseConnectionStub() {
    initializeTestData();
  }

  /**
   * Resets the test data to its initial state.
   * This method should be called before each test to ensure a clean state.
   */
  public void resetTestData() {
    testEmployees.clear();
    testDepartments.clear();
    testOrganizations.clear();
    initializeTestData();
  }

  @Override
  public boolean updateDepartment(int organizationId, Department department) {
    List<Department> departments = testDepartments.get(organizationId);
    if (departments == null) {
      return false;
    }

    if (department.getHead() != null) {
      Employee head = getEmployee(organizationId, department.getHead().getId());
      if (head == null) {
        return false;
      }
    }

    for (int i = 0; i < departments.size(); i++) {
      if (departments.get(i).getId() == department.getId()) {
        departments.set(i, department);
        return true;
      }
    }
    return false;
  }

  @Override
  public int addEmployeeToDepartment(int organizationId, int departmentId, Employee employee) {
    List<Employee> employees = testEmployees.get(organizationId);
    List<Department> departments = testDepartments.get(organizationId);

    if (employees == null || departments == null) {
      return -1;
    }

    Department targetDept = null;
    for (Department dept : departments) {
      if (dept.getId() == (departmentId % 10000)) {
        targetDept = dept;
        break;
      }
    }

    if (targetDept == null) {
      return -1;
    }

    int maxId = 0;
    for (Employee emp : employees) {

      if (emp.getId() > maxId) {
        maxId = emp.getId();
      }
    }
    int newEmployeeId = maxId + 1;

    Employee newEmployee = new Employee(newEmployeeId, employee.getName(), employee.getHireDate());
    employees.add(newEmployee);
    targetDept.addEmployee(newEmployee);

    return organizationId * 10000 + newEmployeeId;
  }

  @Override
  public boolean removeEmployeeFromDepartment(int organizationId, int departmentId, int employeeId) {
    List<Department> departments = testDepartments.get(organizationId);
    if (departments == null) {
      return false;
    }

    int externalDepartmentId = departmentId % 10000;
    int externalEmployeeId = employeeId % 10000;

    Department targetDept = null;
    for (Department dept : departments) {
      if (dept.getId() == externalDepartmentId) {
        targetDept = dept;
        break;
      }
    }

    if (targetDept == null) {
      return false;
    }

    Employee targetEmployee = null;
    List<Employee> employees = testEmployees.get(organizationId);
    if (employees != null) {
      for (Employee emp : employees) {
        if (emp.getId() == externalEmployeeId) {
          targetEmployee = emp;
          break;
        }
      }
    }

    if (targetEmployee == null) {
      return false;
    }

    if (targetDept.getHead() != null && targetDept.getHead().getId() == targetEmployee.getId()) {
      targetDept.setHead(null);
    }

    boolean employeeRemoved = employees.remove(targetEmployee);

    boolean deptRemoved = false;
    List<Employee> deptEmployees = targetDept.getEmployees();
    if (deptEmployees != null) {
      deptRemoved = deptEmployees.remove(targetEmployee);
    }

    return employeeRemoved && deptRemoved;
  }

  /**
   * Retrieves an employee for a given organization by external employee ID.
   *
   * @param organizationId the organization ID (client ID)
   * @param externalEmployeeId the external employee ID
   * @return the Employee object if found, null otherwise
   */
  public Employee getEmployee(int organizationId, int externalEmployeeId) {
    List<Employee> employees = testEmployees.get(organizationId);
    if (employees != null) {
      for (Employee employee : employees) {
        if (employee.getId() == externalEmployeeId) {
          return employee;
        }
      }
    }
    return null;
  }

  /**
   * Retrieves a department for a given organization by external department ID.
   *
   * @param organizationId the organization ID (client ID)
   * @param externalDepartmentId the external department ID
   * @return the Department object if found, null otherwise
   */
  public Department getDepartment(int organizationId, int externalDepartmentId) {
    List<Department> departments = testDepartments.get(organizationId);
    if (departments != null) {
      for (Department department : departments) {
        if (department.getId() == externalDepartmentId) {
          return department;
        }
      }
    }
    return null;
  }

  /**
   * Retrieves a list of employees for a given organization.
   *
   * @param organizationId the organization ID (client ID)
   * @return a list of Employee objects
   */
  public List<Employee> getEmployees(int organizationId) {
    return testEmployees.getOrDefault(organizationId, new ArrayList<>());
  }

  /**
   * Retrieves a list of departments for a given organization.
   *
   * @param organizationId the organization ID (client ID)
   * @return a list of Department objects
   */
  public List<Department> getDepartments(int organizationId) {
    return testDepartments.getOrDefault(organizationId, new ArrayList<>());
  }

  /**
   * Retrieves the organization for a given organization ID.
   *
   * @param organizationId the organization ID (client ID)
   * @return the Organization object
   */
  public Organization getOrganization(int organizationId) {
    return testOrganizations.getOrDefault(
            organizationId, new Organization(organizationId, "Unknown"));
  }

  /** Initializes the test data for the stub. */
  private void initializeTestData() {
    // Client 1 Data
    int clientId1 = 1;

    // Departments for Client 1
    Department engineering1 = new Department(1, "Engineering", new ArrayList<>());
    Department marketing1 = new Department(2, "Marketing", new ArrayList<>());

    // Employees for Client 1
    Employee johnDoe = new Employee(1, "John Doe", new Date());
    Employee janeSmith = new Employee(2, "Jane Smith", new Date());

    // Add employees to departments for Client 1
    engineering1.addEmployee(johnDoe);
    marketing1.addEmployee(janeSmith);

    // Organization for Client 1
    Organization organization1 = new Organization(clientId1, "Organization One");
    organization1.addDepartment(engineering1);
    organization1.addDepartment(marketing1);

    // Store data for Client 1
    testEmployees.put(clientId1, new ArrayList<>(List.of(johnDoe, janeSmith)));
    testDepartments.put(clientId1, new ArrayList<>(List.of(engineering1, marketing1)));
    testOrganizations.put(clientId1, organization1);

    // Client 2 Data
    int clientId2 = 2;

    // Departments for Client 2
    Department engineering2 = new Department(1, "Engineering", new ArrayList<>());
    Department marketing2 = new Department(2, "Marketing", new ArrayList<>());

    // Employees for Client 2
    Employee aliceJohnson = new Employee(1, "Alice Johnson", new Date());
    Employee bobBrown = new Employee(2, "Bob Brown", new Date());

    // Add employees to departments for Client 2
    engineering2.addEmployee(aliceJohnson);
    marketing2.addEmployee(bobBrown);

    // Organization for Client 2
    Organization organization2 = new Organization(clientId2, "Organization Two");
    organization2.addDepartment(engineering2);
    organization2.addDepartment(marketing2);

    // Store data for Client 2
    testEmployees.put(clientId2, new ArrayList<>(List.of(aliceJohnson, bobBrown)));
    testDepartments.put(clientId2, new ArrayList<>(List.of(engineering2, marketing2)));
    testOrganizations.put(clientId2, organization2);
  }

  /**
   * Returns the unique instance of the database connection.
   * Designed with "double-checked locking" mechanism to ensure thread safety.
   *
   * @return the database connection instance
   */
  public static DatabaseConnection getInstance() {
    if (instance == null) {
      synchronized (DatabaseConnectionStub.class) {
        if (instance == null) {
          instance = new DatabaseConnectionStub();
        }
      }
    }
    return instance;
  }
}
