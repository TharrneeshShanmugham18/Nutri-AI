package com.nutriai;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DatabaseConnectivityIntegrationTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Verify DataSource connects to PostgreSQL and validates connection")
    void testDataSourceConnection() throws SQLException {
        assertThat(dataSource).isNotNull();
        try (Connection connection = dataSource.getConnection()) {
            assertThat(connection.isValid(2)).isTrue();
            assertThat(connection.getMetaData().getDatabaseProductName()).isEqualTo("PostgreSQL");
            assertThat(connection.getCatalog()).isEqualTo("nutriai");
        }
    }

    @Test
    @DisplayName("Verify Flyway migration executed V1 schema and created system_metadata")
    void testFlywayMigrationExecuted() {
        // Verify flyway schema history table exists and recorded V1
        Integer migrationCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM \"flyway_schema_history\" WHERE \"version\" = '1' AND \"success\" = true",
                Integer.class
        );
        assertThat(migrationCount).isNotNull().isEqualTo(1);

        // Verify system_metadata table exists and contains the seeded baseline records
        Integer metadataCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM system_metadata",
                Integer.class
        );
        assertThat(metadataCount).isNotNull().isGreaterThanOrEqualTo(2);

        String schemaVersion = jdbcTemplate.queryForObject(
                "SELECT value FROM system_metadata WHERE key = 'schema_version'",
                String.class
        );
        assertThat(schemaVersion).isEqualTo("1.0.0");
    }
}

