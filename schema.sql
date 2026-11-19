CREATE DATABASE IF NOT EXISTS sims;
USE sims;

CREATE TABLE IF NOT EXISTS students (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    reg_no     VARCHAR(20)  NOT NULL UNIQUE,
    name       VARCHAR(100) NOT NULL,
    email      VARCHAR(100),
    phone      VARCHAR(15),
    department VARCHAR(50),
    year       INT,
    cgpa       DECIMAL(3,2)
);

INSERT IGNORE INTO students (reg_no, name, email, phone, department, year, cgpa)
VALUES ('AP24110011909', 'T. Jaideep', 'jaideep@example.com', '9999999999', 'CSE', 2, 8.50);
