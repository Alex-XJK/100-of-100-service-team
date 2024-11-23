package dev.coms4156.project.command;

import dev.coms4156.project.HrDatabaseFacade;
import dev.coms4156.project.Organization;
import dev.coms4156.project.utils.CodecUtils;
import java.util.HashMap;
import java.util.Map;

/**
 * A command to create a new organization.
 */
public class RegisterCmd implements Command {
  private final String name;

  /**
   * Constructs a command to create a new organization.
   *
   * @param name         the name of the organization
   */
  public RegisterCmd(String name) {
    this.name = name;
  }

  @Override
  public Object execute() {
    Map<String, String> response = new HashMap<>();
    Organization partialOrg = new Organization(-1, this.name);
    Organization newOrg = HrDatabaseFacade.insertOrganization(partialOrg);
    if (newOrg == null) {
      response.put("status", "failed");
      response.put("message", "Failed to create organization");
    } else {
      response.put("status", "success");
      response.put("message", "Organization " + newOrg.getName() + " created");
      String orgId = CodecUtils.encode((newOrg.getId() + ""));
      response.put("token", orgId);
    }
    return response;
  }
}
