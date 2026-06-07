package com.sportshop.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class UserSchemaMigration implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(UserSchemaMigration.class);

    private final JdbcTemplate jdbcTemplate;

    public UserSchemaMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            if (!columnExists("users", "created_at")) {
                log.info("Adding column users.created_at");
                jdbcTemplate.execute(
                        "ALTER TABLE `users` ADD COLUMN `created_at` DATETIME NULL DEFAULT NULL AFTER `verify`");
            }
            if (!columnExists("users", "updated_at")) {
                log.info("Adding column users.updated_at");
                jdbcTemplate.execute(
                        "ALTER TABLE `users` ADD COLUMN `updated_at` DATETIME NULL DEFAULT NULL AFTER `created_at`");
            }
            jdbcTemplate.execute("ALTER TABLE `users` MODIFY COLUMN `updated_at` DATETIME NULL DEFAULT NULL");
            if (columnExists("users", "regtime")) {
                jdbcTemplate.update(
                        "UPDATE `users` SET `created_at` = `regtime` WHERE `created_at` IS NULL AND `regtime` IS NOT NULL");
                log.info("Dropping column users.regtime");
                jdbcTemplate.execute("ALTER TABLE `users` DROP COLUMN `regtime`");
            }
            jdbcTemplate.update("UPDATE `users` SET `created_at` = NOW() WHERE `created_at` IS NULL");
            jdbcTemplate.update("UPDATE `users` SET `updated_at` = NULL WHERE `updated_at` = `created_at`");
        } catch (Exception e) {
            log.error("User schema migration failed: {}", e.getMessage(), e);
        }
    }

    private boolean columnExists(String table, String column) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.COLUMNS " +
                        "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?",
                Integer.class,
                table,
                column);
        return count != null && count > 0;
    }
}
