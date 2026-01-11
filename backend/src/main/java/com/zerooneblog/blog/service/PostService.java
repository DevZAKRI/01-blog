package com.zerooneblog.blog.service;

import java.util.HashSet;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.zerooneblog.blog.exception.NotFoundException;
import com.zerooneblog.blog.model.Post;
import com.zerooneblog.blog.model.User;
import com.zerooneblog.blog.repository.PostRepository;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final NotificationService notificationService;

    public PostService(PostRepository postRepository, NotificationService notificationService) {
        this.postRepository = postRepository;
        this.notificationService = notificationService;
    }

    public Post create(Post p) {
        System.out.println("[PostService] Step 1: Checking if author is banned");
        if (p.getAuthor() != null && p.getAuthor().isBanned()) {
            System.err.println("[PostService] ERROR: Author is banned");
            throw new NotFoundException("User not found");
        }
        
        System.out.println("[PostService] Step 2: Saving post to database");
        System.out.println("[PostService] Post details - Title: " + p.getTitle() + ", Author ID: " + p.getAuthor().getId());
        Post saved = postRepository.save(p);
        System.out.println("[PostService] Step 3: Post saved with ID: " + saved.getId());
        
        // notify subscribers
        System.out.println("[PostService] Step 4: Notifying subscribers");
        User author = saved.getAuthor();
        Set<User> receivers = new HashSet<>(author.getSubscribers());
        System.out.println("[PostService] Subscriber count: " + receivers.size());
        
        for (User r : receivers) {
            try {
                notificationService.createNotification(r, "new_post", "New post from " + author.getUsername());
            } catch (Exception e) {
                System.err.println("[PostService] ERROR: Failed to notify user " + r.getId() + ": " + e.getMessage());
            }
        }
        System.out.println("[PostService] Step 5: All notifications sent");
        return saved;
    }

    public Post edit(Long id, Post updated, User requester) {
        if (requester == null || requester.isBanned()) throw new NotFoundException("Post not found");
        Post existing = postRepository.findById(id).orElseThrow(() -> new NotFoundException("Post not found"));
        if (!existing.getAuthor().getId().equals(requester.getId()) && !"ADMIN".equals(requester.getRole())) {
            throw new NotFoundException("Post not found");
        }
        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        existing.setMediaUrls(updated.getMediaUrls());
        return postRepository.save(existing);
    }

    public void delete(Long id, User requester) {
        if (requester == null || requester.isBanned()) throw new NotFoundException("Post not found");
        Post existing = postRepository.findById(id).orElseThrow(() -> new NotFoundException("Post not found"));
        if (!existing.getAuthor().getId().equals(requester.getId()) && !"ADMIN".equals(requester.getRole())) {
            throw new NotFoundException("Post not found");
        }
        postRepository.delete(existing);
    }

    public Post getById(Long id) {
        return postRepository.findById(id).orElseThrow(() -> new NotFoundException("Post not found"));
    }

    public Post getByIdVisibleTo(Long id, User requester) {
        Post p = postRepository.findById(id).orElseThrow(() -> new NotFoundException("Post not found"));
        if (p.isHidden() && (requester == null || !"ADMIN".equals(requester.getRole()))) {
            throw new NotFoundException("Post not found");
        }
        return p;
    }

    public Page<Post> feedFor(User user, Pageable pageable) {
        return postRepository.findByAuthorInAndHiddenFalse(user.getSubscriptions(), pageable);
    }
}
