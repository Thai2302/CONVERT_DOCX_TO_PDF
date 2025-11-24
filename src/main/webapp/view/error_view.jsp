<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Lỗi - DOCX to PDF Converter</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
    <style>
        .error-container {
            max-width: 600px;
            text-align: center;
        }
        
        .error-icon {
            font-size: 80px;
            margin-bottom: 20px;
        }
        
        .error-code {
            font-size: 48px;
            color: #dc3545;
            margin-bottom: 10px;
        }
        
        .error-message {
            font-size: 18px;
            color: #666;
            margin-bottom: 30px;
        }
    </style>
</head>
<body>
    <div class="container error-container">
        <div class="error-icon">😞</div>
        <div class="error-code">
            <%= response.getStatus() %>
        </div>
        <div class="error-message">
            <% 
                int statusCode = response.getStatus();
                String message = "";
                
                if (statusCode == 404) {
                    message = "Trang bạn tìm kiếm không tồn tại!";
                } else if (statusCode == 500) {
                    message = "Đã xảy ra lỗi nội bộ server!";
                } else {
                    message = "Đã xảy ra lỗi!";
                }
            %>
            <%= message %>
        </div>
        
        <a href="<%= request.getContextPath() %>/jobs" class="btn">
            🏠 Về trang chủ
        </a>
    </div>
</body>
</html>
