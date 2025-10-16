// src/main/java/com/routelink/trip/UnifiedSearchService.java
package com.routelink.trip;

import com.routelink.trip.dto.UnifiedSearchRequest;
import com.routelink.trip.dto.UnifiedSearchResult;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UnifiedSearchService {
  private final NearSearchService near;
  private final AlongRouteSearchService along;

  public UnifiedSearchService(NearSearchService near, AlongRouteSearchService along) {
    this.near = near;
    this.along = along;
  }

  public List<UnifiedSearchResult> search(UnifiedSearchRequest q, ZoneId zone) {
    // Each returns List<TripMatch> where TripMatch = (tripId, score, matchedBy)
    var nearHits  = near.search(q.startLat(), q.startLng(), q.endLat(), q.endLng(),
                                q.seats(), q.date(), zone);
    var alongHits = along.search(q.startLat(), q.startLng(), q.endLat(), q.endLng(),
                                 q.seats(), q.date(), zone);

    // Merge by tripId, accumulate near/along scores
    Map<Long, Acc> map = new HashMap<>();

    for (TripMatch m : nearHits) {
      map.computeIfAbsent(m.tripId(), k -> new Acc()).near = m.score();
    }
    for (TripMatch m : alongHits) {
      map.computeIfAbsent(m.tripId(), k -> new Acc()).along = m.score();
    }

    // Build lean results (tripId, score, matchedBy) and sort
    return map.entrySet().stream()
        .map(e -> e.getValue().toUnified(e.getKey()))
        .sorted(
            Comparator.comparing((UnifiedSearchResult r) -> rank(r.matchedBy()))
                      .thenComparingDouble(UnifiedSearchResult::score).reversed()
        )
        .collect(Collectors.toList());
  }

  private static int rank(String tag) {
    return "BOTH".equals(tag) ? 3 : "ALONG".equals(tag) ? 2 : 1; // NEAR=1
  }

  /** Accumulator for one trip across NEAR/ALONG buckets. */
  private static final class Acc {
    Double near;   // nullable
    Double along;  // nullable

    UnifiedSearchResult toUnified(Long tripId) {
      String matchedBy;
      double score;
      if (near != null && along != null) {
        matchedBy = "BOTH";
        score = (near + along) / 2.0;
      } else if (along != null) {
        matchedBy = "ALONG";
        score = along;
      } else {
        matchedBy = "NEAR";
        score = (near != null ? near : 0.0);
      }
      return new UnifiedSearchResult(tripId, score, matchedBy);
    }
  }
}
