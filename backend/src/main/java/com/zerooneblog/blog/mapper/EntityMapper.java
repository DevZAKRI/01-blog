package com.zerooneblog.blog.mapper;

import java.util.stream.Collectors;

import com.zerooneblog.blog.dto.response.CommentDto;
import com.zerooneblog.blog.dto.response.NotificationDto;
import com.zerooneblog.blog.dto.response.PostDto;
import com.zerooneblog.blog.dto.response.ReportDto;
import com.zerooneblog.blog.dto.response.UserDto;
import com.zerooneblog.blog.model.Comment;
import com.zerooneblog.blog.model.Notification;
import com.zerooneblog.blog.model.Post;
import com.zerooneblog.blog.model.Report;
import com.zerooneblog.blog.model.User;
import com.zerooneblog.blog.repository.CommentRepository;
import com.zerooneblog.blog.repository.LikeRepository;

public class EntityMapper {
    private static CommentRepository commentRepository;
    private static LikeRepository likeRepository;

    public EntityMapper(CommentRepository commentRepository, LikeRepository likeRepository) {
        EntityMapper.commentRepository = commentRepository;
        EntityMapper.likeRepository = likeRepository;
    }

    public static UserDto toDto(User u) {
        if (u == null) return null;
        UserDto d = new UserDto();
        d.setId(u.getId());
        d.setUsername(u.getUsername());
        d.setEmail(u.getEmail());
        d.setBio(u.getBio());
        d.setAvatar(u.getAvatarUrl() == null || u.getAvatarUrl().isBlank() ? "/uploads/default-avatar.svg" : u.getAvatarUrl());
        d.setBanned(u.isBanned());
        d.setCreatedAt(u.getCreatedAt());
        d.setRole(u.getRole());
        return d;
    }

    public static PostDto toDto(Post p) {
        return toDto(p, null);
    }

    public static PostDto toDto(Post p, User currentUser) {
        if (p == null) return null;
        PostDto d = new PostDto();
        d.setId(p.getId());
        if (p.getAuthor() != null) { d.setAuthorId(p.getAuthor().getId()); d.setAuthorUsername(p.getAuthor().getUsername()); }
        d.setTitle(p.getTitle());
        d.setDescription(p.getDescription());

        // Parse JSON mediaUrls array
        if (p.getMediaUrls() != null && !p.getMediaUrls().isEmpty()) {
            try {
                d.setMediaUrls(new com.fasterxml.jackson.databind.ObjectMapper().readValue(p.getMediaUrls(), String[].class));
            } catch (Exception e) {
                System.err.println("Error deserializing mediaUrls: " + e.getMessage());
                d.setMediaUrls(new String[]{});
            }
        }

        d.setCreatedAt(p.getCreatedAt());
        d.setUpdatedAt(p.getUpdatedAt());
        d.setHidden(p.isHidden());

        // Set like and comment counts if repositories are initialized
        if (likeRepository != null) {
            d.setLikesCount(likeRepository.countByPost(p));
            if (currentUser != null) {
                d.setLiked(likeRepository.findByUserAndPost(currentUser, p).isPresent());
            }
        }
        if (commentRepository != null) {
            d.setCommentsCount(commentRepository.countByPost(p));
        }

        return d;
    }

    public static CommentDto toDto(Comment c) {
        if (c == null) return null;
        CommentDto d = new CommentDto();
        d.setId(c.getId());
        if (c.getUser() != null) { d.setUserId(c.getUser().getId()); d.setUsername(c.getUser().getUsername()); }
        if (c.getPost() != null) d.setPostId(c.getPost().getId());
        d.setText(c.getText());
        d.setCreatedAt(c.getCreatedAt());
        return d;
    }

    public static NotificationDto toDto(Notification n) {
        if (n == null) return null;
        NotificationDto d = new NotificationDto();
        d.setId(n.getId());
        if (n.getReceiver() != null) d.setReceiverId(n.getReceiver().getId());
        d.setType(n.getType());
        d.setContent(n.getContent());
        d.setRead(n.isRead());
        d.setCreatedAt(n.getCreatedAt());
        return d;
    }

    public static ReportDto toDto(Report r) {
        if (r == null) return null;
        ReportDto d = new ReportDto();
        d.setId(r.getId());
        if (r.getReporter() != null) d.setReporterId(r.getReporter().getId());
        if (r.getTargetUser() != null) d.setTargetUserId(r.getTargetUser().getId());
        d.setReason(r.getReason());
        d.setStatus(r.getStatus());
        if (r.getReporter() != null) d.setReporter(toDto(r.getReporter()));
        if (r.getTargetUser() != null) d.setReportedUser(toDto(r.getTargetUser()));
        d.setCreatedAt(r.getCreatedAt());
        return d;
    }
}
