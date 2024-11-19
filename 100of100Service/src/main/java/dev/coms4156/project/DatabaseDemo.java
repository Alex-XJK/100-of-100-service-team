package dev.coms4156.project;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseDemo {
    private static final String URL = "jdbc:mysql://demo-db.c3uqsummqbeu.us-east-1.rds.amazonaws.com:3306/demo_db";
    private static final String USER = "admin";
    private static final String PASSWORD = "12345678";

    public static void main(String[] args) {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connected to the database.");

            int organizationId = 1;
            int employeeId = 1;
            getEmployee(connection, organizationId, employeeId);

            int departmentId = 1;
            insertEmployee(connection, organizationId, departmentId, "New Employee", "Junior Developer", 60000.00);

            // Confirm the insertion by retrieving the last inserted employee
            int newEmployeeId = getLastInsertedEmployeeId(connection);
            if (newEmployeeId != -1) {
                getEmployee(connection, organizationId, newEmployeeId);
            } else {
                System.out.println("\nFailed to retrieve the new employee ID.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                    System.out.println("Disconnected from the database.");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public static void getEmployee(Connection connection, int organizationId, int employeeId) {
        String query = "SELECT * FROM employees WHERE organization_id = ? AND employee_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, organizationId);
            pstmt.setInt(2, employeeId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                System.out.println("\nEmployee Details:");
                System.out.println("Employee ID: " + rs.getInt("employee_id"));
                System.out.println("Name: " + rs.getString("name"));
                System.out.println("Position: " + rs.getString("position"));
                System.out.println("Hire Date: " + rs.getDate("hire_date"));
                System.out.println("Salary: " + rs.getDouble("salary"));
                System.out.println("Performance: " + rs.getDouble("performance"));
            } else {
                System.out.println("\nEmployee not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void insertEmployee(Connection connection, int organizationId, int departmentId, String name, String position, double salary) {
        String insertQuery = "INSERT INTO employees (organization_id, department_id, name, position, hire_date, salary, performance) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertQuery)) {
            pstmt.setInt(1, organizationId);
            pstmt.setInt(2, departmentId);
            pstmt.setString(3, name);
            pstmt.setString(4, position);
            pstmt.setDate(5, new java.sql.Date(System.currentTimeMillis()));
            pstmt.setDouble(6, salary);
            pstmt.setDouble(7, 99);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("\nInserted new employee:");
                System.out.println("Name: " + name);
                System.out.println("Position: " + position);
            } else {
                System.out.println("\nFailed to insert new employee.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static int getLastInsertedEmployeeId(Connection connection) {
        String query = "SELECT LAST_INSERT_ID() AS last_id";
        try (PreparedStatement pstmt = connection.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("last_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

}
