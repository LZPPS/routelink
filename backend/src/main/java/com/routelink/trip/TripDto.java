package com.routelink.trip;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record TripDto(
    Long id,
    Long driverId,
    String startPlace, double startLat, double startLng,
    String endPlace,   double endLat,   double endLng,
    String polyline,
    OffsetDateTime rideAt,
    BigDecimal pricePerSeat,
    int seatsTotal,
    int seatsLeft,
    TripStatus status,
    boolean active
) {
  public static TripDto from(Trip t) {
    return new TripDto(
        t.getId(),
        (t.getDriver() != null ? t.getDriver().getId() : null),
        t.getStartPlace(), t.getStartLat(), t.getStartLng(),
        t.getEndPlace(),   t.getEndLat(),   t.getEndLng(),
        t.getPolyline(),
        t.getRideAt(),
        t.getPricePerSeat(),
        t.getSeatsTotal(),
        t.getSeatsLeft(),
        t.getStatus(),
        t.isActive()
    );
  }
}
