package com.zerooneblog.blog.controller;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zerooneblog.blog.dto.response.PostDto;
import com.zerooneblog.blog.dto.response.ReportDto;
import com.zerooneblog.blog.dto.response.UserDto;
import com.zerooneblog.blog.exception.NotFoundException;
import com.zerooneblog.blog.mapper.EntityMapper;
import com.zerooneblog.blog.model.Post;
import com.zerooneblog.blog.model.Report;
import com.zerooneblog.blog.model.User;
import com.zerooneblog.blog.repository.CommentRepository;
import com.zerooneblog.blog.repository.LikeRepository;
import com.zerooneblog.blog.repository.NotificationRepository;
import com.zerooneblog.blog.repository.PostRepository;
import com.zerooneblog.blog.repository.ReportRepository;
import com.zerooneblog.blog.repository.SubscriptionRepository;
import com.zerooneblog.blog.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final ReportRepository reportRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final NotificationRepository notificationRepository;
    private final SubscriptionRepository subscriptionRepository;

    public AdminController(
            UserRepository userRepository,
            PostRepository postRepository,
            ReportRepository reportRepository,
            CommentRepository commentRepository,
            LikeRepository likeRepository,
            NotificationRepository notificationRepository,
            SubscriptionRepository subscriptionRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.reportRepository = reportRepository;
        this.commentRepository = commentRepository;
        this.likeRepository = likeRepository;
        this.notificationRepository = notificationRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    // ==================== USER MANAGEMENT ====================

    @GetMapping("/users")
    public Page<UserDto> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return userRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(EntityMapper::toDto);
    }

    @GetMapping("/users/{id}")
    public UserDto getUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return EntityMapper.toDto(user);
    }

    @PutMapping("/users/{id}/ban")
    public ResponseEntity<Map<String, Object>> banUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        
        // Prevent admin from banning themselves
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        if (user.getEmail().equalsIgnoreCase(currentEmail)) {
            return ResponseEntity.badRequest().body(Map.of("error", "You cannot ban yourself"));
        }
        
        user.setBanned(true);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "User banned successfully", "banned", true));
    }

    @PutMapping("/users/{id}/unban")
    public ResponseEntity<Map<String, Object>> unbanUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        user.setBanned(false);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "User unbanned successfully", "banned", false));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        
        // Don't allow deleting admin users
        if ("ADMIN".equals(user.getRole())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Cannot delete admin users"));
        }
        
        // Prevent admin from deleting themselves
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        if (user.getEmail().equalsIgnoreCase(currentEmail)) {
            return ResponseEntity.badRequest().body(Map.of("error", "You cannot delete yourself"));
        }
        
        // Delete all related data
        // 1. Delete notifications where user is receiver
        notificationRepository.deleteByReceiver(user);
        
        // 2. Delete subscriptions (both directions - using userId)
        subscriptionRepository.deleteBySubscriberId(user.getId());
        subscriptionRepository.deleteByUserId(user.getId());
        
        // 3. Delete reports (both as reporter and target)
        reportRepository.deleteByReporter(user);
        reportRepository.deleteByTargetUser(user);
        
        // 4. Delete likes on user's posts and by user
        likeRepository.deleteByUser(user);
        
        // 5. Delete comments on user's posts and by user
        commentRepository.deleteByUser(user);
        
        // 6. Delete user's posts (this will cascade delete likes/comments on those posts)
        for (Post post : postRepository.findByAuthor(user)) {
            likeRepository.deleteByPost(post);
            commentRepository.deleteByPost(post);
            postRepository.delete(post);
        }
        
        // 7. Finally delete the user
        userRepository.delete(user);
        
        return ResponseEntity.ok(Map.of("message", "User and all related data deleted successfully"));
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<Map<String, Object>> updateUserRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        
        String newRole = payload.get("role");
        if (newRole == null || (!newRole.equals("USER") && !newRole.equals("ADMIN"))) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid role. Must be USER or ADMIN"));
        }
        
        user.setRole(newRole);
        userRepository.save(user);
        
        return ResponseEntity.ok(Map.of("message", "User role updated", "role", newRole));
    }

    // ==================== POST MANAGEMENT ====================

    @GetMapping("/posts")
    public Page<PostDto> getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return postRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(EntityMapper::toDto);
    }

    @GetMapping("/posts/{id}")
    public PostDto getPost(@PathVariable Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Post not found"));
        return EntityMapper.toDto(post);
    }

    @PutMapping("/posts/{id}/hide")
    public ResponseEntity<Map<String, Object>> hidePost(@PathVariable Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Post not found"));
        post.setHidden(true);
        postRepository.save(post);
        return ResponseEntity.ok(Map.of("message", "Post hidden successfully", "hidden", true));
    }

    @PutMapping("/posts/{id}/unhide")
    public ResponseEntity<Map<String, Object>> unhidePost(@PathVariable Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Post not found"));
        post.setHidden(false);
        postRepository.save(post);
        return ResponseEntity.ok(Map.of("message", "Post unhidden successfully", "hidden", false));
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Map<String, String>> deletePost(@PathVariable Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Post not found"));
        
        // Delete related data first
        likeRepository.deleteByPost(post);
        commentRepository.deleteByPost(post);
        postRepository.delete(post);
        
        return ResponseEntity.ok(Map.of("message", "Post deleted successfully"));
    }

    // ==================== REPORT MANAGEMENT ====================

    @GetMapping("/reports")
    public Page<ReportDto> getReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        if (status != null && !status.isEmpty()) {
            return reportRepository.findByStatus(status, pageRequest).map(EntityMapper::toDto);
        }
        return reportRepository.findAll(pageRequest).map(EntityMapper::toDto);
    }

    @GetMapping("/reports/{id}")
    public ReportDto getReport(@PathVariable Long id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Report not found"));
        return EntityMapper.toDto(report);
    }

    @PatchMapping("/reports/{id}")
    public ReportDto updateReportStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Report not found"));
        
        String status = payload.get("status");
        if (status != null) {
            // Validate status
            if (!status.equals("PENDING") && !status.equals("REVIEWED") && !status.equals("RESOLVED")) {
                throw new IllegalArgumentException("Invalid status. Must be PENDING, REVIEWED, or RESOLVED");
            }
            report.setStatus(status);
            reportRepository.save(report);
        }
        
        return EntityMapper.toDto(report);
    }

    @DeleteMapping("/reports/{id}")
    public ResponseEntity<Map<String, String>> deleteReport(@PathVariable Long id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Report not found"));
        reportRepository.delete(report);
        return ResponseEntity.ok(Map.of("message", "Report deleted successfully"));
    }

    @PutMapping("/reports/{id}/ban-user")
    public ResponseEntity<Map<String, Object>> banReportedUser(@PathVariable Long id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Report not found"));
        
        User targetUser = report.getTargetUser();
        if (targetUser == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "No target user in this report"));
        }
        
        targetUser.setBanned(true);
        userRepository.save(targetUser);
        
        // Mark report as resolved
        report.setStatus("RESOLVED");
        reportRepository.save(report);
        
        return ResponseEntity.ok(Map.of(
            "message", "User banned and report resolved",
            "bannedUserId", targetUser.getId(),
            "reportStatus", "RESOLVED"
        ));
    }

    // ==================== DASHBOARD STATS ====================

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        long totalUsers = userRepository.count();
        long totalPosts = postRepository.count();
        long pendingReports = reportRepository.countByStatus("PENDING");
        long bannedUsers = userRepository.countByBanned(true);
        long hiddenPosts = postRepository.countByHidden(true);
        
        return ResponseEntity.ok(Map.of(
            "totalUsers", totalUsers,
            "totalPosts", totalPosts,
            "pendingReports", pendingReports,
            "bannedUsers", bannedUsers,
            "hiddenPosts", hiddenPosts
        ));
    }
}
