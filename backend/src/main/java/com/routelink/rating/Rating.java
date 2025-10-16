// src/main/java/com/routelink/rating/Rating.java
package com.routelink.rating;

import com.routelink.booking.Booking;
import com.routelink.user.User;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
  name = "ratings",
  uniqueConstraints = @UniqueConstraint(columnNames = {"booking_id","rater_id"})
)
public class Rating {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false) private Booking booking;

  @ManyToOne(optional = false) @JoinColumn(name = "rater_id")
  private User rater;

  @ManyToOne(optional = false) @JoinColumn(name = "ratee_id")
  private User ratee;

  private int stars;

  @Column(length = 400)
  private String comment;

  private Instant createdAt = Instant.now();

  // getters/setters
  public Long getId() { return id; }
  public Booking getBooking() { return booking; }
  public void setBooking(Booking b) { this.booking = b; }
  public User getRater() { return rater; }
  public void setRater(User u) { this.rater = u; }
  public User getRatee() { return ratee; }
  public void setRatee(User u) { this.ratee = u; }
  public int getStars() { return stars; }
  public void setStars(int s) { this.stars = s; }
  public String getComment() { return comment; }
  public void setComment(String c) { this.comment = c; }
  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant t) { this.createdAt = t; }
}
