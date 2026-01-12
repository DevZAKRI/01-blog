package com.zerooneblog.blog.controller;

import java.util.Map;
import java.util.logging.Logger;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.zerooneblog.blog.exception.BadRequestException;
import com.zerooneblog.blog.service.FileStorageService;

@RestController
@RequestMapping("/api/v1/uploads")
public class UploadController {
    private static final Logger logger = Logger.getLogger(UploadController.class.getName());
    private final FileStorageService fileStorageService;

    public UploadController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file, Authentication auth) {
        logger.info("[UploadController] Upload request received");
        logger.info("[UploadController] User: " + (auth != null ? auth.getName() : "anonymous"));
        logger.info("[UploadController] Filename: " + file.getOriginalFilename());
        logger.info("[UploadController] Size: " + file.getSize() + " bytes");
        logger.info("[UploadController] Content-Type: " + file.getContentType());
        
        try {
            String path = fileStorageService.store(file);
            logger.info("[UploadController] Upload successful: " + path);
            
            // Return both the relative path and media type
            String mediaType = fileStorageService.isVideo(file.getContentType()) ? "video" : "image";
            
            return ResponseEntity.ok().body(Map.of(
                "path", path,
                "mediaType", mediaType,
                "filename", file.getOriginalFilename(),
                "size", file.getSize()
            ));
        } catch (BadRequestException e) {
            logger.warning("[UploadController] Bad request: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.severe("[UploadController] Upload failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to upload file"));
        }
    }
}
