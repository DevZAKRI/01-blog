package com.zerooneblog.blog.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.zerooneblog.blog.service.FileStorageService;

@RestController
@RequestMapping("/api/v1/uploads")
public class UploadController {
    private final FileStorageService fileStorageService;

    public UploadController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file, Authentication auth) {
        String path = fileStorageService.store(file);
        return ResponseEntity.ok().body(java.util.Map.of("path", path));
    }
}
