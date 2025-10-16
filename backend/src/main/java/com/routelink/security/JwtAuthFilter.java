package com.routelink.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

  private final JwtUtil jwt;
  private final UserDetailsService uds;

  public JwtAuthFilter(JwtUtil jwt, AppUserDetailsService uds) {
    this.jwt = jwt;
    this.uds = uds;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws ServletException, IOException {

    // allow auth & health endpoints without token
    String path = req.getServletPath();
    if (path != null && (path.startsWith("/auth") || path.startsWith("/api/health"))) {
      chain.doFilter(req, res);
      return;
    }

    String header = req.getHeader(HttpHeaders.AUTHORIZATION);
    if (header == null || !header.startsWith("Bearer ")) {
      chain.doFilter(req, res);
      return;
    }

    String token = header.substring(7);
    try {
      String email = jwt.subject(token);
      if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        UserDetails ud = uds.loadUserByUsername(email);
        if (jwt.isTokenValid(token, ud)) {
          UsernamePasswordAuthenticationToken auth =
              new UsernamePasswordAuthenticationToken(ud, null, ud.getAuthorities());
          auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
          SecurityContextHolder.getContext().setAuthentication(auth);
        }
      }
    } catch (Exception e) {
      logger.warn("JWT validation failed: " + e.getMessage());
    }

    chain.doFilter(req, res);
  }
}
