package com.docxtopdf.controller;

import com.docxtopdf.model.bean.UserBean;
import com.docxtopdf.model.bo.UserBO;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Controller xử lý đăng nhập
 * Theo mô hình MVC - Controller Layer
 */
public class LoginController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private UserBO userBO;
    
    @Override
    public void init() throws ServletException {
        userBO = new UserBO();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Hiển thị trang login
        request.getRequestDispatcher("/view/login_view.jsp").forward(request, response);
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
        
        // Validate input
        if (username == null || username.trim().isEmpty() || 
            password == null || password.trim().isEmpty()) {
            request.setAttribute("errorMessage", "Vui lòng nhập đầy đủ thông tin!");
            request.getRequestDispatcher("/view/login_view.jsp").forward(request, response);
            return;
        }
        
        // Thực hiện đăng nhập
        UserBean user = userBO.loginUser(username.trim(), password);
        
        if (user != null) {
            // Đăng nhập thành công
            HttpSession session = request.getSession();
            session.setAttribute("currentUser", user);
            session.setAttribute("userId", user.getUserId());
            session.setAttribute("username", user.getUsername());
            session.setMaxInactiveInterval(30 * 60); // 30 phút
            
            System.out.println("User " + username + " đã đăng nhập thành công");
            
            // Redirect đến dashboard
            response.sendRedirect(request.getContextPath() + "/jobs");
        } else {
            // Đăng nhập thất bại
            request.setAttribute("errorMessage", "Username hoặc password không đúng!");
            request.setAttribute("username", username);
            request.getRequestDispatcher("/view/login_view.jsp").forward(request, response);
        }
    }
}
