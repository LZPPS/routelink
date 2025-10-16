package com.routelink.geo;

public final class GeoUtils {
  private GeoUtils() {}
  private static final double EARTH_R = 6371.0088; // km

  public static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
    double dLat = Math.toRadians(lat2 - lat1);
    double dLon = Math.toRadians(lon2 - lon1);
    double a = Math.sin(dLat/2)*Math.sin(dLat/2) +
        Math.cos(Math.toRadians(lat1))*Math.cos(Math.toRadians(lat2)) *
        Math.sin(dLon/2)*Math.sin(dLon/2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    return EARTH_R * c;
  }

  public static double pointToSegmentDistanceKm(double plat, double plon,
                                                double alat, double alon,
                                                double blat, double blon) {
    double latRad = Math.toRadians(alat);
    double xP = Math.toRadians(plon - alon) * Math.cos(latRad) * EARTH_R;
    double yP = Math.toRadians(plat - alat) * EARTH_R;
    double xB = Math.toRadians(blon - alon) * Math.cos(latRad) * EARTH_R;
    double yB = Math.toRadians(blat - alat) * EARTH_R;
    double vx = xB, vy = yB;
    double c1 = vx*xP + vy*yP;
    double c2 = vx*vx + vy*vy;
    double t = (c2 == 0) ? 0 : Math.max(0, Math.min(1, c1 / c2));
    double projX = t * vx, projY = t * vy;
    return Math.hypot(xP - projX, yP - projY);
  }

  public static double projectionT(double plat, double plon,
                                   double alat, double alon,
                                   double blat, double blon) {
    double latRad = Math.toRadians(alat);
    double xP = Math.toRadians(plon - alon) * Math.cos(latRad) * EARTH_R;
    double yP = Math.toRadians(plat - alat) * EARTH_R;
    double xB = Math.toRadians(blon - alon) * Math.cos(latRad) * EARTH_R;
    double yB = Math.toRadians(blat - alat) * EARTH_R;
    double vx = xB, vy = yB;
    double c1 = vx*xP + vy*yP;
    double c2 = vx*vx + vy*vy;
    if (c2 == 0) return 0;
    double t = c1 / c2;
    if (t < 0) return 0;
    if (t > 1) return 1;
    return t;
  }
}
