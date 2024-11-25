package dev.coms4156.project;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * Test the RouteController class with the real database connection.
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class RealRouteControllerTest {

  private final String base64_1 = "MQ";
  private final String base64_9999 = "OTk5OQ";

  @Autowired
  private MockMvc mockMvc;

  private static Map<Integer, String> org_1_depts;
  private static List<Integer> org_1_employees;

  /**
   * Parse the response from the server.
   *
   * @param result The result from the server.
   * @return The response from the server.
   * @throws Exception If the response cannot be parsed.
   */
  private static JSONObject parseResponse(MvcResult result) throws Exception {
    return new JSONObject(result.getResponse().getContentAsString());
  }

  /**
   * Set up the test environment to use the real database connection.
   */
  @BeforeAll
  public static void setUp() {
    MysqlConnection mysql = MysqlConnection.getInstance();
    HrDatabaseFacade.setConnection(mysql);
  }



  @Test
  @Order(4)
  public void testGetOrgSuccess() throws Exception {
    org_1_depts = new HashMap<>();

    MvcResult result = mockMvc.perform(get("/getOrgInfo")
            .param("cid", base64_1)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andReturn();

    JSONObject response = parseResponse(result);
    Assertions.assertEquals(1, response.getInt("id"));
    JSONArray departments_id = response.getJSONArray("departments_id");
    Assertions.assertTrue(departments_id.length() > 0);
    JSONArray departments = response.getJSONArray("departments");
    Assertions.assertEquals(departments_id.length(), departments.length());
    for (int i = 0; i < departments_id.length(); i++) {
      JSONObject department = departments.getJSONObject(i);
      Assertions.assertEquals(departments_id.getInt(i), department.getInt("id"));
      System.out.println(
          departments_id.getInt(i) + ": " +
              department.getInt("id") + " --> " +
              department.getString("name")
      );
      org_1_depts.put(departments_id.getInt(i), department.getString("name"));
    }

    Assumptions.assumeTrue(!org_1_depts.isEmpty());
  }

  @Test
  @Execution(ExecutionMode.CONCURRENT)
  @Order(5)
  public void testGetOrgNotexist() throws Exception {
    mockMvc.perform(get("/getOrgInfo")
            .param("cid", base64_9999)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andReturn();
  }

  @Test
  @Execution(ExecutionMode.CONCURRENT)
  @Order(6)
  public void testGetOrgFailure() throws Exception {
    mockMvc.perform(get("/getOrgInfo")
            .param("cid", "invalid_clienttoken")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError())
        .andReturn();
  }

  @Test
  @Execution(ExecutionMode.CONCURRENT)
  @Order(7)
  public void testGetOrgNoparam() throws Exception {
    mockMvc.perform(get("/getOrgInfo")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden())
        .andReturn();
  }

  @Test
  @Order(8)
  public void testGetDeptSuccess() throws Exception {
    org_1_employees = new ArrayList<>();
    for (Map.Entry<Integer, String> entry : org_1_depts.entrySet()) {
      MvcResult result = mockMvc.perform(get("/getDeptInfo")
              .param("cid", base64_1)
              .param("did", entry.getKey().toString())
              .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andReturn();

      JSONObject response = parseResponse(result);
      Assertions.assertEquals(entry.getKey(), response.getInt("id"));
      Assertions.assertEquals(entry.getValue(), response.getString("name"));
      JSONArray employees = response.getJSONArray("employees");
      Assertions.assertEquals(response.getInt("employeeCount"), employees.length());
      for (int i = 0; i < employees.length(); i++) {
        JSONObject employee = employees.getJSONObject(i);
        org_1_employees.add(employee.getInt("id"));
        System.out.println(
            "Dept " + entry.getKey() + ": Employee " +
                employee.getInt("id") + " --> " +
                employee.getString("name")
        );
      }
    }
  }

  @Test
  @Execution(ExecutionMode.CONCURRENT)
  @Order(9)
  public void testGetDeptNotexist() throws Exception {
    mockMvc.perform(get("/getDeptInfo")
            .param("cid", base64_1)
            .param("did", "9999")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andReturn();
  }

  @Test
  @Execution(ExecutionMode.CONCURRENT)
  @Order(10)
  public void testGetEmployeeSuccess() throws Exception {
    for (int employee_id : org_1_employees) {
      MvcResult result = mockMvc.perform(get("/getEmpInfo")
              .param("cid", base64_1)
              .param("eid", String.valueOf(employee_id))
              .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andReturn();

      JSONObject response = parseResponse(result);
      Assertions.assertEquals(employee_id, response.getInt("ID"));
      System.out.println(
          "Employee " + employee_id + ": " +
              response.getString("name") + " hired on " +
              response.getString("hireDate")
      );
    }
  }
}
