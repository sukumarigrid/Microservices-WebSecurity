package org.example;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class NotificationRepository {

    private final ConcurrentMap<Long, Notification> notifications = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    public Notification save(long postId,
                             String recipientUsername,
                             String actorUsername,
                             String sourceService,
                             String message) {
        long id = sequence.incrementAndGet();
        Notification notification = new Notification(
                id,
                postId,
                recipientUsername,
                actorUsername,
                sourceService,
                message,
                Instant.now());
        notifications.put(id, notification);
        return notification;
    }

    public List<Notification> findAll() {
        return notifications.values().stream()
                .sorted(Comparator.comparingLong(Notification::id))
                .toList();
    }

    public List<Notification> findForRecipient(String recipientUsername) {
        return findAll().stream()
                .filter(notification -> notification.recipientUsername().equals(recipientUsername))
                .toList();
    }

    public long count() {
        return notifications.size();
    }
}
