package com.zerooneblog.blog.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.zerooneblog.blog.model.Notification;
import com.zerooneblog.blog.model.User;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByReceiver(User receiver, Pageable pageable);
    java.util.List<Notification> findByReceiverAndIsReadFalse(User receiver);
    long countByReceiverAndIsReadFalse(User receiver);
}
