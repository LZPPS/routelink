// src/main/java/com/routelink/auth/AuthDebugController.java
package com.routelink.auth;

import com.routelink.common.NotFoundException;
import com.routelink.user.User;
import com.routelink.user.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AuthDebugController {
  private final UserRepository users;
  public AuthDebugController(UserRepository users) { this.users = users; }

  public record MeDto(Long id, String email, String name, String role) {}

  @GetMapping("/me")
  public MeDto me() {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || auth.getName() == null) throw new IllegalStateException("Unauthenticated");
    User u = users.findByEmail(auth.getName()).orElseThrow(() -> new NotFoundException("User not found"));
    return new MeDto(u.getId(), u.getEmail(), u.getName(), u.getRole() == null ? "USER" : u.getRole().name());
  }
}
