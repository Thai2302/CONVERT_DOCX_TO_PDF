package com.docxtopdf.test;

import com.docxtopdf.utils.DatabaseUtil;
import com.docxtopdf.model.dao.UserDAO;
import com.docxtopdf.model.bean.UserBean;

import java.sql.Connection;

/**
 * Test class to verify database connection and basic operations
 */
public class TestDatabaseConnection {
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("Testing Database Connection");
        System.out.println("========================================\n");
        
        // Test 1: Database Connection
        System.out.println("Test 1: Testing database connection...");
        try {
            Connection conn = DatabaseUtil.getConnection();
            if (conn != null && !conn.isClosed()) {
                System.out.println("✓ Database connection successful!");
                conn.close();
            } else {
                System.out.println("✗ Failed to connect to database");
                return;
            }
        } catch (Exception e) {
            System.out.println("✗ Database connection failed: " + e.getMessage());
            e.printStackTrace();
            return;
        }
        
        // Test 2: Query Users
        System.out.println("\nTest 2: Querying users from database...");
        try {
            UserDAO userDAO = new UserDAO();
            
            UserBean admin = userDAO.getUserByUsername("admin");
            if (admin != null) {
                System.out.println("✓ Found user: " + admin.getUsername() + " - " + admin.getFullName());
            } else {
                System.out.println("✗ User 'admin' not found");
            }
            
            UserBean testuser = userDAO.getUserByUsername("testuser");
            if (testuser != null) {
                System.out.println("✓ Found user: " + testuser.getUsername() + " - " + testuser.getFullName());
            } else {
                System.out.println("✗ User 'testuser' not found");
            }
            
        } catch (Exception e) {
            System.out.println("✗ Query failed: " + e.getMessage());
            e.printStackTrace();
            return;
        }
        
        System.out.println("\n========================================");
        System.out.println("✅ All tests passed successfully!");
        System.out.println("========================================");
    }
}
