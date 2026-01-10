package com.zerooneblog.blog.model;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    private String email;
    private String role = "USER";
    private String bio;
    private String avatarUrl;
    private boolean banned = false;

    @Column(updatable = false)
    private Instant createdAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_subscribers",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "subscriber_id")
    )
    private Set<User> subscribers = new HashSet<>();

    @ManyToMany(mappedBy = "subscribers", fetch = FetchType.LAZY)
    private Set<User> subscriptions = new HashSet<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
    }

    // Relationship to posts removed for now because Post entity is not present.
    // Add back when `Post` model exists:
    // @OneToMany(mappedBy = "user")
    // private List<Post> posts;
}
