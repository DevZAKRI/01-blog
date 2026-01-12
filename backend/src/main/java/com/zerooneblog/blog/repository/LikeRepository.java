package com.zerooneblog.blog.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zerooneblog.blog.model.Post;
import com.zerooneblog.blog.model.PostLike;
import com.zerooneblog.blog.model.User;

public interface LikeRepository extends JpaRepository<PostLike, Long> {
    Optional<PostLike> findByUserAndPost(User user, Post post);
    long countByPost(Post post);
    
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    void deleteByUser(User user);
    
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    void deleteByPost(Post post);
}
