package com.docxtopdf.utils;

import org.docx4j.Docx4J;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.fonts.IdentityPlusMapper;
import org.docx4j.fonts.Mapper;
import org.docx4j.fonts.PhysicalFonts;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Utility class để convert DOCX sang PDF
 * Sử dụng thư viện Docx4j để đảm bảo chất lượng conversion
 * Theo mô hình MVC - Utils Layer
 */
public class ConversionUtil {
    
    /**
     * Convert DOCX file sang PDF
     * @param docxFilePath Đường dẫn file DOCX đầu vào
     * @param pdfFilePath Đường dẫn file PDF đầu ra
     * @return true nếu convert thành công, false nếu thất bại
     */
    public static boolean convertDocxToPdf(String docxFilePath, String pdfFilePath) {
        FileOutputStream fos = null;
        try {
            System.out.println("Bắt đầu convert file: " + docxFilePath);
            
            // Load DOCX file
            File docxFile = new File(docxFilePath);
            if (!docxFile.exists()) {
                System.err.println("File DOCX không tồn tại: " + docxFilePath);
                return false;
            }
            
            // Load WordprocessingMLPackage
            WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(docxFile);
            
            // Cấu hình font mapper để giữ nguyên font
            Mapper fontMapper = new IdentityPlusMapper();
            wordMLPackage.setFontMapper(fontMapper);
            
            // Tạo thư mục đầu ra nếu chưa tồn tại
            File pdfFile = new File(pdfFilePath);
            File parentDir = pdfFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            // Convert sang PDF với cấu hình tối ưu
            fos = new FileOutputStream(pdfFile);
            Docx4J.toPDF(wordMLPackage, fos);
            
            System.out.println("Convert thành công: " + pdfFilePath);
            return true;
            
        } catch (Exception e) {
            System.err.println("Lỗi khi convert DOCX sang PDF: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Convert DOCX với các options nâng cao (Unused - Simple method preferred)
     * @param docxFilePath Đường dẫn file DOCX đầu vào
     * @param pdfFilePath Đường dẫn file PDF đầu ra
     * @param saveFO true nếu muốn lưu file FO (Formatting Objects) trung gian
     * @return true nếu convert thành công, false nếu thất bại
     */
    @SuppressWarnings("unused")
    public static boolean convertDocxToPdfAdvanced(String docxFilePath, String pdfFilePath, boolean saveFO) {
        // This method is kept for reference but not used
        // The simple convertDocxToPdf method is sufficient for our needs
        return convertDocxToPdf(docxFilePath, pdfFilePath);
    }
    
    /**
     * Validate file DOCX trước khi convert
     */
    public static boolean validateDocxFile(String docxFilePath) {
        try {
            File docxFile = new File(docxFilePath);
            
            if (!docxFile.exists()) {
                System.err.println("File không tồn tại: " + docxFilePath);
                return false;
            }
            
            if (!docxFile.canRead()) {
                System.err.println("Không thể đọc file: " + docxFilePath);
                return false;
            }
            
            if (docxFile.length() == 0) {
                System.err.println("File rỗng: " + docxFilePath);
                return false;
            }
            
            // Thử load file để kiểm tra tính hợp lệ
            WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(docxFile);
            if (wordMLPackage == null) {
                System.err.println("Không thể load file DOCX: " + docxFilePath);
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            System.err.println("Lỗi khi validate file DOCX: " + e.getMessage());
            return false;
        }
    }
}
