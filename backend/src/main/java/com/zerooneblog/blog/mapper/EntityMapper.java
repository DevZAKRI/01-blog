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

public class EntityMapper {
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
        if (u.getSubscribers() != null)
            d.setSubscriberIds(u.getSubscribers().stream().map(User::getId).collect(Collectors.toSet()));
        if (u.getSubscriptions() != null)
            d.setSubscriptionIds(u.getSubscriptions().stream().map(User::getId).collect(Collectors.toSet()));
        return d;
    }

    public static PostDto toDto(Post p) {
        if (p == null) return null;
        PostDto d = new PostDto();
        d.setId(p.getId());
        if (p.getAuthor() != null) { d.setAuthorId(p.getAuthor().getId()); d.setAuthorUsername(p.getAuthor().getUsername()); }
        d.setDescription(p.getDescription());
        d.setMediaUrl(p.getMediaUrl());
        d.setCreatedAt(p.getCreatedAt());
        d.setUpdatedAt(p.getUpdatedAt());
        d.setHidden(p.isHidden());
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
