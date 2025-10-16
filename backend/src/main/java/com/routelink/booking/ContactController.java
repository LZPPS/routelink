// src/main/java/com/routelink/booking/ContactController.java
package com.routelink.booking;

import com.routelink.common.MaskingUtil;
import com.routelink.common.ForbiddenException;
import com.routelink.common.NotFoundException;
import com.routelink.user.User;
import com.routelink.user.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
public class ContactController {

  private final BookingRepository bookingRepo;
  private final UserRepository userRepo;

  public record ContactDto(String driverName, String driverEmail, String driverPhone) {}

  public ContactController(BookingRepository bookingRepo, UserRepository userRepo) {
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

  @GetMapping("/{id}/contact")
  public ContactDto contact(@PathVariable Long id) {
    Long me = currentUserId();

    Booking bk = bookingRepo.findById(id)
        .orElseThrow(() -> new NotFoundException("Booking not found"));

    Long driverId = bk.getTrip().getDriver().getId();
    Long riderId  = bk.getRider().getId();

    if (!me.equals(driverId) && !me.equals(riderId))
      throw new ForbiddenException("Not allowed");

    User driver = userRepo.findById(driverId)
        .orElseThrow(() -> new NotFoundException("Driver not found"));

    boolean unmask = bk.getStatus() == BookingStatus.CONFIRMED;
    String email = unmask ? driver.getEmail() : MaskingUtil.maskEmail(driver.getEmail());
    String phone = unmask ? driver.getPhone() : MaskingUtil.maskPhone(driver.getPhone());

    return new ContactDto(driver.getName(), email, phone);
  }
}
