DROP DATABASE IF EXISTS demo_db;

CREATE DATABASE demo_db;
USE demo_db;

DROP TABLE IF EXISTS employees;
DROP TABLE IF EXISTS departments;
DROP TABLE IF EXISTS organizations;

CREATE TABLE organizations (
    organization_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    details JSON
);

CREATE TABLE departments (
    department_id INT PRIMARY KEY AUTO_INCREMENT,
    organization_id INT,
    name VARCHAR(255) NOT NULL,
    head_employee_id INT,
    FOREIGN KEY (organization_id) REFERENCES organizations(organization_id)
);

CREATE TABLE employees (
    employee_id INT PRIMARY KEY AUTO_INCREMENT,
    organization_id INT,
    department_id INT,
    name VARCHAR(255) NOT NULL,
    position VARCHAR(100),
    hire_date DATE,
    salary DECIMAL(10, 2),
    performance DECIMAL(5, 2),
    contact_info JSON,
    FOREIGN KEY (organization_id) REFERENCES organizations(organization_id),
    FOREIGN KEY (department_id) REFERENCES departments(department_id)
);

-- Insert organizations
INSERT INTO organizations (organization_id, name, details) VALUES
(1, 'Acme Corp', '{"founded": "1990-01-01", "industry": "Technology"}'),
(2, 'Beta Inc', '{"founded": "2000-05-15", "industry": "Finance"}');

-- Insert departments
INSERT INTO departments (department_id, organization_id, name) VALUES
(1, 1, 'Engineering'),
(2, 1, 'Marketing');

-- Insert employees
INSERT INTO employees (employee_id, organization_id, department_id, name, position, hire_date, salary, performance, contact_info) VALUES
(1, 1, 1, 'John Doe', 'Software Engineer', '2020-01-15', 75000.00, 90.00, '{"email": "john.doe@acme.com", "phone": "123-456-7890"}'),
(2, 1, 2, 'Jane Smith', 'Marketing Manager', '2019-05-01', 80000.00, 85.50, '{"email": "jane.smith@acme.com", "phone": "098-765-4321"}'),
(3, 1, 1, 'Tom Brown', 'Software Engineer', '2021-03-20', 70000.00, 88.25, '{"email": "tom.brown@acme.com", "phone": "123-456-7890"}');

-- Update department heads
UPDATE departments SET head_employee_id = 1 WHERE department_id = 1;
UPDATE departments SET head_employee_id = 2 WHERE department_id = 2;
