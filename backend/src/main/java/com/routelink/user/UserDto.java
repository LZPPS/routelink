package com.routelink.user;

import java.time.Instant;

public record UserDto(
    Long id,
    String name,
    String email,
    String phone,
    String role,
    boolean verified,
    double ratingAvg,
    int ratingCount,
    Instant createdAt
) {
  public static UserDto from(User u) {
    return new UserDto(
        u.getId(),
        u.getName(),
        u.getEmail(),
        u.getPhone(),
        u.getRole() != null ? u.getRole().name() : null,
        u.isVerified(),
        u.getRatingAvg(),
        u.getRatingCount(),
        u.getCreatedAt()
    );
  }
}
