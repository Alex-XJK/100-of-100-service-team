package dev.coms4156.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * External integration tests for the HR system.
 * These tests verify how the system interacts with the
 * external real MySQL database.
 * Each test method specifies the external resource being integrated.
 */
public class ExternalIntegrationTest {

  private static Connection connection;
  private static MysqlConnection mysqlConnection;

  /**
   * Sets up the real database connection and test table before all tests.
   *
   * @throws Exception if an error occurs during setup
   */
  @BeforeAll
  public static void setup() throws Exception {
    // Establish a connection to the real database
    String url = "jdbc:mysql://new-db.c3uqsummqbeu.us-east-1.rds."
        + "amazonaws.com:3306/organization_management";
    String username = "admin";
    String password = "12345678";

    connection = DriverManager.getConnection(url, username, password);
    connection.setAutoCommit(false);

    mysqlConnection = MysqlConnection.getInstance();

    // Create test table
    createTestTable();
  }

  /**
   * Rolls back any changes and closes the database connection after all tests.
   *
   * @throws Exception if an error occurs during teardown
   */
  @AfterAll
  public static void tearDown() throws Exception {
    if (connection != null && !connection.isClosed()) {
      // Drop the test table
      dropTestTable();
      // Roll back any changes made during the tests
      connection.rollback();
      connection.close();
    }
  }

  /**
   * Creates a test table in the database for testing CRUD operations.
   *
   * @throws Exception if an error occurs during table creation
   */
  private static void createTestTable() throws Exception {
    String createTestTable = "CREATE TABLE IF NOT EXISTS test_table ("
        + "id INT PRIMARY KEY AUTO_INCREMENT,"
        + "name VARCHAR(255),"
        + "value INT"
        + ");";
    try (Statement stmt = connection.createStatement()) {
      stmt.execute(createTestTable);
    }
  }

  /**
   * Drops the test table from the database after testing.
   *
   * @throws Exception if an error occurs during table dropping
   */
  private static void dropTestTable() throws Exception {
    String dropTestTable = "DROP TABLE IF EXISTS test_table;";
    try (Statement stmt = connection.createStatement()) {
      stmt.execute(dropTestTable);
    }
  }

  /**
   * Test 1: Verifies that data can be inserted into the test table.
   * External resource integrated: Real MySQL Database via MysqlConnection
   */
  @Test
  public void testCreateOperation() throws Exception {
    String insertQuery = "INSERT INTO test_table (name, value) VALUES (?, ?);";
    try (PreparedStatement pstmt = connection.prepareStatement(insertQuery)) {
      pstmt.setString(1, "Test Name");
      pstmt.setInt(2, 123);
      int rowsAffected = pstmt.executeUpdate();
      assertEquals(1, rowsAffected, "One row should be inserted");
    }
  }

  /**
   * Test 2: Verifies that data can be read from the test table.
   *
   * <p>External resource integrated: Real MySQL Database via MysqlConnection
   */
  @Test
  public void testReadOperation() throws Exception {
    // Insert a test record
    testCreateOperation();

    String selectQuery = "SELECT * FROM test_table WHERE name = ?;";
    try (PreparedStatement pstmt = connection.prepareStatement(selectQuery)) {
      pstmt.setString(1, "Test Name");
      try (ResultSet rs = pstmt.executeQuery()) {
        assertTrue(rs.next(), "At least one record should be found");
        int id = rs.getInt("id");
        String name = rs.getString("name");
        int value = rs.getInt("value");

        assertEquals("Test Name", name, "Name should match");
        assertEquals(456, value, "Value should match");
      }
    }
  }

  /**
   * Test 3: Verifies that data in the test table can be updated.
   * External resource integrated: Real MySQL Database via MysqlConnection
   */
  @Test
  public void testUpdateOperation() throws Exception {
    testCreateOperation();

    String updateQuery = "UPDATE test_table SET value = ? WHERE name = ?;";
    try (PreparedStatement pstmt = connection.prepareStatement(updateQuery)) {
      pstmt.setInt(1, 456);
      pstmt.setString(2, "Test Name");
      int rowsAffected = pstmt.executeUpdate();
      assertEquals(1, rowsAffected, "One row should be updated");
    }

    String selectQuery = "SELECT value FROM test_table WHERE name = ?;";
    try (PreparedStatement pstmt = connection.prepareStatement(selectQuery)) {
      pstmt.setString(1, "Test Name");
      try (ResultSet rs = pstmt.executeQuery()) {
        assertTrue(rs.next(), "Record should exist");
        int value = rs.getInt("value");
        assertEquals(456, value, "Value should be updated to 456");
      }
    }
  }

  /**
   * Test 4: Verifies that data can be deleted from the test table.
   * External resource integrated: Real MySQL Database via MysqlConnection
   */
  @Test
  public void testDeleteOperation() throws Exception {
    testCreateOperation();

    String deleteQuery = "DELETE FROM test_table WHERE name = ?;";
    try (PreparedStatement pstmt = connection.prepareStatement(deleteQuery)) {
      pstmt.setString(1, "Test Name");
      int rowsAffected = pstmt.executeUpdate();
      assertEquals(1, rowsAffected, "One row should be deleted");
    }

    String selectQuery = "SELECT * FROM test_table WHERE name = ?;";
    try (PreparedStatement pstmt = connection.prepareStatement(selectQuery)) {
      pstmt.setString(1, "Test Name");
      try (ResultSet rs = pstmt.executeQuery()) {
        assertTrue(!rs.next(), "No records should be found after deletion");
      }
    }
  }
}
