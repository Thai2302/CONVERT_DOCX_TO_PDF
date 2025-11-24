package com.docxtopdf.model.bean;

import java.sql.Timestamp;

/**
 * Bean class đại diện cho Conversion Job trong hệ thống
 * Theo mô hình MVC - Model Layer (BEAN)
 */
public class ConversionJobBean {
    // Enum cho trạng thái conversion
    public enum JobStatus {
        PENDING, PROCESSING, COMPLETED, FAILED
    }

    private int jobId;
    private int userId;
    private String originalFilename;
    private String storedFilename;
    private String convertedFilename;
    private long fileSize;
    private JobStatus status;
    private String errorMessage;
    private String uploadPath;
    private String convertedPath;
    private Timestamp createdAt;
    private Timestamp startedAt;
    private Timestamp completedAt;

    // Constructor mặc định
    public ConversionJobBean() {
        this.status = JobStatus.PENDING;
    }

    // Constructor đầy đủ
    public ConversionJobBean(int jobId, int userId, String originalFilename, 
                            String storedFilename, String convertedFilename, 
                            long fileSize, JobStatus status, String errorMessage,
                            String uploadPath, String convertedPath, 
                            Timestamp createdAt, Timestamp startedAt, Timestamp completedAt) {
        this.jobId = jobId;
        this.userId = userId;
        this.originalFilename = originalFilename;
        this.storedFilename = storedFilename;
        this.convertedFilename = convertedFilename;
        this.fileSize = fileSize;
        this.status = status;
        this.errorMessage = errorMessage;
        this.uploadPath = uploadPath;
        this.convertedPath = convertedPath;
        this.createdAt = createdAt;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
    }

    // Constructor để tạo job mới
    public ConversionJobBean(int userId, String originalFilename, String storedFilename,
                            long fileSize, String uploadPath) {
        this.userId = userId;
        this.originalFilename = originalFilename;
        this.storedFilename = storedFilename;
        this.fileSize = fileSize;
        this.uploadPath = uploadPath;
        this.status = JobStatus.PENDING;
    }

    // Getters và Setters
    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getStoredFilename() {
        return storedFilename;
    }

    public void setStoredFilename(String storedFilename) {
        this.storedFilename = storedFilename;
    }

    public String getConvertedFilename() {
        return convertedFilename;
    }

    public void setConvertedFilename(String convertedFilename) {
        this.convertedFilename = convertedFilename;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getUploadPath() {
        return uploadPath;
    }

    public void setUploadPath(String uploadPath) {
        this.uploadPath = uploadPath;
    }

    public String getConvertedPath() {
        return convertedPath;
    }

    public void setConvertedPath(String convertedPath) {
        this.convertedPath = convertedPath;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Timestamp startedAt) {
        this.startedAt = startedAt;
    }

    public Timestamp getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Timestamp completedAt) {
        this.completedAt = completedAt;
    }

    // Phương thức tiện ích để format file size
    public String getFormattedFileSize() {
        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.2f KB", fileSize / 1024.0);
        } else {
            return String.format("%.2f MB", fileSize / (1024.0 * 1024.0));
        }
    }

    // Phương thức để lấy thời gian xử lý
    public String getProcessingTime() {
        if (startedAt != null && completedAt != null) {
            long diff = completedAt.getTime() - startedAt.getTime();
            long seconds = diff / 1000;
            if (seconds < 60) {
                return seconds + " giây";
            } else {
                long minutes = seconds / 60;
                seconds = seconds % 60;
                return minutes + " phút " + seconds + " giây";
            }
        }
        return "N/A";
    }

    @Override
    public String toString() {
        return "ConversionJobBean{" +
                "jobId=" + jobId +
                ", userId=" + userId +
                ", originalFilename='" + originalFilename + '\'' +
                ", storedFilename='" + storedFilename + '\'' +
                ", fileSize=" + fileSize +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}
