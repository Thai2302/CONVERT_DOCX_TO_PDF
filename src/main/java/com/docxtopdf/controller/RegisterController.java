package com.docxtopdf.controller;

import com.docxtopdf.model.bean.UserBean;
import com.docxtopdf.model.bo.UserBO;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Controller xử lý đăng ký user mới
 * Theo mô hình MVC - Controller Layer
 */
public class RegisterController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private UserBO userBO;
    
    @Override
    public void init() throws ServletException {
        userBO = new UserBO();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Hiển thị trang đăng ký
        request.getRequestDispatcher("/view/register_view.jsp").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Set encoding
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        // Lấy thông tin từ form
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        String email = request.getParameter("email");
        String fullName = request.getParameter("fullName");
        
        // Validate input
        StringBuilder errorMessage = new StringBuilder();
        
        if (username == null || username.trim().isEmpty()) {
            errorMessage.append("Username không được rỗng!<br>");
        } else if (username.length() < 3) {
            errorMessage.append("Username phải có ít nhất 3 ký tự!<br>");
        }
        
        if (password == null || password.isEmpty()) {
            errorMessage.append("Password không được rỗng!<br>");
        } else if (password.length() < 6) {
            errorMessage.append("Password phải có ít nhất 6 ký tự!<br>");
        }
        
        if (confirmPassword == null || !confirmPassword.equals(password)) {
            errorMessage.append("Password xác nhận không khớp!<br>");
        }
        
        if (email == null || email.trim().isEmpty()) {
            errorMessage.append("Email không được rỗng!<br>");
        } else if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            errorMessage.append("Email không hợp lệ!<br>");
        }
        
        if (fullName == null || fullName.trim().isEmpty()) {
            errorMessage.append("Họ tên không được rỗng!<br>");
        }
        
        // Nếu có lỗi, hiển thị lại form
        if (errorMessage.length() > 0) {
            request.setAttribute("errorMessage", errorMessage.toString());
            request.setAttribute("username", username);
            request.setAttribute("email", email);
            request.setAttribute("fullName", fullName);
            request.getRequestDispatcher("/view/register_view.jsp").forward(request, response);
            return;
        }
        
        // Thực hiện đăng ký
        UserBean newUser = userBO.registerUser(username.trim(), password, email.trim(), fullName.trim());
        
        if (newUser != null) {
            // Đăng ký thành công
            System.out.println("User " + username + " đã đăng ký thành công");
            request.setAttribute("successMessage", "Đăng ký thành công! Vui lòng đăng nhập.");
            request.getRequestDispatcher("/view/login_view.jsp").forward(request, response);
        } else {
            // Đăng ký thất bại
            request.setAttribute("errorMessage", "Đăng ký thất bại! Username hoặc Email đã tồn tại.");
            request.setAttribute("username", username);
            request.setAttribute("email", email);
            request.setAttribute("fullName", fullName);
            request.getRequestDispatcher("/view/register_view.jsp").forward(request, response);
        }
    }
}
