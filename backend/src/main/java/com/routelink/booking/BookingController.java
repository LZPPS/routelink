package com.routelink.booking;

import com.routelink.common.NotFoundException;
import com.routelink.user.User;
import com.routelink.user.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

  private final BookingService bookingService;
  private final BookingRepository bookingRepo;
  private final UserRepository userRepo;

  public BookingController(BookingService bookingService,
                           BookingRepository bookingRepo,
                           UserRepository userRepo) {
    this.bookingService = bookingService;
    this.bookingRepo = bookingRepo;
    this.userRepo = userRepo;
  }

  private Long currentUserId() {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || auth.getName() == null) throw new IllegalStateException("Unauthenticated");
    return userRepo.findByEmail(auth.getName())
        .map(User::getId)
        .orElseThrow(() -> new NotFoundException("User not found"));
  }

  // Request a booking (rider) -> 201 Created
  @PostMapping("/request")
  public ResponseEntity<BookingDto> request(
      @Valid @RequestBody BookingService.RequestBooking req,
      UriComponentsBuilder uri) {

    Booking b = bookingService.request(req);
    return ResponseEntity
        .created(uri.path("/api/bookings/id/{id}").buildAndExpand(b.getId()).toUri())
        .body(BookingDto.from(b));
  }

  // Confirm / Decline (driver)
  @PostMapping("/{id}/confirm")
  public ResponseEntity<BookingDto> confirm(@PathVariable Long id) {
    Booking b = bookingService.confirm(id);
    return ResponseEntity.ok(BookingDto.from(b));
  }

  @PostMapping("/{id}/decline")
  public ResponseEntity<BookingDto> decline(@PathVariable Long id) {
    Booking b = bookingService.decline(id);
    return ResponseEntity.ok(BookingDto.from(b));
  }

  // Cancel (rider)
  @PostMapping("/{id}/cancel")
  public ResponseEntity<BookingDto> cancel(@PathVariable Long id) {
    Booking b = bookingService.cancel(id);
    return ResponseEntity.ok(BookingDto.from(b));
  }

  // ---------- Helpers for testing / inspection ----------
  @GetMapping("/id/{id}") // <- avoids conflict with "/me"
  public ResponseEntity<BookingDto> get(@PathVariable Long id) {
    return bookingService.get(id)
        .map(b -> ResponseEntity.ok(BookingDto.from(b)))
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/me")
  public List<BookingDto> myBookings() {
    Long me = currentUserId();
    return bookingRepo.findByRider_Id(me).stream().map(BookingDto::from).toList();
  }

  @GetMapping("/trip/{tripId}")
  public List<BookingWithRiderDto> tripBookings(@PathVariable Long tripId) {
	  return bookingRepo.findByTrip_Id(tripId).stream()
	      .map(BookingWithRiderDto::from).toList();
	
  }

  /* ---------- Simple error mapping ---------- */

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<?> handleBadRequest(IllegalArgumentException e) {
    String msg = e.getMessage() == null ? "" : e.getMessage();
    if (msg.toLowerCase().contains("not found")) {
      return ResponseEntity.status(404).body(Map.of("error", msg));
    }
    return ResponseEntity.badRequest().body(Map.of("error", msg));
  }

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<?> handleConflict(IllegalStateException e) {
    String msg = e.getMessage() == null ? "Conflict" : e.getMessage();
    return ResponseEntity.status(409).body(Map.of("error", msg));
  }
}
