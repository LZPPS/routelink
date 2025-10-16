package com.routelink.trip;

import com.routelink.user.User;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "trips",
       indexes = {
         @Index(name="idx_trips_ride_at", columnList = "ride_at"),
         @Index(name="idx_trips_status",  columnList = "status")
       })
public class Trip {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "driver_id", nullable = false)
  private User driver;

  @Column(nullable = false) private String startPlace;
  @Column(nullable = false) private double startLat;
  @Column(nullable = false) private double startLng;

  @Column(nullable = false) private String endPlace;
  @Column(nullable = false) private double endLat;
  @Column(nullable = false) private double endLng;

  // route polyline as text for now (PostGIS later)
  @Column(columnDefinition = "text") private String polyline;

  @Column(name = "ride_at", nullable = false, columnDefinition = "timestamptz")
  private OffsetDateTime rideAt;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal pricePerSeat;

  @Column(nullable = false) private int seatsTotal;
  @Column(nullable = false) private int seatsLeft;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private TripStatus status = TripStatus.OPEN;

  // NEW: soft-visible in search. When FULL/CLOSED -> set false
  @Column(nullable = false)
  private boolean active = true;

  @PrePersist
  void prePersist() { if (seatsLeft <= 0) seatsLeft = seatsTotal; }

  // getters/setters
  public Long getId() { return id; }

  public User getDriver() { return driver; }
  public void setDriver(User driver) { this.driver = driver; }

  public String getStartPlace() { return startPlace; }
  public void setStartPlace(String s) { this.startPlace = s; }

  public double getStartLat() { return startLat; }
  public void setStartLat(double v) { this.startLat = v; }

  public double getStartLng() { return startLng; }
  public void setStartLng(double v) { this.startLng = v; }

  public String getEndPlace() { return endPlace; }
  public void setEndPlace(String s) { this.endPlace = s; }

  public double getEndLat() { return endLat; }
  public void setEndLat(double v) { this.endLat = v; }

  public double getEndLng() { return endLng; }
  public void setEndLng(double v) { this.endLng = v; }

  public String getPolyline() { return polyline; }
  public void setPolyline(String p) { this.polyline = p; }

  public OffsetDateTime getRideAt() { return rideAt; }
  public void setRideAt(OffsetDateTime t) { this.rideAt = t; }

  public BigDecimal getPricePerSeat() { return pricePerSeat; }
  public void setPricePerSeat(BigDecimal p) { this.pricePerSeat = p; }

  public int getSeatsTotal() { return seatsTotal; }
  public void setSeatsTotal(int s) { this.seatsTotal = s; }

  public int getSeatsLeft() { return seatsLeft; }
  public void setSeatsLeft(int s) { this.seatsLeft = s; }

  public TripStatus getStatus() { return status; }
  public void setStatus(TripStatus st) { this.status = st; }

  public boolean isActive() { return active; }
  public void setActive(boolean active) { this.active = active; }
}
