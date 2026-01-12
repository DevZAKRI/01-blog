package com.zerooneblog.blog.service;

import java.util.logging.Logger;

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
    private static final Logger logger = Logger.getLogger(CommentService.class.getName());
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final NotificationService notificationService;

    public CommentService(CommentRepository commentRepository, PostRepository postRepository, NotificationService notificationService) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.notificationService = notificationService;
    }

    public Comment addComment(Long postId, User user, String text) {
        logger.info("[CommentService] addComment() - Step 1: Finding post with ID: " + postId);
        Post post = postRepository.findById(postId).orElseThrow(() -> {
            logger.severe("[CommentService] addComment() - Post not found: " + postId);
            return new NotFoundException("Post not found");
        });
        logger.fine("[CommentService] addComment() - Step 2: Post found, author: " + post.getAuthor().getUsername());
        
        logger.fine("[CommentService] addComment() - Step 3: Creating comment object");
        Comment c = new Comment();
        c.setPost(post);
        c.setUser(user);
        c.setText(text);
        
        logger.info("[CommentService] addComment() - Step 4: Saving comment to database");
        Comment saved = commentRepository.save(c);
        logger.info("[CommentService] addComment() - Step 5: Comment saved with ID: " + saved.getId());
        
        logger.info("[CommentService] addComment() - Step 6: Notifying post author");
        try {
            notificationService.createNotification(post.getAuthor(), "new_comment", "New comment on your post by " + user.getUsername());
            logger.fine("[CommentService] addComment() - Step 7: Notification sent successfully");
        } catch (Exception e) {
            logger.severe("[CommentService] addComment() - ERROR: Failed to send notification: " + e.getMessage());
        }
        return saved;
    }

    public Page<Comment> listComments(Long postId, Pageable pageable) {
        logger.info("[CommentService] listComments() - Listing comments for post ID: " + postId);
        Post post = postRepository.findById(postId).orElseThrow(() -> {
            logger.severe("[CommentService] listComments() - Post not found: " + postId);
            return new NotFoundException("Post not found");
        });
        Page<Comment> result = commentRepository.findByPost(post, pageable);
        logger.info("[CommentService] listComments() - Found " + result.getTotalElements() + " total comments");
        return result;
    }

    public void deleteComment(Long postId, Long commentId, User requester) {
        logger.info("[CommentService] deleteComment() - Deleting comment ID: " + commentId + " from post ID: " + postId);
        Comment c = commentRepository.findById(commentId).orElseThrow(() -> {
            logger.severe("[CommentService] deleteComment() - Comment not found: " + commentId);
            return new NotFoundException("Comment not found");
        });
        if (!c.getPost().getId().equals(postId)) {
            logger.severe("[CommentService] deleteComment() - Comment does not belong to post");
            throw new NotFoundException("Comment not found");
        }
        if (!c.getUser().getId().equals(requester.getId()) && !"ADMIN".equals(requester.getRole())) {
            logger.severe("[CommentService] deleteComment() - User not authorized to delete comment");
            throw new NotFoundException("Comment not found");
        }
        commentRepository.delete(c);
        logger.info("[CommentService] deleteComment() - Comment deleted successfully");
    }
}
