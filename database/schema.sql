-- Database setup for person-backend1
-- Run this in MySQL Workbench or your MySQL client

-- Create database
CREATE DATABASE IF NOT EXISTS person_db;
USE person_db;

-- Create persons table
CREATE TABLE IF NOT EXISTS persons (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    age INT NOT NULL
);

-- Insert initial data
INSERT INTO persons (name, age) VALUES 
    ('Ahmed', 21),
    ('Sara', 23);

-- Verify
SELECT * FROM persons;
