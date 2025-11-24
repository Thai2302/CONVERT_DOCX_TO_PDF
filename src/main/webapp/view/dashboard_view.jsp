<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.docxtopdf.model.bean.UserBean" %>
<%@ page import="com.docxtopdf.model.bean.ConversionJobBean" %>
<%@ page import="com.docxtopdf.model.bo.ConversionJobBO" %>
<%@ page import="java.util.List" %>
<%
    // Kiểm tra đăng nhập
    UserBean currentUser = (UserBean) session.getAttribute("currentUser");
    if (currentUser == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
    
    // Lấy danh sách jobs và statistics từ request
    List<ConversionJobBean> jobs = (List<ConversionJobBean>) request.getAttribute("jobs");
    ConversionJobBO.JobStatistics statistics = (ConversionJobBO.JobStatistics) request.getAttribute("statistics");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard - DOCX to PDF Converter</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/dashboard.css">
</head>
<body>
    <!-- Header -->
    <div class="header">
        <div class="header-content">
            <h1>📄 DOCX to PDF Converter</h1>
            <div class="user-info">
                <span>Xin chào, <strong><%= currentUser.getFullName() %></strong></span>
                <a href="<%= request.getContextPath() %>/logout" class="btn-logout">Đăng xuất</a>
            </div>
        </div>
    </div>
    
    <!-- Main Container -->
    <div class="main-container">
        <!-- Navigation Tabs -->
        <div class="nav-tabs">
            <a href="<%= request.getContextPath() %>/jobs" class="nav-tab active">Danh sách Jobs</a>
            <a href="<%= request.getContextPath() %>/upload" class="nav-tab">Upload File</a>
        </div>
        
        <!-- Success/Error Messages -->
        <% String successMessage = (String) session.getAttribute("successMessage");
           if (successMessage != null) { 
               session.removeAttribute("successMessage"); %>
            <div class="alert alert-success">
                <%= successMessage %>
            </div>
        <% } %>
        
        <% String errorMessage = (String) session.getAttribute("errorMessage");
           if (errorMessage != null) { 
               session.removeAttribute("errorMessage"); %>
            <div class="alert alert-error">
                <%= errorMessage %>
            </div>
        <% } %>
        
        <!-- Statistics -->
        <div class="statistics">
            <div class="stat-card">
                <h3>Tổng số Jobs</h3>
                <div class="number"><%= statistics.getTotalJobs() %></div>
            </div>
            <div class="stat-card">
                <h3>Đang chờ</h3>
                <div class="number" style="color: #ffc107;"><%= statistics.getPendingJobs() %></div>
            </div>
            <div class="stat-card">
                <h3>Đang xử lý</h3>
                <div class="number" style="color: #17a2b8;"><%= statistics.getProcessingJobs() %></div>
            </div>
            <div class="stat-card">
                <h3>Hoàn thành</h3>
                <div class="number" style="color: #28a745;"><%= statistics.getCompletedJobs() %></div>
            </div>
            <div class="stat-card">
                <h3>Thất bại</h3>
                <div class="number" style="color: #dc3545;"><%= statistics.getFailedJobs() %></div>
            </div>
        </div>
        
        <!-- Jobs Table -->
        <div class="content-card">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
                <h2>Danh sách Conversion Jobs</h2>
                <button onclick="window.location.reload()" class="btn-primary">🔄 Làm mới</button>
            </div>
            
            <% if (jobs != null && jobs.size() > 0) { %>
                <div class="table-container">
                    <table>
                        <thead>
                            <tr>
                                <th>Job ID</th>
                                <th>Tên file gốc</th>
                                <th>Kích thước</th>
                                <th>Trạng thái</th>
                                <th>Thời gian tạo</th>
                                <th>Thời gian xử lý</th>
                                <th>Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% for (ConversionJobBean job : jobs) { 
                                String statusClass = "";
                                switch(job.getStatus()) {
                                    case PENDING: statusClass = "status-pending"; break;
                                    case PROCESSING: statusClass = "status-processing"; break;
                                    case COMPLETED: statusClass = "status-completed"; break;
                                    case FAILED: statusClass = "status-failed"; break;
                                }
                            %>
                                <tr>
                                    <td>#<%= job.getJobId() %></td>
                                    <td><%= job.getOriginalFilename() %></td>
                                    <td><%= job.getFormattedFileSize() %></td>
                                    <td>
                                        <span class="status-badge <%= statusClass %>">
                                            <%= job.getStatus() %>
                                        </span>
                                    </td>
                                    <td><%= job.getCreatedAt() != null ? new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(job.getCreatedAt()) : "N/A" %></td>
                                    <td><%= job.getProcessingTime() %></td>
                                    <td>
                                        <% if (job.getStatus() == ConversionJobBean.JobStatus.COMPLETED) { %>
                                            <a href="<%= request.getContextPath() %>/download?jobId=<%= job.getJobId() %>" 
                                               class="btn-success">📥 Download</a>
                                        <% } %>
                                        
                                        <% if (job.getStatus() != ConversionJobBean.JobStatus.PROCESSING) { %>
                                            <form action="<%= request.getContextPath() %>/deleteJob" 
                                                  method="post" 
                                                  style="display: inline;"
                                                  onsubmit="return confirm('Bạn có chắc muốn xóa job này?');">
                                                <input type="hidden" name="jobId" value="<%= job.getJobId() %>">
                                                <button type="submit" class="btn-danger">🗑️ Xóa</button>
                                            </form>
                                        <% } %>
                                        
                                        <% if (job.getStatus() == ConversionJobBean.JobStatus.FAILED && job.getErrorMessage() != null) { %>
                                            <br><small style="color: #dc3545;"><%= job.getErrorMessage() %></small>
                                        <% } %>
                                    </td>
                                </tr>
                            <% } %>
                        </tbody>
                    </table>
                </div>
            <% } else { %>
                <div class="empty-state">
                    <div style="font-size: 64px; margin-bottom: 20px;">📭</div>
                    <p>Bạn chưa có job nào. Hãy <a href="<%= request.getContextPath() %>/upload" style="color: #667eea;">upload file DOCX</a> để bắt đầu!</p>
                </div>
            <% } %>
        </div>
    </div>
    
    <script>
        // Auto refresh every 5 seconds
        setTimeout(function() {
            window.location.reload();
        }, 5000);
    </script>
</body>
</html>
