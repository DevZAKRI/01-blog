package com.zerooneblog.blog.model;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User receiver;

    // The user who triggered the notification (e.g., who followed, who posted)
    private Long actorId;

    private String type; // new_post, new_subscriber

    @Column(columnDefinition = "TEXT")
    private String content;

    @JsonProperty("isRead")
    @Column(name = "is_read")
    private boolean isRead = false;

    private Instant createdAt;

    @PrePersist
    public void prePersist() { this.createdAt = Instant.now(); }
}
