package com.routelink.auth;

import com.routelink.security.JwtUtil;
import com.routelink.user.Role;
import com.routelink.user.User;
import com.routelink.user.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class AuthService {

  private final UserRepository users;
  private final PasswordEncoder encoder;
  private final AuthenticationManager authManager;
  private final JwtUtil jwt;

  public AuthService(UserRepository users, PasswordEncoder encoder,
                     AuthenticationManager authManager, JwtUtil jwt) {
    this.users = users;
    this.encoder = encoder;
    this.authManager = authManager;
    this.jwt = jwt;
  }

  /** Register new user and return JWT */
  @Transactional
  public String signup(String name, String email, String password, String roleName) {
    users.findByEmail(email)
        .ifPresent(u -> { throw new IllegalStateException("Email already exists"); });

    Role role = "DRIVER".equalsIgnoreCase(roleName) ? Role.DRIVER : Role.RIDER;

    User u = new User();
    u.setName(name);
    u.setEmail(email);
    u.setPasswordHash(encoder.encode(password));
    u.setRole(role);
    users.save(u);

    // Create token with role + user ID
    return jwt.create(u.getEmail(), Map.of("uid", u.getId(), "roles", role.name()));
  }

  /** Login existing user and return JWT */
  public String login(String email, String password) {
    Authentication auth = authManager.authenticate(
        new UsernamePasswordAuthenticationToken(email, password)
    );
    User u = users.findByEmail(email).orElseThrow();
    return jwt.create(u.getEmail(), Map.of("uid", u.getId(), "roles", u.getRole().name()));
  }
}
