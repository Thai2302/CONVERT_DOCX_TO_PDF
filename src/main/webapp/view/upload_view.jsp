<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.docxtopdf.model.bean.UserBean" %>
<%
    // Kiểm tra đăng nhập
    UserBean currentUser = (UserBean) session.getAttribute("currentUser");
    if (currentUser == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Upload File - DOCX to PDF Converter</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/dashboard.css">
    <style>
        .selected-files {
            margin-top: 20px;
            max-height: 300px;
            overflow-y: auto;
        }
        
        .file-item {
            background-color: #f8f9fa;
            padding: 10px;
            margin-bottom: 10px;
            border-radius: 5px;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        
        .file-item .file-name {
            flex-grow: 1;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }
        
        .file-item .file-size {
            margin-left: 10px;
            color: #666;
            font-size: 12px;
        }
        
        .file-item .remove-btn {
            margin-left: 10px;
            padding: 5px 10px;
            background-color: #dc3545;
            color: white;
            border: none;
            border-radius: 3px;
            cursor: pointer;
        }
        
        .upload-info {
            background-color: #d1ecf1;
            border: 1px solid #bee5eb;
            color: #0c5460;
            padding: 15px;
            border-radius: 5px;
            margin-bottom: 20px;
        }
        
        .upload-info ul {
            margin: 10px 0 0 20px;
        }
    </style>
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
            <a href="<%= request.getContextPath() %>/jobs" class="nav-tab">Danh sách Jobs</a>
            <a href="<%= request.getContextPath() %>/upload" class="nav-tab active">Upload File</a>
        </div>
        
        <!-- Success/Error Messages -->
        <% String successMessage = (String) request.getAttribute("successMessage");
           if (successMessage != null) { %>
            <div class="alert alert-success">
                <%= successMessage %>
            </div>
        <% } %>
        
        <% String errorMessage = (String) request.getAttribute("errorMessage");
           if (errorMessage != null) { %>
            <div class="alert alert-error">
                <%= errorMessage %>
            </div>
        <% } %>
        
        <!-- Upload Form -->
        <div class="content-card">
            <h2>Upload File DOCX để Convert sang PDF</h2>
            
            <div class="upload-info">
                <strong>📌 Lưu ý:</strong>
                <ul>
                    <li>Chỉ chấp nhận file có định dạng <strong>.docx</strong></li>
                    <li>Kích thước file tối đa: <strong>50MB</strong></li>
                    <li>Bạn có thể upload <strong>nhiều files cùng lúc</strong></li>
                    <li>File sẽ được xử lý trong hàng đợi và bạn có thể xem kết quả trong trang Dashboard</li>
                </ul>
            </div>
            
            <form action="<%= request.getContextPath() %>/upload" 
                  method="post" 
                  enctype="multipart/form-data"
                  id="uploadForm">
                
                <div class="upload-area" id="uploadArea">
                    <label class="upload-label" for="fileInput">
                        📤 Click để chọn file DOCX
                    </label>
                    <input type="file" 
                           id="fileInput" 
                           name="files" 
                           accept=".docx"
                           multiple
                           style="display: none;"
                           onchange="handleFileSelect(event)">
                    <div class="file-info">
                        <p>Hoặc kéo thả file vào đây</p>
                        <p style="font-size: 12px; color: #999; margin-top: 5px;">
                            Hỗ trợ nhiều files, mỗi file tối đa 50MB
                        </p>
                    </div>
                </div>
                
                <div id="selectedFiles" class="selected-files"></div>
                
                <button type="submit" class="btn-primary" id="uploadBtn" style="display: none;">
                    🚀 Upload và Convert
                </button>
            </form>
        </div>
    </div>
    
    <script>
        let selectedFiles = [];
        
        // Handle file selection
        function handleFileSelect(event) {
            const files = Array.from(event.target.files);
            
            files.forEach(file => {
                // Validate file type
                if (!file.name.toLowerCase().endsWith('.docx')) {
                    alert('File "' + file.name + '" không phải là file DOCX!');
                    return;
                }
                
                // Validate file size (50MB)
                if (file.size > 50 * 1024 * 1024) {
                    alert('File "' + file.name + '" vượt quá kích thước cho phép (50MB)!');
                    return;
                }
                
                // Add to selected files
                if (!selectedFiles.find(f => f.name === file.name)) {
                    selectedFiles.push(file);
                }
            });
            
            displaySelectedFiles();
        }
        
        // Display selected files
        function displaySelectedFiles() {
            const container = document.getElementById('selectedFiles');
            const uploadBtn = document.getElementById('uploadBtn');
            
            if (selectedFiles.length === 0) {
                container.innerHTML = '';
                uploadBtn.style.display = 'none';
                return;
            }
            
            container.innerHTML = '<h3 style="margin-bottom: 15px;">File đã chọn (' + selectedFiles.length + '):</h3>';
            
            selectedFiles.forEach((file, index) => {
                const fileItem = document.createElement('div');
                fileItem.className = 'file-item';
                
                const fileName = document.createElement('div');
                fileName.className = 'file-name';
                fileName.textContent = file.name;
                
                const fileSize = document.createElement('div');
                fileSize.className = 'file-size';
                fileSize.textContent = formatFileSize(file.size);
                
                const removeBtn = document.createElement('button');
                removeBtn.className = 'remove-btn';
                removeBtn.textContent = '✕';
                removeBtn.type = 'button';
                removeBtn.onclick = () => removeFile(index);
                
                fileItem.appendChild(fileName);
                fileItem.appendChild(fileSize);
                fileItem.appendChild(removeBtn);
                
                container.appendChild(fileItem);
            });
            
            uploadBtn.style.display = 'block';
        }
        
        // Remove file from selection
        function removeFile(index) {
            selectedFiles.splice(index, 1);
            displaySelectedFiles();
            
            // Reset file input
            document.getElementById('fileInput').value = '';
        }
        
        // Format file size
        function formatFileSize(bytes) {
            if (bytes < 1024) return bytes + ' B';
            if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB';
            return (bytes / (1024 * 1024)).toFixed(2) + ' MB';
        }
        
        // Form submission
        document.getElementById('uploadForm').addEventListener('submit', function(e) {
            if (selectedFiles.length === 0) {
                e.preventDefault();
                alert('Vui lòng chọn ít nhất một file để upload!');
                return false;
            }
            
            // Create new DataTransfer to update file input
            const dataTransfer = new DataTransfer();
            selectedFiles.forEach(file => {
                dataTransfer.items.add(file);
            });
            document.getElementById('fileInput').files = dataTransfer.files;
            
            // Disable button to prevent double submission
            const uploadBtn = document.getElementById('uploadBtn');
            uploadBtn.disabled = true;
            uploadBtn.textContent = '⏳ Đang upload...';
        });
        
        // Click to select files
        document.getElementById('uploadArea').addEventListener('click', function(e) {
            // Only trigger file input if clicking on the upload area itself, not the label
            if (e.target === this || e.target.classList.contains('file-info') || e.target.tagName === 'P') {
                document.getElementById('fileInput').click();
            }
        });
        
        // Drag and drop support
        const uploadArea = document.querySelector('.upload-area');
        
        uploadArea.addEventListener('dragover', (e) => {
            e.preventDefault();
            uploadArea.style.backgroundColor = '#f0f0f0';
        });
        
        uploadArea.addEventListener('dragleave', () => {
            uploadArea.style.backgroundColor = '';
        });
        
        uploadArea.addEventListener('drop', (e) => {
            e.preventDefault();
            e.stopPropagation();
            uploadArea.style.backgroundColor = '';
            
            const files = Array.from(e.dataTransfer.files);
            
            // Create a fake event object
            const fakeEvent = {
                target: {
                    files: files
                }
            };
            
            handleFileSelect(fakeEvent);
        });
    </script>
</body>
</html>
