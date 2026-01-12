package com.zerooneblog.blog.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.zerooneblog.blog.exception.BadRequestException;

@Service
public class FileStorageService {
    private static final Logger logger = Logger.getLogger(FileStorageService.class.getName());
    
    private final Path uploadDir;
    
    // Supported MIME types for images and videos
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
        "image/png", "image/jpeg", "image/jpg", "image/gif", "image/webp", "image/svg+xml"
    );
    
    private static final Set<String> ALLOWED_VIDEO_TYPES = Set.of(
        "video/mp4", "video/quicktime", "video/webm", "video/ogg", "video/mpeg", "video/x-msvideo"
    );
    
    // Max file size: 50MB to match frontend
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024;

    public FileStorageService() throws IOException {
        String wd = System.getProperty("user.dir");
        this.uploadDir = Path.of(wd, "uploads");
        Files.createDirectories(this.uploadDir);
        logger.info("[FileStorageService] Upload directory initialized at: " + this.uploadDir.toAbsolutePath());
    }

    public String store(MultipartFile file) {
        logger.info("[FileStorageService] Storing file: " + file.getOriginalFilename());
        logger.info("[FileStorageService] Content type: " + file.getContentType());
        logger.info("[FileStorageService] File size: " + file.getSize() + " bytes");
        
        // Validate file is not empty
        if (file.isEmpty()) {
            logger.warning("[FileStorageService] Rejected: Empty file");
            throw new BadRequestException("Cannot upload empty file");
        }
        
        // Validate file size
        if (file.getSize() > MAX_FILE_SIZE) {
            logger.warning("[FileStorageService] Rejected: File too large (" + file.getSize() + " bytes, max: " + MAX_FILE_SIZE + ")");
            throw new BadRequestException("File size exceeds maximum allowed (50MB)");
        }
        
        // Validate content type
        String contentType = file.getContentType();
        if (contentType == null) {
            logger.warning("[FileStorageService] Rejected: Unknown content type");
            throw new BadRequestException("Cannot determine file type");
        }
        
        boolean isImage = ALLOWED_IMAGE_TYPES.contains(contentType);
        boolean isVideo = ALLOWED_VIDEO_TYPES.contains(contentType);
        
        if (!isImage && !isVideo) {
            logger.warning("[FileStorageService] Rejected: Unsupported content type: " + contentType);
            throw new BadRequestException("Unsupported file type: " + contentType + ". Allowed types: images (PNG, JPEG, GIF, WebP) and videos (MP4, WebM, MOV)");
        }
        
        try {
            // Generate unique filename with original extension
            String original = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
            String ext = "";
            int idx = original.lastIndexOf('.');
            if (idx > 0) {
                ext = original.substring(idx).toLowerCase();
            } else {
                // Infer extension from content type
                ext = getExtensionFromContentType(contentType);
            }
            
            String filename = java.util.UUID.randomUUID().toString() + ext;
            Path target = uploadDir.resolve(filename);
            
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            
            String resultPath = "/uploads/" + filename;
            logger.info("[FileStorageService] File stored successfully: " + resultPath);
            return resultPath;
            
        } catch (IOException e) {
            logger.severe("[FileStorageService] Failed to store file: " + e.getMessage());
            throw new RuntimeException("Failed to store file: " + e.getMessage(), e);
        }
    }
    
    private String getExtensionFromContentType(String contentType) {
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/jpeg", "image/jpg" -> ".jpg";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            case "image/svg+xml" -> ".svg";
            case "video/mp4" -> ".mp4";
            case "video/quicktime" -> ".mov";
            case "video/webm" -> ".webm";
            case "video/ogg" -> ".ogv";
            case "video/mpeg" -> ".mpeg";
            case "video/x-msvideo" -> ".avi";
            default -> "";
        };
    }
    
    public boolean isImage(String contentType) {
        return contentType != null && ALLOWED_IMAGE_TYPES.contains(contentType);
    }
    
    public boolean isVideo(String contentType) {
        return contentType != null && ALLOWED_VIDEO_TYPES.contains(contentType);
    }
    
    public Path getUploadDir() {
        return uploadDir;
    }
}
