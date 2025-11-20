package com.example.contract_service.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.storage.base-path:uploads/contracts}")
    private String configuredBasePath; //Dùng cho UC06

    @Value("${file.storage.appendix-path:uploads/appendix}")
    private String appendixStoragePath; // Dùng cho UC08

    private String getBasePath() {
        // ✅ Lấy thư mục gốc project thật, không phải temp folder của Tomcat
        return new File(System.getProperty("user.dir"), configuredBasePath).getAbsolutePath();
    }

    private String getAbsolutePath(String relativePath) {
        return new File(System.getProperty("user.dir"), relativePath).getAbsolutePath();
    }

    // ======================================================
    // =============== UC06: LƯU MAIN CONTRACT ==============
    // ======================================================
    public String saveContractFile(String tenantId, MultipartFile file, String contractCode) throws IOException {
        if (file == null || file.isEmpty() || !file.getOriginalFilename().endsWith(".pdf")) {
            throw new RuntimeException("Invalid file. Only PDF files are accepted.");
        }

        // ✅ Xác định đường dẫn gốc và thư mục tenant
        String basePath = getBasePath();
        Path tenantDir = Paths.get(basePath, tenantId);
        Files.createDirectories(tenantDir); // Tạo nếu chưa có

        // ✅ Sinh tên file duy nhất
        String filename = contractCode + "_" + UUID.randomUUID() + ".pdf";
        Path targetPath = tenantDir.resolve(filename);

        // ✅ Đảm bảo thư mục cha tồn tại
        Files.createDirectories(targetPath.getParent());

        // ✅ Ghi file vào thư mục thật của project
        file.transferTo(targetPath.toFile());

        return targetPath.toString();
    }

    // ======================================================
    // =============== UC08: LƯU APPENDIX SIGNED =============
    // ======================================================

    /**
     * Lưu file phụ lục đã ký số (PDF).
     *
     * @param tenantId   tenant đang sở hữu phụ lục
     * @param appendixId id của phụ lục dịch vụ
     * @param pdfBytes   nội dung PDF dạng byte[] sau khi sinh bằng iText hoặc Jasper
     * @return File path tuyệt đối đã lưu
     */
    public String saveSignedAppendix(String tenantId, String appendixId, byte[] pdfBytes) {
        try {
            // uploads/appendix/<tenantId>/<appendixId>/appendix_signed.pdf
            String baseDir = getAbsolutePath(appendixStoragePath);

            Path appendixDir =
                    Paths.get(baseDir, tenantId, appendixId);

            Files.createDirectories(appendixDir);

            Path pdfPath = appendixDir.resolve("appendix_signed.pdf");

            Files.write(pdfPath, pdfBytes);

            return pdfPath.toString();

        } catch (IOException e) {
            throw new RuntimeException("Cannot save appendix PDF: " + e.getMessage(), e);
        }
    }

    // ======================================================
    // =============== DELETE FILE ==========================
    // ======================================================
    public boolean deleteFile(String path) {
        if (path == null) return false;
        File file = new File(path);
        return file.exists() && file.delete();
    }
}
