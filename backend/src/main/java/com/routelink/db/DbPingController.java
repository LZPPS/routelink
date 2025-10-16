package com.routelink.db;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DbPingController {
    private final JdbcTemplate jdbc;
    public DbPingController(JdbcTemplate jdbc){ this.jdbc = jdbc; }

    @GetMapping("/api/db/ping")
    public String ping() {
        String version = jdbc.queryForObject("select version()", String.class);
        return "DB OK: " + version;
    }
}
