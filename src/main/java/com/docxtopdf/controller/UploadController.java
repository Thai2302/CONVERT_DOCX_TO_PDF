package com.docxtopdf.controller;

import com.docxtopdf.model.bean.ConversionJobBean;
import com.docxtopdf.model.bean.UserBean;
import com.docxtopdf.model.bo.ConversionJobBO;
import com.docxtopdf.utils.FileUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import jakarta.servlet.annotation.MultipartConfig;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collection;

/**
 * Controller xử lý upload file DOCX
 * Theo mô hình MVC - Controller Layer
 */
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2, // 2MB
    maxFileSize = 1024 * 1024 * 50,      // 50MB
    maxRequestSize = 1024 * 1024 * 100   // 100MB (cho phép upload nhiều files)
)
public class UploadController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private ConversionJobBO conversionJobBO;
    private String uploadDirectory;
    
    @Override
    public void init() throws ServletException {
        conversionJobBO = new ConversionJobBO();
        
        // Lấy đường dẫn thư mục uploads
        uploadDirectory = getServletContext().getRealPath("/") + "..\\..\\uploads";
        
        // Tạo thư mục nếu chưa tồn tại
        FileUtil.createDirectoryIfNotExists(uploadDirectory);
        
        System.out.println("Upload directory: " + uploadDirectory);
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
        
        // Hiển thị trang upload
        request.getRequestDispatcher("/view/upload_view.jsp").forward(request, response);
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
        
        try {
            // Lấy tất cả các parts (files) được upload
            Collection<Part> parts = request.getParts();
            int successCount = 0;
            int failCount = 0;
            StringBuilder errorMessages = new StringBuilder();
            
            for (Part part : parts) {
                // Chỉ xử lý các part là file
                if (part.getName().equals("files") && part.getSize() > 0) {
                    String result = processFilePart(part, userId);
                    
                    if (result.startsWith("SUCCESS")) {
                        successCount++;
                    } else {
                        failCount++;
                        errorMessages.append(result).append("<br>");
                    }
                }
            }
            
            // Tạo thông báo kết quả
            if (successCount > 0) {
                String message = "Đã upload thành công " + successCount + " file(s)! ";
                message += "File(s) đang được xử lý trong hàng đợi.";
                request.setAttribute("successMessage", message);
            }
            
            if (failCount > 0) {
                String message = "Có " + failCount + " file(s) upload thất bại:<br>" + errorMessages.toString();
                request.setAttribute("errorMessage", message);
            }
            
            if (successCount == 0 && failCount == 0) {
                request.setAttribute("errorMessage", "Vui lòng chọn ít nhất một file để upload!");
            }
            
        } catch (Exception e) {
            System.err.println("Lỗi khi upload file: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("errorMessage", "Lỗi khi upload file: " + e.getMessage());
        }
        
        // Forward về trang upload
        request.getRequestDispatcher("/view/upload_view.jsp").forward(request, response);
    }
    
    /**
     * Xử lý từng file part
     */
    private String processFilePart(Part filePart, int userId) {
        String originalFilename = getFileName(filePart);
        
        try {
            // Validate file
            if (originalFilename == null || originalFilename.isEmpty()) {
                return "ERROR: Tên file không hợp lệ";
            }
            
            // Kiểm tra extension
            if (!FileUtil.isValidDocxFile(originalFilename)) {
                return "ERROR: File '" + originalFilename + "' không phải là file DOCX";
            }
            
            // Kiểm tra kích thước
            long fileSize = filePart.getSize();
            if (!FileUtil.isValidFileSize(fileSize)) {
                return "ERROR: File '" + originalFilename + "' vượt quá kích thước cho phép (50MB)";
            }
            
            // Tạo tên file unique
            String storedFilename = FileUtil.generateUniqueFilename(originalFilename);
            String uploadPath = uploadDirectory + File.separator + storedFilename;
            
            // Lưu file
            try (InputStream fileContent = filePart.getInputStream()) {
                Files.copy(fileContent, new File(uploadPath).toPath(), 
                          StandardCopyOption.REPLACE_EXISTING);
            }
            
            System.out.println("Đã lưu file: " + uploadPath);
            
            // Tạo job và thêm vào hàng đợi
            ConversionJobBean job = conversionJobBO.createAndSubmitJob(
                userId, originalFilename, storedFilename, fileSize, uploadPath);
            
            if (job != null) {
                return "SUCCESS: " + originalFilename;
            } else {
                // Xóa file nếu không tạo được job
                FileUtil.deleteFile(uploadPath);
                return "ERROR: Không thể tạo job cho file '" + originalFilename + "'";
            }
            
        } catch (Exception e) {
            System.err.println("Lỗi khi xử lý file " + originalFilename + ": " + e.getMessage());
            e.printStackTrace();
            return "ERROR: Lỗi khi xử lý file '" + originalFilename + "': " + e.getMessage();
        }
    }
    
    /**
     * Lấy tên file từ Part
     */
    private String getFileName(Part part) {
        String contentDisposition = part.getHeader("content-disposition");
        
        if (contentDisposition != null) {
            for (String token : contentDisposition.split(";")) {
                if (token.trim().startsWith("filename")) {
                    return token.substring(token.indexOf('=') + 1).trim()
                            .replace("\"", "");
                }
            }
        }
        return null;
    }
}
