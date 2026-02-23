package com.iliyadev.springboot.controllers

import com.iliyadev.springboot.config.security.UserPrincipal
import com.iliyadev.springboot.models.*
import com.iliyadev.springboot.services.SpecialFolderService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/special-folder")
class SpecialFolderController(
    private val specialFolderService: SpecialFolderService
) {
    @GetMapping
    fun getSpecialFolder(
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<SpecialFolderDto>> {
        val folder: SpecialFolderDto = specialFolderService.getSpecialFolder(principal.id)
        return ResponseEntity.ok(ApiResponse(success = true, message = "OK", data = folder))
    }

    @GetMapping("/ai-bots/{botId}/messages")
    fun getAiBotMessages(
        @PathVariable botId: UUID,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") size: Int,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<List<AiBotMessageDto>>> {
        val messages: List<AiBotMessageDto> = specialFolderService.getAiBotMessages(botId, principal.id, page, size)
        return ResponseEntity.ok(ApiResponse(success = true, message = "OK", data = messages))
    }

    @PostMapping("/ai-bots/{botId}/messages")
    fun sendAiBotMessage(
        @PathVariable botId: UUID,
        @RequestBody request: SendAiBotMessageRequest,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<List<AiBotMessageDto>>> {
        val messages: List<AiBotMessageDto> = specialFolderService.sendAiBotMessage(botId, principal.id, request.content)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Messages sent", data = messages))
    }
}

@RestController
@RequestMapping("/api/admin/special-folder")
class SpecialFolderAdminController(
    private val specialFolderService: SpecialFolderService
) {
    // ── AI Bots ───────────────────────────────────────────────────────────────

    @GetMapping("/ai-bots")
    fun getAllAiBots(): ResponseEntity<ApiResponse<List<AiBotDto>>> {
        val bots: List<AiBotDto> = specialFolderService.getAllAiBots()
        return ResponseEntity.ok(ApiResponse(success = true, message = "OK", data = bots))
    }

    @PostMapping("/ai-bots")
    fun createAiBot(
        @RequestBody request: CreateAiBotRequest
    ): ResponseEntity<ApiResponse<AiBotDto>> {
        val bot: AiBotDto = specialFolderService.createAiBot(request)
        return ResponseEntity.ok(ApiResponse(success = true, message = "AI bot created", data = bot))
    }

    @DeleteMapping("/ai-bots/{botId}")
    fun deleteAiBot(
        @PathVariable botId: UUID
    ): ResponseEntity<ApiResponse<String>> {
        specialFolderService.deleteAiBot(botId)
        return ResponseEntity.ok(ApiResponse(success = true, message = "AI bot deleted", data = "AI bot deleted"))
    }

    // ── Official Channels ────────────────────────────────────────────────────

    @GetMapping("/official-channels")
    fun getAllOfficialChannels(): ResponseEntity<ApiResponse<List<OfficialChannelAdminDto>>> {
        val channels: List<OfficialChannelAdminDto> = specialFolderService.getAllOfficialChannels()
        return ResponseEntity.ok(ApiResponse(success = true, message = "OK", data = channels))
    }

    @PostMapping("/official-channels")
    fun createOfficialChannel(
        @RequestBody request: CreateOfficialChannelRequest,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<SpecialChannelDto>> {
        val channel: SpecialChannelDto = specialFolderService.createOfficialChannel(request, principal.id)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Official channel created", data = channel))
    }

    @DeleteMapping("/official-channels/{channelId}")
    fun deleteOfficialChannel(
        @PathVariable channelId: UUID
    ): ResponseEntity<ApiResponse<String>> {
        specialFolderService.deleteOfficialChannel(channelId)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Official channel deleted", data = "Deleted"))
    }

    @PutMapping("/official-channels/{channelId}")
    fun updateOfficialChannel(
        @PathVariable channelId: UUID,
        @RequestBody request: CreateOfficialChannelRequest
    ): ResponseEntity<ApiResponse<OfficialChannelAdminDto>> {
        val updated: OfficialChannelAdminDto = specialFolderService.updateOfficialChannel(channelId, request)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Official channel updated", data = updated))
    }

    @GetMapping("/official-channels/{channelId}/admins")
    fun getChannelAdmins(
        @PathVariable channelId: UUID
    ): ResponseEntity<ApiResponse<List<UserDto>>> {
        val admins: List<UserDto> = specialFolderService.getChannelAdmins(channelId)
        return ResponseEntity.ok(ApiResponse(success = true, message = "OK", data = admins))
    }

    @PostMapping("/official-channels/{channelId}/admins")
    fun addChannelAdmin(
        @PathVariable channelId: UUID,
        @RequestBody request: AddOfficialAdminRequest
    ): ResponseEntity<ApiResponse<String>> {
        specialFolderService.addChannelAdmin(channelId, request.userId)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Admin added to official channel", data = "Admin added"))
    }

    @DeleteMapping("/official-channels/{channelId}/admins/{userId}")
    fun removeChannelAdmin(
        @PathVariable channelId: UUID,
        @PathVariable userId: UUID
    ): ResponseEntity<ApiResponse<String>> {
        specialFolderService.removeChannelAdmin(channelId, userId)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Admin removed from channel", data = "Admin removed"))
    }

    // ── Official Groups ──────────────────────────────────────────────────────

    @GetMapping("/official-groups")
    fun getAllOfficialGroups(): ResponseEntity<ApiResponse<List<OfficialGroupAdminDto>>> {
        val groups: List<OfficialGroupAdminDto> = specialFolderService.getAllOfficialGroups()
        return ResponseEntity.ok(ApiResponse(success = true, message = "OK", data = groups))
    }

    @PostMapping("/official-groups")
    fun createOfficialGroup(
        @RequestBody request: CreateOfficialGroupRequest,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<SpecialGroupDto>> {
        val group: SpecialGroupDto = specialFolderService.createOfficialGroup(request, principal.id)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Official group created", data = group))
    }

    @DeleteMapping("/official-groups/{groupId}")
    fun deleteOfficialGroup(
        @PathVariable groupId: UUID
    ): ResponseEntity<ApiResponse<String>> {
        specialFolderService.deleteOfficialGroup(groupId)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Official group deleted", data = "Deleted"))
    }

    @PutMapping("/official-groups/{groupId}")
    fun updateOfficialGroup(
        @PathVariable groupId: UUID,
        @RequestBody request: CreateOfficialGroupRequest
    ): ResponseEntity<ApiResponse<OfficialGroupAdminDto>> {
        val updated: OfficialGroupAdminDto = specialFolderService.updateOfficialGroup(groupId, request)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Official group updated", data = updated))
    }

    @GetMapping("/official-groups/{groupId}/admins")
    fun getGroupAdmins(
        @PathVariable groupId: UUID
    ): ResponseEntity<ApiResponse<List<UserDto>>> {
        val admins: List<UserDto> = specialFolderService.getGroupAdmins(groupId)
        return ResponseEntity.ok(ApiResponse(success = true, message = "OK", data = admins))
    }

    @PostMapping("/official-groups/{groupId}/admins")
    fun addGroupAdmin(
        @PathVariable groupId: UUID,
        @RequestBody request: AddOfficialAdminRequest
    ): ResponseEntity<ApiResponse<String>> {
        specialFolderService.addGroupAdmin(groupId, request.userId)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Admin added to official group", data = "Admin added"))
    }

    @DeleteMapping("/official-groups/{groupId}/admins/{userId}")
    fun removeGroupAdmin(
        @PathVariable groupId: UUID,
        @PathVariable userId: UUID
    ): ResponseEntity<ApiResponse<String>> {
        specialFolderService.removeGroupAdmin(groupId, userId)
        return ResponseEntity.ok(ApiResponse(success = true, message = "Admin removed from group", data = "Admin removed"))
    }
}


