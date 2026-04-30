package com.iliyadev.springboot.config

import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component

@Component
class DatabaseMigrationRunner(
    private val jdbcTemplate: JdbcTemplate
) {

    @EventListener(ApplicationReadyEvent::class)
    fun runMigrations() {
        try {
            jdbcTemplate.execute("ALTER TABLE courses ADD COLUMN IF NOT EXISTS average_rating float8 DEFAULT 0.0;")
            jdbcTemplate.execute("ALTER TABLE courses ADD COLUMN IF NOT EXISTS review_count int4 DEFAULT 0;")
            jdbcTemplate.execute("ALTER TABLE courses ADD COLUMN IF NOT EXISTS discount_percentage int4 DEFAULT 0;")
        } catch (e: Exception) {
            println("Database migration error: ${e.message}")
        }
    }
}
