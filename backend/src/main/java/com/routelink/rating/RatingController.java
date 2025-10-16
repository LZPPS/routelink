// src/main/java/com/routelink/rating/RatingController.java
package com.routelink.rating;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ratings")
public class RatingController {
  private final RatingService service;
  public RatingController(RatingService service) { this.service = service; }

  @PostMapping
  public ResponseEntity<RatingDto> create(@RequestBody RatingService.CreateRating req) {
    Rating r = service.rate(req);
    return ResponseEntity.ok(RatingDto.from(r));
  }
}
