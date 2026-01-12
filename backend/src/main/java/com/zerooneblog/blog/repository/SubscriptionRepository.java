package com.zerooneblog.blog.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.zerooneblog.blog.model.Subscription;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findByUserIdAndSubscriberId(Long userId, Long subscriberId);
    
    List<Subscription> findBySubscriberId(Long subscriberId);
    
    long countByUserId(Long userId);
    
    long countBySubscriberId(Long subscriberId);
    
    boolean existsByUserIdAndSubscriberId(Long userId, Long subscriberId);
    
    void deleteByUserIdAndSubscriberId(Long userId, Long subscriberId);
    
    // Delete all subscriptions for a user (when user is deleted)
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    void deleteByUserId(Long userId);
    
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    void deleteBySubscriberId(Long subscriberId);
}
