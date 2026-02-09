package com.iliyadev.springboot.controllers

import com.iliyadev.springboot.models.*
import com.iliyadev.springboot.services.ElmPeakService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/elm-peak")
class ElmPeakController(private val service: ElmPeakService) {

    @GetMapping("/events")
    fun getEvents(): ResponseEntity<ElmPeakResponse> {
        return ResponseEntity.ok(service.getElmPeakData())
    }

    @PostMapping("/ideas")
    fun submitIdea(
        @RequestAttribute("userId") userId: UUID,
        @RequestBody request: IdeaSubmissionRequest
    ): ResponseEntity<Any> {
        service.submitIdea(userId, request)
        return ResponseEntity.ok(mapOf("message" to "ایده شما با موفقیت ثبت شد و پس از بررسی با شما تماس خواهیم گرفت."))
    }

    @PostMapping("/reports")
    fun reportEvent(
        @RequestAttribute("userId") userId: UUID,
        @RequestBody request: EventReportRequest
    ): ResponseEntity<Any> {
        // Now saves to elm_events table with isApproved=false for admin approval
        service.submitUserEvent(userId, request)
        return ResponseEntity.ok(mapOf("message" to "رویداد شما ثبت شد و پس از تأیید توسط ادمین در اپلیکیشن نمایش داده خواهد شد."))
    }

    @GetMapping("/universities")
    fun getUniversities(): ResponseEntity<List<University>> {
        return ResponseEntity.ok(service.getUniversities())
    }
}
