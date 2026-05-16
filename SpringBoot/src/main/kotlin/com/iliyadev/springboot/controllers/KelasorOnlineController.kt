package com.iliyadev.springboot.controllers

import com.iliyadev.springboot.models.ApiResponse
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.nio.charset.StandardCharsets
import java.util.*
import javax.crypto.SecretKey

// ═══════════════════════════════════════════════════════════════════════════════
// 🎓 کلاسور آنلاین — پورتال وب ویدئو کنفرانس (Jitsi Meet + JWT)
// ═══════════════════════════════════════════════════════════════════════════════

data class OrganizerLoginRequest(
    val username: String,
    val password: String,
    val roomName: String = "کلاسور آنلاین"
)

data class GuestJoinRequest(
    val guestName: String,
    val roomId: String
)

data class RoomInfo(
    val roomId: String,
    val joinUrl: String,
    val roomName: String
)

@RestController
@RequestMapping("/api/kelasor-online")
class KelasorOnlineController {
    private val logger = LoggerFactory.getLogger(KelasorOnlineController::class.java)

    @Value("\${JITSI_JWT_APP_ID:kelasor-online}")
    private lateinit var jwtAppId: String

    @Value("\${JITSI_JWT_SECRET_V2:K3las0r_J1ts1_S3cr3t_2026_KelasorApp_V2}")
    private lateinit var jwtSecret: String

    // ── Hardcoded test organizer credentials ──
    private data class OrganizerAccount(
        val username: String,
        val password: String,
        val displayName: String
    )

    private val testAccounts: List<OrganizerAccount> = listOf(
        OrganizerAccount("admin", "kelasor2026", "مدیر کلاسور"),
        OrganizerAccount("teacher1", "teacher123", "استاد تستی ۱"),
        OrganizerAccount("teacher2", "teacher456", "استاد تستی ۲")
    )

    // Track active rooms: roomId -> roomName (for guest lookup)
    private val activeRooms: MutableMap<String, String> = mutableMapOf()

    /**
     * Generate a Jitsi JWT token for the given user.
     * In Jitsi, ANY user with a valid JWT becomes an authenticated user (Moderator).
     */
    private fun generateJitsiJwt(
        displayName: String,
        roomName: String
    ): String {
        val secretBytes: ByteArray = jwtSecret.toByteArray(StandardCharsets.UTF_8)
        val key: SecretKey = Keys.hmacShaKeyFor(secretBytes)
        val now: Date = Date()
        val expiration: Date = Date(now.time + 24 * 60 * 60 * 1000) // 24 hours
        val context: Map<String, Any> = mapOf(
            "user" to mapOf(
                "name" to displayName,
                "email" to "admin@kelasorapp.ir",
                "id" to UUID.randomUUID().toString()
            )
        )
        return Jwts.builder()
            .setHeaderParam("typ", "JWT")
            .setIssuer(jwtAppId)
            .setSubject("meet.jitsi")
            .setAudience(jwtAppId)
            .setIssuedAt(now)
            .setExpiration(expiration)
            .claim("context", context)
            .claim("room", roomName) // Exact room name instead of '*'
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }

    /**
     * POST /api/kelasor-online/login
     * Organizer logs in, gets a Jitsi room URL with moderator JWT.
     */
    @PostMapping("/login")
    fun organizerLogin(@RequestBody request: OrganizerLoginRequest): ResponseEntity<ApiResponse<RoomInfo>> {
        val account: OrganizerAccount? = testAccounts.find {
            it.username == request.username && it.password == request.password
        }
        if (account == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse(success = false, message = "نام کاربری یا رمز عبور اشتباه است", data = null))
        }
        // Generate room ID (URL-safe, lowercase)
        val roomId: String = "kelasor-${UUID.randomUUID().toString().substring(0, 8)}"
        val roomDisplayName: String = request.roomName.ifBlank { "کلاسور آنلاین" }
        // Store room for guest access
        activeRooms[roomId] = roomDisplayName
        
        // Generate moderator JWT
        val token: String = generateJitsiJwt(
            displayName = account.displayName,
            roomName = roomId
        )
        
        // Build join URL (Jitsi room URL with JWT). Organizer becomes moderator.
        val joinUrl: String = "/${roomId}?jwt=${token}#config.subject=${java.net.URLEncoder.encode(roomDisplayName, "UTF-8")}"
        
        logger.info("Kelasor Online room created: $roomId by ${account.username}")
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "اتاق با موفقیت ایجاد شد",
                data = RoomInfo(roomId = roomId, joinUrl = joinUrl, roomName = roomDisplayName)
            )
        )
    }

    /**
     * POST /api/kelasor-online/guest-join
     * Guest gets a Jitsi room URL WITHOUT a JWT, making them an anonymous guest.
     */
    @PostMapping("/guest-join")
    fun guestJoin(@RequestBody request: GuestJoinRequest): ResponseEntity<ApiResponse<RoomInfo>> {
        val guestName: String = request.guestName.trim()
        if (guestName.isBlank()) {
            return ResponseEntity.badRequest()
                .body(ApiResponse(success = false, message = "لطفاً نام خود را وارد کنید", data = null))
        }
        val roomName: String? = activeRooms[request.roomId]
        if (roomName == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse(success = false, message = "اتاق یافت نشد. لطفاً کد اتاق را بررسی کنید.", data = null))
        }
        
        // Guests DO NOT get a JWT. If they had a JWT, they would become authenticated users (moderators).
        // Instead, they get the URL with their name injected via URL params.
        val encodedName = java.net.URLEncoder.encode(guestName, "UTF-8")
        val joinUrl: String = "/${request.roomId}#userInfo.displayName=\"${encodedName}\""
        
        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "در حال ورود به کلاس...",
                data = RoomInfo(roomId = request.roomId, joinUrl = joinUrl, roomName = roomName)
            )
        )
    }

    /**
     * GET /api/kelasor-online/rooms
     * List active rooms (for debugging/admin).
     */
    @GetMapping("/rooms")
    fun listRooms(): ResponseEntity<ApiResponse<Map<String, String>>> {
        return ResponseEntity.ok(
            ApiResponse(success = true, message = "اتاق‌های فعال", data = activeRooms)
        )
    }
}
