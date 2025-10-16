// src/main/java/com/routelink/trip/dto/UnifiedSearchRequest.java
package com.routelink.trip.dto;

import java.time.LocalDate;

public record UnifiedSearchRequest(
    String startText,
    String endText,
    double startLat,
    double startLng,
    double endLat,
    double endLng,
    int seats,
    LocalDate date
) {}
