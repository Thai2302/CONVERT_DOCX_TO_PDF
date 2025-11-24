package com.docxtopdf.controller;

import com.docxtopdf.model.bean.ConversionJobBean;
import com.docxtopdf.model.bean.UserBean;
import com.docxtopdf.model.bo.ConversionJobBO;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

/**
 * Controller xử lý hiển thị danh sách jobs và trạng thái
 * Theo mô hình MVC - Controller Layer
 */
public class JobStatusController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private ConversionJobBO conversionJobBO;
    
    @Override
    public void init() throws ServletException {
        conversionJobBO = new ConversionJobBO();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
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
        
        // Lấy danh sách jobs của user
        List<ConversionJobBean> jobs = conversionJobBO.getJobsByUserId(userId);
        
        // Lấy thống kê
        ConversionJobBO.JobStatistics statistics = conversionJobBO.getJobStatistics(userId);
        
        // Set attributes
        request.setAttribute("jobs", jobs);
        request.setAttribute("statistics", statistics);
        
        // Forward đến dashboard view
        request.getRequestDispatcher("/view/dashboard_view.jsp").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
