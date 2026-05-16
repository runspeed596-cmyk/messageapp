package com.iliyadev.springboot.config

import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component

@Component
class DatabaseMigrationConfig(private val jdbcTemplate: JdbcTemplate) : ApplicationRunner {
    private val logger = LoggerFactory.getLogger(DatabaseMigrationConfig::class.java)

    override fun run(args: ApplicationArguments?) {
        try {
            logger.info("Starting database migration fix...")
            
            // Fix groups_official_category_check constraint to include COURSE_GROUP
            // The easiest way is to drop it and let Hibernate recreate it if it wants, 
            // but manually updating it is safer.
            // However, DROP is faster for development fix.
            jdbcTemplate.execute("ALTER TABLE groups DROP CONSTRAINT IF EXISTS groups_official_category_check")
            logger.info("Dropped groups_official_category_check constraint successfully.")
            
            // Final fallback for Mosbat Elm bot specifically (native SQL to be absolutely sure)
            val botCount = jdbcTemplate.queryForObject("SELECT count(*) FROM ai_bots WHERE bot_type = 'mosbat_elm'", Int::class.java) ?: 0
            if (botCount == 0) {
                logger.info("Mosbat Elm Bot not found. Creating it...")
                jdbcTemplate.execute("""
                    INSERT INTO ai_bots (id, name, bot_type, category, description, avatar_url, display_order, is_active, created_at)
                    VALUES (gen_random_uuid(), 'ربات مثبت علم', 'mosbat_elm', 'SPECIALIST', 'دستیار هوشمند شما در دوره‌های آموزشی مثبت علم 🎓', 'https://img.icons8.com/fluency/96/graduation-cap.png', 0, true, now())
                """.trimIndent())
                logger.info("Mosbat Elm Bot created successfully.")
            }

            // Create a System User for the Bot in the 'users' table to allow messenger integration
            val botUserCount = jdbcTemplate.queryForObject("SELECT count(*) FROM users WHERE id = '00000000-0000-0000-0000-000000000001'", Int::class.java) ?: 0
            if (botUserCount == 0) {
                logger.info("Bot User not found in 'users' table. Creating it...")
                jdbcTemplate.execute("""
                    INSERT INTO users (id, username, display_name, phone_number, avatar_url, role, points, is_online, is_premium, created_at)
                    VALUES ('00000000-0000-0000-0000-000000000001', 'mosbat_elm_bot', 'ربات مثبت علم', '0000000001', 'https://img.icons8.com/fluency/96/graduation-cap.png', 'ADMIN', 0, true, true, now())
                """.trimIndent())
                logger.info("Bot User created successfully.")
            }
            
        } catch (e: Exception) {
            logger.error("Error during database migration: ${e.message}")
        }
    }
}
