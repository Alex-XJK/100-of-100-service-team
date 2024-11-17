package dev.coms4156.project;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.junit.jupiter.api.Test;

/**
 * A test class for the HrDatabaseFacade class.
 */
public class HrDatabaseFacadeTest {

  @Test
  public void testGetOrganization() {
    HrDatabaseFacade facade = HrDatabaseFacade.getInstance(1);
    Organization org = facade.getOrganization();
    assertNotNull(org, "Organization should not be null");
    assumeTrue(org.getNumEmployees() > 0, "Organization should have employees");
    System.out.println("Successfully retrieved: " + org.getName());
    System.out.println(Organization.displayStructure(org, 0));
  }
}