package com.stock.heatmap.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class DatasourceStartupLogger implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DatasourceStartupLogger.class);

    private final Environment environment;

    public DatasourceStartupLogger(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        String url = environment.getProperty("SPRING_DATASOURCE_URL");
        String user = environment.getProperty("SPRING_DATASOURCE_USERNAME");
        if (url == null || url.isBlank()) {
            log.warn("SPRING_DATASOURCE_URL is not set – falling back to localhost");
            return;
        }
        log.info("Database target: user={} url={}", user, sanitizeUrl(url));
    }

    static String sanitizeUrl(String url) {
        return url.replaceAll("://([^:@/]+):([^@/]+)@", "://$1:***@");
    }
}
