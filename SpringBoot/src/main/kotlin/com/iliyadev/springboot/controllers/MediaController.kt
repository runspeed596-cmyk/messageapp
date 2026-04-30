package com.iliyadev.springboot.controllers

import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.nio.file.Files
import java.nio.file.Paths

@RestController
@RequestMapping("/api/media")
class MediaController {

    @Value("\${app.upload.dir:uploads}")
    private lateinit var uploadDir: String

    @GetMapping("/{folder}/{filename}")
    fun serveFile(
        @PathVariable folder: String,
        @PathVariable filename: String
    ): ResponseEntity<Resource> {
        return try {
            val filePath = Paths.get(uploadDir, folder, filename)
            val resource = UrlResource(filePath.toUri())
            if (!resource.exists() || !resource.isReadable) {
                return ResponseEntity.notFound().build()
            }
            val contentType = Files.probeContentType(filePath) ?: "application/octet-stream"
            ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"$filename\"")
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=2592000, immutable")
                .body(resource)
        } catch (e: Exception) {
            ResponseEntity.notFound().build()
        }
    }
}
