package org.example;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Post {

    private final long id;
    private final String author;
    private String title;
    private String body;
    private final Instant createdAt;
    private Instant updatedAt;
    private final Set<String> likedBy = ConcurrentHashMap.newKeySet();

    public Post(long id, String author, String title, String body, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.author = author;
        this.title = title;
        this.body = body;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public synchronized void update(String title, String body) {
        this.title = title;
        this.body = body;
        this.updatedAt = Instant.now();
    }

    public synchronized boolean like(String username) {
        return likedBy.add(username);
    }

    public synchronized int likeCount() {
        return likedBy.size();
    }

    public synchronized List<String> likedBy() {
        return likedBy.stream().sorted(Comparator.naturalOrder()).toList();
    }

    public long getId() {
        return id;
    }

    public String getAuthor() {
        return author;
    }

    public synchronized String getTitle() {
        return title;
    }

    public synchronized String getBody() {
        return body;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public synchronized Instant getUpdatedAt() {
        return updatedAt;
    }
}
