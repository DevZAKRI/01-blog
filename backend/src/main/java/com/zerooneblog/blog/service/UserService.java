package com.zerooneblog.blog.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zerooneblog.blog.dto.request.UpdateUserRequest;
import com.zerooneblog.blog.exception.NotFoundException;
import com.zerooneblog.blog.model.Post;
import com.zerooneblog.blog.model.Subscription;
import com.zerooneblog.blog.model.User;
import com.zerooneblog.blog.repository.PostRepository;
import com.zerooneblog.blog.repository.SubscriptionRepository;
import com.zerooneblog.blog.repository.UserRepository;
import com.zerooneblog.blog.util.HtmlSanitizer;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final NotificationService notificationService;
    private final PasswordEncoder passwordEncoder;
    private final HtmlSanitizer htmlSanitizer;

    public UserService(UserRepository userRepository, PostRepository postRepository, 
                      SubscriptionRepository subscriptionRepository, NotificationService notificationService, 
                      PasswordEncoder passwordEncoder, HtmlSanitizer htmlSanitizer) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.notificationService = notificationService;
        this.passwordEncoder = passwordEncoder;
        this.htmlSanitizer = htmlSanitizer;
    }

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
    }

    public Page<Post> listPosts(Long userId, Pageable pageable) {
        User u = findById(userId);
        return postRepository.findByAuthorAndHiddenFalse(u, pageable);
    }

    @Transactional
    public void subscribe(Long userId, Long subscriberId) {
        if (userId.equals(subscriberId)) {
            throw new IllegalArgumentException("Cannot subscribe to yourself");
        }
        
        // Check if already subscribed
        if (subscriptionRepository.existsByUserIdAndSubscriberId(userId, subscriberId)) {
            return; // Already subscribed
        }
        
        // Verify both users exist
        User targetUser = findById(userId);
        User subscriber = findById(subscriberId);
        
        // Create subscription
        Subscription subscription = new Subscription();
        subscription.setUserId(userId);
        subscription.setSubscriberId(subscriberId);
        subscriptionRepository.save(subscription);
        
        // Send notification with actorId
        try {
            notificationService.createNotification(targetUser, "new_subscriber", 
                "@" + subscriber.getUsername() + " started following you.", subscriberId);
        } catch (Exception e) {
            // Don't block subscribe on notification failure
        }
    }

    @Transactional
    public void unsubscribe(Long userId, Long subscriberId) {
        // Delete the subscription
        subscriptionRepository.deleteByUserIdAndSubscriberId(userId, subscriberId);
        
        // Remove the follow notification
        try {
            User targetUser = findById(userId);
            notificationService.deleteNotification(targetUser, "new_subscriber", subscriberId);
        } catch (Exception e) {
            // Don't block unsubscribe on notification failure
        }
    }
    
    public long getSubscriberCount(Long userId) {
        return subscriptionRepository.countByUserId(userId);
    }
    
    public long getSubscriptionsCount(Long userId) {
        return subscriptionRepository.countBySubscriberId(userId);
    }
    
    public boolean isSubscribed(Long userId, Long subscriberId) {
        return subscriptionRepository.existsByUserIdAndSubscriberId(userId, subscriberId);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new NotFoundException("User not found"));
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Transactional(readOnly = true)
    public Page<User> listAll(Pageable pageable) { 
        return userRepository.findAll(pageable); 
    }

    @Transactional(readOnly = true)
    public Page<User> listAll(Pageable pageable, String search) {
        if (search != null && !search.isBlank()) {
            return userRepository.findByUsernameContainingIgnoreCase(search.trim(), pageable);
        }
        return userRepository.findAll(pageable); 
    }

    public Page<Post> listPostsByAuthor(Long authorId, Pageable pageable) {
        User author = userRepository.findById(authorId).orElseThrow(() -> new NotFoundException("Author not found"));
        return postRepository.findByAuthorAndHiddenFalseOrderByCreatedAtDesc(author, pageable);
    }

    public User updateProfile(User user, UpdateUserRequest req) {
        if (req.getUsername() != null && !req.getUsername().isBlank()) {
            user.setUsername(htmlSanitizer.sanitizePlainText(req.getUsername()));
        }
        if (req.getEmail() != null && !req.getEmail().isBlank()) user.setEmail(req.getEmail());
        if (req.getPassword() != null && !req.getPassword().isBlank()) user.setPassword(passwordEncoder.encode(req.getPassword()));
        if (req.getBio() != null) {
            user.setBio(htmlSanitizer.sanitizePlainText(req.getBio()));
        }
        if (req.getAvatar() != null) user.setAvatarUrl(req.getAvatar());
        return userRepository.save(user);
    }
}
