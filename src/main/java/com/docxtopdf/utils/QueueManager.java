package com.docxtopdf.utils;

import com.docxtopdf.model.bean.ConversionJobBean;
import com.docxtopdf.model.dao.ConversionJobDAO;

import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Queue Manager để quản lý hàng đợi conversion jobs
 * Sử dụng BlockingQueue và Thread Pool để xử lý jobs bất đồng bộ
 * Theo mô hình MVC - Utils Layer
 */
public class QueueManager {
    
    // Singleton instance
    private static QueueManager instance;
    
    // BlockingQueue để lưu trữ jobs
    private BlockingQueue<ConversionJobBean> jobQueue;
    
    // Thread Pool để xử lý jobs
    private ExecutorService executorService;
    
    // DAO để cập nhật database
    private ConversionJobDAO conversionJobDAO;
    
    // Số lượng worker threads
    private static final int NUM_WORKERS = 3;
    
    // Flag để kiểm tra đã khởi động chưa
    private boolean isRunning = false;
    
    /**
     * Private constructor cho Singleton pattern
     */
    private QueueManager() {
        jobQueue = new LinkedBlockingQueue<>();
        executorService = Executors.newFixedThreadPool(NUM_WORKERS);
        conversionJobDAO = new ConversionJobDAO();
    }
    
    /**
     * Lấy instance của QueueManager (Singleton)
     */
    public static synchronized QueueManager getInstance() {
        if (instance == null) {
            instance = new QueueManager();
        }
        return instance;
    }
    
    /**
     * Khởi động Queue Manager
     */
    public synchronized void start() {
        if (isRunning) {
            System.out.println("Queue Manager đã được khởi động rồi");
            return;
        }
        
        System.out.println("Đang khởi động Queue Manager với " + NUM_WORKERS + " worker threads...");
        
        // Khởi động các worker threads
        for (int i = 0; i < NUM_WORKERS; i++) {
            executorService.submit(new ConversionWorker(i + 1));
        }
        
        isRunning = true;
        System.out.println("Queue Manager đã được khởi động thành công!");
    }
    
    /**
     * Dừng Queue Manager
     */
    public synchronized void stop() {
        if (!isRunning) {
            System.out.println("Queue Manager chưa được khởi động");
            return;
        }
        
        System.out.println("Đang dừng Queue Manager...");
        isRunning = false;
        executorService.shutdown();
        System.out.println("Queue Manager đã được dừng!");
    }
    
    /**
     * Thêm job vào hàng đợi
     */
    public boolean addJob(ConversionJobBean job) {
        try {
            jobQueue.put(job);
            System.out.println("Đã thêm job #" + job.getJobId() + " vào hàng đợi. Số jobs đang chờ: " + jobQueue.size());
            return true;
        } catch (InterruptedException e) {
            System.err.println("Lỗi khi thêm job vào hàng đợi: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Lấy số lượng jobs đang chờ trong queue
     */
    public int getQueueSize() {
        return jobQueue.size();
    }
    
    /**
     * Kiểm tra Queue Manager có đang chạy không
     */
    public boolean isRunning() {
        return isRunning;
    }
    
    /**
     * Inner class: Conversion Worker
     * Thread worker để xử lý conversion jobs
     */
    private class ConversionWorker implements Runnable {
        private int workerId;
        
        public ConversionWorker(int workerId) {
            this.workerId = workerId;
        }
        
        @Override
        public void run() {
            System.out.println("Worker #" + workerId + " đã được khởi động");
            
            while (isRunning) {
                try {
                    // Lấy job từ queue (blocking call)
                    ConversionJobBean job = jobQueue.take();
                    
                    System.out.println("Worker #" + workerId + " đang xử lý job #" + job.getJobId());
                    
                    // Cập nhật trạng thái job sang PROCESSING
                    conversionJobDAO.updateJobToProcessing(job.getJobId());
                    
                    // Thực hiện conversion
                    boolean success = processConversion(job);
                    
                    if (success) {
                        System.out.println("Worker #" + workerId + " đã hoàn thành job #" + job.getJobId());
                    } else {
                        System.err.println("Worker #" + workerId + " thất bại khi xử lý job #" + job.getJobId());
                    }
                    
                } catch (InterruptedException e) {
                    System.err.println("Worker #" + workerId + " bị gián đoạn: " + e.getMessage());
                    break;
                } catch (Exception e) {
                    System.err.println("Worker #" + workerId + " gặp lỗi: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            System.out.println("Worker #" + workerId + " đã dừng");
        }
        
        /**
         * Xử lý conversion
         */
        private boolean processConversion(ConversionJobBean job) {
            try {
                // Lấy đường dẫn file
                String docxPath = job.getUploadPath();
                String pdfFilename = FileUtil.getFilenameWithoutExtension(job.getStoredFilename()) + ".pdf";
                
                // Tạo đường dẫn file PDF đầu ra
                File uploadFile = new File(docxPath);
                String uploadDir = uploadFile.getParent();
                String convertedDir = uploadDir.replace("uploads", "converted");
                
                // Tạo thư mục converted nếu chưa có
                FileUtil.createDirectoryIfNotExists(convertedDir);
                
                String pdfPath = convertedDir + File.separator + pdfFilename;
                
                // Thực hiện conversion
                boolean success = ConversionUtil.convertDocxToPdf(docxPath, pdfPath);
                
                if (success) {
                    // Cập nhật database
                    conversionJobDAO.updateJobToCompleted(job.getJobId(), pdfFilename, pdfPath);
                    System.out.println("Job #" + job.getJobId() + " hoàn thành thành công");
                    return true;
                } else {
                    // Cập nhật database với lỗi
                    conversionJobDAO.updateJobToFailed(job.getJobId(), "Không thể convert file DOCX sang PDF");
                    return false;
                }
                
            } catch (Exception e) {
                System.err.println("Lỗi khi xử lý conversion job #" + job.getJobId() + ": " + e.getMessage());
                e.printStackTrace();
                
                // Cập nhật database với lỗi
                conversionJobDAO.updateJobToFailed(job.getJobId(), 
                    "Lỗi: " + e.getMessage());
                return false;
            }
        }
    }
}
