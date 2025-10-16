package com.routelink.booking;

import com.routelink.notification.EmailService;
import com.routelink.trip.Trip;
import com.routelink.trip.TripRepository;
import com.routelink.trip.TripStatus;
import com.routelink.user.User;
import com.routelink.user.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static com.routelink.booking.BookingStatus.*;

@Service
public class BookingService {
  private final BookingRepository bookings;
  private final TripRepository trips;
  private final UserRepository users;
  private final EmailService email;

  public BookingService(BookingRepository bookings,
                        TripRepository trips,
                        UserRepository users,
                        EmailService email) {
    this.bookings = bookings;
    this.trips = trips;
    this.users = users;
    this.email = email;
  }

  /** Body for POST /api/bookings/request */
  public static record RequestBooking(Long tripId, int seats) {}

  /* ---------- helpers ---------- */

  private static String currentEmail() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || auth.getName() == null) throw new IllegalStateException("Unauthenticated");
    return auth.getName();
  }

  private User requireUserByEmail(String email) {
    return users.findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));
  }

  private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a");
  private static String orDash(String s) { return (s == null || s.isBlank()) ? "—" : s; }

  /* ---------- Rider actions ---------- */

  @Transactional
  public Booking request(RequestBooking req) {
    if (req == null || req.tripId() == null) throw new IllegalArgumentException("tripId is required");
    int seats = Math.max(1, req.seats());

    Trip trip = trips.findById(req.tripId())
        .orElseThrow(() -> new IllegalArgumentException("Trip not found: " + req.tripId()));

    User rider = requireUserByEmail(currentEmail());

    if (!trip.isActive() || trip.getStatus() != TripStatus.OPEN)
      throw new IllegalStateException("Trip not bookable");

    if (trip.getDriver().getId().equals(rider.getId()))
      throw new IllegalStateException("Driver cannot book own trip");

    Optional<Booking> existingOpt = bookings.findByTrip_IdAndRider_Id(trip.getId(), rider.getId());
    if (existingOpt.isPresent()) {
      Booking existing = existingOpt.get();
      if (existing.getStatus() == REQUESTED || existing.getStatus() == CONFIRMED) {
        throw new IllegalStateException("You already have a booking for this trip");
      }
      existing.setSeats(seats);
      existing.setStatus(REQUESTED);
      return bookings.save(existing);
    }

    try {
      Booking b = new Booking();
      b.setTrip(trip);
      b.setRider(rider);
      b.setSeats(seats);
      b.setStatus(REQUESTED);
      return bookings.save(b);
    } catch (DataIntegrityViolationException e) {
      throw new IllegalStateException("You already have a booking for this trip");
    }
  }

  @Transactional
  public Booking cancel(Long id) {
    Booking b = bookings.findByIdForUpdate(id)
        .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

    Long me = requireUserByEmail(currentEmail()).getId();
    if (!b.getRider().getId().equals(me)) throw new IllegalStateException("Only rider can cancel");

    Trip t = b.getTrip();

    if (b.getStatus() == CONFIRMED) {
      // LOCK trip, return seats
      Trip tLocked = trips.findByIdForUpdate(t.getId())
          .orElseThrow(() -> new IllegalStateException("Trip not found"));

      tLocked.setSeatsLeft(tLocked.getSeatsLeft() + b.getSeats());
      if (tLocked.getStatus() == TripStatus.FULL && tLocked.getSeatsLeft() > 0) tLocked.setStatus(TripStatus.OPEN);
      if (tLocked.getStatus() != TripStatus.CLOSED && tLocked.getSeatsLeft() > 0) tLocked.setActive(true);
      trips.save(tLocked);
      t = tLocked; // use locked instance below for email snapshot
    }

    b.setStatus(CANCELLED);
    Booking saved = bookings.save(b);
    Trip tSnap = t; // snapshot for lambda

    // Email the driver AFTER commit
    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
      @Override public void afterCommit() {
        try {
          User driver = tSnap.getDriver();
          User rider = saved.getRider();
          String when = (tSnap.getRideAt() != null) ? tSnap.getRideAt().format(DF) : "—";

          String msg =
              "Hi " + driver.getName() + ",\n\n" +
              "The rider canceled a confirmed booking.\n\n" +
              "Trip:\n" +
              " From: " + orDash(tSnap.getStartPlace()) + "\n" +
              " To:   " + orDash(tSnap.getEndPlace()) + "\n" +
              " When: " + when + "\n" +
              " Seats freed: " + saved.getSeats() + "\n\n" +
              "Rider:\n " + rider.getName() + " | " + rider.getEmail() + " | " + orDash(rider.getPhone()) + "\n";

          if (driver.getEmail() != null && !driver.getEmail().isBlank()) {
            email.sendText(driver.getEmail(), "RouteLink Booking Canceled by Rider", msg);
          }
        } catch (Exception e) {
          System.err.println("Cancel email failed: " + e.getMessage());
        }
      }
    });

    return saved;
  }

  /* ---------- Driver actions ---------- */

  @Transactional
  public Booking confirm(Long id) {
    Booking b = bookings.findByIdForUpdate(id)
        .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

    Trip t = trips.findByIdForUpdate(b.getTrip().getId())
        .orElseThrow(() -> new IllegalArgumentException("Trip not found"));

    Long me = requireUserByEmail(currentEmail()).getId();
    if (!t.getDriver().getId().equals(me)) throw new IllegalStateException("Only driver can confirm");
    if (t.getStatus() == TripStatus.CLOSED) throw new IllegalStateException("Trip already closed");
    if (b.getStatus() != REQUESTED) throw new IllegalStateException("Not in REQUESTED state");
    if (t.getSeatsLeft() < b.getSeats()) throw new IllegalStateException("Not enough seats left");

    t.setSeatsLeft(t.getSeatsLeft() - b.getSeats());
    if (t.getSeatsLeft() == 0) { t.setStatus(TripStatus.FULL); t.setActive(false); }
    trips.save(t);

    b.setStatus(CONFIRMED);
    Booking saved = bookings.save(b);
    Trip tSnap = t; // snapshot for email

    // Email both parties AFTER commit
    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
      @Override public void afterCommit() {
        try {
          sendConfirmationEmails(saved, tSnap);
        } catch (Exception e) {
          System.err.println("Email sending failed for booking " + saved.getId() + ": " + e.getMessage());
        }
      }
    });

    return saved;
  }

  private void sendConfirmationEmails(Booking saved, Trip trip) {
    User rider = saved.getRider();
    User driver = trip.getDriver();

    String from = orDash(trip.getStartPlace());
    String to   = orDash(trip.getEndPlace());
    String when = (trip.getRideAt() != null) ? trip.getRideAt().format(DF) : "—";

    String riderMsg =
        "Hi " + rider.getName() + ",\n\n" +
        "Your RouteLink booking is confirmed.\n\n" +
        "Trip:\n" +
        " From: " + from + "\n" +
        " To:   " + to + "\n" +
        " When: " + when + "\n" +
        " Seats: " + saved.getSeats() + "\n\n" +
        "Driver:\n" +
        " " + driver.getName() + " | " + driver.getEmail() + " | " + orDash(driver.getPhone()) + "\n\n" +
        "Please coordinate pickup and timing directly.";

    String driverMsg =
        "Hi " + driver.getName() + ",\n\n" +
        "A rider just booked your RouteLink trip.\n\n" +
        "Trip:\n" +
        " From: " + from + "\n" +
        " To:   " + to + "\n" +
        " When: " + when + "\n" +
        " Seats booked: " + saved.getSeats() + "\n\n" +
        "Rider:\n" +
        " " + rider.getName() + " | " + rider.getEmail() + " | " + orDash(rider.getPhone()) + "\n\n" +
        "Please reach out to coordinate pickup.";

    if (rider.getEmail() != null && !rider.getEmail().isBlank()) {
      email.sendText(rider.getEmail(), "RouteLink Booking Confirmed", riderMsg);
    }
    if (driver.getEmail() != null && !driver.getEmail().isBlank()) {
      email.sendText(driver.getEmail(), "New Rider Booked Your Trip", driverMsg);
    }
  }

  @Transactional
  public Booking decline(Long id) {
    Booking b = bookings.findByIdForUpdate(id)
        .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
    Trip t = b.getTrip();

    Long me = requireUserByEmail(currentEmail()).getId();
    if (!t.getDriver().getId().equals(me)) throw new IllegalStateException("Only driver can decline");
    if (b.getStatus() != REQUESTED) throw new IllegalStateException("Not in REQUESTED state");

    b.setStatus(DECLINED);
    Booking saved = bookings.save(b);
    Trip tSnap = t; // snapshot for email

    // Email the rider AFTER commit
    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
      @Override public void afterCommit() {
        try {
          User rider = saved.getRider();
          User driver = tSnap.getDriver();
          String when = (tSnap.getRideAt() != null) ? tSnap.getRideAt().format(DF) : "—";

          String msg =
              "Hi " + rider.getName() + ",\n\n" +
              "Your booking request was declined by the driver.\n\n" +
              "Trip:\n" +
              " From: " + orDash(tSnap.getStartPlace()) + "\n" +
              " To:   " + orDash(tSnap.getEndPlace()) + "\n" +
              " When: " + when + "\n\n" +
              "Driver:\n " + driver.getName() + " | " + driver.getEmail() + " | " + orDash(driver.getPhone()) + "\n";

          if (rider.getEmail() != null && !rider.getEmail().isBlank()) {
            email.sendText(rider.getEmail(), "RouteLink Booking Declined", msg);
          }
        } catch (Exception e) {
          System.err.println("Decline email failed: " + e.getMessage());
        }
      }
    });

    return saved;
  }

  /* ---------- Query ---------- */
  public Optional<Booking> get(Long id) { return bookings.findById(id); }
}
