// src/main/java/com/routelink/rating/RatingDto.java
package com.routelink.rating;

import java.time.Instant;

public record RatingDto(
    Long id, Long bookingId, Long raterId, Long rateeId,
    int stars, String comment, Instant createdAt
) {
  public static RatingDto from(Rating r) {
    return new RatingDto(
        r.getId(),
        r.getBooking().getId(),
        r.getRater().getId(),
        r.getRatee().getId(),
        r.getStars(),
        r.getComment(),
        r.getCreatedAt()
    );
  }
}
