package com.zerooneblog.blog.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.zerooneblog.blog.exception.BadRequestException;

@Service
public class FileStorageService {
    private final Path uploadDir;
    private final Set<String> allowed = Set.of("image/png","image/jpeg","image/jpg","image/gif","video/mp4","video/quicktime");
    private final long maxSize = 10 * 1024 * 1024; // 10MB

    public FileStorageService() throws IOException {
        String wd = System.getProperty("user.dir");
        this.uploadDir = Path.of(wd, "uploads");
        Files.createDirectories(this.uploadDir);
    }

    public String store(MultipartFile file) {
        if (file.isEmpty()) throw new BadRequestException("Empty file");
        if (file.getSize() > maxSize) throw new BadRequestException("File too large");
        if (!allowed.contains(file.getContentType())) throw new BadRequestException("Unsupported file type");
        try {
            String original = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
            String ext = "";
            int idx = original.lastIndexOf('.');
            if (idx > 0) ext = original.substring(idx);
            String filename = java.util.UUID.randomUUID().toString() + ext;
            Path target = uploadDir.resolve(filename);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }
}
