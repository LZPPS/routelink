package com.routelink.security;

import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableMethodSecurity // allows @PreAuthorize, etc.
public class SecurityConfig {

  private final JwtAuthFilter jwtFilter;
  private final com.routelink.common.SecurityErrorHandlers securityErrors;

  public SecurityConfig(JwtAuthFilter jwtFilter,
                        com.routelink.common.SecurityErrorHandlers securityErrors) {
    this.jwtFilter = jwtFilter;
    this.securityErrors = securityErrors;
  }

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
      .csrf(csrf -> csrf.disable())
      .cors(withDefaults())
      .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

      // ✅ Return ApiError JSON for 401/403 triggered by the filter chain
      .exceptionHandling(ex -> ex
          .authenticationEntryPoint(securityErrors) // 401
          .accessDeniedHandler(securityErrors)      // 403
      )

      .authorizeHttpRequests(auth -> auth

        // ---- Public endpoints ----
        .requestMatchers(HttpMethod.POST, "/api/trips/search-unified").permitAll()
        .requestMatchers(HttpMethod.GET,  "/api/trips/**").permitAll()
        .requestMatchers("/auth/**", "/actuator/health",
            "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
        .requestMatchers("/dev/mail/test").permitAll() 

        // ---- Trip mutations: DRIVERS only ----
        .requestMatchers(HttpMethod.POST,   "/api/trips").hasRole("DRIVER")
        .requestMatchers(HttpMethod.PUT,    "/api/trips/**").hasRole("DRIVER")
        .requestMatchers(HttpMethod.PATCH,  "/api/trips/**").hasRole("DRIVER")
        .requestMatchers(HttpMethod.DELETE, "/api/trips/**").hasRole("DRIVER")
        .requestMatchers(HttpMethod.POST,   "/api/trips/**").hasRole("DRIVER") // close/reopen etc.

        // ---- Bookings: riders request; drivers manage ----
        .requestMatchers(HttpMethod.GET,  "/api/bookings/me").hasRole("RIDER")
        .requestMatchers(HttpMethod.POST, "/api/bookings/request").hasRole("RIDER")
        .requestMatchers(HttpMethod.POST, "/api/bookings/*/cancel").hasRole("RIDER")

        .requestMatchers(HttpMethod.GET,  "/api/bookings/trip/**").hasRole("DRIVER")
        .requestMatchers(HttpMethod.POST, "/api/bookings/*/confirm").hasRole("DRIVER")
        .requestMatchers(HttpMethod.POST, "/api/bookings/*/decline").hasRole("DRIVER")

        // ---- Everything else must be authenticated ----
        .anyRequest().authenticated()
      )

      .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource(
      @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:5174,http://localhost:5175,http://localhost:5176}") String origins,
      @Value("${app.cors.allowed-methods:GET,POST,PUT,PATCH,DELETE,OPTIONS}") String methods,
      @Value("${app.cors.allowed-headers:Authorization,Content-Type,Accept}") String headers,
      @Value("${app.cors.allow-credentials:true}") boolean allowCredentials
  ) {
    CorsConfiguration cfg = new CorsConfiguration();
    cfg.setAllowedOrigins(Arrays.asList(origins.split(",")));
    cfg.setAllowedMethods(Arrays.asList(methods.split(",")));
    cfg.setAllowedHeaders(Arrays.asList(headers.split(",")));
    cfg.setAllowCredentials(allowCredentials);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", cfg);
    return source;
  }

  @Bean
  AuthenticationManager authenticationManager(AuthenticationConfiguration c) throws Exception {
    return c.getAuthenticationManager();
  }
}
