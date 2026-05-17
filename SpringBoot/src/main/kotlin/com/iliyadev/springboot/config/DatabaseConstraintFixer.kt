package com.iliyadev.springboot.config

import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import javax.sql.DataSource

/**
 * Fixes PostgreSQL CHECK constraints on startup.
 * Hibernate ddl-auto=update does NOT update existing CHECK constraints
 * when new enum values are added, so we must do it manually.
 */
@Component
class DatabaseConstraintFixer(
    private val dataSource: DataSource
) : ApplicationRunner {
    private val logger = LoggerFactory.getLogger(javaClass)
    override fun run(args: ApplicationArguments?) {
        val allowedTypes = listOf(
            "TEXT", "IMAGE", "VIDEO", "VIDEO_NOTE", "VOICE", "AUDIO",
            "FILE", "LOCATION", "CONTACT", "STICKER", "GIF", "POLL", "LINK"
        )
        val checkExpr = allowedTypes.joinToString(", ") { "'$it'" }
        val tables = listOf("messages", "group_messages", "channel_posts")
        dataSource.connection.use { conn ->
            for (table in tables) {
                val constraintName = "${table}_type_check"
                try {
                    conn.createStatement().use { stmt ->
                        stmt.execute("ALTER TABLE $table DROP CONSTRAINT IF EXISTS $constraintName")
                        stmt.execute(
                            "ALTER TABLE $table ADD CONSTRAINT $constraintName CHECK (type::text = ANY(ARRAY[$checkExpr]))"
                        )
                    }
                    logger.info("✅ Updated $constraintName with all MessageType values")
                } catch (e: Exception) {
                    logger.warn("⚠️ Could not update $constraintName: ${e.message}")
                }
            }
            // Fix courses status constraint
            try {
                val courseStatuses = listOf("DRAFT", "PENDING", "APPROVED", "REJECTED", "ACTIVE", "COMPLETED", "CANCELLED")
                val courseCheckExpr = courseStatuses.joinToString(", ") { "'$it'" }
                conn.createStatement().use { stmt ->
                    stmt.execute("ALTER TABLE courses DROP CONSTRAINT IF EXISTS courses_status_check")
                    stmt.execute("ALTER TABLE courses ADD CONSTRAINT courses_status_check CHECK (status::text = ANY(ARRAY[$courseCheckExpr]))")
                }
                logger.info("✅ Updated courses_status_check with all CourseStatus values")
            } catch (e: Exception) {
                logger.warn("⚠️ Could not update courses_status_check: ${e.message}")
            }
        }
        
        // Fix: Drop deprecated display_order column from reference tables
        val referenceTables = listOf(
            "fields_of_study", "education_levels", "universities", 
            "cities", "provinces", "ministries", "banners"
        )
        dataSource.connection.use { conn ->
            for (table in referenceTables) {
                try {
                    conn.createStatement().use { stmt ->
                        stmt.execute("ALTER TABLE $table DROP COLUMN IF EXISTS display_order CASCADE")
                    }
                    logger.info("✅ Dropped display_order from $table if existed")
                } catch (e: Exception) {
                    logger.warn("⚠️ Could not drop display_order from $table: ${e.message}")
                }
            }
        }
    }
}
