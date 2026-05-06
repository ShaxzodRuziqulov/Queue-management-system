package com.example.queuemanagementsystem.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );
    private static final long MAX_SIZE = 5 * 1024 * 1024L;

    private final Path uploadRoot;

    public FileStorageService(@Value("${app.upload.dir:uploads}") String uploadDir) {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadRoot);
        } catch (IOException e) {
            throw new RuntimeException("Yuklash papkasini yaratib bo'lmadi: " + uploadRoot, e);
        }
    }

    public String store(MultipartFile file, String subDir) {
        validate(file);
        String filename = UUID.randomUUID() + extension(file.getOriginalFilename());
        Path dir = uploadRoot.resolve(subDir);
        try {
            Files.createDirectories(dir);
            Files.copy(file.getInputStream(), dir.resolve(filename));
        } catch (IOException e) {
            throw new RuntimeException("Faylni saqlashda xato yuz berdi", e);
        }
        return "/uploads/" + subDir + "/" + filename;
    }

    public void delete(String url) {
        if (url == null || !url.startsWith("/uploads/")) return;
        Path target = uploadRoot.resolve(url.replaceFirst("^/uploads/", "")).normalize();
        if (!target.startsWith(uploadRoot)) return;
        try {
            Files.deleteIfExists(target);
        } catch (IOException ignored) {
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Fayl bo'sh bo'lmasligi kerak");
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("Faqat rasm fayllari qabul qilinadi (JPEG, PNG, GIF, WEBP)");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new IllegalArgumentException("Fayl hajmi 5MB dan oshmasligi kerak");
        }
    }

    private String extension(String filename) {
        if (filename == null) return "";
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot).toLowerCase() : "";
    }
}