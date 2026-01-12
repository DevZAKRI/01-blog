package com.zerooneblog.blog.service;

import java.util.logging.Logger;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.zerooneblog.blog.model.Notification;
import com.zerooneblog.blog.model.User;
import com.zerooneblog.blog.repository.NotificationRepository;

@Service
public class NotificationService {
    private static final Logger logger = Logger.getLogger(NotificationService.class.getName());
    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public Notification createNotification(User receiver, String type, String content) {
        logger.info("[NotificationService] createNotification() - Creating notification for user: " + receiver.getUsername() + ", type: " + type);
        try {
            Notification n = new Notification();
            n.setReceiver(receiver);
            n.setType(type);
            n.setContent(content);
            Notification saved = notificationRepository.save(n);
            logger.info("[NotificationService] createNotification() - Notification created with ID: " + saved.getId());
            return saved;
        } catch (Exception e) {
            logger.severe("[NotificationService] createNotification() - Error creating notification: " + e.getMessage());
            throw e;
        }
    }

    public Page<Notification> list(User receiver, Pageable pageable) {
        logger.info("[NotificationService] list() - Listing notifications for user: " + receiver.getUsername());
        try {
            Page<Notification> result = notificationRepository.findByReceiver(receiver, pageable);
            logger.info("[NotificationService] list() - Found " + result.getTotalElements() + " total notifications");
            return result;
        } catch (Exception e) {
            logger.severe("[NotificationService] list() - Error listing notifications: " + e.getMessage());
            throw e;
        }
    }

    public Notification markRead(Long id, User receiver, boolean read) {
        logger.info("[NotificationService] markRead() - Marking notification ID: " + id + " as " + (read ? "read" : "unread"));
        try {
            Notification n = notificationRepository.findById(id).orElseThrow(() -> {
                logger.severe("[NotificationService] markRead() - Notification not found: " + id);
                return new IllegalArgumentException("Notification not found");
            });
            if (!n.getReceiver().getId().equals(receiver.getId())) {
                logger.severe("[NotificationService] markRead() - User not authorized to modify this notification");
                throw new IllegalArgumentException("Not allowed");
            }
            n.setRead(read);
            Notification saved = notificationRepository.save(n);
            logger.info("[NotificationService] markRead() - Notification updated successfully");
            return saved;
        } catch (Exception e) {
            logger.severe("[NotificationService] markRead() - Error marking notification: " + e.getMessage());
            throw e;
        }
    }

    public long countUnread(User receiver) {
        logger.info("[NotificationService] countUnread() - Counting unread notifications for user: " + receiver.getUsername());
        try {
            long count = notificationRepository.countByReceiverAndIsReadFalse(receiver);
            logger.info("[NotificationService] countUnread() - Unread count: " + count);
            return count;
        } catch (Exception e) {
            logger.severe("[NotificationService] countUnread() - Error counting unread: " + e.getMessage());
            throw e;
        }
    }

    public void markAllRead(User receiver) {
        logger.info("[NotificationService] markAllRead() - Marking all notifications as read for user: " + receiver.getUsername());
        try {
            var list = notificationRepository.findByReceiverAndIsReadFalse(receiver);
            if (list.isEmpty()) {
                logger.info("[NotificationService] markAllRead() - No unread notifications found");
                return;
            }
            list.forEach(n -> n.setRead(true));
            notificationRepository.saveAll(list);
            logger.info("[NotificationService] markAllRead() - Marked " + list.size() + " notifications as read");
        } catch (Exception e) {
            logger.severe("[NotificationService] markAllRead() - Error marking all as read: " + e.getMessage());
            throw e;
        }
    }
}
