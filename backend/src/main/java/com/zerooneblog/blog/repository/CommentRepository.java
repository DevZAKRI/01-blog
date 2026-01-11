package com.zerooneblog.blog.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.zerooneblog.blog.model.Comment;
import com.zerooneblog.blog.model.Post;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByPost(Post post, Pageable pageable);
    long countByPost(Post post);
}
