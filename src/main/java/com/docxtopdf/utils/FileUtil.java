package com.docxtopdf.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Utility class để xử lý các thao tác với file
 * Theo mô hình MVC - Utils Layer
 */
public class FileUtil {
    
    // Kích thước file tối đa: 50MB
    public static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB in bytes
    
    // Các extension được phép
    private static final String[] ALLOWED_EXTENSIONS = {".docx", ".DOCX"};
    
    /**
     * Kiểm tra file có phải là DOCX không
     */
    public static boolean isValidDocxFile(String filename) {
        if (filename == null || filename.isEmpty()) {
            return false;
        }
        
        for (String ext : ALLOWED_EXTENSIONS) {
            if (filename.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Kiểm tra kích thước file có hợp lệ không
     */
    public static boolean isValidFileSize(long fileSize) {
        return fileSize > 0 && fileSize <= MAX_FILE_SIZE;
    }
    
    /**
     * Tạo tên file unique dựa trên timestamp và UUID
     */
    public static String generateUniqueFilename(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return timestamp + "_" + uuid + extension;
    }
    
    /**
     * Lấy extension của file
     */
    public static String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex);
        }
        return "";
    }
    
    /**
     * Lấy tên file không có extension
     */
    public static String getFilenameWithoutExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return filename.substring(0, lastDotIndex);
        }
        return filename;
    }
    
    /**
     * Tạo thư mục nếu chưa tồn tại
     */
    public static boolean createDirectoryIfNotExists(String directoryPath) {
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            return directory.mkdirs();
        }
        return true;
    }
    
    /**
     * Xóa file
     */
    public static boolean deleteFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }
        
        try {
            Path path = Paths.get(filePath);
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            System.err.println("Lỗi khi xóa file: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Kiểm tra file có tồn tại không
     */
    public static boolean fileExists(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }
        return new File(filePath).exists();
    }
    
    /**
     * Lấy kích thước file
     */
    public static long getFileSize(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return 0;
        }
        return new File(filePath).length();
    }
    
    /**
     * Format file size thành chuỗi dễ đọc
     */
    public static String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else {
            return String.format("%.2f MB", size / (1024.0 * 1024.0));
        }
    }
    
    /**
     * Sanitize filename - loại bỏ các ký tự không hợp lệ
     */
    public static String sanitizeFilename(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "unnamed";
        }
        
        // Loại bỏ các ký tự đặc biệt không an toàn
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
