package com.iliyadev.springboot.controllers

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.iliyadev.springboot.models.ApiResponse
import jakarta.annotation.PostConstruct
import org.springframework.core.io.ClassPathResource
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

data class ProvinceData(
    val province: String,
    val cities: List<String>
)

@RestController
@RequestMapping("/api/locations")
class LocationController {

    private var iranLocations: Map<String, List<String>> = emptyMap()

    @PostConstruct
    fun init() {
        val mapper = jacksonObjectMapper()
        val resource = ClassPathResource("iran_cities.json")
        val provinceList: List<ProvinceData> = mapper.readValue(resource.inputStream)
        iranLocations = provinceList.associate { it.province to it.cities }
    }

    @GetMapping("/countries")
    fun getCountries(): ResponseEntity<ApiResponse<List<String>>> {
        return ResponseEntity.ok(ApiResponse(true, "Success", listOf("ایران")))
    }

    @GetMapping("/provinces/{country}")
    fun getProvinces(@PathVariable country: String): ResponseEntity<ApiResponse<List<String>>> {
        return if (country == "ایران" || country.lowercase() == "iran") {
            ResponseEntity.ok(ApiResponse(true, "Success", iranLocations.keys.toList().sorted()))
        } else {
            ResponseEntity.ok(ApiResponse(true, "Success", emptyList()))
        }
    }

    @GetMapping("/cities/{province}")
    fun getCities(@PathVariable province: String): ResponseEntity<ApiResponse<List<String>>> {
        val cities: List<String> = iranLocations[province] ?: emptyList()
        return ResponseEntity.ok(ApiResponse(true, "Success", cities))
    }
}
