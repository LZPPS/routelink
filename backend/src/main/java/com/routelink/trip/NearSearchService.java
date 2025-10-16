package com.routelink.trip;

import com.routelink.trip.TripMatch;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
public class NearSearchService {
  private final TripRepository trips;

  @Value("${search.near.radius.km:5}")
  private double RADIUS_KM;

  public NearSearchService(TripRepository trips) { this.trips = trips; }

  public List<TripMatch> search(double sLat, double sLng, double eLat, double eLng,
                                int seats, LocalDate date, ZoneId zone) {
    var from = date.atStartOfDay(zone).toOffsetDateTime();
    var to   = date.plusDays(1).atStartOfDay(zone).toOffsetDateTime();

    var candidates = trips.searchActiveOpenList(from, to, List.of(TripStatus.OPEN), seats, null, null);
    List<TripMatch> out = new ArrayList<>();

    for (Trip t : candidates) {
      if (t.getSeatsLeft() < seats) continue;

      double dStart = com.routelink.geo.Geo.haversineKm(sLat, sLng, t.getStartLat(), t.getStartLng());
      if (dStart > RADIUS_KM) continue;

      double dEnd   = com.routelink.geo.Geo.haversineKm(eLat, eLng, t.getEndLat(), t.getEndLng());
      if (dEnd > RADIUS_KM) continue;

      double score = 1.0 / (1.0 + dStart + dEnd);
      out.add(TripMatch.from(t, score, "near"));
    }
    return out;
  }
}
