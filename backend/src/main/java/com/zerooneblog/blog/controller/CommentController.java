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

    public CommentController(CommentService commentService, UserRepository userRepository) {
        this.commentService = commentService;
        this.userRepository = userRepository;
    }

    private User currentUser(Authentication auth) { return userRepository.findByEmail(auth.getName()).orElseThrow(); }

    @PostMapping
    public ResponseEntity<CommentDto> add(@PathVariable Long postId, @jakarta.validation.Valid @org.springframework.web.bind.annotation.RequestBody com.zerooneblog.blog.dto.request.CreateCommentRequest req, Authentication auth) {
        try {
            logger.info("[CommentController] POST /posts/{postId}/comments - Creating comment for post ID: " + postId);
            User user = currentUser(auth);
            Comment c = commentService.addComment(postId, user, req.getText());
            logger.info("[CommentController] Comment created with ID: " + c.getId());
            CommentDto dto = EntityMapper.toDto(c);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            logger.severe("[CommentController] Error creating comment: " + e.getMessage());
            throw e;
        }
    }

    @GetMapping
    public Page<CommentDto> list(@PathVariable Long postId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        logger.info("[CommentController] GET /posts/{postId}/comments - Listing comments for post ID: " + postId);
        try {
            Page<CommentDto> result = commentService.listComments(postId, PageRequest.of(page, size)).map(EntityMapper::toDto);
            logger.info("[CommentController] Comments listed - Total: " + result.getTotalElements());
            return result;
        } catch (Exception e) {
            logger.severe("[CommentController] Error listing comments: " + e.getMessage());
            throw e;
        }
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> delete(@PathVariable Long postId, @PathVariable Long commentId, Authentication auth) {
        logger.info("[CommentController] DELETE /posts/{postId}/comments/{commentId} - Deleting comment ID: " + commentId);
        try {
            commentService.deleteComment(postId, commentId, currentUser(auth));
            logger.info("[CommentController] Comment deleted successfully");
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.severe("[CommentController] Error deleting comment: " + e.getMessage());
            throw e;
        }
    }
}
