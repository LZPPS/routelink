// src/main/java/com/routelink/rating/RatingService.java
package com.routelink.rating;

import com.routelink.booking.Booking;
import com.routelink.booking.BookingRepository;
import com.routelink.common.BadRequestException;
import com.routelink.common.ForbiddenException;
import com.routelink.common.NotFoundException;
import com.routelink.trip.TripStatus;
import com.routelink.user.User;
import com.routelink.user.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RatingService {

  private final RatingRepository ratings;
  private final BookingRepository bookings;
  private final UserRepository users;

  public RatingService(RatingRepository ratings, BookingRepository bookings, UserRepository users) {
    this.ratings = ratings;
    this.bookings = bookings;
    this.users = users;
  }

  public record CreateRating(Long bookingId, int stars, String comment) {}

  private User requireMe() {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || auth.getName() == null) throw new BadRequestException("Unauthenticated");
    return users.findByEmail(auth.getName()).orElseThrow(() -> new NotFoundException("User not found"));
  }

  @Transactional
  public Rating rate(CreateRating req) {
    if (req.bookingId() == null) throw new BadRequestException("bookingId is required");
    if (req.stars() < 1 || req.stars() > 5) throw new BadRequestException("stars must be 1..5");

    User me = requireMe();

    Booking b = bookings.findById(req.bookingId())
        .orElseThrow(() -> new NotFoundException("Booking not found"));

    // Only after the trip is CLOSED
    if (b.getTrip().getStatus() != TripStatus.CLOSED)
      throw new BadRequestException("Trip not closed yet");

    Long riderId = b.getRider().getId();
    Long driverId = b.getTrip().getDriver().getId();

    // Who am I rating?
    final User ratee;
    if (me.getId().equals(riderId)) {
      ratee = b.getTrip().getDriver();             // rider rates driver
    } else if (me.getId().equals(driverId)) {
      ratee = b.getRider();                        // driver rates rider
    } else {
      throw new ForbiddenException("Not your booking");
    }

    // Only one rating per person per booking
    if (ratings.existsByBooking_IdAndRater_Id(b.getId(), me.getId()))
      throw new BadRequestException("You already rated this booking");

    // Save rating
    Rating r = new Rating();
    r.setBooking(b);
    r.setRater(me);
    r.setRatee(ratee);
    r.setStars(req.stars());
    r.setComment(req.comment());
    Rating saved = ratings.save(r);

    // Roll-up into user's average + count (simple incremental)
    int    count = ratee.getRatingCount();   // e.g., 0 on a new user
    double avg   = ratee.getRatingAvg();     // e.g., 0.0 on a new user

    double newAvg = ((avg * count) + req.stars()) / (count + 1.0);

    ratee.setRatingCount(count + 1);
    ratee.setRatingAvg(newAvg);
    users.save(ratee);

    return saved;
  }
}
