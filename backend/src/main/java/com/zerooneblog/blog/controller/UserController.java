package com.zerooneblog.blog.controller;

import java.util.logging.Logger;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zerooneblog.blog.dto.request.UpdateUserRequest;
import com.zerooneblog.blog.dto.response.UserDto;
import com.zerooneblog.blog.mapper.EntityMapper;
import com.zerooneblog.blog.model.User;
import com.zerooneblog.blog.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private static final Logger logger = Logger.getLogger(UserController.class.getName());
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public UserDto publicProfile(@PathVariable Long id, Authentication auth) {
        logger.info("[UserController] GET /users/{id} - Fetching user with id: " + id);
        try {
            User u = userService.findById(id);
            logger.info("[UserController] User found: " + u.getUsername());
            
            UserDto dto = EntityMapper.toDto(u);
            dto.setSubscriberIds(new java.util.HashSet<>());
            dto.setSubscriptionIds(new java.util.HashSet<>());
            
            // Set subscriber count (followers)
            long subscriberCount = userService.getSubscriberCount(id);
            dto.setSubscribersCount(subscriberCount);
            
            // Set subscriptions count (following) - count how many users this user is subscribed to
            long subscriptionsCount = userService.getSubscriptionsCount(id);
            dto.setSubscriptionsCount(subscriptionsCount);
            
            // Check if current user is subscribed
            if (auth != null && auth.getName() != null) {
                try {
                    User currentUser = userService.findByEmail(auth.getName());
                    boolean isSubscribed = userService.isSubscribed(id, currentUser.getId());
                    logger.info("[UserController] isSubscribed check: userId=" + id + ", subscriberId=" + currentUser.getId() + ", result=" + isSubscribed);
                    dto.setSubscribed(isSubscribed);
                } catch (Exception e) {
                    logger.warning("[UserController] Could not check subscription status: " + e.getMessage());
                    dto.setSubscribed(false);
                }
            } else {
                dto.setSubscribed(false);
                logger.info("[UserController] No authentication, setting isSubscribed=false for userId=" + id);
            }
            
            return dto;
        } catch (Exception e) {
            logger.severe("[UserController] Error fetching user " + id + ": " + e.getMessage());
            throw e;
        }
    }

    @PostMapping("/{id}/subscribe")
    public ResponseEntity<?> subscribe(@PathVariable Long id, Authentication auth) {
        logger.info("[UserController] POST /users/{id}/subscribe - User: " + auth.getName() + " subscribing to user id: " + id);
        try {
            User subscriber = userService.findByEmail(auth.getName());
            logger.fine("[UserController] Subscriber found: " + subscriber.getUsername());
            userService.subscribe(id, subscriber.getId());
            logger.info("[UserController] Subscription successful");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.severe("[UserController] Subscribe failed: " + e.getMessage());
            throw e;
        }
    }

    @PostMapping("/{id}/unsubscribe")
    public ResponseEntity<?> unsubscribe(@PathVariable Long id, Authentication auth) {
        logger.info("[UserController] POST /users/{id}/unsubscribe - User: " + auth.getName() + " unsubscribing from user id: " + id);
        try {
            User subscriber = userService.findByEmail(auth.getName());
            logger.fine("[UserController] Subscriber found: " + subscriber.getUsername());
            userService.unsubscribe(id, subscriber.getId());
            logger.info("[UserController] Unsubscription successful");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.severe("[UserController] Unsubscribe failed: " + e.getMessage());
            throw e;
        }
    }

    @GetMapping
    public Page<UserDto> listUsers(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, Authentication auth) {
        logger.info("[UserController] GET /users - Listing users - page: " + page + ", size: " + size);
        try {
            User currentUser = null;
            if (auth != null && auth.getName() != null) {
                try {
                    currentUser = userService.findByEmail(auth.getName());
                } catch (Exception e) {
                    logger.warning("[UserController] Could not get current user: " + e.getMessage());
                }
            }
            
            final User finalCurrentUser = currentUser;
            Page<UserDto> result = userService.listAll(PageRequest.of(page, size)).map(user -> {
                UserDto dto = EntityMapper.toDto(user);
                dto.setSubscriberIds(new java.util.HashSet<>());
                dto.setSubscriptionIds(new java.util.HashSet<>());
                
                if (finalCurrentUser != null) {
                    boolean isSubscribed = userService.isSubscribed(user.getId(), finalCurrentUser.getId());
                    logger.info("[UserController] listUsers - userId=" + user.getId() + ", subscriberId=" + finalCurrentUser.getId() + ", isSubscribed=" + isSubscribed);
                    dto.setSubscribed(isSubscribed);
                } else {
                    dto.setSubscribed(false);
                }
                return dto;
            });
            logger.info("[UserController] Users listed successfully - Total: " + result.getTotalElements() + ", Current page: " + result.getContent().size());
            return result;
        } catch (Exception e) {
            logger.severe("[UserController] Error listing users: " + e.getMessage());
            throw e;
        }
    }

    @GetMapping("/{authorId}/posts")
    public org.springframework.http.ResponseEntity<?> listUserPosts(@PathVariable Long authorId, Authentication auth,
                                               @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        // Public endpoint - anyone can view a user's posts
        var result = userService.listPostsByAuthor(authorId, PageRequest.of(page, size)).map(p -> {
            // Include like status if user is authenticated
            if (auth != null && auth.getName() != null) {
                try {
                    User currentUser = userService.findByEmail(auth.getName());
                    return com.zerooneblog.blog.mapper.EntityMapper.toDto(p, currentUser);
                } catch (Exception e) {
                    return com.zerooneblog.blog.mapper.EntityMapper.toDto(p);
                }
            }
            return com.zerooneblog.blog.mapper.EntityMapper.toDto(p);
        });
        if (result.getTotalElements() == 0) {
            return org.springframework.http.ResponseEntity.ok(java.util.Map.of(
                "message", "No posts found for this author",
                "content", result.getContent(),
                "totalElements", result.getTotalElements()
            ));
        }
        return org.springframework.http.ResponseEntity.ok(result);
    }

    @PutMapping("/me")
    public ResponseEntity<UserDto> updateMe(@Valid @RequestBody UpdateUserRequest req, Authentication auth) {
        User user = userService.findByEmail(auth.getName());
        User updated = userService.updateProfile(user, req);
        return ResponseEntity.ok(EntityMapper.toDto(updated));
    }
}
