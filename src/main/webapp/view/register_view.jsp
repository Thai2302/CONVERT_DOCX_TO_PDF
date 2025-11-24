<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng ký - DOCX to PDF Converter</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
    <div class="container">
        <h2>Đăng ký tài khoản</h2>
        
        <% String errorMessage = (String) request.getAttribute("errorMessage"); %>
        <% if (errorMessage != null) { %>
            <div class="alert alert-error">
                <%= errorMessage %>
            </div>
        <% } %>
        
        <form action="<%= request.getContextPath() %>/register" method="post">
            <div class="form-group">
                <label for="username">Username:</label>
                <input type="text" 
                       id="username" 
                       name="username" 
                       value="<%= request.getAttribute("username") != null ? request.getAttribute("username") : "" %>"
                       placeholder="Username (tối thiểu 3 ký tự)"
                       required
                       minlength="3">
            </div>
            
            <div class="form-group">
                <label for="email">Email:</label>
                <input type="email" 
                       id="email" 
                       name="email" 
                       value="<%= request.getAttribute("email") != null ? request.getAttribute("email") : "" %>"
                       placeholder="Email của bạn"
                       required>
            </div>
            
            <div class="form-group">
                <label for="fullName">Họ và tên:</label>
                <input type="text" 
                       id="fullName" 
                       name="fullName" 
                       value="<%= request.getAttribute("fullName") != null ? request.getAttribute("fullName") : "" %>"
                       placeholder="Họ và tên đầy đủ"
                       required>
            </div>
            
            <div class="form-group">
                <label for="password">Password:</label>
                <input type="password" 
                       id="password" 
                       name="password" 
                       placeholder="Password (tối thiểu 6 ký tự)"
                       required
                       minlength="6">
            </div>
            
            <div class="form-group">
                <label for="confirmPassword">Xác nhận Password:</label>
                <input type="password" 
                       id="confirmPassword" 
                       name="confirmPassword" 
                       placeholder="Nhập lại password"
                       required
                       minlength="6">
            </div>
            
            <button type="submit" class="btn">Đăng ký</button>
        </form>
        
        <div class="link-text">
            Đã có tài khoản? <a href="<%= request.getContextPath() %>/login">Đăng nhập</a>
        </div>
    </div>
    
    <script>
        // Validate password match
        document.querySelector('form').addEventListener('submit', function(e) {
            var password = document.getElementById('password').value;
            var confirmPassword = document.getElementById('confirmPassword').value;
            
            if (password !== confirmPassword) {
                e.preventDefault();
                alert('Password xác nhận không khớp!');
                return false;
            }
        });
    </script>
</body>
</html>
