package com.connectly.domain.event;
public record PostLiked(Long postId, Long actorId, Long postAuthorId) {}