package com.stock.heatmap.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Converts Railway / Heroku-style DATABASE_URL into Spring datasource properties
 * when SPRING_DATASOURCE_URL is not explicitly set.
 */
public class RailwayDatabaseEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String PROP_URL = "SPRING_DATASOURCE_URL";
    private static final String PROP_USER = "SPRING_DATASOURCE_USERNAME";
    private static final String PROP_PASSWORD = "SPRING_DATASOURCE_PASSWORD";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (hasText(environment.getProperty(PROP_URL))) {
            return;
        }

        Map<String, Object> props = new HashMap<>();

        String databaseUrl = environment.getProperty("DATABASE_URL");
        if (hasText(databaseUrl)) {
            applyDatabaseUrl(databaseUrl, props);
        } else {
            applyPgVariables(environment, props);
        }

        if (!props.isEmpty()) {
            environment.getPropertySources().addFirst(new MapPropertySource("railwayDatabase", props));
        }
    }

    private void applyDatabaseUrl(String databaseUrl, Map<String, Object> props) {
        try {
            String normalized = databaseUrl.replaceFirst("^postgres://", "postgresql://");
            URI uri = new URI(normalized);

            int port = uri.getPort() > 0 ? uri.getPort() : 5432;
            StringBuilder jdbcUrl = new StringBuilder("jdbc:postgresql://")
                    .append(uri.getHost())
                    .append(":")
                    .append(port)
                    .append(uri.getPath());

            if (uri.getQuery() != null && !uri.getQuery().isBlank()) {
                jdbcUrl.append("?").append(uri.getQuery());
            } else if (uri.getHost() != null && uri.getHost().contains("supabase.co")) {
                jdbcUrl.append("?sslmode=require");
            }

            props.put(PROP_URL, jdbcUrl.toString());

            if (uri.getUserInfo() != null) {
                String[] parts = uri.getUserInfo().split(":", 2);
                props.put(PROP_USER, decode(parts[0]));
                if (parts.length > 1) {
                    props.put(PROP_PASSWORD, decode(parts[1]));
                }
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse DATABASE_URL for Spring datasource config", ex);
        }
    }

    private void applyPgVariables(ConfigurableEnvironment environment, Map<String, Object> props) {
        String host = environment.getProperty("PGHOST");
        if (!hasText(host)) {
            return;
        }

        String port = environment.getProperty("PGPORT", "5432");
        String database = environment.getProperty("PGDATABASE", "railway");
        String username = environment.getProperty("PGUSER");
        String password = environment.getProperty("PGPASSWORD");

        StringBuilder jdbcUrl = new StringBuilder("jdbc:postgresql://")
                .append(host)
                .append(":")
                .append(port)
                .append("/")
                .append(database);

        if (host.contains("supabase.co")) {
            jdbcUrl.append("?sslmode=require");
        }

        props.put(PROP_URL, jdbcUrl.toString());
        if (hasText(username)) {
            props.put(PROP_USER, username);
        }
        if (hasText(password)) {
            props.put(PROP_PASSWORD, password);
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
