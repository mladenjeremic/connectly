package com.connectly.domain.model;

public record PostImage(Long id, Long postId, String fileName, String contentType, long fileSize, int sortOrder) {}
