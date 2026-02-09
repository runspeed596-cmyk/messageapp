package com.iliyadev.springboot.controllers

import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.nio.file.Files
import java.nio.file.Paths

@RestController
@RequestMapping("/api/media")
class MediaServeController {

    @Value("\${app.upload.dir:uploads}")
    private lateinit var uploadDir: String

    @GetMapping("/banners/{fileName}")
    fun serveBanner(@PathVariable fileName: String): ResponseEntity<Resource> {
        return serveFile("banners", fileName)
    }

    @GetMapping("/videos/{fileName}")
    fun serveVideo(@PathVariable fileName: String): ResponseEntity<Resource> {
        return serveFile("videos", fileName)
    }

    @GetMapping("/audio/{fileName}")
    fun serveAudio(@PathVariable fileName: String): ResponseEntity<Resource> {
        return serveFile("audio", fileName)
    }

    @GetMapping("/images/{fileName}")
    fun serveImage(@PathVariable fileName: String): ResponseEntity<Resource> {
        return serveFile("images", fileName)
    }

    private fun serveFile(subDir: String, fileName: String): ResponseEntity<Resource> {
        val path = Paths.get(uploadDir, subDir, fileName)
        
        if (!Files.exists(path)) {
            return ResponseEntity.notFound().build()
        }

        val resource = FileSystemResource(path)
        val contentType = Files.probeContentType(path) ?: "application/octet-stream"

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"$fileName\"")
            .body(resource)
    }
}
