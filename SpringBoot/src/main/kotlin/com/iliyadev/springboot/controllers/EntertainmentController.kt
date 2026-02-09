package com.iliyadev.springboot.controllers

import com.iliyadev.springboot.config.security.UserPrincipal
import com.iliyadev.springboot.models.*
import com.iliyadev.springboot.services.EntertainmentService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.nio.file.Paths
import java.util.UUID

@RestController
@RequestMapping("/api/entertainment")
class EntertainmentController(private val entertainmentService: EntertainmentService) {

    @GetMapping
    fun getEntertainment(): ApiResponse<EntertainmentResponse> {
        return ApiResponse(true, "Success", entertainmentService.getEntertainmentData())
    }

    @PostMapping("/solve")
    fun solveRiddle(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestBody request: SolveRiddleRequest
    ): ApiResponse<RiddleResult> {
        return try {
            val result = entertainmentService.solveRiddle(principal.id, request.riddleId, request.answerIndex)
            ApiResponse(true, "Success", result)
        } catch (e: Exception) {
            ApiResponse(false, e.message ?: "Error solving riddle", null)
        }
    }

    @GetMapping("/media/video")
    fun getVideo(): ResponseEntity<Resource> {
        // Absolute path from the project structure
        val path = Paths.get("e:/Learn/programming/ponisha/MessageApp2/SpringBoot/Recording 2026-01-30 182829.mp4")
        val resource = FileSystemResource(path)
        
        if (!resource.exists()) {
            return ResponseEntity.notFound().build()
        }

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("video/mp4"))
            .body(resource)
    }

    @GetMapping("/media/music")
    fun getMusic(): ResponseEntity<Resource> {
        // Absolute path from the project structure
        val path = Paths.get("e:/Learn/programming/ponisha/MessageApp2/SpringBoot/No Name - Studying Music (320).mp3")
        val resource = FileSystemResource(path)

        if (!resource.exists()) {
            return ResponseEntity.notFound().build()
        }

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("audio/mpeg"))
            .body(resource)
    }
}

data class SolveRiddleRequest(
    val riddleId: UUID,
    val answerIndex: Int
)
