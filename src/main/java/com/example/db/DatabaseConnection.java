package com.example.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    
    private static final String URL = "jdbc:mysql://localhost:3306/person_db";
    private static final String USER = "root"; // Change if needed
    private static final String PASSWORD = "1234"; // Add your MySQL password here
    
    static {
        try {
            // Load MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC Driver not found", e);
        }
    }
    
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
    
    // Test method to create database and table if they don't exist
    public static void main(String[] args) {
        System.out.println("Testing database connection...");
        
        // First connect without specifying database to create it
        String baseUrl = "jdbc:mysql://localhost:3306/";
        try (Connection conn = DriverManager.getConnection(baseUrl, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {
            
            System.out.println("✓ Connected to MySQL server");
            
            // Create database
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS person_db");
            System.out.println("✓ Database 'person_db' created/exists");
            
            // Use the database
            stmt.executeUpdate("USE person_db");
            
            // Create table
            String createTable = "CREATE TABLE IF NOT EXISTS persons (" +
                "id INT PRIMARY KEY AUTO_INCREMENT, " +
                "name VARCHAR(100) NOT NULL, " +
                "age INT NOT NULL)";
            stmt.executeUpdate(createTable);
            System.out.println("✓ Table 'persons' created/exists");
            
            // Insert sample data if table is empty
            stmt.executeUpdate("INSERT INTO persons (name, age) " +
                "SELECT 'Ahmed', 21 WHERE NOT EXISTS (SELECT 1 FROM persons)");
            stmt.executeUpdate("INSERT INTO persons (name, age) " +
                "SELECT 'Sara', 23 WHERE NOT EXISTS (SELECT 1 FROM persons WHERE name='Sara')");
            System.out.println("✓ Sample data inserted");
            
            System.out.println("\n✅ Database setup complete! You can now see 'person_db' in MySQL Workbench.");
            
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
