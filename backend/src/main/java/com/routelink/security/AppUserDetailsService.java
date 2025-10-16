package com.routelink.security;

import com.routelink.user.User;
import com.routelink.user.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppUserDetailsService implements UserDetailsService {
  private final UserRepository users;
  public AppUserDetailsService(UserRepository users) { this.users = users; }

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    User u = users.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("No user: " + email));
    var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole().name()));
    return org.springframework.security.core.userdetails.User
        .withUsername(u.getEmail())
        .password(u.getPasswordHash())
        .authorities(authorities)
        .build();
  }
}
