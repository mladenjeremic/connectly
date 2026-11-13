package com.connectly.domain.event;

public record AccountStatusChanged(Long userId, String message) {}
