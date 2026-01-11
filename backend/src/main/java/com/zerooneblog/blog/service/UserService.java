package com.zerooneblog.blog.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.zerooneblog.blog.dto.request.UpdateUserRequest;
import com.zerooneblog.blog.exception.NotFoundException;
import com.zerooneblog.blog.model.Post;
import com.zerooneblog.blog.model.User;
import com.zerooneblog.blog.repository.PostRepository;
import com.zerooneblog.blog.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final NotificationService notificationService;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PostRepository postRepository, NotificationService notificationService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.notificationService = notificationService;
        this.passwordEncoder = passwordEncoder;
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
    }

    public Page<Post> listPosts(Long userId, Pageable pageable) {
        User u = findById(userId);
        return postRepository.findByAuthorAndHiddenFalse(u, pageable);
    }

    public void subscribe(User target, User subscriber) {
        if (target.getId().equals(subscriber.getId())) throw new IllegalArgumentException("Cannot subscribe to yourself");
        if (target.getSubscribers().add(subscriber)) {
            userRepository.save(target);
            // trigger notification to the target user about new subscriber
            try {
                notificationService.createNotification(target, "new_subscriber", "@" + subscriber.getUsername() + " started following you.");
            } catch (Exception e) {
                // do not block subscribe on notification failure
                org.slf4j.LoggerFactory.getLogger(UserService.class).warn("Failed to send notification: {}", e.getMessage());
            }
        }
    }

    public void unsubscribe(User target, User subscriber) {
        if (target.getSubscribers().remove(subscriber)) {
            userRepository.save(target);
        }
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new NotFoundException("User not found"));
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new NotFoundException("User not found"));
    }

    public Page<User> listAll(Pageable pageable) { return userRepository.findAll(pageable); }

    public Page<Post> listPostsByAuthor(Long authorId, Pageable pageable) {
        User author = userRepository.findById(authorId).orElseThrow(() -> new NotFoundException("Author not found"));
        return postRepository.findByAuthorAndHiddenFalse(author, pageable);
    }

    public Page<Post> listPostsForSubscription(User owner, Long authorId, Pageable pageable) {
        User author = userRepository.findById(authorId).orElseThrow(() -> new NotFoundException("Author not found"));
        if (!owner.getSubscriptions().contains(author)) throw new IllegalArgumentException("Not subscribed to this author");
        return postRepository.findByAuthor(author, pageable);
    }

    public User updateProfile(User user, UpdateUserRequest req) {
        if (req.getUsername() != null && !req.getUsername().isBlank()) user.setUsername(req.getUsername());
        if (req.getEmail() != null && !req.getEmail().isBlank()) user.setEmail(req.getEmail());
        if (req.getPassword() != null && !req.getPassword().isBlank()) user.setPassword(passwordEncoder.encode(req.getPassword()));
        if (req.getBio() != null) user.setBio(req.getBio());
        if (req.getAvatar() != null) user.setAvatarUrl(req.getAvatar());
        return userRepository.save(user);
    }
}
