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
        }
    }
}
