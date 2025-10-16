// com.routelink.housekeeping.CleanupJobs
package com.routelink.housekeeping;

import com.routelink.trip.TripRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
public class CleanupJobs {
  private final TripRepository trips;
  public CleanupJobs(TripRepository trips) { this.trips = trips; }

  // Run daily at 00:00
  @Scheduled(cron = "0 0 0 * * *")
  public void purgeHiddenOldTrips() {
    OffsetDateTime cutoff = OffsetDateTime.now().minusDays(1);
    int deleted = trips.deleteInactiveBefore(cutoff);
    // log if you want
  }
}
