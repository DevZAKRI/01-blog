package com.zerooneblog.blog.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.zerooneblog.blog.model.Notification;
import com.zerooneblog.blog.model.User;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByReceiverOrderByCreatedAtDesc(User receiver, Pageable pageable);
    java.util.List<Notification> findByReceiverAndIsReadFalse(User receiver);
    long countByReceiverAndIsReadFalse(User receiver);
    
    // For removing follow notifications on unfollow
    void deleteByReceiverAndTypeAndActorId(User receiver, String type, Long actorId);
    
    // Find notifications by receiver and type
    java.util.List<Notification> findByReceiverAndType(User receiver, String type);
    
    // Delete all notifications for a user
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    void deleteByReceiver(User receiver);
}
