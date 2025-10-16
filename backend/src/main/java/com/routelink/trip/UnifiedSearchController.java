// src/main/java/com/routelink/trip/UnifiedSearchController.java
package com.routelink.trip;

import com.routelink.trip.dto.TripSearchDto;
import com.routelink.trip.dto.UnifiedSearchRequest;
import com.routelink.trip.dto.UnifiedSearchResult;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/trips")
public class UnifiedSearchController {
  private final UnifiedSearchService service;
  private final TripRepository trips;
  private final ZoneId zone = ZoneId.systemDefault();

  public UnifiedSearchController(UnifiedSearchService service, TripRepository trips) {
    this.service = service;
    this.trips = trips;
  }

  @PostMapping("/search-unified")
  public List<TripSearchDto> search(@RequestBody UnifiedSearchRequest q) {
    try {
      System.out.printf(
          "[search-unified] start='%s' (%.6f,%.6f)  end='%s' (%.6f,%.6f)  seats=%d  date=%s%n",
          q.startText(), q.startLat(), q.startLng(),
          q.endText(),   q.endLat(),   q.endLng(),
          q.seats(), q.date()
      );

      // 1) run unified matcher
      List<UnifiedSearchResult> results = service.search(q, zone);
      System.out.println("[search-unified] unified results: " + results.size());

      if (results.isEmpty()) {
        return List.of();
      }

      // 2) fetch trips with driver eagerly to avoid LazyInitializationException
      Set<Long> ids = results.stream()
          .map(UnifiedSearchResult::tripId)
          .collect(Collectors.toSet());

      // IMPORTANT: use the fetch-join method
      Map<Long, Trip> byId = trips.findAllByIdFetchDriver(ids).stream()
          .collect(Collectors.toMap(Trip::getId, t -> t));

      // 3) build view rows
      List<TripSearchDto> out = new ArrayList<>(results.size());
      for (UnifiedSearchResult r : results) {
        Trip t = byId.get(r.tripId());
        if (t == null) continue; // was removed meanwhile
        out.add(new TripSearchDto(TripDto.from(t), r.score(), r.matchedBy()));
      }
      return out;
    } catch (Exception ex) {
      ex.printStackTrace(); // keep a stack in server logs
      throw ex;             // surface 500 so you notice during dev
    }
  }
}
