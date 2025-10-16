package com.routelink.trip;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@RestController
@RequestMapping("/api/trips")
public class TripController {
  private final TripRepository trips;
  private final TripService tripService;

  public TripController(TripRepository trips, TripService tripService) {
    this.trips = trips;
    this.tripService = tripService;
  }

  // --- DTO for create (no driverId here; driver = current user) ---
  public record CreateTripReq(
      @NotBlank String startPlace, double startLat, double startLng,
      @NotBlank String endPlace,   double endLat,   double endLng,
      String polyline,
      @NotNull OffsetDateTime rideAt,
      @NotNull @Min(0) BigDecimal pricePerSeat,
      @Min(1) int seatsTotal
  ) {}

  // Create a trip (driver = authenticated user via TripService)
  @PostMapping
  public ResponseEntity<TripDto> create(@Valid @RequestBody CreateTripReq req) {
    Trip t = tripService.create(new TripService.CreateTrip(
        req.startPlace(), req.startLat(), req.startLng(),
        req.endPlace(), req.endLat(), req.endLng(),
        req.polyline(),
        req.rideAt(),
        req.pricePerSeat(),
        req.seatsTotal()
    ));
    return ResponseEntity.created(URI.create("/api/trips/" + t.getId()))
        .body(TripDto.from(t));
  }

  // List all (debug)
  @GetMapping
  public List<TripDto> list() {
    return trips.findAll().stream().map(TripDto::from).toList();
  }

  // Get by id
  @GetMapping("/{id}")
  public ResponseEntity<TripDto> get(@PathVariable Long id) {
    return trips.findById(id).map(TripDto::from)
        .map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
  }

