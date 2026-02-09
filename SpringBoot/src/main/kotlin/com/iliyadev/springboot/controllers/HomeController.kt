package com.iliyadev.springboot.controllers

import com.iliyadev.springboot.models.ApiResponse
import com.iliyadev.springboot.models.HomeDataResponse
import com.iliyadev.springboot.services.HomeService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/home")
class HomeController(
    private val homeService: HomeService
) {
    @GetMapping
    fun getHomeData(): ResponseEntity<ApiResponse<HomeDataResponse>> {
        val data = homeService.getHomeData()
        return ResponseEntity.ok(ApiResponse(true, "Home data retrieved successfully", data))
    }
}
