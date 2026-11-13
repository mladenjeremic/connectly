package com.connectly.domain.model;

import com.connectly.domain.enums.Role;

public record TokenClaims(Long userId, Role role) {}