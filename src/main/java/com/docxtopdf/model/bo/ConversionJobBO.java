package com.docxtopdf.model.bo;

import com.docxtopdf.model.bean.ConversionJobBean;
import com.docxtopdf.model.bean.ConversionJobBean.JobStatus;
import com.docxtopdf.model.dao.ConversionJobDAO;
import com.docxtopdf.utils.QueueManager;
import com.docxtopdf.utils.FileUtil;

import java.util.List;

/**
 * Business Object để xử lý logic nghiệp vụ liên quan đến Conversion Job
 * Theo mô hình MVC - Model Layer (BO)
 */
public class ConversionJobBO {
    
    private ConversionJobDAO conversionJobDAO;
    private QueueManager queueManager;
    
    public ConversionJobBO() {
        this.conversionJobDAO = new ConversionJobDAO();
        this.queueManager = QueueManager.getInstance();
    }
    
    /**
     * Tạo và submit job mới vào hàng đợi
     */
    public ConversionJobBean createAndSubmitJob(int userId, String originalFilename, 
                                                String storedFilename, long fileSize, 
                                                String uploadPath) {
        // Validate input
        if (userId <= 0) {
            System.err.println("User ID không hợp lệ");
            return null;
        }
        
        if (!FileUtil.isValidDocxFile(originalFilename)) {
            System.err.println("File không phải là DOCX: " + originalFilename);
            return null;
        }
        
        if (!FileUtil.isValidFileSize(fileSize)) {
            System.err.println("Kích thước file không hợp lệ: " + FileUtil.formatFileSize(fileSize));
            return null;
        }
        
        if (!FileUtil.fileExists(uploadPath)) {
            System.err.println("File không tồn tại: " + uploadPath);
            return null;
        }
        
        // Tạo ConversionJobBean
        ConversionJobBean job = new ConversionJobBean(userId, originalFilename, 
                                                     storedFilename, fileSize, uploadPath);
        
        // Insert vào database
        int jobId = conversionJobDAO.insertJob(job);
        
        if (jobId > 0) {
            job.setJobId(jobId);
            
            // Thêm vào hàng đợi
            boolean addedToQueue = queueManager.addJob(job);
            
            if (addedToQueue) {
                System.out.println("Job #" + jobId + " đã được tạo và thêm vào hàng đợi");
                return job;
            } else {
                System.err.println("Không thể thêm job vào hàng đợi");
                // Có thể xóa job khỏi database nếu muốn
                return null;
            }
        }
        
        return null;
    }
    
    /**
     * Lấy job theo ID
     */
    public ConversionJobBean getJobById(int jobId) {
        return conversionJobDAO.getJobById(jobId);
    }
    
    /**
     * Lấy tất cả jobs của user
     */
    public List<ConversionJobBean> getJobsByUserId(int userId) {
        return conversionJobDAO.getJobsByUserId(userId);
    }
    
    /**
     * Lấy các jobs đang PENDING
     */
    public List<ConversionJobBean> getPendingJobs() {
        return conversionJobDAO.getPendingJobs();
    }
    
    /**
     * Xóa job và file liên quan
     */
    public boolean deleteJob(int jobId, int userId) {
        // Lấy thông tin job
        ConversionJobBean job = conversionJobDAO.getJobById(jobId);
        
        if (job == null) {
            System.err.println("Không tìm thấy job #" + jobId);
            return false;
        }
        
        // Kiểm tra quyền: chỉ user sở hữu job mới có thể xóa
        if (job.getUserId() != userId) {
            System.err.println("User #" + userId + " không có quyền xóa job #" + jobId);
            return false;
        }
        
        // Không cho phép xóa job đang PROCESSING
        if (job.getStatus() == JobStatus.PROCESSING) {
            System.err.println("Không thể xóa job đang được xử lý");
            return false;
        }
        
        // Xóa file upload nếu tồn tại
        if (job.getUploadPath() != null && FileUtil.fileExists(job.getUploadPath())) {
            FileUtil.deleteFile(job.getUploadPath());
            System.out.println("Đã xóa file upload: " + job.getUploadPath());
        }
        
        // Xóa file converted nếu tồn tại
        if (job.getConvertedPath() != null && FileUtil.fileExists(job.getConvertedPath())) {
            FileUtil.deleteFile(job.getConvertedPath());
            System.out.println("Đã xóa file converted: " + job.getConvertedPath());
        }
        
        // Xóa job khỏi database
        boolean deleted = conversionJobDAO.deleteJob(jobId);
        
        if (deleted) {
            System.out.println("Đã xóa job #" + jobId);
            return true;
        }
        
        return false;
    }
    
    /**
     * Đếm số jobs theo trạng thái của user
     */
    public int countJobsByUserAndStatus(int userId, JobStatus status) {
        return conversionJobDAO.countJobsByUserAndStatus(userId, status);
    }
    
    /**
     * Lấy thống kê jobs của user
     */
    public JobStatistics getJobStatistics(int userId) {
        int totalJobs = getJobsByUserId(userId).size();
        int pendingJobs = countJobsByUserAndStatus(userId, JobStatus.PENDING);
        int processingJobs = countJobsByUserAndStatus(userId, JobStatus.PROCESSING);
        int completedJobs = countJobsByUserAndStatus(userId, JobStatus.COMPLETED);
        int failedJobs = countJobsByUserAndStatus(userId, JobStatus.FAILED);
        
        return new JobStatistics(totalJobs, pendingJobs, processingJobs, completedJobs, failedJobs);
    }
    
    /**
     * Inner class: Job Statistics
     */
    public static class JobStatistics {
        private int totalJobs;
        private int pendingJobs;
        private int processingJobs;
        private int completedJobs;
        private int failedJobs;
        
        public JobStatistics(int totalJobs, int pendingJobs, int processingJobs, 
                           int completedJobs, int failedJobs) {
            this.totalJobs = totalJobs;
            this.pendingJobs = pendingJobs;
            this.processingJobs = processingJobs;
            this.completedJobs = completedJobs;
            this.failedJobs = failedJobs;
        }
        
        public int getTotalJobs() { return totalJobs; }
        public int getPendingJobs() { return pendingJobs; }
        public int getProcessingJobs() { return processingJobs; }
        public int getCompletedJobs() { return completedJobs; }
        public int getFailedJobs() { return failedJobs; }
    }
}
