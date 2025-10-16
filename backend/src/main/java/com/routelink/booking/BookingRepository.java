package com.routelink.booking;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

  // Load rider eagerly for driver dashboard
  @EntityGraph(attributePaths = "rider")
  List<Booking> findByTrip_Id(Long tripId);

  // If your "My bookings" page shows trip info, you can also eager-load trip
  @EntityGraph(attributePaths = {"trip"})
  List<Booking> findByRider_Id(Long riderId);

  Optional<Booking> findByTrip_IdAndRider_Id(Long tripId, Long riderId);

  boolean existsByTrip_IdAndRider_IdAndStatusIn(
      Long tripId, Long riderId, Collection<BookingStatus> statuses);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select b from Booking b where b.id = :id")
  Optional<Booking> findByIdForUpdate(@Param("id") Long id);
}
