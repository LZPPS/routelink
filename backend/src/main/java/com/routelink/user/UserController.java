package com.routelink.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
  private final UserRepository repo;
  private final PasswordEncoder encoder;

  public UserController(UserRepository repo, PasswordEncoder encoder) {
    this.repo = repo;
    this.encoder = encoder;
  }

  /** Admin: list all users */
  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public List<UserDto> list() {
    return repo.findAll().stream().map(UserDto::from).toList();
  }

  /** Admin: get by id */
  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<UserDto> get(@PathVariable Long id) {
    return repo.findById(id).map(UserDto::from)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  public record CreateUserReq(
      @NotBlank String name,
      @Email String email,
      String phone,
      @NotBlank String password,
      Role role // optional; defaults to RIDER
  ) {}

  /** Admin: create a user (sets passwordHash + role) */
  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<?> create(@Valid @RequestBody CreateUserReq req) {
    String email = req.email().toLowerCase().trim();
    if (repo.existsByEmail(email)) {
      return ResponseEntity.status(409).body(Map.of("error", "Email already registered"));
    }

    User u = new User();
    u.setName(req.name().trim());
    u.setEmail(email);
    u.setPhone(req.phone() != null ? req.phone().trim() : null);
    u.setPasswordHash(encoder.encode(req.password()));
    u.setRole(req.role() == null ? Role.RIDER : req.role());

    User saved = repo.save(u);
    return ResponseEntity
        .created(URI.create("/api/users/" + saved.getId()))
        .body(UserDto.from(saved));
  }
}
