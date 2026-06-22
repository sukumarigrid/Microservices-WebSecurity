package org.example;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class PostRepository {

    private final ConcurrentMap<Long, Post> posts = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    public Post create(String author, String title, String body) {
        long id = sequence.incrementAndGet();
        Instant now = Instant.now();
        Post post = new Post(id, author, title, body, now, now);
        posts.put(id, post);
        return post;
    }

    public Optional<Post> findById(long id) {
        return Optional.ofNullable(posts.get(id));
    }

    public List<Post> findAll() {
        return posts.values().stream()
                .sorted(Comparator.comparingLong(Post::getId))
                .toList();
    }

    public void delete(long id) {
        posts.remove(id);
    }

    public long count() {
        return posts.size();
    }
}
