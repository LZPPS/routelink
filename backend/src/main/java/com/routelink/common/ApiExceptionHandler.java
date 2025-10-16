package com.routelink.common;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {

  public record ApiError(String code, String message, Instant timestamp) {}

  /* ---- Your custom exceptions ---- */

  @ExceptionHandler(NotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ApiError notFound(NotFoundException ex) {
    return new ApiError("NOT_FOUND", ex.getMessage(), Instant.now());
  }

  @ExceptionHandler(BadRequestException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiError badRequest(BadRequestException ex) {
    return new ApiError("BAD_REQUEST", ex.getMessage(), Instant.now());
  }

  @ExceptionHandler(ForbiddenException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public ApiError forbidden(ForbiddenException ex) {
    return new ApiError("FORBIDDEN", ex.getMessage(), Instant.now());
  }

  /* ---- Common framework/runtime exceptions (very useful during testing) ---- */

  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiError illegalArg(IllegalArgumentException ex) {
    return new ApiError("BAD_REQUEST", msg(ex), Instant.now());
  }

  @ExceptionHandler(IllegalStateException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public ApiError illegalState(IllegalStateException ex) {
    return new ApiError("CONFLICT", msg(ex), Instant.now());
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public ApiError dataIntegrity(DataIntegrityViolationException ex) {
    return new ApiError("CONFLICT", root(ex), Instant.now());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ApiError beanValidation(MethodArgumentNotValidException ex) {
    String details = ex.getBindingResult().getFieldErrors().stream()
        .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
        .collect(Collectors.joining("; "));
    return new ApiError("BAD_REQUEST", details, Instant.now());
  }

  // Security
  @ExceptionHandler(AuthenticationException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public ApiError auth(AuthenticationException ex) {
    return new ApiError("UNAUTHORIZED", "Invalid or missing token", Instant.now());
  }

  @ExceptionHandler(AccessDeniedException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public ApiError accessDenied(AccessDeniedException ex) {
    return new ApiError("FORBIDDEN", "Not allowed", Instant.now());
  }

  /* ---- Fallback: include class + message so 500s are readable in Postman ---- */

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ApiError fallback(Exception ex) {
    return new ApiError("INTERNAL_ERROR",
        ex.getClass().getSimpleName() + ": " + msg(ex),
        Instant.now());
  }

  /* ---- helpers ---- */
  private static String msg(Throwable t) {
    return t == null ? "" : (t.getMessage() == null ? "" : t.getMessage());
  }
  private static String root(Throwable t) {
    Throwable r = t;
    while (r != null && r.getCause() != null && r.getCause() != r) r = r.getCause();
    return msg(r);
  }
}
