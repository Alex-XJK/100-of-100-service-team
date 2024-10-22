package dev.coms4156.project;

import java.util.ArrayList;
import java.util.List;

/**
 * An organization composite in the HR system.
 * Just an abstract class that implements the OrganizationComponent interface.
 */
public abstract class OrganizationComposite implements OrganizationComponent {
  protected final int id;
  protected final String name;
  protected final List<OrganizationComponent> children;
  protected String typeName;

  /**
   * Constructs an organization composite with the given ID and name.
   *
   * @param id the ID of the organization composite
   * @param name the name of the organization composite
   */
  public OrganizationComposite(int id, String name) {
    this.id = id;
    this.name = name;
    this.children = new ArrayList<>();
    this.typeName = "Abstract";
  }

  @Override
  public int getId() {
    return this.id;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public String getTypeName() {
    return this.typeName;
  }

  @Override
  public List<OrganizationComponent> getChildren() {
    return this.children;
  }

  /**
   * Adds a child component to the organization.
   *
   * @param organizationComponent the child component to add
   * @return true if the child component was added, false otherwise
   */
  boolean add(OrganizationComponent organizationComponent) {
    return this.children.add(organizationComponent);
  }

  /**
   * Removes a child component from the organization.
   *
   * @param organizationComponent the child component to remove
   * @return true if the child component was removed, false otherwise
   */
  boolean remove(OrganizationComponent organizationComponent) {
    return this.children.remove(organizationComponent);
  }

  /**
   * Returns the number of children of the organization component.
   *
   * @return the number of children of the organization component
   */
  int getNumChildren() {
    return this.children.size();
  }

}
