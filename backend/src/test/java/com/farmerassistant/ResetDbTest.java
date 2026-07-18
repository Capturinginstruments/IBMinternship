package com.farmerassistant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("dev")
public class ResetDbTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void resetDatabase() {
        System.out.println("Starting clean database reset...");
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
        jdbcTemplate.execute("DROP TABLE IF EXISTS users;");
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1;");
        System.out.println("All tables dropped successfully!");
    }
}
