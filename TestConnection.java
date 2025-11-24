import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestConnection {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/docx_to_pdf_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        String username = "root";
        String password = "23022005";
        
        try {
            // Load MySQL Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("✓ MySQL Driver loaded successfully");
            
            // Connect to database
            Connection conn = DriverManager.getConnection(url, username, password);
            System.out.println("✓ Connected to database successfully");
            
            // Test query
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM users");
            
            if (rs.next()) {
                int count = rs.getInt("count");
                System.out.println("✓ Found " + count + " users in database");
            }
            
            // Get user details
            rs = stmt.executeQuery("SELECT username, full_name FROM users");
            System.out.println("\n📋 Users in database:");
            while (rs.next()) {
                System.out.println("  - " + rs.getString("username") + " (" + rs.getString("full_name") + ")");
            }
            
            rs.close();
            stmt.close();
            conn.close();
            
            System.out.println("\n✅ All tests passed!");
            
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
