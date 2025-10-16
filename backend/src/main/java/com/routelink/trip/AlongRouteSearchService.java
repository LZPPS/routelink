package com.routelink.trip;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Matches pickup & drop that lie within a corridor around the driver's path.
 * Uses polyline if provided; otherwise falls back to straight segment Start→End.
 * Enforces pickup-before-drop with a simple index/order heuristic.
 */
@Service
public class AlongRouteSearchService {
  private final TripRepository trips;

  // Wider, realistic defaults for inter-city rides (override in application.yml)
  @Value("${search.near.radius.km:25}")   // corridor radius around path (km)
  private double NEAR_RADIUS_KM;

  @Value("${search.max.detour.km:40}")    // cap on allowable off-path distance (km)
  private double MAX_DETOUR_KM;

  public AlongRouteSearchService(TripRepository trips) { this.trips = trips; }

  public List<TripMatch> search(double pLat, double pLng, double qLat, double qLng,
                                int seats, LocalDate date, ZoneId zone) {
    var from = date.atStartOfDay(zone).toOffsetDateTime();
    var to   = date.plusDays(1).atStartOfDay(zone).toOffsetDateTime();

    var candidates = trips.searchActiveOpenList(from, to, List.of(TripStatus.OPEN), seats, null, null);
    List<TripMatch> out = new ArrayList<>();

    for (Trip t : candidates) {
      if (t.getSeatsLeft() < seats) continue;

      double dPick, dDrop;
      boolean orderOk;
      double orderScore;

      if (t.getPolyline() != null && !t.getPolyline().isBlank()) {
        var path = com.routelink.geo.Polyline.decode(t.getPolyline());

        dPick = com.routelink.geo.Geo.distancePointToPathKm(pLat, pLng, path);
        dDrop = com.routelink.geo.Geo.distancePointToPathKm(qLat, qLng, path);

        // both must be inside corridor and under detour cap
        if (dPick > NEAR_RADIUS_KM || dDrop > NEAR_RADIUS_KM) continue;
        if (dPick > MAX_DETOUR_KM || dDrop > MAX_DETOUR_KM) continue;

        int iPick = closestIndexOnPath(pLat, pLng, path);
        int iDrop = closestIndexOnPath(qLat, qLng, path);
        orderOk = iPick < iDrop;

        // prefer longer usable segment
        orderScore = Math.max(0, iDrop - iPick) / (double) Math.max(1, path.size() - 1);
      } else {
        // fallback: straight segment distance & order using projection
        dPick = com.routelink.geo.Geo.distancePointToSegmentKm(
            pLat, pLng, t.getStartLat(), t.getStartLng(), t.getEndLat(), t.getEndLng());
        dDrop = com.routelink.geo.Geo.distancePointToSegmentKm(
            qLat, qLng, t.getStartLat(), t.getStartLng(), t.getEndLat(), t.getEndLng());

        if (dPick > NEAR_RADIUS_KM || dDrop > NEAR_RADIUS_KM) continue;
        if (dPick > MAX_DETOUR_KM || dDrop > MAX_DETOUR_KM) continue;

        double tp = com.routelink.geo.Geo.projectionT(
            pLat, pLng, t.getStartLat(), t.getStartLng(), t.getEndLat(), t.getEndLng());
        double tq = com.routelink.geo.Geo.projectionT(
            qLat, qLng, t.getStartLat(), t.getStartLng(), t.getEndLat(), t.getEndLng());
        orderOk = tp <= tq;                 // allow near-equal due to rounding
        orderScore = Math.max(0, tq - tp);  // longer in-path distance is better
      }

      if (!orderOk) continue;

      // score: closer to path + better along-path ordering
      double score = 1.0 / (1.0 + dPick + dDrop) + orderScore;

      // ✅ IMPORTANT: use lean TripMatch and tag the source as "route"
      out.add(TripMatch.from(t, score, "route"));
    }
    return out;
  }

  private static int closestIndexOnPath(double lat, double lng, List<double[]> path) {
    int best = 0;
    double bestD = Double.MAX_VALUE;
    for (int i = 0; i < path.size(); i++) {
      double[] p = path.get(i);
      double d = com.routelink.geo.Geo.haversineKm(lat, lng, p[0], p[1]);
      if (d < bestD) { bestD = d; best = i; }
    }
    return best;
  }
}
