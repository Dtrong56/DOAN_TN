package com.example.auth_service.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.io.File;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;

@Service
public class FileStorageService {

    @Value("${file.storage.base-path:uploads/signatures}")
    private String configuredBasePath;

    private String getBasePath() {
        return new File(System.getProperty("user.dir"), configuredBasePath).getAbsolutePath();
    }

    public String save(MultipartFile file, String folder) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Invalid file");
        }

        String basePath = getBasePath();
        Path dir = Paths.get(basePath, folder);
        Files.createDirectories(dir);

        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path target = dir.resolve(filename);

        file.transferTo(target.toFile());
        return target.toString();
    }
}


