package com.routelink.trip;

import com.routelink.user.User;
import com.routelink.user.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Service
public class TripService {
  private final TripRepository trips;
  private final UserRepository users;

  public TripService(TripRepository trips, UserRepository users) {
    this.trips = trips;
    this.users = users;
  }

  /* -------- DTO used by TripController.create(...) -------- */
  public record CreateTrip(
      String startPlace, double startLat, double startLng,
      String endPlace,   double endLat,   double endLng,
      String polyline,
      OffsetDateTime rideAt,
      BigDecimal pricePerSeat,
      int seatsTotal
  ) {}

  /* ----------------- helpers ----------------- */
  private static String currentEmail() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || auth.getName() == null) throw new IllegalStateException("Unauthenticated");
    return auth.getName();
  }

  private User requireCurrentUser() {
    String email = currentEmail();
    return users.findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));
  }

  private void ensureDriverOwns(Trip t, Long meId) {
    if (t.getDriver() == null || !t.getDriver().getId().equals(meId)) {
      throw new IllegalStateException("Only the driver who posted this trip can perform this action");
    }
  }

  /* ----------------- commands ----------------- */

  @Transactional
  public Trip create(CreateTrip req) {
    if (req == null) throw new IllegalArgumentException("Request is required");
    if (req.seatsTotal() <= 0) throw new IllegalArgumentException("seatsTotal must be > 0");
    if (req.rideAt() == null) throw new IllegalArgumentException("rideAt is required");
    if (req.pricePerSeat() == null || req.pricePerSeat().signum() < 0)
      throw new IllegalArgumentException("pricePerSeat must be >= 0");

    User driver = requireCurrentUser();

    Trip t = new Trip();
    t.setDriver(driver);

    t.setStartPlace(req.startPlace());
    t.setStartLat(req.startLat());
    t.setStartLng(req.startLng());

    t.setEndPlace(req.endPlace());
    t.setEndLat(req.endLat());
    t.setEndLng(req.endLng());

    t.setPolyline(req.polyline());
    t.setRideAt(req.rideAt());
    t.setPricePerSeat(req.pricePerSeat());

    t.setSeatsTotal(req.seatsTotal());
    t.setSeatsLeft(req.seatsTotal());
    t.setStatus(TripStatus.OPEN);
    t.setActive(true);

    return trips.save(t);
  }

  @Transactional
  public Trip close(Long id) {
    Trip t = trips.findByIdForUpdate(id)
        .orElseThrow(() -> new IllegalArgumentException("Trip not found"));
    Long me = requireCurrentUser().getId();
    ensureDriverOwns(t, me);

    if (t.getStatus() == TripStatus.CLOSED && !t.isActive()) {
      return t; // idempotent
    }
    t.setStatus(TripStatus.CLOSED);
    t.setActive(false);
    return t; // managed; flushed on commit
  }

  @Transactional
  public Trip reopen(Long id) {
    Trip t = trips.findByIdForUpdate(id)
        .orElseThrow(() -> new IllegalArgumentException("Trip not found"));
    Long me = requireCurrentUser().getId();
    ensureDriverOwns(t, me);

    if (t.getSeatsLeft() <= 0) throw new IllegalStateException("Cannot reopen: no seats left");
    if (t.getStatus() != TripStatus.OPEN) t.setStatus(TripStatus.OPEN);
    t.setActive(true);
    return t;
  }
}
