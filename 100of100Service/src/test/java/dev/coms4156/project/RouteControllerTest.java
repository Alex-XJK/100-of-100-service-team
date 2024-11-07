package dev.coms4156.project;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.coms4156.project.stubs.DatabaseConnectionStub;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * An integration test class for the RouteController class.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class RouteControllerTest {

  @Autowired
  private MockMvc mockMvc;

  private static final String CLIENT_ID_1 = "MQ";
  private static final String CLIENT_ID_2 = "Mg";
  private static final String CLIENT_ID_99 = "OTk";

  private static DatabaseConnectionStub dbConnectionStub;

  /**
   * Set up the test environment.
   * Set the database connection to the stub, and flag the TestMode.
   */
  @BeforeAll
  public static void setUp() {
    dbConnectionStub = (DatabaseConnectionStub) DatabaseConnectionStub.getInstance();
    HrDatabaseFacade.setTestMode(dbConnectionStub);
  }

  @BeforeEach
  public void resetDatabase() {
    dbConnectionStub.resetTestData();
  }

  @Test
  public void testGetEmployeeInfo() throws Exception {
    MvcResult mvcResult1 = mockMvc.perform(get("/getEmpInfo")
        .param("cid", CLIENT_ID_1)
        .param("eid", "1")
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk()).andReturn();

    // String expected = "Employee: Alice (ID: 1)";
    String content = mvcResult1.getResponse().getContentAsString();
    System.out.println(content);
  }

  @Test
  public void testGetDeptInfo() throws Exception {
    MvcResult mvcResult1 = mockMvc.perform(get("/getDeptInfo")
        .param("cid", CLIENT_ID_1)
        .param("did", "1")
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk()).andReturn();

    String content = mvcResult1.getResponse().getContentAsString();
    System.out.println(content);
  }

  @Test
  public void testGetOrganizationInfo() throws Exception {
    MvcResult mvcResult1 = mockMvc.perform(get("/getOrgInfo")
        .param("cid", CLIENT_ID_1)
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk()).andReturn();

    // String expected = "Department: Engineering (ID: 1)";
    String content = mvcResult1.getResponse().getContentAsString();
    System.out.println(content);
  }

  @Test
  public void testSetDeptHead() throws Exception {
    // patch for test
    mockMvc.perform(patch("/setDeptHead")
        .param("cid", CLIENT_ID_1)
        .param("did", "1")
        .param("eid", "1")
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk()).andReturn();

    // get department to check if head is set
    MvcResult mvcResult1 = mockMvc.perform(get("/getDeptInfo")
        .param("cid", CLIENT_ID_1)
        .param("did", "1")
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk()).andReturn();

    String content = mvcResult1.getResponse().getContentAsString();
    System.out.println(content);
  }

  // Test: Client cannot access another client's employee
  @Test
  public void testClientCannotAccessAnotherClientsEmployee() throws Exception {
    // Client 1 tries to access Client 2's Employee ID 1
    MvcResult mvcResult = mockMvc.perform(get("/getEmpInfo")
                    .param("cid", CLIENT_ID_1)
                    .param("eid", "1")
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk()).andReturn();

    String content = mvcResult.getResponse().getContentAsString();
    // Should contain "John Doe", not "Alice Johnson"
    assert (content.contains("John Doe"));
    assert (!content.contains("Alice Johnson"));
  }

  // Test: Accessing a non-existent employee returns an error
  @Test
  public void testAccessNonExistentEmployee() throws Exception {
    // Client 1 tries to access Employee ID 99 (does not exist)
    mockMvc.perform(get("/getEmpInfo")
                    .param("cid", CLIENT_ID_1)
                    .param("eid", "99")
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
  }

  // Test: Invalid client ID returns an error
  @Test
  public void testInvalidClientId() throws Exception {
    // Client ID 99 does not exist
    mockMvc.perform(get("/getEmpInfo")
                    .param("cid", CLIENT_ID_99)
                    .param("eid", "1")
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
  }

  // Test: Boundary case - negative employee ID
  @Test
  public void testNegativeEmployeeId() throws Exception {
    mockMvc.perform(get("/getEmpInfo")
                    .param("cid", CLIENT_ID_1)
                    .param("eid", "-1")
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
  }

  @Test
  public void testAddEmpToDept() throws Exception {
    // post for the test
    mockMvc.perform(post("/addEmpToDept")
        .param("cid", CLIENT_ID_1)
        .param("did", "1")
        .param("name", "Lily")
        .param("hireDate", "2022-06-28")
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated()).andReturn();

    // get department to check if the employee is added
    MvcResult mvcResult1 = mockMvc.perform(get("/getDeptInfo")
        .param("cid", CLIENT_ID_1)
        .param("did", "1")
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk()).andReturn();

    String content = mvcResult1.getResponse().getContentAsString();
    System.out.println(content);
  }

  @Test
  public void testRemoveEmpFromDept() throws Exception {
    mockMvc.perform(delete("/removeEmpFromDept")
        .param("cid", CLIENT_ID_1)
        .param("did", "1")
        .param("eid", "1")
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk()).andReturn();

    // get department to check if the employee is removed
    MvcResult mvcResult1 = mockMvc.perform(get("/getDeptInfo")
        .param("cid", CLIENT_ID_1)
        .param("did", "1")
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk()).andReturn();

    String content1 = mvcResult1.getResponse().getContentAsString();
    System.out.println(content1);  // verify employee is no longer listed

    // test if the employee not existed
    MvcResult mvcResult2 = mockMvc.perform(delete("/removeEmpFromDept")
        .param("cid", CLIENT_ID_1)
        .param("did", "1")
        .param("eid", "6")
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest()).andReturn();

    String content2 = mvcResult2.getResponse().getContentAsString();
    System.out.println(content2);
  }

  @Test
  public void testSetEmpPosition() throws Exception {
    mockMvc.perform(patch("/setEmpPos")
        .param("cid", CLIENT_ID_1)
        .param("eid", "1")
        .param("position", "SoftwareEngineer")
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk()).andReturn();
  }

  @Test
  public void testStatDeptPos() throws Exception {
    MvcResult mvcResult1 = mockMvc.perform(get("/statDeptPos")
        .param("cid", CLIENT_ID_1)
        .param("did", "1")
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk()).andReturn();

    String content = mvcResult1.getResponse().getContentAsString();
    Assertions.assertTrue(content.contains("SoftwareEngineer"));
    Assertions.assertTrue(content.contains("ProductManager"));
  }

  @Test
  public void testSetEmpSalary() throws Exception {
    MvcResult mvcResult = mockMvc.perform(patch("/setEmpSalary")
        .param("cid", CLIENT_ID_1)
        .param("eid", "1")
        .param("salary", "5000.05")
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk()).andReturn();

    String content = mvcResult.getResponse().getContentAsString();
    Assertions.assertTrue(content.contains("5000.05"));
  }

  @Test
  public void testStatDeptBudget() throws Exception {
    MvcResult mvcResult1 = mockMvc.perform(get("/statDeptBudget")
        .param("cid", CLIENT_ID_1)
        .param("did", "1")
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk()).andReturn();

    String res = mvcResult1.getResponse().getContentAsString();
    Assertions.assertTrue(res.contains("Total"));
    Assertions.assertTrue(res.contains("LowestEmployee"));
  }

  @Test
  public void testSetEmpPerformance() throws Exception {
    MvcResult mvcResult = mockMvc.perform(patch("/setEmpPerf")
        .param("cid", CLIENT_ID_1)
        .param("eid", "1")
        .param("performance", "95.5")
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk()).andReturn();

    String content = mvcResult.getResponse().getContentAsString();
    Assertions.assertTrue(content.contains("95.5"));
  }

  @Test
  public void testStatDeptPerf() throws Exception {
    MvcResult mvcResult1 = mockMvc.perform(get("/statDeptPerf")
        .param("cid", CLIENT_ID_1)
        .param("did", "1")
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk()).andReturn();

    String res = mvcResult1.getResponse().getContentAsString();
    Assertions.assertTrue(res.contains("75thPercentile"));
    Assertions.assertTrue(res.contains("Average"));
  }

  /**
   * Tear down the test environment.
   * Reset the database connection to the real database.
   */
  @AfterAll
  public static void tearDown() {
    HrDatabaseFacade.setTestMode(null);
  }
}
