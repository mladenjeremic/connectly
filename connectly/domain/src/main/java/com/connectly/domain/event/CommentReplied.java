package com.connectly.domain.event;
public record CommentReplied(Long commentId, Long postId, Long actorId, Long repliedToAuthorId) {}
