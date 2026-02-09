package com.Kelasor.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import com.Kelasor.app.data.remote.api.ApiService
import javax.inject.Inject

// Data Models (Updated to match backend DTOs)
data class HomeState(
    val userCount: Long = 0,
    val banners: List<BannerItem> = emptyList(),
    val scienceEvents: List<EventItem> = emptyList(),
    val discounts: List<DiscountItem> = emptyList(),
    val movies: List<MovieItem> = emptyList(),
    val universities: List<UniversityItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

data class BannerItem(val id: String, val title: String, val imageUrl: String, val colorStart: Long, val colorEnd: Long)
data class EventItem(val id: String, val title: String, val date: String, val type: String)
data class DiscountItem(val id: String, val title: String, val code: String, val percent: Int)
data class MovieItem(val id: String, val title: String, val thumbnailUrl: String?)
data class UniversityItem(val id: String, val name: String, val imageUrl: String?)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val response = apiService.getHomeData()
                if (response.isSuccessful) {
                    val apiRes = response.body()
                    if (apiRes != null && apiRes.success && apiRes.data != null) {
                        val homeData = apiRes.data
                        _state.update {
                            it.copy(
                                userCount = homeData.userCount,
                                isLoading = false,
                                banners = homeData.banners.map { b -> 
                                    BannerItem(b.id, b.title, b.imageUrl, b.colorStart, b.colorEnd) 
                                },
                                scienceEvents = homeData.scienceEvents.map { e -> 
                                    EventItem(e.id ?: "", e.title, e.date, e.type.name) 
                                },
                                discounts = homeData.discounts.map { d -> 
                                    DiscountItem(d.id, d.title, d.code ?: "", d.percent) 
                                },
                                movies = homeData.movies.map { m -> 
                                    MovieItem(m.id, m.title, m.thumbnailUrl) 
                                },
                                universities = homeData.universities.map { u ->
                                    UniversityItem(u.id, u.name, u.imageUrl)
                                }
                            )
                        }
                    } else {
                        _state.update { it.copy(isLoading = false, error = apiRes?.message ?: "خطای نامشخص") }
                    }
                } else {
                    _state.update { it.copy(isLoading = false, error = "خطای سرور: ${response.code()}") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun fetchHomeData() {
        loadHomeData()
    }
}
