package com.routelink.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {
  private final String issuer;
  private final SecretKey key;
  private final long ttlMillis;
  private final long skewMillis;

  public JwtUtil(
      @Value("${security.jwt.issuer:routelink}") String issuer,
      @Value("${security.jwt.secret:change_me_change_me_change_me_change_me_32bytes}") String secret,
      @Value("${security.jwt.ttl-min:1440}") long ttlMin) {

    this.issuer = issuer;
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); // >=32 bytes
    this.ttlMillis = ttlMin * 60_000L;
    this.skewMillis = Duration.ofSeconds(30).toMillis();
  }

  public String create(String subject, Map<String, Object> claims) {
    Instant now = Instant.now();
    return Jwts.builder()
        .issuer(issuer)
        .subject(subject)
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plusMillis(ttlMillis)))
        .claims(claims)
        .signWith(key)
        .compact();
  }

  public Claims claims(String token) {
    return Jwts.parser()
        .clock(Date::new)
        .setAllowedClockSkewSeconds(skewMillis / 1000)
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  public String subject(String token) { return claims(token).getSubject(); }

  public boolean isTokenValid(String token, UserDetails ud) {
    try {
      Claims c = claims(token);
      if (issuer != null && !issuer.equals(c.getIssuer())) return false;
      String sub = c.getSubject();
      if (sub == null || ud == null || !sub.equalsIgnoreCase(ud.getUsername())) return false;
      Date exp = c.getExpiration();
      return exp != null && exp.toInstant().isAfter(Instant.now().minusMillis(skewMillis));
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }
}
