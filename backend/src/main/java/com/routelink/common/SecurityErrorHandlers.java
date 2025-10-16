package com.routelink.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

/** Emits your ApiError JSON for 401/403 raised by Spring Security. */
@Component
public class SecurityErrorHandlers implements AuthenticationEntryPoint, AccessDeniedHandler {
  private final ObjectMapper om = new ObjectMapper();

  private void write(HttpServletResponse res, HttpStatus status, String code, String message) throws IOException {
    res.setStatus(status.value());
    res.setContentType("application/json");
    var body = Map.of(
        "code", code,
        "message", message,
        "timestamp", Instant.now().toString()
    );
    om.writeValue(res.getOutputStream(), body);
  }

  /** 401 – unauthenticated */
  @Override
  public void commence(HttpServletRequest req, HttpServletResponse res, AuthenticationException ex) throws IOException {
    write(res, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Login required");
  }

  /** 403 – authenticated but not allowed */
  @Override
  public void handle(HttpServletRequest req, HttpServletResponse res, AccessDeniedException ex) throws IOException {
    write(res, HttpStatus.FORBIDDEN, "FORBIDDEN", "You don't have permission to perform this action");
  }
}
