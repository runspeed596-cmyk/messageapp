package com.iliyadev.springboot.controllers

import com.iliyadev.springboot.models.*
import com.iliyadev.springboot.repositories.ElmEventRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/admin/elm-peak")
class ElmPeakAdminController(
    private val eventRepository: ElmEventRepository
) {

    // Get all pending (unapproved) events
    @GetMapping("/pending")
    fun getPendingEvents(): ResponseEntity<ApiResponse<List<ElmEvent>>> {
        val pending = eventRepository.findAll().filter { !it.isApproved }
        return ResponseEntity.ok(ApiResponse(true, "Success", pending))
    }

    // Get all approved events
    @GetMapping("/approved")
    fun getApprovedEvents(): ResponseEntity<ApiResponse<List<ElmEvent>>> {
        val approved = eventRepository.findAll().filter { it.isApproved }
        return ResponseEntity.ok(ApiResponse(true, "Success", approved))
    }

    // Approve an event
    @PutMapping("/{id}/approve")
    fun approveEvent(@PathVariable id: UUID): ResponseEntity<ApiResponse<Unit>> {
        val event = eventRepository.findById(id).orElse(null)
            ?: return ResponseEntity.notFound().build()
        
        event.isApproved = true
        eventRepository.save(event)
        return ResponseEntity.ok(ApiResponse(true, "رویداد تأیید شد", Unit))
    }

    // Reject (delete) an event
    @DeleteMapping("/{id}")
    fun rejectEvent(@PathVariable id: UUID): ResponseEntity<ApiResponse<Unit>> {
        eventRepository.deleteById(id)
        return ResponseEntity.ok(ApiResponse(true, "رویداد رد شد و حذف گردید", Unit))
    }

    // Create event (admin can create pre-approved)
    @PostMapping
    fun createEvent(@RequestBody event: ElmEvent): ResponseEntity<ApiResponse<ElmEvent>> {
        event.isApproved = true // Admin-created events are auto-approved
        return ResponseEntity.ok(ApiResponse(true, "رویداد ایجاد شد", eventRepository.save(event)))
    }
}
