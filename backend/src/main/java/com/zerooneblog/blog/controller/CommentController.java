package com.zerooneblog.blog.controller;

import java.util.logging.Logger;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zerooneblog.blog.dto.response.CommentDto;
import com.zerooneblog.blog.mapper.EntityMapper;
import com.zerooneblog.blog.model.Comment;
import com.zerooneblog.blog.model.User;
import com.zerooneblog.blog.repository.UserRepository;
import com.zerooneblog.blog.service.CommentService;

@RestController
@RequestMapping("/api/v1/posts/{postId}/comments")
public class CommentController {
    private static final Logger logger = Logger.getLogger(CommentController.class.getName());
    private final CommentService commentService;
    private final UserRepository userRepository;
    private final com.zerooneblog.blog.service.CommentLikeService commentLikeService;

    public CommentController(CommentService commentService, UserRepository userRepository, com.zerooneblog.blog.service.CommentLikeService commentLikeService) {
        this.commentService = commentService;
        this.userRepository = userRepository;
        this.commentLikeService = commentLikeService;
    }

    private User currentUser(Authentication auth) { return userRepository.findByEmail(auth.getName()).orElseThrow(); }

    @PostMapping
    public ResponseEntity<CommentDto> add(@PathVariable Long postId, @jakarta.validation.Valid @org.springframework.web.bind.annotation.RequestBody com.zerooneblog.blog.dto.request.CreateCommentRequest req, Authentication auth) {
        try {
            logger.info("[CommentController] POST /posts/{postId}/comments - Step 1: Starting comment creation for post ID: " + postId);
            logger.info("[CommentController] Auth name: " + (auth != null ? auth.getName() : "null"));
            logger.fine("[CommentController] Comment text length: " + (req.getText() != null ? req.getText().length() : 0));
            
            logger.info("[CommentController] Step 2: Getting current user");
            User user = currentUser(auth);
            logger.info("[CommentController] User ID: " + user.getId() + ", username: " + user.getUsername());
            
            logger.info("[CommentController] Step 3: Calling commentService.addComment()");
            Comment c = commentService.addComment(postId, user, req.getText());
            logger.info("[CommentController] Step 4: Comment saved with ID: " + c.getId());
            
            logger.fine("[CommentController] Step 5: Converting to DTO");
            CommentDto dto = EntityMapper.toDto(c);
            logger.info("[CommentController] Step 6: Comment creation successful");
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            logger.severe("[CommentController] ERROR: Exception occurred: " + e.getClass().getName());
            logger.severe("[CommentController] ERROR: Message: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @GetMapping
    public Page<CommentDto> list(@PathVariable Long postId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        logger.info("[CommentController] GET /posts/{postId}/comments - Listing comments for post ID: " + postId + ", page: " + page + ", size: " + size);
        try {
            Page<CommentDto> result = commentService.listComments(postId, PageRequest.of(page, size)).map(EntityMapper::toDto);
            logger.info("[CommentController] Comments listed successfully - Total: " + result.getTotalElements() + ", Current page: " + result.getContent().size());
            return result;
        } catch (Exception e) {
            logger.severe("[CommentController] Error listing comments: " + e.getMessage());
            throw e;
        }
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> delete(@PathVariable Long postId, @PathVariable Long commentId, Authentication auth) {
        logger.info("[CommentController] DELETE /posts/{postId}/comments/{commentId} - Deleting comment ID: " + commentId + " from post ID: " + postId);
        try {
            commentService.deleteComment(postId, commentId, currentUser(auth));
            logger.info("[CommentController] Comment deleted successfully");
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.severe("[CommentController] Error deleting comment: " + e.getMessage());
            throw e;
        }
    }

    @PostMapping("/{commentId}/like")
    public ResponseEntity<?> likeComment(@PathVariable Long postId, @PathVariable Long commentId, Authentication auth) {
        logger.info("[CommentController] POST /posts/{postId}/comments/{commentId}/like - Liking comment ID: " + commentId);
        try {
            commentLikeService.like(commentId, currentUser(auth));
            logger.info("[CommentController] Comment liked successfully");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.severe("[CommentController] Error liking comment: " + e.getMessage());
            throw e;
        }
    }

    @DeleteMapping("/{commentId}/like")
    public ResponseEntity<?> unlikeComment(@PathVariable Long postId, @PathVariable Long commentId, Authentication auth) {
        commentLikeService.unlike(commentId, currentUser(auth));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{commentId}/likes/count")
    public long getCommentLikesCount(@PathVariable Long postId, @PathVariable Long commentId) {
        Comment c = commentService.listComments(postId, PageRequest.of(0, 1)).getContent().stream().filter(cm -> cm.getId().equals(commentId)).findFirst().orElseThrow(() -> new com.zerooneblog.blog.exception.NotFoundException("Comment not found"));
        return commentLikeService.countLikes(c);
    }
}
