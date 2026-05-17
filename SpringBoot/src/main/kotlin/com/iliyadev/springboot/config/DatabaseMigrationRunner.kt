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
            // Drop the enum check constraint to allow new enum types (e.g. TEACHER_INVITE, ADMIN_INVITE)
            jdbcTemplate.execute("ALTER TABLE notifications DROP CONSTRAINT IF EXISTS notifications_type_check;")
            
            jdbcTemplate.execute("ALTER TABLE courses ADD COLUMN IF NOT EXISTS average_rating float8 DEFAULT 0.0;")
            jdbcTemplate.execute("ALTER TABLE courses ADD COLUMN IF NOT EXISTS review_count int4 DEFAULT 0;")
            jdbcTemplate.execute("ALTER TABLE courses ADD COLUMN IF NOT EXISTS discount_percentage int4 DEFAULT 0;")
            jdbcTemplate.execute("ALTER TABLE courses ADD COLUMN IF NOT EXISTS click_count bigint DEFAULT 0;")
            jdbcTemplate.execute("ALTER TABLE courses ADD COLUMN IF NOT EXISTS view_count bigint DEFAULT 0;")
            
            // Special folder system official isolation migrations
            jdbcTemplate.execute("ALTER TABLE channels ADD COLUMN IF NOT EXISTS is_system_official boolean default false;")
            jdbcTemplate.execute("ALTER TABLE groups ADD COLUMN IF NOT EXISTS is_system_official boolean default false;")
            // Seed existing system-official folders (excluding teacher/course channels and groups)
            jdbcTemplate.execute("UPDATE channels SET is_system_official = true WHERE is_official = true AND classification != 'VERIFIED_TEACHER';")
            jdbcTemplate.execute("UPDATE groups SET is_system_official = true WHERE is_official = true AND official_category != 'COURSE_GROUP';")
        } catch (e: Exception) {
            println("Database migration error: ${e.message}")
        }
    }
}
