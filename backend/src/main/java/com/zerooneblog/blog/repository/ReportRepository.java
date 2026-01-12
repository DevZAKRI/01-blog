package com.zerooneblog.blog.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import com.zerooneblog.blog.model.Post;
import com.zerooneblog.blog.model.Report;
import com.zerooneblog.blog.model.User;

public interface ReportRepository extends JpaRepository<Report, Long> {
    Page<Report> findByStatus(String status, Pageable pageable);
    long countByStatus(String status);
    
    @Modifying
    @Transactional
    void deleteByReporter(User reporter);
    
    @Modifying
    @Transactional
    void deleteByTargetUser(User targetUser);
    
    @Modifying
    @Transactional
    void deleteByTargetPost(Post targetPost);
}
