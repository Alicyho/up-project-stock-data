package com.stock.heatmap.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class DbHealthController {

    private final DataSource dataSource;

    public DbHealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/api/health/db")
    public Map<String, Object> databaseHealth() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("hint", "Use Supabase Session pooler port 5432, username postgres.<project-ref>, region ap-northeast-1");
        try (Connection connection = dataSource.getConnection()) {
            body.put("status", "UP");
            body.put("database", connection.getCatalog());
            body.put("url", connection.getMetaData().getURL());
            return body;
        } catch (Exception ex) {
            body.put("status", "DOWN");
            body.put("error", ex.getMessage());
            return body;
        }
    }
}
