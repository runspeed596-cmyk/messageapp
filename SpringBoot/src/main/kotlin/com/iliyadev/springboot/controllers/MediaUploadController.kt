package com.iliyadev.springboot.controllers

import com.iliyadev.springboot.models.ApiResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.UUID

@RestController
@RequestMapping("/api/admin/upload")
class MediaUploadController {

    @Value("\${app.upload.dir:uploads}")
    private lateinit var uploadDir: String

    @Value("\${app.base-url:http://localhost:8080}")
    private lateinit var baseUrl: String

    @PostMapping("/banner")
    fun uploadBannerImage(@RequestParam("file") file: MultipartFile): ResponseEntity<ApiResponse<Map<String, String>>> {
        return uploadFile(file, "banners", listOf("image/jpeg", "image/png", "image/webp", "image/gif"))
    }

    @PostMapping("/video")
    fun uploadVideo(@RequestParam("file") file: MultipartFile): ResponseEntity<ApiResponse<Map<String, String>>> {
        return uploadFile(file, "videos", listOf("video/mp4", "video/webm", "video/quicktime", "video/x-msvideo"))
    }

    @PostMapping("/audio")
    fun uploadAudio(@RequestParam("file") file: MultipartFile): ResponseEntity<ApiResponse<Map<String, String>>> {
        return uploadFile(file, "audio", listOf("audio/mpeg", "audio/wav", "audio/ogg", "audio/mp3", "audio/aac"))
    }

    @PostMapping("/image")
    fun uploadImage(@RequestParam("file") file: MultipartFile): ResponseEntity<ApiResponse<Map<String, String>>> {
        return uploadFile(file, "images", listOf("image/jpeg", "image/png", "image/webp", "image/gif"))
    }

    private fun uploadFile(
        file: MultipartFile,
        subDir: String,
        allowedTypes: List<String>
    ): ResponseEntity<ApiResponse<Map<String, String>>> {
        if (file.isEmpty) {
            return ResponseEntity.badRequest()
                .body(ApiResponse(false, "فایل خالی است", emptyMap()))
        }

        val contentType = file.contentType ?: ""
        if (contentType !in allowedTypes) {
            return ResponseEntity.badRequest()
                .body(ApiResponse(false, "نوع فایل مجاز نیست: $contentType", emptyMap()))
        }

        try {
            val extension = file.originalFilename?.substringAfterLast('.', "") ?: "bin"
            val fileName = "${UUID.randomUUID()}.$extension"
            val targetDir = Paths.get(uploadDir, subDir)
            
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir)
            }

            val targetPath = targetDir.resolve(fileName)
            Files.copy(file.inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING)

            val fileUrl = "/api/media/$subDir/$fileName"
            
            return ResponseEntity.ok(
                ApiResponse(
                    true,
                    "فایل با موفقیت آپلود شد",
                    mapOf("url" to fileUrl, "fileName" to fileName)
                )
            )
        } catch (e: Exception) {
            return ResponseEntity.internalServerError()
                .body(ApiResponse(false, "خطا در آپلود فایل: ${e.message}", emptyMap()))
        }
    }
}
