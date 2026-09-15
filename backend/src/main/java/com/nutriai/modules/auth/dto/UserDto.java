package com.nutriai.modules.auth.dto;

import com.nutriai.modules.auth.domain.User;
import com.nutriai.modules.auth.domain.UserStatus;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record UserDto(
    UUID id,
    String email,
    UserStatus status,
    Set<String> roles,
    Instant createdAt
) {
    public static UserDto fromEntity(User user) {
        Set<String> roleNames = user.getRoles().stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toSet());
        return new UserDto(
                user.getId(),
                user.getEmail(),
                user.getStatus(),
                roleNames,
                user.getCreatedAt()
        );
    }
}

