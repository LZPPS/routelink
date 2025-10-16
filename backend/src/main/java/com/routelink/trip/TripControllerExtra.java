// src/main/java/com/routelink/trip/TripControllerExtra.java
package com.routelink.trip;

import com.routelink.common.BadRequestException;
import com.routelink.common.ForbiddenException;
import com.routelink.common.NotFoundException;
import com.routelink.user.User;
import com.routelink.user.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/trips")
public class TripControllerExtra {

  private final TripRepository tripRepo;
  private final UserRepository userRepo;

  public TripControllerExtra(TripRepository tripRepo, UserRepository userRepo) {
    this.tripRepo = tripRepo;
    this.userRepo = userRepo;
  }

  private Long currentUserId() {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || auth.getName() == null) {
      throw new BadRequestException("Unauthenticated");
    }
    User u = userRepo.findByEmail(auth.getName())
        .orElseThrow(() -> new NotFoundException("User not found"));
    return u.getId();
  }

  @PostMapping("/{id}/complete")
  @Transactional
  public Map<String, Object> complete(@PathVariable Long id) {
    Long me = currentUserId();

    // lock the trip row to avoid races
    Trip trip = tripRepo.findByIdForUpdate(id)
        .orElseThrow(() -> new NotFoundException("Trip not found"));

    if (trip.getDriver() == null) {
      throw new BadRequestException("Trip has no driver assigned");
    }

    Long driverId = trip.getDriver().getId();
    if (!Objects.equals(driverId, me)) {
      throw new ForbiddenException("Only driver can complete the trip");
    }

    if (trip.getStatus() == TripStatus.CLOSED) {
      throw new BadRequestException("Trip already closed");
    }

    trip.setStatus(TripStatus.CLOSED);
    trip.setActive(false);
    tripRepo.save(trip);

    // return a minimal, safe payload
    return Map.of(
        "id", trip.getId(),
        "status", trip.getStatus().name(),
        "active", trip.isActive()
    );
  }
}