  // ------------------------------
  // Search 1: date search (DB paging + sorting + filters)
  // ------------------------------
  @GetMapping("/search")
  public ResponseEntity<List<TripDto>> searchByDate(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
      @RequestParam(required = false) OffsetDateTime at,       // e.g. 2025-10-01T09:00:00-04:00
      @RequestParam(defaultValue = "120") int windowMin,       // +/- minutes around "at"
      @RequestParam(defaultValue = "rideAt") String sort,      // rideAt | pricePerSeat | time | price
      @RequestParam(defaultValue = "asc") String order,        // asc | desc
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(defaultValue = "1") int minSeats,
      @RequestParam(required = false) BigDecimal minPrice,
      @RequestParam(required = false) BigDecimal maxPrice
  ) {
    OffsetDateTime dayStart = date.atStartOfDay().atOffset(ZoneOffset.UTC);
    OffsetDateTime dayEnd   = dayStart.plusDays(1);
    OffsetDateTime from = dayStart, to = dayEnd.minusNanos(1);
    if (at != null) {
      from = at.minusMinutes(windowMin);
      to   = at.plusMinutes(windowMin);
      if (from.isBefore(dayStart)) from = dayStart;
      if (to.isAfter(dayEnd))      to   = dayEnd;
    }

    String sortProp = ("price".equalsIgnoreCase(sort) || "pricePerSeat".equalsIgnoreCase(sort))
        ? "pricePerSeat" : "rideAt";
    Sort s = "desc".equalsIgnoreCase(order) ? Sort.by(sortProp).descending() : Sort.by(sortProp).ascending();
    Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), s);

    int seats = Math.max(1, minSeats);
    if (minPrice != null && minPrice.signum() < 0) minPrice = BigDecimal.ZERO;
    if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
      BigDecimal tmp = minPrice; minPrice = maxPrice; maxPrice = tmp;
    }

    var pageResult = trips.searchActiveOpenPaged(
        from, to, List.of(TripStatus.OPEN), seats, minPrice, maxPrice, pageable
    );

    return ResponseEntity.ok()
        .header("X-Total-Count", String.valueOf(pageResult.getTotalElements()))
        .body(pageResult.getContent().stream().map(TripDto::from).toList());
  }

  // ------------------------------
  // Search 2: start/end within radius (prefilter in DB, geo + paginate in-memory)
  // ------------------------------
  @GetMapping("/search/near")
  public ResponseEntity<List<TripDto>> searchNear(
      @RequestParam double startLat,
      @RequestParam double startLng,
      @RequestParam double endLat,
      @RequestParam double endLng,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
      @RequestParam(defaultValue = "25") double radiusKm,
      @RequestParam(required = false) OffsetDateTime at,
      @RequestParam(defaultValue = "120") int windowMin,
      @RequestParam(defaultValue = "time") String sort,   // time | price
      @RequestParam(defaultValue = "asc") String order,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(defaultValue = "1") int minSeats,
      @RequestParam(required = false) BigDecimal minPrice,
      @RequestParam(required = false) BigDecimal maxPrice
  ) {
    OffsetDateTime dayStart = date.atStartOfDay().atOffset(ZoneOffset.UTC);
    OffsetDateTime dayEnd   = dayStart.plusDays(1);
    OffsetDateTime from = dayStart, to = dayEnd.minusNanos(1);
    if (at != null) {
      from = at.minusMinutes(windowMin);
      to   = at.plusMinutes(windowMin);
      if (from.isBefore(dayStart)) from = dayStart;
      if (to.isAfter(dayEnd))      to   = dayEnd;
    }

    int seats = Math.max(1, minSeats);
    if (minPrice != null && minPrice.signum() < 0) minPrice = BigDecimal.ZERO;
    if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
      BigDecimal tmp = minPrice; minPrice = maxPrice; maxPrice = tmp;
    }

    var prefiltered = trips.searchActiveOpenList(from, to, List.of(TripStatus.OPEN), seats, minPrice, maxPrice);

    var filtered = prefiltered.stream()
        .filter(t ->
            com.routelink.geo.Geo.haversineKm(startLat, startLng, t.getStartLat(), t.getStartLng()) <= radiusKm &&
            com.routelink.geo.Geo.haversineKm(endLat,   endLng,   t.getEndLat(),   t.getEndLng())   <= radiusKm
        )
        .sorted((a, b) -> {
          int cmp = "price".equalsIgnoreCase(sort)
              ? a.getPricePerSeat().compareTo(b.getPricePerSeat())
              : a.getRideAt().compareTo(b.getRideAt());
          return "desc".equalsIgnoreCase(order) ? -cmp : cmp;
        })
        .map(TripDto::from)
        .toList();

    int pSize = Math.max(1, size);
    int fromIdx = Math.max(0, page * pSize);
    int toIdx   = Math.min(filtered.size(), fromIdx + pSize);
    List<TripDto> pageItems = fromIdx >= filtered.size() ? List.of() : filtered.subList(fromIdx, toIdx);

    return ResponseEntity.ok()
        .header("X-Total-Count", String.valueOf(filtered.size()))
        .body(pageItems);
  }

  // ------------------------------
  // Search 3: along-route (point-to-path distance using polyline)
  // ------------------------------
  @GetMapping("/search/route")
  public ResponseEntity<List<TripDto>> searchAlongRoute(
      @RequestParam double pickupLat,
      @RequestParam double pickupLng,
      @RequestParam double dropLat,
      @RequestParam double dropLng,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
      @RequestParam(defaultValue = "10") double radiusKm, // tighter corridor
      @RequestParam(required = false) OffsetDateTime at,
      @RequestParam(defaultValue = "120") int windowMin,
      @RequestParam(defaultValue = "time") String sort,   // time | price
      @RequestParam(defaultValue = "asc") String order,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(defaultValue = "1") int minSeats,
      @RequestParam(required = false) BigDecimal minPrice,
      @RequestParam(required = false) BigDecimal maxPrice
  ) {
    OffsetDateTime dayStart = date.atStartOfDay().atOffset(ZoneOffset.UTC);
    OffsetDateTime dayEnd   = dayStart.plusDays(1);
    OffsetDateTime from = dayStart, to = dayEnd.minusNanos(1);
    if (at != null) {
      from = at.minusMinutes(windowMin);
      to   = at.plusMinutes(windowMin);
      if (from.isBefore(dayStart)) from = dayStart;
      if (to.isAfter(dayEnd))      to   = dayEnd;
    }

    int seats = Math.max(1, minSeats);
    if (minPrice != null && minPrice.signum() < 0) minPrice = BigDecimal.ZERO;
    if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
      BigDecimal tmp = minPrice; minPrice = maxPrice; maxPrice = tmp;
    }

    var prefiltered = trips.searchActiveOpenList(from, to, List.of(TripStatus.OPEN), seats, minPrice, maxPrice);

    var filtered = prefiltered.stream()
        .filter(t -> {
          boolean pickupOk;
          boolean dropOk;
          if (t.getPolyline() != null && !t.getPolyline().isBlank()) {
            var path = com.routelink.geo.Polyline.decode(t.getPolyline());
            double d1 = com.routelink.geo.Geo.distancePointToPathKm(pickupLat, pickupLng, path);
            double d2 = com.routelink.geo.Geo.distancePointToPathKm(dropLat, dropLng, path);
            pickupOk = d1 <= radiusKm;
            dropOk   = d2 <= radiusKm;
          } else {
            pickupOk = com.routelink.geo.Geo.haversineKm(pickupLat, pickupLng, t.getStartLat(), t.getStartLng()) <= radiusKm;
            dropOk   = com.routelink.geo.Geo.haversineKm(dropLat,   dropLng,   t.getEndLat(),   t.getEndLng())   <= radiusKm;
          }
          return pickupOk && dropOk;
        })
        .sorted((a, b) -> {
          int cmp = "price".equalsIgnoreCase(sort)
              ? a.getPricePerSeat().compareTo(b.getPricePerSeat())
              : a.getRideAt().compareTo(b.getRideAt());
          return "desc".equalsIgnoreCase(order) ? -cmp : cmp;
        })
        .map(TripDto::from)
        .toList();

    int pSize = Math.max(1, size);
    int fromIdx = Math.max(0, page * pSize);
    int toIdx   = Math.min(filtered.size(), fromIdx + pSize);
    List<TripDto> pageItems = fromIdx >= filtered.size() ? List.of() : filtered.subList(fromIdx, toIdx);

    return ResponseEntity.ok()
        .header("X-Total-Count", String.valueOf(filtered.size()))
        .body(pageItems);
  }

  // --- Set/replace a trip's polyline so /search/route can use it ---
  public static class PolylinePoint { public double lat; public double lng; }
  public static class PolylineReq { public java.util.List<PolylinePoint> points; }

  @PutMapping("/{id}/polyline")
  public ResponseEntity<TripDto> setPolyline(@PathVariable Long id, @RequestBody PolylineReq req) {
    return trips.findById(id).map(t -> {
      var path = (req.points == null) ? java.util.List.<double[]>of()
          : req.points.stream().map(p -> new double[]{p.lat, p.lng}).toList();
      String enc = com.routelink.geo.Polyline.encode(path);
      t.setPolyline(enc);
      trips.save(t);
      return ResponseEntity.ok(TripDto.from(t));
    }).orElse(ResponseEntity.notFound().build());
  }

  // Close a trip (hide from search) — driver-only enforced in TripService
  @PostMapping("/{id}/close")
  public ResponseEntity<TripDto> close(@PathVariable Long id) {
    try { return ResponseEntity.ok(TripDto.from(tripService.close(id))); }
    catch (IllegalArgumentException e) { return ResponseEntity.notFound().build(); }
  }

  // Reopen a trip (if seatsLeft > 0) — driver-only enforced in TripService
  @PostMapping("/{id}/reopen")
  public ResponseEntity<TripDto> reopen(@PathVariable Long id) {
    try { return ResponseEntity.ok(TripDto.from(tripService.reopen(id))); }
    catch (IllegalArgumentException e) { return ResponseEntity.notFound().build(); }
  }
}
