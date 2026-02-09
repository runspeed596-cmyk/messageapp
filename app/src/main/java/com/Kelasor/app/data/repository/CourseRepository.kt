package com.Kelasor.app.data.repository

import com.Kelasor.app.data.local.dao.CourseDao
import com.Kelasor.app.data.local.entity.CourseEntity
import com.Kelasor.app.data.session.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CourseRepository @Inject constructor(
    private val courseDao: CourseDao,
    private val channelRepository: ChannelRepository, // Inject ChannelRepository to reuse createChannel logic
    private val sessionManager: SessionManager
) {
    fun observeAllCourses(): Flow<List<CourseEntity>> = courseDao.observeAllCourses()

    suspend fun createCourse(title: String, description: String): Result<CourseEntity> {
        return try {
            // 1. Create a Channel for this course
            // We use ChannelRepository's createChannel. 
            // Note: ChannelRepository.createChannel returns ChannelResult<Channel> or similar.
            // Let's assume it returns a domain model Channel or Result.
            // Based on previous files, ChannelRepository.createChannel(name, description, isPublic) seems likely.
            
            // We'll create a PUBLIC channel by default for courses? Or private? 
            // User requested "a channel with same title and topic is created".
            
            val channelResult = channelRepository.createChannel(
                name = title,
                description = description,
                isPublic = true, // Courses usually imply public or discoverable content? 
                publicId = null // Auto-generate
            ).filter { it !is ChannelResult.Loading }.first()

            // Depending on ChannelRepository implementation (Flow/Result), we extract the ID.
            // If createChannel is a suspend function returning Result/Channel:
            
            var channelId = ""
            var creatorId = sessionManager.userId.first() ?: ""
            
            if (channelResult is ChannelResult.Success) {
                 android.util.Log.d("CourseRepo", "Channel created successfully: ${channelResult.data.id}")
                 channelId = channelResult.data.id
                 
                 // 2. Create Course Entity linked to Channel
                 val courseId = UUID.randomUUID().toString()
                 val course = CourseEntity(
                     id = courseId,
                     title = title,
                     description = description,
                     channelId = channelId,
                     creatorId = creatorId
                 )
                 
                 // 3. Save to local DB
                 courseDao.insertCourse(course)
                 android.util.Log.d("CourseRepo", "Course inserted into DB: $courseId")
                 
                 Result.success(course)
            } else if (channelResult is ChannelResult.Error) {
                android.util.Log.e("CourseRepo", "Channel creation failed: ${channelResult.message}")
                Result.failure(Exception(channelResult.message))
            } else {
                 Result.failure(Exception("Unknown error creating channel for course"))
            }
        } catch (e: Exception) {
            android.util.Log.e("CourseRepo", "Exception in createCourse", e)
            Result.failure(e)
        }
    }
}
