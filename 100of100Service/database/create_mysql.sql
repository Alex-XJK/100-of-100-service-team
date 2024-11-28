DROP DATABASE IF EXISTS organization_management;

CREATE DATABASE organization_management;
USE organization_management;

DROP TABLE IF EXISTS employees;
DROP TABLE IF EXISTS departments;
DROP TABLE IF EXISTS organizations;

CREATE TABLE organizations (
    organization_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    details JSON
);

CREATE TABLE departments (
    department_id INT PRIMARY KEY,
    organization_id INT,
    name VARCHAR(255) NOT NULL,
    head_employee_id INT,
    FOREIGN KEY (organization_id) REFERENCES organizations(organization_id)
);

CREATE TABLE employees (
    employee_id INT PRIMARY KEY,
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

CREATE TABLE shifts (
    organization_id INT NOT NULL,
    employee_id INT NOT NULL,
    day_of_week INT NOT NULL CHECK (day_of_week BETWEEN 1 AND 7), -- DayOfWeek enum uses 1-7
    time_slot INT NOT NULL CHECK (time_slot BETWEEN 0 AND 2),
    PRIMARY KEY (organization_id, employee_id, day_of_week, time_slot),
    FOREIGN KEY (organization_id, employee_id) REFERENCES employees(organization_id, employee_id)
);

-- Add foreign key constraint for department head after employees table is created
ALTER TABLE departments
ADD CONSTRAINT fk_department_head
FOREIGN KEY (head_employee_id) REFERENCES employees(employee_id);

INSERT INTO organizations (organization_id, name, details) VALUES
(1, 'City Hospital', '{"founded": "1990-01-01", "industry": "Hospital"}'),
(2, 'Green Valley Medical Center', '{"founded": "2000-05-15", "industry": "Hospital"}');

-- Departments for clientId = 1
INSERT INTO departments (department_id, organization_id, name) VALUES
(10001, 1, 'Surgery', 10001),
(10002, 1, 'Cardiology', 10002),
(10003, 1, 'Emergency', 10004);

-- Departments for clientId = 2
INSERT INTO departments (department_id, organization_id, name) VALUES
(20001, 2, 'Rehabilitation', 20001),
(20002, 2, 'Pediatrics', 20002),
(20003, 2, 'Dermatology', 20003);

-- Employees for clientId = 1
INSERT INTO employees (employee_id, organization_id, department_id, name, position, hire_date, salary, performance, contact_info) VALUES
(10001, 1, 10001, 'Elizabeth Carter', 'General Surgeon', '2020-01-15', 220000.00, 94.00, '{"email": "ecarter@cityhospital.com", "phone": "555-123-4567"}'),
(10002, 1, 10002, 'Michael Nguyen', 'Cardiologist', '2019-05-01', 180000.00, 85.50, '{"email": "mnguyen@cityhospital.com", "phone": "555-987-6543"}'),
(10003, 1, 10001, 'Priya Singh', 'Internist', '2020-03-18', 150000.00, 88.25, '{"email": "psingh@cityhospital.com", "phone": "555-876-2345"}'),
(10004, 1, 10003, 'James Hwang', 'Physician', '2016-11-17', 190000.00, 96.00, '{"email": "jhwang@cityhospital.com", "phone": "555-321-7890"}'),
(10005, 1, 10001, 'Daniel Rivera', 'Orthopedic Surgeon', '2014-11-07', 250000.00, 91.00, '{"email": "drivera@cityhospital.com", "phone": "555-789-1234"}'),
(10006, 1, 10001, 'Sofia Martinez', 'General Surgeon', '2024-11-04', 120000.00, 70.00, '{"email": "smartinez@cityhospital.com", "phone": "555-234-6789"}'),
(10007, 1, 10002, 'Rajesh Kapoor', 'Cardiologist', '2022-07-04', 210000.00, 86.50, '{"email": "rkapoor@cityhospital.com", "phone": "555-456-7890"}'),
(10008, 1, 10002, 'Alice Zhang', 'Cardiac Electrophysiologist', '2023-01-11', 190000.00, 82.00, '{"email": "azhang@cityhospital.com", "phone": "555-678-1234"}'),
(10009, 1, 10003, 'Matthew Collins', 'Physician', '2024-04-16', 180000.00, 90.00, '{"email": "mcollins@cityhospital.com", "phone": "555-321-6547"}'),
(10010, 1, 10003, 'Olivia Chen', 'Trauma Specialist', '2018-11-30', 185000.00, 99.00, '{"email": "ochen@cityhospital.com", "phone": "555-987-4561"}'),
(10011, 1, 10003, 'Henry Walsh', 'Physician', '2020-11-06', 175000.00, 89.00, '{"email": "hwalsh@cityhospital.com", "phone": "555-543-7890"}'),
(10012, 1, 10001, 'Lucas Peterson', 'Vascular Surgeon', '2020-12-28', 200000.00, 84.00, '{"email": "lpeterson@cityhospital.com", "phone": "555-654-8901"}'),
(10013, 1, 10002, 'Ayesha Malik', 'Cardiologist', '2013-11-06', 155000.00, 70.00, '{"email": "amalik@cityhospital.com", "phone": "555-123-6789"}'),
(10014, 1, 10003, 'Ethan Blake', 'Physician', '2022-05-15', 190000.00, 90.00, '{"email": "eblake@cityhospital.com", "phone": "555-432-5678"}'),
(10015, 1, 10003, 'Mia Rodriguez', 'Critical Care Specialist', '2017-11-30', 195000.00, 90.00, '{"email": "mrodriguez@cityhospital.com", "phone": "555-678-4321"}');

-- Employees for clientId = 2
INSERT INTO employees (employee_id, organization_id, department_id, name, position, hire_date, salary, performance, contact_info) VALUES
(20001, 2, 20001, 'Emily Brown', 'Rehabilitation Specialist', '2018-03-10', 200000.00, 92.75, '{"email": "ebrown@greenvalley.com", "phone": "332-654-3210"}'),
(20002, 2, 20002, 'Ahmed Patel', 'Pediatrician', '2021-07-22', 160000.00, 80.00, '{"email": "apatel@greenvalley.com", "phone": "332-789-4321"}'),
(20003, 2, 20003, 'Sarah Lee', 'Dermatologist', '2021-08-11', 170000.00, 74.00, '{"email": "slee@greenvalley.com", "phone": "332-234-5678"}');

-- Insert shift assignments for City Hospital (organization_id = 1)

-- John Doe's shifts (employee_id = 10001)
INSERT INTO shifts (organization_id, employee_id, day_of_week, time_slot) VALUES
(1, 10001, 1, 0),  -- Monday morning (9-12)
(1, 10001, 3, 1),  -- Wednesday afternoon (2-5)
(1, 10001, 5, 2);  -- Friday evening (6-9)

-- Jane Smith's shifts (employee_id = 10002)
INSERT INTO shifts (organization_id, employee_id, day_of_week, time_slot) VALUES
(1, 10002, 2, 0),  -- Tuesday morning (9-12)
(1, 10002, 4, 1),  -- Thursday afternoon (2-5)
(1, 10002, 5, 0);  -- Friday morning (9-12)

-- Tom Brown's shifts (employee_id = 10003)
INSERT INTO shifts (organization_id, employee_id, day_of_week, time_slot) VALUES
(1, 10003, 1, 1),  -- Monday afternoon (2-5)
(1, 10003, 3, 0),  -- Wednesday morning (9-12)
(1, 10003, 5, 1);  -- Friday afternoon (2-5)

-- Insert shift assignments for Green Valley Medical Center (organization_id = 2)

-- Alice Johnson's shifts (employee_id = 20001)
INSERT INTO shifts (organization_id, employee_id, day_of_week, time_slot) VALUES
(2, 20001, 1, 0),  -- Monday morning (9-12)
(2, 20001, 2, 1),  -- Tuesday afternoon (2-5)
(2, 20001, 4, 2);  -- Thursday evening (6-9)

-- Bob Brown's shifts (employee_id = 20002)
INSERT INTO shifts (organization_id, employee_id, day_of_week, time_slot) VALUES
(2, 20002, 2, 0),  -- Tuesday morning (9-12)
(2, 20002, 3, 1),  -- Wednesday afternoon (2-5)
(2, 20002, 5, 2);  -- Friday evening (6-9)

-- Update department heads
UPDATE departments SET head_employee_id = 10001 WHERE department_id = 10001;
UPDATE departments SET head_employee_id = 10002 WHERE department_id = 10002;
UPDATE departments SET head_employee_id = 10004 WHERE department_id = 10003;
UPDATE departments SET head_employee_id = 20001 WHERE department_id = 20001;
UPDATE departments SET head_employee_id = 20002 WHERE department_id = 20002;
UPDATE departments SET head_employee_id = 20003 WHERE department_id = 20003;