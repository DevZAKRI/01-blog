package com.zerooneblog.blog.service;

import org.springframework.stereotype.Service;

import com.zerooneblog.blog.exception.NotFoundException;
import com.zerooneblog.blog.model.Comment;
import com.zerooneblog.blog.model.CommentLike;
import com.zerooneblog.blog.model.User;
import com.zerooneblog.blog.repository.CommentLikeRepository;
import com.zerooneblog.blog.repository.CommentRepository;

@Service
public class CommentLikeService {
    private final CommentLikeRepository commentLikeRepository;
    private final CommentRepository commentRepository;

    public CommentLikeService(CommentLikeRepository commentLikeRepository, CommentRepository commentRepository) {
        this.commentLikeRepository = commentLikeRepository;
        this.commentRepository = commentRepository;
    }

    public void like(Long commentId, User user) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new NotFoundException("Comment not found"));
        commentLikeRepository.findByUserAndComment(user, comment).ifPresent(l -> { throw new IllegalStateException("Already liked"); });
        CommentLike l = new CommentLike();
        l.setComment(comment);
        l.setUser(user);
        commentLikeRepository.save(l);
    }

    public void unlike(Long commentId, User user) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new NotFoundException("Comment not found"));
        commentLikeRepository.findByUserAndComment(user, comment).ifPresent(commentLikeRepository::delete);
    }

    public long countLikes(Comment comment) { return commentLikeRepository.countByComment(comment); }
}
