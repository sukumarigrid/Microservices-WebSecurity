package org.example;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public List<PostResponse> listPosts() {
        return postService.listPosts();
    }

    @GetMapping("/{postId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public PostResponse getPost(@PathVariable long postId) {
        return postService.getPost(postId);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public PostResponse createPost(@Valid @RequestBody CreatePostRequest request, Authentication authentication) {
        return postService.createPost(authentication.getName(), request);
    }

    @PutMapping("/{postId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public PostResponse updatePost(@PathVariable long postId,
                                   @Valid @RequestBody UpdatePostRequest request,
                                   Authentication authentication) {
        return postService.updatePost(postId, authentication, request);
    }

    @DeleteMapping("/{postId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public void deletePost(@PathVariable long postId, Authentication authentication) {
        postService.deletePost(postId, authentication);
    }

    @PostMapping("/{postId}/likes")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public LikeResponse likePost(@PathVariable long postId, Authentication authentication) {
        return postService.likePost(postId, authentication);
    }
}
