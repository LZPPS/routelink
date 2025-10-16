package com.routelink.booking;

import java.time.Instant;

public record BookingWithRiderDto(
    Long id,
    Long tripId,
    int seats,
    BookingStatus status,
    Instant createdAt,
    RiderMini rider
) {
  public static BookingWithRiderDto from(Booking b) {
    RiderMini rm = (b.getRider() == null)
        ? null
        : new RiderMini(b.getRider().getId(), b.getRider().getName(), b.getRider().getEmail());
    return new BookingWithRiderDto(
        b.getId(),
        (b.getTrip() != null ? b.getTrip().getId() : null),
        b.getSeats(),
        b.getStatus(),
        b.getCreatedAt(),
        rm
    );
  }

  public record RiderMini(Long id, String name, String email) {}
}
