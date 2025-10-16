package com.routelink.trip;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
public interface TripRepository extends JpaRepository<Trip, Long> {

	  // ...everything you already have...

	  /** Fetch trips with driver initialized to avoid LazyInitializationException */
	  @Query("""
	    select t from Trip t
	    left join fetch t.driver
	    where t.id in :ids
	  """)
  List<Trip> findAllByIdFetchDriver(@Param("ids") Set<Long> ids);


  List<Trip> findByRideAtBetweenAndStatusIn(
      OffsetDateTime startInclusive,
      OffsetDateTime endInclusive,
      List<TripStatus> statuses
  );

  List<Trip> findByRideAtBetweenAndStatusInAndActiveTrue(
      OffsetDateTime startInclusive,
      OffsetDateTime endInclusive,
      List<TripStatus> statuses
  );

  Page<Trip> findByRideAtBetweenAndStatusInAndActiveTrue(
      OffsetDateTime startInclusive,
      OffsetDateTime endInclusive,
      List<TripStatus> statuses,
      Pageable pageable
  );

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select t from Trip t where t.id = :id")
  Optional<Trip> findByIdForUpdate(@Param("id") Long id);

  // Paged + filterable (used by /api/trips/search)
  @Query("""
    select t from Trip t
    where t.active = true
      and t.status in :statuses
      and t.rideAt between :from and :to
      and t.seatsLeft >= :minSeats
      and (:minPrice is null or t.pricePerSeat >= :minPrice)
      and (:maxPrice is null or t.pricePerSeat <= :maxPrice)
    """)
  Page<Trip> searchActiveOpenPaged(
      @Param("from") OffsetDateTime from,
      @Param("to") OffsetDateTime to,
      @Param("statuses") List<TripStatus> statuses,
      @Param("minSeats") int minSeats,
      @Param("minPrice") BigDecimal minPrice,
      @Param("maxPrice") BigDecimal maxPrice,
      Pageable pageable
  );

  // List + filterable (used by /search/near and /search/route before geo filtering)
  @Query("""
    select t from Trip t
    where t.active = true
      and t.status in :statuses
      and t.rideAt between :from and :to
      and t.seatsLeft >= :minSeats
      and (:minPrice is null or t.pricePerSeat >= :minPrice)
      and (:maxPrice is null or t.pricePerSeat <= :maxPrice)
    """)
  List<Trip> searchActiveOpenList(
      @Param("from") OffsetDateTime from,
      @Param("to") OffsetDateTime to,
      @Param("statuses") List<TripStatus> statuses,
      @Param("minSeats") int minSeats,
      @Param("minPrice") BigDecimal minPrice,
      @Param("maxPrice") BigDecimal maxPrice
  );

  // Cleanup job (explicit JPQL delete)
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Transactional
  @Query("delete from Trip t where t.active = false and t.rideAt < :cutoff")
  int deleteInactiveBefore(@Param("cutoff") OffsetDateTime cutoff);
}
