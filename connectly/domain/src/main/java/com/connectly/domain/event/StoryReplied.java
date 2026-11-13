package com.connectly.domain.event;
public record StoryReplied(Long storyId, Long actorId, Long storyAuthorId, String content) {}
