package com.zerooneblog.blog.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.zerooneblog.blog.exception.NotFoundException;
import com.zerooneblog.blog.model.Comment;
import com.zerooneblog.blog.model.Post;
import com.zerooneblog.blog.model.User;
import com.zerooneblog.blog.repository.CommentRepository;
import com.zerooneblog.blog.repository.PostRepository;

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final NotificationService notificationService;

    public CommentService(CommentRepository commentRepository, PostRepository postRepository, NotificationService notificationService) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.notificationService = notificationService;
    }

    public Comment addComment(Long postId, User user, String text) {
        System.out.println("[CommentService] Step 1: Finding post with ID: " + postId);
        Post post = postRepository.findById(postId).orElseThrow(() -> new NotFoundException("Post not found"));
        System.out.println("[CommentService] Step 2: Post found, author: " + post.getAuthor().getUsername());
        
        System.out.println("[CommentService] Step 3: Creating comment object");
        Comment c = new Comment();
        c.setPost(post);
        c.setUser(user);
        c.setText(text);
        
        System.out.println("[CommentService] Step 4: Saving comment to database");
        Comment saved = commentRepository.save(c);
        System.out.println("[CommentService] Step 5: Comment saved with ID: " + saved.getId());
        
        System.out.println("[CommentService] Step 6: Notifying post author");
        try {
            notificationService.createNotification(post.getAuthor(), "new_comment", "New comment on your post by " + user.getUsername());
            System.out.println("[CommentService] Step 7: Notification sent successfully");
        } catch (Exception e) {
            System.err.println("[CommentService] ERROR: Failed to send notification: " + e.getMessage());
        }
        return saved;
    }

    public Page<Comment> listComments(Long postId, Pageable pageable) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new NotFoundException("Post not found"));
        return commentRepository.findByPost(post, pageable);
    }

    public void deleteComment(Long postId, Long commentId, User requester) {
        Comment c = commentRepository.findById(commentId).orElseThrow(() -> new NotFoundException("Comment not found"));
        if (!c.getPost().getId().equals(postId)) {
            throw new NotFoundException("Comment not found");
        }
        if (!c.getUser().getId().equals(requester.getId()) && !"ADMIN".equals(requester.getRole())) {
            throw new NotFoundException("Comment not found");
        }
        commentRepository.delete(c);
    }
}
