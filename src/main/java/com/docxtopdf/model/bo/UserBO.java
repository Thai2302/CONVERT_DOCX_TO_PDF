package com.docxtopdf.model.bo;

import com.docxtopdf.model.bean.UserBean;
import com.docxtopdf.model.dao.UserDAO;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Business Object để xử lý logic nghiệp vụ liên quan đến User
 * Theo mô hình MVC - Model Layer (BO)
 */
public class UserBO {
    
    private UserDAO userDAO;
    
    public UserBO() {
        this.userDAO = new UserDAO();
    }
    
    /**
     * Đăng ký user mới
     * @return UserBean nếu thành công, null nếu thất bại
     */
    public UserBean registerUser(String username, String password, String email, String fullName) {
        // Validate input
        if (username == null || username.trim().isEmpty()) {
            System.err.println("Username không được rỗng");
            return null;
        }
        
        if (password == null || password.length() < 6) {
            System.err.println("Password phải có ít nhất 6 ký tự");
            return null;
        }
        
        if (email == null || !isValidEmail(email)) {
            System.err.println("Email không hợp lệ");
            return null;
        }
        
        if (fullName == null || fullName.trim().isEmpty()) {
            System.err.println("Họ tên không được rỗng");
            return null;
        }
        
        // Kiểm tra username đã tồn tại chưa
        if (userDAO.isUsernameExists(username)) {
            System.err.println("Username đã tồn tại: " + username);
            return null;
        }
        
        // Kiểm tra email đã tồn tại chưa
        if (userDAO.isEmailExists(email)) {
            System.err.println("Email đã tồn tại: " + email);
            return null;
        }
        
        // Hash password
        String hashedPassword = hashPassword(password);
        
        // Tạo UserBean
        UserBean user = new UserBean(username, hashedPassword, email, fullName);
        
        // Insert vào database
        boolean success = userDAO.insertUser(user);
        
        if (success) {
            // Lấy lại user từ database để có đầy đủ thông tin
            return userDAO.getUserByUsername(username);
        }
        
        return null;
    }
    
    /**
     * Đăng nhập user
     * @return UserBean nếu đăng nhập thành công, null nếu thất bại
     */
    public UserBean loginUser(String username, String password) {
        // Validate input
        if (username == null || username.trim().isEmpty()) {
            System.err.println("Username không được rỗng");
            return null;
        }
        
        if (password == null || password.trim().isEmpty()) {
            System.err.println("Password không được rỗng");
            return null;
        }
        
        // Lấy user từ database
        UserBean user = userDAO.getUserByUsername(username);
        
        if (user == null) {
            System.err.println("Không tìm thấy user: " + username);
            return null;
        }
        
        // Kiểm tra password
        if (checkPassword(password, user.getPassword())) {
            System.out.println("Đăng nhập thành công: " + username);
            return user;
        } else {
            System.err.println("Password không đúng cho user: " + username);
            return null;
        }
    }
    
    /**
     * Lấy thông tin user theo ID
     */
    public UserBean getUserById(int userId) {
        return userDAO.getUserById(userId);
    }
    
    /**
     * Lấy thông tin user theo username
     */
    public UserBean getUserByUsername(String username) {
        return userDAO.getUserByUsername(username);
    }
    
    /**
     * Cập nhật thông tin user
     */
    public boolean updateUser(UserBean user) {
        if (user == null || user.getUserId() <= 0) {
            return false;
        }
        return userDAO.updateUser(user);
    }
    
    /**
     * Đổi password
     */
    public boolean changePassword(int userId, String oldPassword, String newPassword) {
        // Validate
        if (newPassword == null || newPassword.length() < 6) {
            System.err.println("Password mới phải có ít nhất 6 ký tự");
            return false;
        }
        
        // Lấy user hiện tại
        UserBean user = userDAO.getUserById(userId);
        if (user == null) {
            return false;
        }
        
        // Kiểm tra password cũ
        if (!checkPassword(oldPassword, user.getPassword())) {
            System.err.println("Password cũ không đúng");
            return false;
        }
        
        // Hash password mới và cập nhật
        String hashedNewPassword = hashPassword(newPassword);
        return userDAO.updatePassword(userId, hashedNewPassword);
    }
    
    /**
     * Hash password bằng BCrypt
     */
    private String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(10));
    }
    
    /**
     * Kiểm tra password
     */
    private boolean checkPassword(String plainPassword, String hashedPassword) {
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            System.err.println("Lỗi khi kiểm tra password: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Validate email đơn giản
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        // Simple email validation
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
}
