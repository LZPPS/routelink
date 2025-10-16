// src/main/java/com/routelink/trip/TripMatch.java
package com.routelink.trip;

public record TripMatch(long tripId, double score, String matchedBy) {
  public static TripMatch from(Trip t, double score) {
    // DO NOT read t.getDriver() (or any other lazy relation) here
    return new TripMatch(t.getId(), score, "near");
  }
  public static TripMatch from(Trip t, double score, String matchedBy) {
    return new TripMatch(t.getId(), score, matchedBy);
  }
}
