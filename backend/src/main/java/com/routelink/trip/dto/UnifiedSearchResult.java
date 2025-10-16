// src/main/java/com/routelink/trip/dto/UnifiedSearchResult.java
package com.routelink.trip.dto;

public record UnifiedSearchResult(
    Long tripId,
    double score,
    String matchedBy // "NEAR" | "ALONG" | "BOTH"
) {}
