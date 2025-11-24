package com.docxtopdf.controller;

import com.docxtopdf.model.bean.ConversionJobBean;
import com.docxtopdf.model.bean.ConversionJobBean.JobStatus;
import com.docxtopdf.model.bean.UserBean;
import com.docxtopdf.model.bo.ConversionJobBO;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Controller xử lý download file PDF đã convert
 * Theo mô hình MVC - Controller Layer
 */
public class DownloadController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private ConversionJobBO conversionJobBO;
    
    @Override
    public void init() throws ServletException {
        conversionJobBO = new ConversionJobBO();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
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
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Job ID không được cung cấp");
            return;
        }
        
        try {
            int jobId = Integer.parseInt(jobIdStr);
            
            // Lấy thông tin job
            ConversionJobBean job = conversionJobBO.getJobById(jobId);
            
            if (job == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy job");
                return;
            }
            
            // Kiểm tra quyền: chỉ user sở hữu job mới có thể download
            if (job.getUserId() != userId) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền download file này");
                return;
            }
            
            // Kiểm tra trạng thái job
            if (job.getStatus() != JobStatus.COMPLETED) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, 
                    "File chưa được convert hoặc đang được xử lý");
                return;
            }
            
            // Kiểm tra file tồn tại
            String filePath = job.getConvertedPath();
            File file = new File(filePath);
            
            if (!file.exists() || !file.isFile()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "File không tồn tại");
                return;
            }
            
            // Tạo tên file download từ tên gốc, chỉ thay đổi extension
            String originalFilename = job.getOriginalFilename();
            String downloadFilename = originalFilename;
            
            // Thay đổi extension từ .docx sang .pdf
            if (originalFilename.toLowerCase().endsWith(".docx")) {
                downloadFilename = originalFilename.substring(0, originalFilename.length() - 5) + ".pdf";
            } else {
                downloadFilename = originalFilename + ".pdf";
            }
            
            // Set response headers
            response.setContentType("application/pdf");
            response.setContentLengthLong(file.length());
            response.setHeader("Content-Disposition", 
                "attachment; filename=\"" + downloadFilename + "\"");
            
            // Stream file đến client
            try (FileInputStream fis = new FileInputStream(file);
                 OutputStream os = response.getOutputStream()) {
                
                byte[] buffer = new byte[4096];
                int bytesRead;
                
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                
                os.flush();
            }
            
            System.out.println("User " + userId + " đã download file: " + job.getConvertedFilename());
            
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Job ID không hợp lệ");
        } catch (Exception e) {
            System.err.println("Lỗi khi download file: " + e.getMessage());
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi khi download file");
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
