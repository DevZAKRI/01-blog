package com.zerooneblog.blog.service;

import java.util.Set;
import java.util.logging.Logger;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.zerooneblog.blog.exception.NotFoundException;
import com.zerooneblog.blog.model.Post;
import com.zerooneblog.blog.model.User;
import com.zerooneblog.blog.repository.PostRepository;

@Service
public class PostService {
    private static final Logger logger = Logger.getLogger(PostService.class.getName());
    private final PostRepository postRepository;
    private final NotificationService notificationService;
    private final com.zerooneblog.blog.repository.SubscriptionRepository subscriptionRepository;
    private final com.zerooneblog.blog.repository.UserRepository userRepository;

    public PostService(PostRepository postRepository, NotificationService notificationService,
                      com.zerooneblog.blog.repository.SubscriptionRepository subscriptionRepository,
                      com.zerooneblog.blog.repository.UserRepository userRepository) {
        this.postRepository = postRepository;
        this.notificationService = notificationService;
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
    }

    public Post create(Post p) {
        logger.info("[PostService] create() - Step 1: Checking if author is banned");
        if (p.getAuthor() != null && p.getAuthor().isBanned()) {
            logger.severe("[PostService] ERROR: Author is banned");
            throw new NotFoundException("User not found");
        }
        
        logger.info("[PostService] create() - Step 2: Saving post to database");
        logger.fine("[PostService] Post details - Title: " + p.getTitle() + ", Author ID: " + p.getAuthor().getId());
        Post saved = postRepository.save(p);
        logger.info("[PostService] create() - Step 3: Post saved with ID: " + saved.getId());
        
        // notify subscribers
        logger.info("[PostService] create() - Step 4: Notifying subscribers");
        User author = saved.getAuthor();
        
        // Get all subscriber IDs
        java.util.List<com.zerooneblog.blog.model.Subscription> subscriptions = 
            subscriptionRepository.findAll().stream()
                .filter(s -> s.getUserId().equals(author.getId()))
                .toList();
        
        logger.info("[PostService] create() - Subscriber count: " + subscriptions.size());
        
        if (!subscriptions.isEmpty()) {
            subscriptions.forEach(sub -> {
                try {
                    User subscriber = userRepository.findById(sub.getSubscriberId()).orElse(null);
                    if (subscriber != null) {
                        notificationService.createNotification(subscriber, "new_post", 
                            "New post from " + author.getUsername());
                    }
                } catch (Exception e) {
                    logger.severe("[PostService] ERROR: Failed to notify user " + sub.getSubscriberId() + ": " + e.getMessage());
                }
            });
        }
        logger.info("[PostService] create() - Step 5: All notifications sent");
        return saved;
    }

    public Post edit(Long id, Post updated, User requester) {
        logger.info("[PostService] edit() - Editing post ID: " + id);
        if (requester == null || requester.isBanned()) {
            logger.severe("[PostService] edit() - Requester is null or banned");
            throw new NotFoundException("Post not found");
        }
        Post existing = postRepository.findById(id).orElseThrow(() -> {
            logger.severe("[PostService] edit() - Post not found: " + id);
            return new NotFoundException("Post not found");
        });
        if (!existing.getAuthor().getId().equals(requester.getId()) && !"ADMIN".equals(requester.getRole())) {
            logger.severe("[PostService] edit() - User not authorized to edit post");
            throw new NotFoundException("Post not found");
        }
        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        existing.setMediaUrls(updated.getMediaUrls());
        Post saved = postRepository.save(existing);
        logger.info("[PostService] edit() - Post edited successfully");
        return saved;
    }

    public void delete(Long id, User requester) {
        logger.info("[PostService] delete() - Deleting post ID: " + id);
        if (requester == null || requester.isBanned()) {
            logger.severe("[PostService] delete() - Requester is null or banned");
            throw new NotFoundException("Post not found");
        }
        Post existing = postRepository.findById(id).orElseThrow(() -> {
            logger.severe("[PostService] delete() - Post not found: " + id);
            return new NotFoundException("Post not found");
        });
        if (!existing.getAuthor().getId().equals(requester.getId()) && !"ADMIN".equals(requester.getRole())) {
            logger.severe("[PostService] delete() - User not authorized to delete post");
            throw new NotFoundException("Post not found");
        }
        postRepository.delete(existing);
        logger.info("[PostService] delete() - Post deleted successfully");
    }

    public Post getById(Long id) {
        logger.info("[PostService] getById() - Fetching post ID: " + id);
        return postRepository.findById(id).orElseThrow(() -> {
            logger.severe("[PostService] getById() - Post not found: " + id);
            return new NotFoundException("Post not found");
        });
    }

    public Post getByIdVisibleTo(Long id, User requester) {
        Post p = postRepository.findById(id).orElseThrow(() -> new NotFoundException("Post not found"));
        if (p.isHidden() && (requester == null || !"ADMIN".equals(requester.getRole()))) {
            throw new NotFoundException("Post not found");
        }
        return p;
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Page<Post> feedFor(User user, Pageable pageable) {
        // Get all subscriptions where the current user is the subscriber
        java.util.List<com.zerooneblog.blog.model.Subscription> subscriptions = 
            subscriptionRepository.findBySubscriberId(user.getId());
        
        if (subscriptions.isEmpty()) {
            return Page.empty(pageable);
        }
        
        // Extract the user IDs that the current user is subscribed to
        java.util.List<Long> subscribedUserIds = subscriptions.stream()
            .map(com.zerooneblog.blog.model.Subscription::getUserId)
            .toList();
        
        // Get users from IDs
        java.util.Set<User> subscribedUsers = new java.util.HashSet<>(userRepository.findAllById(subscribedUserIds));
        return postRepository.findByAuthorInAndHiddenFalse(subscribedUsers, pageable);
    }
}
