package com.iliyadev.springboot.services

import com.iliyadev.springboot.models.*
import com.iliyadev.springboot.repositories.*
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class HomeService(
    private val bannerRepository: HomeBannerRepository,
    private val universityRepository: UniversityRepository,
    private val discountRepository: DiscountRepository,
    private val userRepository: UserRepository,
    private val elmPeakService: ElmPeakService,
    private val entertainmentService: EntertainmentService
) {
    fun getHomeData(): HomeDataResponse {
        val userCount = userRepository.count()
        val banners = bannerRepository.findAllByIsActiveTrueOrderByDisplayOrderAsc().map { it.toDto() }
        val elmData = elmPeakService.getElmPeakData()
        val entertainmentData = entertainmentService.getEntertainmentData()
        val discounts = discountRepository.findAllByOrderByCreatedAtDesc().take(10).map { it.toDto() }
        val universities = universityRepository.findAll().take(20).map { it.toDto() }

        return HomeDataResponse(
            userCount = userCount,
            banners = banners,
            scienceEvents = elmData.competitions.take(5), // Featured science events
            movies = entertainmentData.movies.take(5),
            discounts = discounts,
            universities = universities
        )
    }

    fun getActiveBanners(): List<HomeBanner> = bannerRepository.findAllByIsActiveTrueOrderByDisplayOrderAsc()
    
    fun getAllUniversities(): List<University> = universityRepository.findAll()
    
    fun getActiveDiscounts(): List<Discount> = discountRepository.findAllByOrderByCreatedAtDesc()
}
