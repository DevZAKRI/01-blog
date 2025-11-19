package com.zerooneblog.blog.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zerooneblog.blog.model.Comment;
import com.zerooneblog.blog.model.CommentLike;
import com.zerooneblog.blog.model.User;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    Optional<CommentLike> findByUserAndComment(User user, Comment comment);
    long countByComment(Comment comment);
}
