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
    private String configuredBasePath;

    private String getBasePath() {
        // ✅ Lấy thư mục gốc project thật, không phải temp folder của Tomcat
        return new File(System.getProperty("user.dir"), configuredBasePath).getAbsolutePath();
    }

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

    public boolean deleteFile(String path) {
        if (path == null) return false;
        File file = new File(path);
        return file.exists() && file.delete();
    }
}
