package dev.coms4156.project;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Test the RouteController class with the real database connection.
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class RealRouteControllerTest {

  private static final String base64_1 = "Mg"; // Organization ID 2
  private static final String apikey_1 = "LWXYHABozUSjY9J-bJoeNi0dnzinfPIRLSlHe_pOyK2BFxo";
  private static final String base64_9999 = "OTk5OQ";
  private static final String apikey_9999 = "LWXYJwyiohgSJfgDkxOnNeX-bD2sVIr511qM_8NvYs-ai9g";


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
            .header("Authorization", apikey_1)
            .param("cid", base64_1)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andReturn();

    JSONObject response = parseResponse(result);
    Assertions.assertEquals(2, response.getInt("id"));
    JSONArray departmentsId = response.getJSONArray("departments_id");
    Assertions.assertTrue(departmentsId.length() > 0);
    JSONArray departments = response.getJSONArray("departments");
    Assertions.assertEquals(departmentsId.length(), departments.length());
    for (int i = 0; i < departmentsId.length(); i++) {
      JSONObject department = departments.getJSONObject(i);
      Assertions.assertEquals(departmentsId.getInt(i), department.getInt("id"));
      System.out.println(
          departmentsId.getInt(i) + ": "
              + department.getInt("id") + " --> "
              + department.getString("name")
      );
      org_1_depts.put(departmentsId.getInt(i), department.getString("name"));
    }

    Assumptions.assumeTrue(!org_1_depts.isEmpty());
  }

  @Test
  @Execution(ExecutionMode.CONCURRENT)
  @Order(5)
  public void testGetOrgNotexist() throws Exception {
    mockMvc.perform(get("/getOrgInfo")
            .header("Authorization", apikey_9999)
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
            .header("Authorization", apikey_1)
            .param("cid", "invalid_clienttoken")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden())
        .andReturn();
  }

  @Test
  @Execution(ExecutionMode.CONCURRENT)
  @Order(7)
  public void testGetOrgNoparam() throws Exception {
    mockMvc.perform(get("/getOrgInfo")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andReturn();
  }

  @Test
  @Execution(ExecutionMode.CONCURRENT)
  @Order(8)
  public void testGetOrgWrongkeyformat() throws Exception {
    mockMvc.perform(get("/getOrgInfo")
            .header("Authorization", "ABCDEFGHIJKLMN")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andReturn();
  }

  @Test
  @Order(9)
  public void testGetDeptSuccess() throws Exception {
    org_1_employees = new ArrayList<>();
    for (Map.Entry<Integer, String> entry : org_1_depts.entrySet()) {
      MvcResult result = mockMvc.perform(get("/getDeptInfo")
              .header("Authorization", apikey_1)
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
            "Dept " + entry.getKey() + ": Employee "
                + employee.getInt("id") + " --> "
                + employee.getString("name")
        );
      }
    }
  }

  @Test
  @Execution(ExecutionMode.CONCURRENT)
  @Order(10)
  public void testGetDeptNotexist() throws Exception {
    mockMvc.perform(get("/getDeptInfo")
            .header("Authorization", apikey_1)
            .param("cid", base64_1)
            .param("did", "9999")
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andReturn();
  }

  @Test
  @Execution(ExecutionMode.CONCURRENT)
  @Order(11)
  public void testGetEmployeeSuccess() throws Exception {
    for (int employeeId : org_1_employees) {
      MvcResult result = mockMvc.perform(get("/getEmpInfo")
              .header("Authorization", apikey_1)
              .param("cid", base64_1)
              .param("eid", String.valueOf(employeeId))
              .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andReturn();

      JSONObject response = parseResponse(result);
      Assertions.assertEquals(employeeId, response.getInt("ID"));
      System.out.println(
          "Employee " + employeeId + ": "
              + response.getString("name") + " hired on "
              + response.getString("hireDate")
      );
    }
  }
}
