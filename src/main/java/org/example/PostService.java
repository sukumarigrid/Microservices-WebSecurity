package org.example;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class PostService {

    private static final String ADMIN_AUTHORITY = "ROLE_ADMIN";

    private final PostRepository postRepository;
    private final NotificationService notificationService;
    private final ServiceTokenService serviceTokenService;

    public PostService(PostRepository postRepository,
                       NotificationService notificationService,
                       ServiceTokenService serviceTokenService) {
        this.postRepository = postRepository;
        this.notificationService = notificationService;
        this.serviceTokenService = serviceTokenService;
    }

    public PostResponse createPost(String author, CreatePostRequest request) {
        return toResponse(postRepository.create(author, request.title(), request.body()));
    }

    public List<PostResponse> listPosts() {
        return postRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public PostResponse getPost(long postId) {
        return toResponse(requirePost(postId));
    }

    public PostResponse updatePost(long postId, Authentication authentication, UpdatePostRequest request) {
        Post post = requirePost(postId);
        ensureCanModify(authentication, post);
        post.update(request.title(), request.body());
        return toResponse(post);
    }

    public void deletePost(long postId, Authentication authentication) {
        Post post = requirePost(postId);
        ensureCanModify(authentication, post);
        postRepository.delete(postId);
    }

    public LikeResponse likePost(long postId, Authentication authentication) {
        Post post = requirePost(postId);
        boolean newlyLiked = post.like(authentication.getName());

        if (newlyLiked && !post.getAuthor().equals(authentication.getName())) {
            Authentication serviceAuthentication = serviceTokenService.createServiceAuthentication("likes-service");
            notificationService.recordInternalNotification(
                    new InternalNotificationRequest(
                            post.getId(),
                            post.getAuthor(),
                            authentication.getName(),
                            "likes-service",
                            authentication.getName() + " liked your post \"" + post.getTitle() + "\""),
                    serviceAuthentication);
        }

        return new LikeResponse(post.getId(), post.likeCount(), newlyLiked);
    }

    public long count() {
        return postRepository.count();
    }

    private Post requirePost(long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
    }

    private void ensureCanModify(Authentication authentication, Post post) {
        if (isAdmin(authentication) || post.getAuthor().equals(authentication.getName())) {
            return;
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the author or an admin can modify this post");
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication != null
                && authentication.getAuthorities().stream().anyMatch(authority -> ADMIN_AUTHORITY.equals(authority.getAuthority()));
    }

    private PostResponse toResponse(Post post) {
        return new PostResponse(
                post.getId(),
                post.getAuthor(),
                post.getTitle(),
                post.getBody(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.likeCount());
    }
}
