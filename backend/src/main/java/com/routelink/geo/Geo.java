package com.routelink.geo;

import java.util.List;

public final class Geo {
  private Geo() {} // util class

  private static final double R_KM = 6371.0088; // km

  /** Haversine distance between two lat/lng points (km). */
  public static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
    double dLat = Math.toRadians(lat2 - lat1);
    double dLon = Math.toRadians(lon2 - lon1);
    double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
        Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
        Math.sin(dLon/2) * Math.sin(dLon/2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R_KM * c;
  }

  /** Fast planar meters using equirectangular approximation around a reference latitude. */
  private static double[] toXY(double lat, double lon, double refLat) {
    double x = Math.toRadians(lon) * Math.cos(Math.toRadians(refLat)) * R_KM * 1000.0;
    double y = Math.toRadians(lat) * R_KM * 1000.0;
    return new double[]{x, y};
  }

  /** Distance from point to segment (lat/lng), approx in km. */
  public static double distancePointToSegmentKm(double plat, double plon,
                                                double aLat, double aLon,
                                                double bLat, double bLon) {
    double ref = (aLat + bLat) / 2.0;
    double[] P = toXY(plat, plon, ref);
    double[] A = toXY(aLat, aLon, ref);
    double[] B = toXY(bLat, bLon, ref);

    double APx = P[0] - A[0], APy = P[1] - A[1];
    double ABx = B[0] - A[0], ABy = B[1] - A[1];
    double ab2 = ABx*ABx + ABy*ABy;
    if (ab2 == 0) return Math.hypot(APx, APy) / 1000.0;

    double t = (APx*ABx + APy*ABy) / ab2;
    if (t < 0) t = 0; else if (t > 1) t = 1;

    double cx = A[0] + t * ABx, cy = A[1] + t * ABy;
    return Math.hypot(P[0]-cx, P[1]-cy) / 1000.0;
  }

  /** Alias for existing code that calls pointToSegmentDistanceKm(...) */
  public static double pointToSegmentDistanceKm(double plat, double plon,
                                                double aLat, double aLon,
                                                double bLat, double bLon) {
    return distancePointToSegmentKm(plat, plon, aLat, aLon, bLat, bLon);
  }

  /**
   * Projection parameter t of point P onto segment AB in the same local approximation.
   * Returns clamped t in [0,1], where 0 = A, 1 = B.
   */
  public static double projectionT(double plat, double plon,
                                   double aLat, double aLon,
                                   double bLat, double bLon) {
    double ref = (aLat + bLat) / 2.0;
    double[] P = toXY(plat, plon, ref);
    double[] A = toXY(aLat, aLon, ref);
    double[] B = toXY(bLat, bLon, ref);

    double APx = P[0] - A[0], APy = P[1] - A[1];
    double ABx = B[0] - A[0], ABy = B[1] - A[1];
    double ab2 = ABx*ABx + ABy*ABy;
    if (ab2 == 0) return 0.0;

    double t = (APx*ABx + APy*ABy) / ab2;
    if (t < 0) return 0.0;
    if (t > 1) return 1.0;
    return t;
  }

  /** Minimum distance (km) from a point to a polyline path (list of [lat,lng]). */
  public static double distancePointToPathKm(double plat, double plon, List<double[]> path) {
    if (path == null || path.isEmpty()) return Double.POSITIVE_INFINITY;
    if (path.size() == 1) {
      double[] p = path.get(0);
      return haversineKm(plat, plon, p[0], p[1]);
    }
    double best = Double.POSITIVE_INFINITY;
    for (int i = 0; i < path.size() - 1; i++) {
      double[] a = path.get(i);
      double[] b = path.get(i+1);
      double d = distancePointToSegmentKm(plat, plon, a[0], a[1], b[0], b[1]);
      if (d < best) best = d;
    }
    return best;
  }
}
