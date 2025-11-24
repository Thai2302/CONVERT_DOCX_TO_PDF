package com.docxtopdf.model.dao;

import com.docxtopdf.model.bean.ConversionJobBean;
import com.docxtopdf.model.bean.ConversionJobBean.JobStatus;
import com.docxtopdf.utils.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO class để thao tác với bảng conversion_jobs trong database
 * Theo mô hình MVC - Model Layer (DAO)
 */
public class ConversionJobDAO {

    /**
     * Thêm conversion job mới vào database
     */
    public int insertJob(ConversionJobBean job) {
        String sql = "INSERT INTO conversion_jobs (user_id, original_filename, stored_filename, " +
                    "file_size, status, upload_path) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, job.getUserId());
            pstmt.setString(2, job.getOriginalFilename());
            pstmt.setString(3, job.getStoredFilename());
            pstmt.setLong(4, job.getFileSize());
            pstmt.setString(5, job.getStatus().name());
            pstmt.setString(6, job.getUploadPath());
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Lấy job theo job_id
     */
    public ConversionJobBean getJobById(int jobId) {
        String sql = "SELECT * FROM conversion_jobs WHERE job_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, jobId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractJobFromResultSet(rs);
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Lấy tất cả jobs của một user
     */
    public List<ConversionJobBean> getJobsByUserId(int userId) {
        String sql = "SELECT * FROM conversion_jobs WHERE user_id = ? ORDER BY created_at DESC";
        List<ConversionJobBean> jobs = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    jobs.add(extractJobFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return jobs;
    }

    /**
     * Lấy tất cả jobs đang PENDING
     */
    public List<ConversionJobBean> getPendingJobs() {
        String sql = "SELECT * FROM conversion_jobs WHERE status = 'PENDING' ORDER BY created_at ASC";
        List<ConversionJobBean> jobs = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                jobs.add(extractJobFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return jobs;
    }

    /**
     * Cập nhật trạng thái job sang PROCESSING
     */
    public boolean updateJobToProcessing(int jobId) {
        String sql = "UPDATE conversion_jobs SET status = 'PROCESSING', started_at = CURRENT_TIMESTAMP " +
                    "WHERE job_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, jobId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Cập nhật job khi hoàn thành thành công
     */
    public boolean updateJobToCompleted(int jobId, String convertedFilename, String convertedPath) {
        String sql = "UPDATE conversion_jobs SET status = 'COMPLETED', converted_filename = ?, " +
                    "converted_path = ?, completed_at = CURRENT_TIMESTAMP WHERE job_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, convertedFilename);
            pstmt.setString(2, convertedPath);
            pstmt.setInt(3, jobId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Cập nhật job khi thất bại
     */
    public boolean updateJobToFailed(int jobId, String errorMessage) {
        String sql = "UPDATE conversion_jobs SET status = 'FAILED', error_message = ?, " +
                    "completed_at = CURRENT_TIMESTAMP WHERE job_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, errorMessage);
            pstmt.setInt(2, jobId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Xóa job
     */
    public boolean deleteJob(int jobId) {
        String sql = "DELETE FROM conversion_jobs WHERE job_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, jobId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Đếm số jobs theo trạng thái của một user
     */
    public int countJobsByUserAndStatus(int userId, JobStatus status) {
        String sql = "SELECT COUNT(*) FROM conversion_jobs WHERE user_id = ? AND status = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.setString(2, status.name());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Extract ConversionJobBean từ ResultSet
     */
    private ConversionJobBean extractJobFromResultSet(ResultSet rs) throws SQLException {
        ConversionJobBean job = new ConversionJobBean();
        job.setJobId(rs.getInt("job_id"));
        job.setUserId(rs.getInt("user_id"));
        job.setOriginalFilename(rs.getString("original_filename"));
        job.setStoredFilename(rs.getString("stored_filename"));
        job.setConvertedFilename(rs.getString("converted_filename"));
        job.setFileSize(rs.getLong("file_size"));
        job.setStatus(JobStatus.valueOf(rs.getString("status")));
        job.setErrorMessage(rs.getString("error_message"));
        job.setUploadPath(rs.getString("upload_path"));
        job.setConvertedPath(rs.getString("converted_path"));
        job.setCreatedAt(rs.getTimestamp("created_at"));
        job.setStartedAt(rs.getTimestamp("started_at"));
        job.setCompletedAt(rs.getTimestamp("completed_at"));
        return job;
    }
}
