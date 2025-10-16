// src/main/java/com/routelink/trip/dto/TripSearchDto.java
package com.routelink.trip.dto;

import com.routelink.trip.TripDto;  // ✅ import the TripDto from the trip package

public record TripSearchDto(
    TripDto trip,
    double score,
    String matchedBy  // NEAR | ALONG | BOTH
) {}
