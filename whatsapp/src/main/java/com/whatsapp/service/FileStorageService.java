package com.whatsapp.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path baseUploadPath = Paths.get("uploads").toAbsolutePath();

    public String saveFile(MultipartFile file, String type) throws IOException {
        // 1. Check file size limit (10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("File too large. Maximum size is 10MB.");
        }

        // 2. Validate file type (folder logic)
        String subfolder;
        if (type.equalsIgnoreCase("image")) {
            subfolder = "pictures";
        } else if (type.equalsIgnoreCase("video")) {
            subfolder = "videos";
        } else {
            throw new IllegalArgumentException("Unsupported file type: " + type);
        }

        // 3. Create target directory if not exists
        Path targetDir = baseUploadPath.resolve(subfolder);
        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
        }

        // 4. Generate safe file name
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = targetDir.resolve(filename);

        // 5. Save file
        file.transferTo(filePath.toFile());

        // 6. Return relative path or URI for later retrieval
        return filePath.toString();
    }
}