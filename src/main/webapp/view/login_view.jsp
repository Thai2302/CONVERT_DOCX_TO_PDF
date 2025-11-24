<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng nhập - DOCX to PDF Converter</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
    <div class="container">
        <h2>Đăng nhập</h2>
        
        <% String successMessage = (String) request.getAttribute("successMessage"); %>
        <% if (successMessage != null) { %>
            <div class="alert alert-success">
                <%= successMessage %>
            </div>
        <% } %>
        
        <% String errorMessage = (String) request.getAttribute("errorMessage"); %>
        <% if (errorMessage != null) { %>
            <div class="alert alert-error">
                <%= errorMessage %>
            </div>
        <% } %>
        
        <form action="<%= request.getContextPath() %>/login" method="post">
            <div class="form-group">
                <label for="username">Username:</label>
                <input type="text" 
                       id="username" 
                       name="username" 
                       value="<%= request.getAttribute("username") != null ? request.getAttribute("username") : "" %>"
                       placeholder="Nhập username của bạn"
                       required>
            </div>
            
            <div class="form-group">
                <label for="password">Password:</label>
                <input type="password" 
                       id="password" 
                       name="password" 
                       placeholder="Nhập password của bạn"
                       required>
            </div>
            
            <button type="submit" class="btn">Đăng nhập</button>
        </form>
        
        <div class="link-text">
            Chưa có tài khoản? <a href="<%= request.getContextPath() %>/register">Đăng ký ngay</a>
        </div>
        
        <div class="link-text" style="margin-top: 10px; font-size: 12px;">
            <strong>Demo accounts:</strong><br>
            admin / test123 hoặc testuser / test123
        </div>
    </div>
</body>
</html>
