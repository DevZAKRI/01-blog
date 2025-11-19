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
        Post post = postRepository.findById(postId).orElseThrow(() -> new NotFoundException("Post not found"));
        Comment c = new Comment();
        c.setPost(post);
        c.setUser(user);
        c.setText(text);
        Comment saved = commentRepository.save(c);
        notificationService.createNotification(post.getAuthor(), "new_comment", "New comment on your post by " + user.getUsername());
        return saved;
    }

    public Page<Comment> listComments(Long postId, Pageable pageable) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new NotFoundException("Post not found"));
        return commentRepository.findByPost(post, pageable);
    }
}
