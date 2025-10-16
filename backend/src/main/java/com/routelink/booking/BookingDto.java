package com.routelink.booking;

import java.time.Instant;

public record BookingDto(
    Long id,
    Long tripId,
    Long riderId,
    int seats,
    BookingStatus status,
    Instant createdAt
) {
  public static BookingDto from(Booking b) {
    return new BookingDto(
        b.getId(),
        (b.getTrip()  != null ? b.getTrip().getId()  : null),
        (b.getRider() != null ? b.getRider().getId() : null),
        b.getSeats(),
        b.getStatus(),
        b.getCreatedAt()
    );
  }
}
