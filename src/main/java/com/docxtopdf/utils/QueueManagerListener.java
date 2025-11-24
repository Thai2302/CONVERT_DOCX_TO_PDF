package com.docxtopdf.utils;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/**
 * Context Listener để khởi động Queue Manager khi ứng dụng start
 * Theo mô hình MVC - Utils Layer
 */
@WebListener
public class QueueManagerListener implements ServletContextListener {
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("========================================");
        System.out.println("Ứng dụng DOCX to PDF Converter đang khởi động...");
        System.out.println("========================================");
        
        // Khởi động Queue Manager
        QueueManager queueManager = QueueManager.getInstance();
        queueManager.start();
        
        // Lưu instance vào ServletContext
        sce.getServletContext().setAttribute("queueManager", queueManager);
        
        System.out.println("Ứng dụng đã sẵn sàng!");
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("========================================");
        System.out.println("Ứng dụng đang shutdown...");
        System.out.println("========================================");
        
        // Dừng Queue Manager
        QueueManager queueManager = (QueueManager) sce.getServletContext().getAttribute("queueManager");
        if (queueManager != null) {
            queueManager.stop();
        }
        
        System.out.println("Ứng dụng đã dừng!");
    }
}
