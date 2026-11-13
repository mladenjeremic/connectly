package com.connectly.domain.event;
public record CommentLiked(Long commentId, Long postId, Long actorId, Long commentAuthorId) {}
