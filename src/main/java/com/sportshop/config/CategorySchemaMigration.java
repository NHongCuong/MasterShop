package com.sportshop.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Adds created_at / updated_at to category when ddl-auto=none and columns are missing.
 */
@Component
public class CategorySchemaMigration implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CategorySchemaMigration.class);

    private final JdbcTemplate jdbcTemplate;

    public CategorySchemaMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            if (!columnExists("category", "created_at")) {
                log.info("Adding column category.created_at");
                jdbcTemplate.execute(
                        "ALTER TABLE `category` ADD COLUMN `created_at` DATETIME NULL DEFAULT NULL AFTER `Icon`");
            }
            if (!columnExists("category", "updated_at")) {
                log.info("Adding column category.updated_at");
                jdbcTemplate.execute(
                        "ALTER TABLE `category` ADD COLUMN `updated_at` DATETIME NULL DEFAULT NULL AFTER `created_at`");
            }
            jdbcTemplate.update("UPDATE `category` SET `created_at` = NOW() WHERE `created_at` IS NULL");
        } catch (Exception e) {
            log.error("Category schema migration failed: {}", e.getMessage(), e);
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
