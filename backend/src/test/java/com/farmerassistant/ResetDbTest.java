package com.farmerassistant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.sql.Connection;
import java.util.Objects;

@SpringBootTest
@ActiveProfiles("dev")
public class ResetDbTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void resetAndSeedDatabase() throws Exception {
        System.out.println("Dropping all existing tables...");
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0;");
        jdbcTemplate.execute("DROP TABLE IF EXISTS scheme_bookmarks;");
        jdbcTemplate.execute("DROP TABLE IF EXISTS government_schemes;");
        jdbcTemplate.execute("DROP TABLE IF EXISTS chat_messages;");
        jdbcTemplate.execute("DROP TABLE IF EXISTS crop_recommendations;");
        jdbcTemplate.execute("DROP TABLE IF EXISTS disease_reports;");
        jdbcTemplate.execute("DROP TABLE IF EXISTS farmers;");
        jdbcTemplate.execute("DROP TABLE IF EXISTS agricultural_officers;");
        jdbcTemplate.execute("DROP TABLE IF EXISTS otp_tokens;");
        jdbcTemplate.execute("DROP TABLE IF EXISTS refresh_tokens;");
        jdbcTemplate.execute("DROP TABLE IF EXISTS notifications;");
        jdbcTemplate.execute("DROP TABLE IF EXISTS market_prices;");
        jdbcTemplate.execute("DROP TABLE IF EXISTS users;");
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1;");
        System.out.println("All tables dropped!");

        System.out.println("Starting clean database reset using schema.sql...");
        try (Connection conn = Objects.requireNonNull(jdbcTemplate.getDataSource()).getConnection()) {
            // First run schema.sql to cleanly recreate all tables with exact default values
            ScriptUtils.executeSqlScript(conn, new FileSystemResource("../database/schema.sql"));
            System.out.println("schema.sql executed successfully!");

            // Run seed.sql
            System.out.println("Seeding database using seed.sql...");
            ScriptUtils.executeSqlScript(conn, new FileSystemResource("../database/seed.sql"));
            System.out.println("seed.sql executed successfully!");

            // Run seed_market_prices.sql
            System.out.println("Seeding database using seed_market_prices.sql...");
            ScriptUtils.executeSqlScript(conn, new FileSystemResource("../database/seed_market_prices.sql"));
            System.out.println("seed_market_prices.sql executed successfully!");
        }
    }
}
