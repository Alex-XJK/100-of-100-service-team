package dev.coms4156.project;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides in-memory data for testing purposes without connecting to a real database.
 */
public final class InmemConnection implements DatabaseConnection {
  private static volatile InmemConnection instance;

  private final Map<Integer, List<Employee>> testEmployees = new HashMap<>();
  private final Map<Integer, List<Department>> testDepartments = new HashMap<>();
  private final Map<Integer, Organization> testOrganizations = new HashMap<>();

  public String connectionName() {
    return "In-memory Database::null";
  }

  /**
   * Constructs a DatabaseConnectionStub and initializes test data.
   */
  private InmemConnection() {
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
  public boolean removeDepartment(int organizationId, int externalDepartmentId) {
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
  public boolean removeEmployeeFromDepartment(
      int organizationId, int departmentId, int employeeId
  ) {
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

  @Override
  public Department insertDepartment(int organizationId, Department department) {
    return null;
  }
  
  /**
   * Updates an employee's information in the stubbed database for a given organization.
   *
   * @param organizationId the organization ID (client ID)
   * @param employee the {@code Employee} object containing the updated information
   * @return {@code true} if the employee was found and updated; {@code false} otherwise
   */
  @Override
  public boolean updateEmployee(int organizationId, Employee employee) {
    List<Employee> employees = testEmployees.get(organizationId);
    boolean updated = false;

    if (employees != null) {
      for (int i = 0; i < employees.size(); i++) {
        if (employees.get(i).getId() == employee.getId()) {
          employees.set(i, employee);
          updated = true;
          break;
        }
      }
    }

    if (updated) {
      // Update the employee in the departments' employee lists
      List<Department> departments = testDepartments.get(organizationId);
      if (departments != null) {
        for (Department dept : departments) {
          List<Employee> deptEmployees = dept.getEmployees();
          if (deptEmployees != null) {
            for (int i = 0; i < deptEmployees.size(); i++) {
              if (deptEmployees.get(i).getId() == employee.getId()) {
                deptEmployees.set(i, employee);
                break;
              }
            }
          }

          Employee head = dept.getHead();
          if (head != null && head.getId() == employee.getId()) {
            dept.setHead(employee);
          }
        }
      }
    }

    return updated;
  }

  /**
   * Retrieves an employee for a given organization by external employee ID.
   *
   * @param organizationId the organization ID (client ID)
   * @param externalEmployeeId the external employee ID
   * @return the Employee object if found, null otherwise
   */
  @Override
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
  @Override
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
  @Override
  public List<Employee> getEmployees(int organizationId) {
    return testEmployees.getOrDefault(organizationId, new ArrayList<>());
  }

  /**
   * Retrieves a list of departments for a given organization.
   *
   * @param organizationId the organization ID (client ID)
   * @return a list of Department objects
   */
  @Override
  public List<Department> getDepartments(int organizationId) {
    return testDepartments.getOrDefault(organizationId, new ArrayList<>());
  }

  /**
   * Retrieves the organization for a given organization ID.
   *
   * @param organizationId the organization ID (client ID)
   * @return the Organization object
   */
  @Override
  public Organization getOrganization(int organizationId) {
    return testOrganizations.getOrDefault(organizationId, null);
  }

  @Override
  public boolean updateOrganization(Organization organization) {
    return false;
  }

  @Override
  public boolean removeOrganization(int organizationId) {
    return false;
  }

  /**
   * Retrieves the organization for a given organization name.
   *
   * @param organization the organization object that contains the name
   * @return the correct Organization object
   */
  @Override
  public Organization insertOrganization(Organization organization) {
    // Get the maximum ID to generate a new ID
    int maxId = 0;
    for (int id : testOrganizations.keySet()) {
      if (id > maxId) {
        maxId = id;
      }
    }
    int newId = maxId + 1;
    // Create a new organization with the new ID
    Organization newOrganization = new Organization(newId, organization.getName());
    testOrganizations.put(newId, newOrganization);
    return newOrganization;
  }

  /** Initializes the test data for the stub. */
  private void initializeTestData() {
    // Client 1 Data
    int clientId1 = 1;

    // Departments for Client 1
    Department engineering1 = new Department(1, "Engineering", new ArrayList<>());
    Department marketing1 = new Department(2, "Marketing", new ArrayList<>());

    // Employees for Client 1
    Employee johnDoe = new Employee(
        1, "John Doe", new Date(), "SoftwareEngineer", 100, 80
    );
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
    Employee bobBrown = new Employee(
        2, "Bob Brown", new Date(), "ProductManager", 200, 99
    );

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
  public static InmemConnection getInstance() {
    if (instance == null) {
      synchronized (InmemConnection.class) {
        if (instance == null) {
          instance = new InmemConnection();
        }
      }
    }
    return instance;
  }
}
