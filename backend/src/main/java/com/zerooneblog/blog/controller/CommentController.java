package com.zerooneblog.blog.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
    private final CommentService commentService;
    private final UserRepository userRepository;

    public CommentController(CommentService commentService, UserRepository userRepository) {
        this.commentService = commentService;
        this.userRepository = userRepository;
    }

    private User currentUser(Authentication auth) { return userRepository.findByUsername(auth.getName()).orElseThrow(); }

    @PostMapping
    public ResponseEntity<CommentDto> add(@PathVariable Long postId, @jakarta.validation.Valid @org.springframework.web.bind.annotation.RequestBody com.zerooneblog.blog.dto.request.CreateCommentRequest req, Authentication auth) {
        Comment c = commentService.addComment(postId, currentUser(auth), req.getText());
        return ResponseEntity.ok(EntityMapper.toDto(c));
    }

    @GetMapping
    public Page<CommentDto> list(@PathVariable Long postId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return commentService.listComments(postId, PageRequest.of(page, size)).map(EntityMapper::toDto);
    }
}
