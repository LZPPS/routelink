package com.routelink.booking;

import com.routelink.trip.Trip;
import com.routelink.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import java.time.Instant;

@Entity
@Table(
  name = "bookings",
  uniqueConstraints = @UniqueConstraint(name = "uq_booking_trip_rider", columnNames = {"trip_id","rider_id"}),
  indexes = {
    @Index(name = "idx_bookings_trip", columnList = "trip_id"),
    @Index(name = "idx_bookings_rider", columnList = "rider_id")
  }
)
public class Booking {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "trip_id", nullable = false)
  private Trip trip;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "rider_id", nullable = false)
  private User rider;

  @Min(1)
  @Column(nullable = false)
  private int seats = 1;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private BookingStatus status = BookingStatus.REQUESTED;

  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  // optional: optimistic locking
  // @Version
  // private long version;

  @PrePersist
  void onCreate() {
    if (status == null) status = BookingStatus.REQUESTED;
    if (seats < 1) seats = 1;
    this.createdAt = Instant.now();
  }

  // getters/setters
  public Long getId(){ return id; }

  public Trip getTrip(){ return trip; }
  public void setTrip(Trip t){ this.trip = t; }

  public User getRider(){ return rider; }
  public void setRider(User r){ this.rider = r; }

  public int getSeats(){ return seats; }
  public void setSeats(int s){ this.seats = s; }

  public BookingStatus getStatus(){ return status; }
  public void setStatus(BookingStatus s){ this.status = s; }

  public Instant getCreatedAt(){ return createdAt; }
  public void setCreatedAt(Instant i){ this.createdAt = i; }
}
