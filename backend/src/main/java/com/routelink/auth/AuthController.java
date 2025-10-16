package com.routelink.auth;

import com.routelink.security.JwtUtil;
import com.routelink.user.Role;
import com.routelink.user.User;
import com.routelink.user.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
  private final UserRepository users;
  private final PasswordEncoder enc;
  private final JwtUtil jwt;

  public AuthController(UserRepository users, PasswordEncoder enc, JwtUtil jwt) {
    this.users = users; this.enc = enc; this.jwt = jwt;
  }

  // DTOs: accept role as String for leniency
  public record SignupReq(@NotBlank String name, @Email String email, @NotBlank String password, String role) {}
  public record LoginReq(@Email String email, @NotBlank String password) {}
  public record AuthRes(String token, Long userId, String email, String name, String role) {}

  @PostMapping("/signup")
  public ResponseEntity<?> signup(@Valid @RequestBody SignupReq req) {
    String email = req.email().toLowerCase().trim();
    if (users.existsByEmail(email)) {
      return ResponseEntity.status(409).body(Map.of("error", "Email already registered"));
    }

    Role finalRole = (req.role()==null || req.role().isBlank())
        ? Role.RIDER
        : Role.valueOf(req.role().toUpperCase());

    User u = new User();
    u.setName(req.name().trim());
    u.setEmail(email);
    u.setPasswordHash(enc.encode(req.password()));
    u.setRole(finalRole);
    users.save(u);

    String token = jwt.create(
        u.getEmail(),
        Map.of("uid", u.getId(), "roles", List.of(u.getRole().name()))
    );

    return ResponseEntity
        .created(URI.create("/users/" + u.getId()))
        .body(new AuthRes(token, u.getId(), u.getEmail(), u.getName(), u.getRole().name()));
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@Valid @RequestBody LoginReq req) {
    String email = req.email().toLowerCase().trim();
    User u = users.findByEmail(email).orElse(null);
    if (u == null || !enc.matches(req.password(), u.getPasswordHash())) {
      return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
    }

    String token = jwt.create(
        u.getEmail(),
        Map.of("uid", u.getId(), "roles", List.of(u.getRole().name()))
    );

    return ResponseEntity.ok(new AuthRes(token, u.getId(), u.getEmail(), u.getName(), u.getRole().name()));
  }

  @GetMapping("/me")
  public ResponseEntity<?> me(@RequestHeader(name = "Authorization", required = false) String auth) {
    if (auth == null || !auth.startsWith("Bearer ")) {
      return ResponseEntity.status(401).body(Map.of("error", "Missing bearer token"));
    }
    try {
      String token = auth.substring(7);
      var claims = jwt.claims(token);
      return ResponseEntity.ok(Map.of(
          "email", jwt.subject(token),
          "uid", claims.get("uid"),
          "roles", claims.get("roles")
      ));
    } catch (Exception e) {
      return ResponseEntity.status(401).body(Map.of("error", "Invalid token"));
    }
  }
}