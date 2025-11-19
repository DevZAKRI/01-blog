package com.zerooneblog.blog.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.zerooneblog.blog.model.Notification;
import com.zerooneblog.blog.model.User;
import com.zerooneblog.blog.repository.NotificationRepository;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public Notification createNotification(User receiver, String type, String content) {
        Notification n = new Notification();
        n.setReceiver(receiver);
        n.setType(type);
        n.setContent(content);
        return notificationRepository.save(n);
    }

    public Page<Notification> list(User receiver, Pageable pageable) {
        return notificationRepository.findByReceiver(receiver, pageable);
    }

    public Notification markRead(Long id, User receiver, boolean read) {
        Notification n = notificationRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        if (!n.getReceiver().getId().equals(receiver.getId())) throw new IllegalArgumentException("Not allowed");
        n.setRead(read);
        return notificationRepository.save(n);
    }

    public long countUnread(User receiver) {
        return notificationRepository.countByReceiverAndIsReadFalse(receiver);
    }

    public void markAllRead(User receiver) {
        var list = notificationRepository.findByReceiverAndIsReadFalse(receiver);
        if (list.isEmpty()) return;
        list.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(list);
    }
}
