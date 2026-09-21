package com.medistock.api.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
public class DataSourceConfig {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceConfig.class);

    @Value("${spring.datasource.url:${DB_URL:${DATABASE_URL:}}}")
    private String rawUrl;

    @Value("${spring.datasource.username:${DB_USER:}}")
    private String username;

    @Value("${spring.datasource.password:${DB_PASSWORD:}}")
    private String password;

    @Bean
    @Primary
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();

        String jdbcUrl = rawUrl != null ? rawUrl.trim() : "";
        String user = username != null ? username.trim() : "";
        String pass = password != null ? password.trim() : "";

        if (!jdbcUrl.isEmpty()) {
            // Handle cloud database URLs starting with postgres:// or postgresql:// (e.g. from Render, Neon, Supabase)
            if (jdbcUrl.startsWith("postgres://") || jdbcUrl.startsWith("postgresql://")) {
                try {
                    URI uri = new URI(jdbcUrl);
                    String host = uri.getHost();
                    int port = uri.getPort() == -1 ? 5432 : uri.getPort();
                    String path = uri.getPath(); // e.g. /medistock_db
                    
                    // Construct standard JDBC PostgreSQL URL
                    jdbcUrl = "jdbc:postgresql://" + host + ":" + port + path;

                    // Automatically extract username and password if present in the URI (user:pass@host)
                    if (uri.getUserInfo() != null && user.isEmpty()) {
                        String[] userInfo = uri.getUserInfo().split(":", 2);
                        user = userInfo[0];
                        if (userInfo.length > 1 && pass.isEmpty()) {
                            pass = userInfo[1];
                        }
                    }
                    logger.info("✓ Cloud PostgreSQL URL parsed: jdbc:postgresql://{}:{}{}", host, port, path);
                } catch (Exception e) {
                    logger.warn("Could not parse DB URI, falling back with jdbc: prefix: {}", e.getMessage());
                    if (!jdbcUrl.startsWith("jdbc:")) {
                        jdbcUrl = "jdbc:" + jdbcUrl;
                    }
                }
            }
        }

        config.setJdbcUrl(jdbcUrl);
        if (!user.isEmpty()) {
            config.setUsername(user);
        }
        if (!pass.isEmpty()) {
            config.setPassword(pass);
        }
        config.setDriverClassName("org.postgresql.Driver");

        // Sensible connection pool settings for cloud containers
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);

        return new HikariDataSource(config);
    }
}
