package com.iliyadev.springboot.controllers

import com.iliyadev.springboot.models.*
import com.iliyadev.springboot.services.FileUploadService
import com.iliyadev.springboot.services.StoryService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@RestController
@RequestMapping("/api/stories")
class StoryController(
    private val storyService: StoryService,
    private val fileUploadService: FileUploadService
) {

    @GetMapping
    fun getStories(@RequestAttribute("userId") userId: UUID): ResponseEntity<List<StoryUserDto>> {
        val stories = storyService.getStoriesFeed(userId)
        return ResponseEntity.ok(stories)
    }

    @PostMapping
    fun postStory(
        @RequestAttribute("userId") userId: UUID,
        @RequestParam("file") file: MultipartFile,
        @RequestParam("type") typeStr: String,
        @RequestParam("duration") duration: Int,
        @RequestParam(value = "caption", required = false) caption: String?
    ): ResponseEntity<Any> {
        return try {
            val type = try {
                StoryType.valueOf(typeStr.uppercase())
            } catch (e: Exception) {
                StoryType.IMAGE
            }

            val mediaUrl = fileUploadService.uploadFile(
                file.bytes,
                file.originalFilename ?: "story_${System.currentTimeMillis()}",
                file.contentType ?: (if (type == StoryType.VIDEO) "video/mp4" else "image/jpeg")
            )

            val story = storyService.postStory(userId, mediaUrl, type, caption, duration)
            ResponseEntity.ok(story.toDto(userId))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @PostMapping("/{id}/view")
    fun markStoryViewed(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable id: UUID
    ): ResponseEntity<Unit> {
        storyService.markAsViewed(userId, id)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/{id}/views")
    fun getStoryViews(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable id: UUID
    ): ResponseEntity<List<StoryViewDto>> {
        return try {
            val views = storyService.getStoryViews(userId, id)
            ResponseEntity.ok(views)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(403).build()
        }
    }
    @DeleteMapping("/{id}")
    fun deleteStory(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable id: UUID
    ): ResponseEntity<Unit> {
        storyService.deleteStory(userId, id)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/group/{groupId}")
    fun postGroupStory(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable groupId: UUID,
        @RequestParam("file") file: MultipartFile,
        @RequestParam("type") typeStr: String,
        @RequestParam("duration") duration: Int,
        @RequestParam(value = "caption", required = false) caption: String?
    ): ResponseEntity<Any> {
        return try {
            val type = try {
                StoryType.valueOf(typeStr.uppercase())
            } catch (e: Exception) {
                StoryType.IMAGE
            }

            val mediaUrl = fileUploadService.uploadFile(
                file.bytes,
                file.originalFilename ?: "group_story_${System.currentTimeMillis()}",
                file.contentType ?: (if (type == StoryType.VIDEO) "video/mp4" else "image/jpeg")
            )

            val story = storyService.postGroupStory(userId, groupId, mediaUrl, type, caption, duration)
            ResponseEntity.ok(story.toDto(userId))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @PostMapping("/channel/{channelId}")
    fun postChannelStory(
        @RequestAttribute("userId") userId: UUID,
        @PathVariable channelId: UUID,
        @RequestParam("file") file: MultipartFile,
        @RequestParam("type") typeStr: String,
        @RequestParam("duration") duration: Int,
        @RequestParam(value = "caption", required = false) caption: String?
    ): ResponseEntity<Any> {
        return try {
            val type = try {
                StoryType.valueOf(typeStr.uppercase())
            } catch (e: Exception) {
                StoryType.IMAGE
            }

            val mediaUrl = fileUploadService.uploadFile(
                file.bytes,
                file.originalFilename ?: "channel_story_${System.currentTimeMillis()}",
                file.contentType ?: (if (type == StoryType.VIDEO) "video/mp4" else "image/jpeg")
            )

            val story = storyService.postChannelStory(userId, channelId, mediaUrl, type, caption, duration)
            ResponseEntity.ok(story.toDto(userId))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @GetMapping("/groups")
    fun getGroupStories(@RequestAttribute("userId") userId: UUID): ResponseEntity<List<StoryUserDto>> {
        val stories = storyService.getGroupStoriesFeed(userId)
        return ResponseEntity.ok(stories)
    }

    @GetMapping("/channels")
    fun getChannelStories(@RequestAttribute("userId") userId: UUID): ResponseEntity<List<StoryUserDto>> {
        val stories = storyService.getChannelStoriesFeed(userId)
        return ResponseEntity.ok(stories)
    }
}
