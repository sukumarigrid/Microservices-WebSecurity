package org.example;

public record LikeResponse(
        long postId,
        int likeCount,
        boolean newlyLiked
) {
}
