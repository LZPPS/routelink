package com.routelink.geo;

import java.util.ArrayList;
import java.util.List;

/** Google polyline codec: decode & encode (lat,lng in degrees). */
public final class Polyline {
  private Polyline() {}

  /** Decode a Google-encoded polyline into a List<double[]{lat, lng}>. */
  public static List<double[]> decode(String encoded) {
    List<double[]> path = new ArrayList<>();
    if (encoded == null || encoded.isBlank()) return path;

    int index = 0, len = encoded.length();
    long lat = 0, lng = 0;

    while (index < len) {
      int b, shift = 0, result = 0;
      do {
        if (index >= len) return path; // defensive
        b = encoded.charAt(index++) - 63;
        result |= (b & 0x1f) << shift;
        shift += 5;
      } while (b >= 0x20);
      long dlat = ((result & 1) != 0) ? ~(result >> 1) : (result >> 1);
      lat += dlat;

      shift = 0; result = 0;
      do {
        if (index >= len) return path; // defensive
        b = encoded.charAt(index++) - 63;
        result |= (b & 0x1f) << shift;
        shift += 5;
      } while (b >= 0x20);
      long dlng = ((result & 1) != 0) ? ~(result >> 1) : (result >> 1);
      lng += dlng;

      path.add(new double[]{ lat / 1e5, lng / 1e5 });
    }
    return path;
  }

  /** Encode a list of {lat,lng} pairs to a Google polyline string. */
  public static String encode(List<double[]> path) {
    if (path == null || path.isEmpty()) return "";
    StringBuilder sb = new StringBuilder();
    long lastLat = 0, lastLng = 0;

    for (double[] p : path) {
      long ilat = Math.round(p[0] * 1e5);
      long ilng = Math.round(p[1] * 1e5);
      writeDelta(sb, ilat - lastLat);
      writeDelta(sb, ilng - lastLng);
      lastLat = ilat; lastLng = ilng;
    }
    return sb.toString();
  }

  private static void writeDelta(StringBuilder sb, long v) {
    v = (v < 0) ? ~(v << 1) : (v << 1);
    while (v >= 0x20) {
      sb.append((char) ((0x20 | (v & 0x1f)) + 63));
      v >>= 5;
    }
    sb.append((char) (v + 63));
  }
}
