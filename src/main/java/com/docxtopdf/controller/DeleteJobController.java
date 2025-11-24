package com.docxtopdf.controller;

import com.docxtopdf.model.bean.UserBean;
import com.docxtopdf.model.bo.ConversionJobBO;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Controller xử lý xóa job
 * Theo mô hình MVC - Controller Layer
 */
public class DeleteJobController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private ConversionJobBO conversionJobBO;
    
    @Override
    public void init() throws ServletException {
        conversionJobBO = new ConversionJobBO();
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Set encoding
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        // Kiểm tra đăng nhập
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        UserBean currentUser = (UserBean) session.getAttribute("currentUser");
        int userId = currentUser.getUserId();
        
        // Lấy job ID
        String jobIdStr = request.getParameter("jobId");
        
        if (jobIdStr == null || jobIdStr.isEmpty()) {
            request.setAttribute("errorMessage", "Job ID không được cung cấp");
            response.sendRedirect(request.getContextPath() + "/jobs");
            return;
        }
        
        try {
            int jobId = Integer.parseInt(jobIdStr);
            
            // Xóa job
            boolean success = conversionJobBO.deleteJob(jobId, userId);
            
            if (success) {
                request.getSession().setAttribute("successMessage", "Đã xóa job thành công!");
            } else {
                request.getSession().setAttribute("errorMessage", "Không thể xóa job!");
            }
            
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("errorMessage", "Job ID không hợp lệ");
        } catch (Exception e) {
            System.err.println("Lỗi khi xóa job: " + e.getMessage());
            e.printStackTrace();
            request.getSession().setAttribute("errorMessage", "Lỗi khi xóa job: " + e.getMessage());
        }
        
        // Redirect về dashboard
        response.sendRedirect(request.getContextPath() + "/jobs");
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Redirect về dashboard nếu dùng GET
        response.sendRedirect(request.getContextPath() + "/jobs");
    }
}
