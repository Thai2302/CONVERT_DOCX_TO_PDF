package com.docxtopdf.utils;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Utility class để quản lý kết nối database
 * Theo mô hình MVC - Utils Layer
 */
public class DatabaseUtil {
    private static String DB_URL;
    private static String DB_USERNAME;
    private static String DB_PASSWORD;
    private static String DB_DRIVER;
    
    static {
        loadDatabaseConfig();
    }
    
    /**
     * Load cấu hình database từ file properties
     */
    private static void loadDatabaseConfig() {
        Properties props = new Properties();
        try (InputStream input = DatabaseUtil.class.getClassLoader()
                .getResourceAsStream("database.properties")) {
            
            if (input == null) {
                System.err.println("Không tìm thấy file database.properties");
                // Sử dụng giá trị mặc định
                DB_DRIVER = "com.mysql.cj.jdbc.Driver";
                DB_URL = "jdbc:mysql://localhost:3306/docx_to_pdf_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
                DB_USERNAME = "root";
                DB_PASSWORD = "";
                return;
            }
            
            props.load(input);
            
            DB_DRIVER = props.getProperty("db.driver");
            DB_URL = props.getProperty("db.url");
            DB_USERNAME = props.getProperty("db.username");
            DB_PASSWORD = props.getProperty("db.password");
            
            // Load MySQL Driver
            try {
                Class.forName(DB_DRIVER);
                System.out.println("MySQL JDBC Driver đã được load thành công");
            } catch (ClassNotFoundException e) {
                System.err.println("Không thể load MySQL JDBC Driver: " + e.getMessage());
                e.printStackTrace();
            }
            
        } catch (IOException e) {
            System.err.println("Lỗi khi đọc file database.properties: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Lấy connection đến database
     */
    public static Connection getConnection() throws SQLException {
        try {
            Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
            return conn;
        } catch (SQLException e) {
            System.err.println("Lỗi khi kết nối database: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Đóng connection
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Lỗi khi đóng connection: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Test connection
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Test connection thất bại: " + e.getMessage());
            return false;
        }
    }
}
