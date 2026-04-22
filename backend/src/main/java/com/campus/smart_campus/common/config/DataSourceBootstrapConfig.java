package com.campus.smart_campus.common.config;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class DataSourceBootstrapConfig {

    private static final Logger log = LoggerFactory.getLogger(DataSourceBootstrapConfig.class);
    private static final String POSTGRES_PREFIX = "jdbc:postgresql://";

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.hikari")
    public DataSource dataSource(
            DataSourceProperties properties,
            @Value("${app.database.name:}") String configuredDatabaseName,
            @Value("${app.database.admin-database:postgres}") String adminDatabaseName,
            @Value("${app.database.auto-create:true}") boolean autoCreateDatabase
    ) {
        String jdbcUrl = properties.determineUrl();
        String username = properties.determineUsername();
        String password = properties.determinePassword();

        ensurePostgresDatabaseExists(
                jdbcUrl,
                username,
                password,
                configuredDatabaseName,
                adminDatabaseName,
                autoCreateDatabase
        );

        return properties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    private void ensurePostgresDatabaseExists(
            String jdbcUrl,
            String username,
            String password,
            String configuredDatabaseName,
            String adminDatabaseName,
            boolean autoCreateDatabase
    ) {
        if (!autoCreateDatabase) {
            return;
        }

        if (jdbcUrl == null || !jdbcUrl.startsWith(POSTGRES_PREFIX)) {
            return;
        }

        String databaseName = (configuredDatabaseName == null || configuredDatabaseName.isBlank())
                ? extractDatabaseName(jdbcUrl)
                : configuredDatabaseName.trim();

        if (databaseName.isBlank()) {
            log.warn("PostgreSQL database auto-create is enabled, but no database name was resolved.");
            return;
        }

        String adminDb = (adminDatabaseName == null || adminDatabaseName.isBlank())
                ? "postgres"
                : adminDatabaseName.trim();
        String adminJdbcUrl = replaceDatabaseName(jdbcUrl, adminDb);

        try (Connection adminConnection = openConnection(adminJdbcUrl, username, password)) {
            if (databaseExists(adminConnection, databaseName)) {
                log.info("PostgreSQL database '{}' already exists.", databaseName);
                return;
            }

            try (Statement statement = adminConnection.createStatement()) {
                statement.execute("CREATE DATABASE \"" + escapeIdentifier(databaseName) + "\"");
            }

            log.info("Created PostgreSQL database '{}'.", databaseName);
        } catch (SQLException exception) {
            if ("42P04".equals(exception.getSQLState())) {
                log.info("PostgreSQL database '{}' already exists.", databaseName);
                return;
            }

            throw new IllegalStateException(
                    "Failed to auto-create PostgreSQL database '" + databaseName + "'. "
                            + "Check DB credentials and CREATE DATABASE permission.",
                    exception
            );
        }
    }

    private Connection openConnection(String jdbcUrl, String username, String password) throws SQLException {
        if (username == null || username.isBlank()) {
            return DriverManager.getConnection(jdbcUrl);
        }

        return DriverManager.getConnection(jdbcUrl, username, password == null ? "" : password);
    }

    private boolean databaseExists(Connection connection, String databaseName) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT 1 FROM pg_database WHERE datname = ?"
        )) {
            statement.setString(1, databaseName);
            return statement.executeQuery().next();
        }
    }

    private String extractDatabaseName(String jdbcUrl) {
        String hostAndPath = jdbcUrl.substring(POSTGRES_PREFIX.length());
        int slashIndex = hostAndPath.indexOf('/');
        if (slashIndex < 0 || slashIndex == hostAndPath.length() - 1) {
            return "";
        }

        String databaseAndQuery = hostAndPath.substring(slashIndex + 1);
        int queryIndex = databaseAndQuery.indexOf('?');
        return (queryIndex >= 0 ? databaseAndQuery.substring(0, queryIndex) : databaseAndQuery).trim();
    }

    private String replaceDatabaseName(String jdbcUrl, String replacementDatabaseName) {
        int queryIndex = jdbcUrl.indexOf('?');
        String queryPart = queryIndex >= 0 ? jdbcUrl.substring(queryIndex) : "";
        String withoutQuery = queryIndex >= 0 ? jdbcUrl.substring(0, queryIndex) : jdbcUrl;

        int slashIndex = withoutQuery.lastIndexOf('/');
        if (slashIndex < POSTGRES_PREFIX.length()) {
            return withoutQuery + "/" + replacementDatabaseName + queryPart;
        }

        return withoutQuery.substring(0, slashIndex + 1) + replacementDatabaseName + queryPart;
    }

    private String escapeIdentifier(String value) {
        return value.replace("\"", "\"\"");
    }
}

