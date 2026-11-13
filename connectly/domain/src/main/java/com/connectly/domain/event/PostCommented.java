package com.connectly.domain.event;
public record PostCommented(Long postId, Long actorId, Long postAuthorId) {}