// src/main/java/com/routelink/rating/RatingRepository.java
package com.routelink.rating;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RatingRepository extends JpaRepository<Rating, Long> {
  boolean existsByBooking_IdAndRater_Id(Long bookingId, Long raterId);
}
