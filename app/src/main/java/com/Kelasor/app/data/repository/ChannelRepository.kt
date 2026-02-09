package com.Kelasor.app.data.repository

import com.Kelasor.app.data.local.dao.*
import com.Kelasor.app.data.remote.api.ApiService
import com.Kelasor.app.data.remote.dto.*
import com.Kelasor.app.domain.mapper.*
import com.Kelasor.app.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import javax.inject.Inject
import javax.inject.Singleton

// ═══════════════════════════════════════════════════════════════════════════════
// 📢 Channel Result Sealed Class
// ═══════════════════════════════════════════════════════════════════════════════

sealed class ChannelResult<out T> {
    data class Success<T>(val data: T) : ChannelResult<T>()
    data class Error(val message: String) : ChannelResult<Nothing>()
    data object Loading : ChannelResult<Nothing>()
}

// ═══════════════════════════════════════════════════════════════════════════════
// 📢 Channel Repository
// ═══════════════════════════════════════════════════════════════════════════════

@Singleton
class ChannelRepository @Inject constructor(
    private val apiService: ApiService,
    private val channelDao: ChannelDao,
    private val channelSubscriberDao: ChannelSubscriberDao,
    private val channelPostDao: ChannelPostDao,
    private val pollRepository: PollRepository,
    private val userDao: UserDao
) {
    // ═══════════════════════════════════════════════════════════════════════════
    // 📺 Channel List Operations
    // ═══════════════════════════════════════════════════════════════════════════
    
    fun observeSubscribedChannels(): Flow<List<Channel>> = 
        channelDao.observeSubscribedChannels().map { channels ->
            channels.map { it.toDomain() }
        }.distinctUntilChanged()
    
    fun observeAllChannels(): Flow<List<Channel>> = 
        channelDao.observeAllChannels().map { channels ->
            channels.map { it.toDomain() }
        }.distinctUntilChanged()
    
    fun observeChannel(channelId: String): Flow<Channel?> = 
        channelDao.observeChannelById(channelId).map { it?.toDomain() }.distinctUntilChanged()
    
    suspend fun getChannels(page: Int = 0, forceRefresh: Boolean = false): Flow<ChannelResult<List<Channel>>> = flow {
        emit(ChannelResult.Loading)
        
        val cachedChannels = channelDao.observeSubscribedChannels().first()
        val archivedChannels = channelDao.observeArchivedChannels().first()
        val allCachedChannels = cachedChannels + archivedChannels
        
        if (allCachedChannels.isNotEmpty() && !forceRefresh) {
            emit(ChannelResult.Success(allCachedChannels.map { it.toDomain() }))
        }
        
        try {
            val response = apiService.getChannels(page)
            if (response.isSuccessful) {
                val channelDtos = response.body()?.channels ?: emptyList()
                
                // Create map of ALL cached channels including archived
                val cachedChannelsMap = allCachedChannels.associateBy { it.id }
                val entitiesToSave = channelDtos.map { dto ->
                    val serverEntity = dto.toEntity()
                    val cached = cachedChannelsMap[dto.id]
                    if (cached != null) {
                        // Preserve all local flags: isAdmin, isSubscribed, isArchived, isPinned
                        serverEntity.copy(
                            isAdmin = cached.isAdmin || serverEntity.isAdmin,
                            isSubscribed = cached.isSubscribed || serverEntity.isSubscribed,
                            isArchived = cached.isArchived,
                            isPinned = cached.isPinned
                        )
                    } else {
                        serverEntity
                    }
                }
                
                channelDao.insertChannels(entitiesToSave)
                emit(ChannelResult.Success(entitiesToSave.map { it.toDomain() }))
            } else {
                if (allCachedChannels.isEmpty()) {
                    emit(ChannelResult.Error("خطا در دریافت کانال‌ها"))
                }
            }
        } catch (e: Exception) {
            if (allCachedChannels.isEmpty()) {
                emit(ChannelResult.Error("خطا در اتصال به سرور: ${e.message}"))
            }
        }
    }
    
    suspend fun getChannelById(channelId: String): Flow<ChannelResult<Channel>> = flow {
        emit(ChannelResult.Loading)
        
        // Emit cached data first
        val cachedChannel = channelDao.getChannelById(channelId)
        if (cachedChannel != null) {
            emit(ChannelResult.Success(cachedChannel.toDomain()))
        }
        
        try {
            val response = apiService.getChannelById(channelId)
            if (response.isSuccessful && response.body()?.success == true) {
                val channelDto = response.body()?.data
                if (channelDto != null) {
                    // IMPORTANT: Preserve all local flags when syncing from API
                    val serverEntity = channelDto.toEntity()
                    
                    val entityToSave = if (cachedChannel != null) {
                        serverEntity.copy(
                            isAdmin = cachedChannel.isAdmin || serverEntity.isAdmin,
                            isSubscribed = cachedChannel.isSubscribed || serverEntity.isSubscribed,
                            isArchived = cachedChannel.isArchived,
                            isPinned = cachedChannel.isPinned
                        )
                    } else {
                         serverEntity
                    }
                    
                    channelDao.insertChannel(entityToSave)
                    emit(ChannelResult.Success(entityToSave.toDomain()))
                }
            } else {
                if (cachedChannel == null) {
                    emit(ChannelResult.Error(response.body()?.message ?: "کانال یافت نشد"))
                }
            }
        } catch (e: Exception) {
            if (cachedChannel == null) {
                emit(ChannelResult.Error("خطا در اتصال به سرور: ${e.message}"))
            }
        }
    }
    
    suspend fun searchChannels(query: String): Flow<ChannelResult<List<Channel>>> = flow {
        emit(ChannelResult.Loading)
        try {
            val searchQuery = query.trim()
            if (searchQuery.isBlank()) {
                emit(ChannelResult.Success(emptyList()))
                return@flow
            }
            
            val isPublicIdSearch = searchQuery.startsWith("@")
            val actualQuery = if (isPublicIdSearch) searchQuery.substring(1) else searchQuery
            
            // ALWAYS check local DB by publicId first (works with or without @)
            val localChannel = channelDao.getChannelByPublicId(actualQuery)
            if (localChannel != null && isPublicIdSearch) {
                // If searching with @, return only exact publicId match
                emit(ChannelResult.Success(listOf(localChannel.toDomain())))
                return@flow
            }
            
            // Search backend API
            val response = apiService.searchChannels(actualQuery)
            if (response.isSuccessful) {
                val channels = response.body()?.channels ?: emptyList()
                
                // Combine local publicId match with API results (avoid duplicates)
                val results = if (localChannel != null) {
                    val apiChannels = channels.filter { it.id != localChannel.id }
                    listOf(localChannel.toDomain()) + apiChannels.map { it.toDomain() }
                } else if (isPublicIdSearch) {
                    // Prioritize exact publicId matches from API
                    val exactMatches = channels.filter { 
                        it.publicId?.equals(actualQuery, ignoreCase = true) == true 
                    }
                    val otherMatches = channels.filter { 
                        it.publicId?.equals(actualQuery, ignoreCase = true) != true 
                    }
                    (exactMatches + otherMatches).map { it.toDomain() }
                } else {
                    channels.map { it.toDomain() }
                }
                
                emit(ChannelResult.Success(results))
            } else {
                // Even if API fails, return local result if found
                if (localChannel != null) {
                    emit(ChannelResult.Success(listOf(localChannel.toDomain())))
                } else {
                    emit(ChannelResult.Error("خطا در جستجو"))
                }
            }
        } catch (e: Exception) {
            // Try to return local result on error
            val searchQuery = query.trim()
            val actualQuery = if (searchQuery.startsWith("@")) searchQuery.substring(1) else searchQuery
            val localChannel = channelDao.getChannelByPublicId(actualQuery)
            if (localChannel != null) {
                emit(ChannelResult.Success(listOf(localChannel.toDomain())))
            } else {
                emit(ChannelResult.Error("خطا در اتصال به سرور: ${e.message}"))
            }
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    // ➕ Create/Update/Delete Channel
    // ═══════════════════════════════════════════════════════════════════════════
    
    suspend fun createChannel(
        name: String,
        description: String?,
        isPublic: Boolean,
        publicId: String? = null,
        memberIds: List<String> = emptyList(),
        avatarFile: java.io.File? = null
    ): Flow<ChannelResult<Channel>> = flow {
        emit(ChannelResult.Loading)
        try {
            val response = apiService.createChannel(CreateChannelRequest(name, description, isPublic, publicId, memberIds))
            if (response.isSuccessful && response.body()?.success == true) {
                var channelDto = response.body()?.data
                
                if (channelDto != null) {
                    // If avatar provided, upload it and update channel
                    if (avatarFile != null) {
                        try {
                            val requestBody = okhttp3.RequestBody.create("image/*".toMediaTypeOrNull(), avatarFile)
                            val part = okhttp3.MultipartBody.Part.createFormData("file", avatarFile.name, requestBody)
                            val uploadResponse = apiService.uploadFile(part)
                            
                            if (uploadResponse.isSuccessful && uploadResponse.body()?.success == true) {
                                val avatarUrl = uploadResponse.body()?.data
                                if (avatarUrl != null) {
                                    // Update channel with avatar
                                    val updateResponse = apiService.updateChannel(
                                        channelDto.id, 
                                        UpdateChannelRequest(name, description, isPublic, publicId, avatarUrl)
                                    )
                                    if (updateResponse.isSuccessful && updateResponse.body()?.success == true) {
                                        channelDto = updateResponse.body()?.data ?: channelDto
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            // Ignore avatar upload failure, channel is created
                            e.printStackTrace()
                        }
                    }
                    
                    // Save channel with isAdmin=true for the creator
                    // This ensures the input bar is visible since we are the channel owner
                    val entity = channelDto!!.toEntity().copy(
                        isAdmin = true,
                        isSubscribed = true
                    )
                    channelDao.insertChannel(entity)
                    emit(ChannelResult.Success(channelDto!!.toDomain().copy(isAdmin = true, isSubscribed = true)))
                } else {
                    emit(ChannelResult.Error("خطا در ایجاد کانال"))
                }
            } else {
                emit(ChannelResult.Error(response.body()?.message ?: "خطا در ایجاد کانال"))
            }
        } catch (e: Exception) {
            emit(ChannelResult.Error("خطا در اتصال به سرور: ${e.message}"))
        }
    }
    
    suspend fun updateChannel(
        channelId: String,
        name: String?,
        description: String?,
        isPublic: Boolean?,
        publicId: String? = null,
        avatarFile: java.io.File? = null
    ): Flow<ChannelResult<Channel>> = flow {
        emit(ChannelResult.Loading)
        try {
            var avatarUrl: String? = null
            
            if (avatarFile != null) {
                val requestBody = okhttp3.RequestBody.create("image/*".toMediaTypeOrNull(), avatarFile)
                val part = okhttp3.MultipartBody.Part.createFormData("file", avatarFile.name, requestBody)
                val uploadResponse = apiService.uploadFile(part)
                if (uploadResponse.isSuccessful && uploadResponse.body()?.success == true) {
                    avatarUrl = uploadResponse.body()?.data
                } else {
                    emit(ChannelResult.Error("خطا در آپلود تصویر کانال: ${uploadResponse.body()?.message}"))
                    return@flow
                }
            }

            val response = apiService.updateChannel(channelId, UpdateChannelRequest(name, description, isPublic, publicId, avatarUrl))
            if (response.isSuccessful && response.body()?.success == true) {
                val channelDto = response.body()?.data
                if (channelDto != null) {
                    channelDao.insertChannel(channelDto.toEntity())
                    emit(ChannelResult.Success(channelDto.toDomain()))
                }
            } else {
                emit(ChannelResult.Error(response.body()?.message ?: "خطا در بروزرسانی کانال"))
            }
        } catch (e: Exception) {
            emit(ChannelResult.Error("خطا در اتصال به سرور: ${e.message}"))
        }
    }
    
    suspend fun deleteChannel(channelId: String): Flow<ChannelResult<Unit>> = flow {
        emit(ChannelResult.Loading)
        try {
            val response = apiService.deleteChannel(channelId)
            if (response.isSuccessful && response.body()?.success == true) {
                channelDao.deleteChannelById(channelId)
                emit(ChannelResult.Success(Unit))
            } else {
                emit(ChannelResult.Error(response.body()?.message ?: "خطا در حذف کانال"))
            }
        } catch (e: Exception) {
            emit(ChannelResult.Error("خطا در اتصال به سرور: ${e.message}"))
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    // 🔔 Subscribe/Unsubscribe
    // ═══════════════════════════════════════════════════════════════════════════
    
    suspend fun subscribe(channelId: String): Flow<ChannelResult<Unit>> = flow {
        emit(ChannelResult.Loading)
        try {
            // First, fetch channel details if not already in DB
            val existingChannel = channelDao.getChannelById(channelId)
            if (existingChannel == null) {
                // Fetch from server first to get channel data
                try {
                    val channelResponse = apiService.getChannelById(channelId)
                    if (channelResponse.isSuccessful && channelResponse.body()?.success == true) {
                        val channelDto = channelResponse.body()?.data
                        if (channelDto != null) {
                            // Insert immediately with isSubscribed=true
                            channelDao.insertChannel(channelDto.toEntity().copy(isSubscribed = true))
                        }
                    }
                } catch (e: Exception) {
                    // Continue with subscription even if fetch fails
                }
            } else {
                // Update existing channel to subscribed immediately
                channelDao.updateSubscriptionStatus(channelId, true)
            }
            
            // Now make the subscription API call
            val response = apiService.subscribeToChannel(channelId)
            if (response.isSuccessful && response.body()?.success == true) {
                // Ensure subscription status is set (redundant but safe)
                channelDao.updateSubscriptionStatus(channelId, true)
                emit(ChannelResult.Success(Unit))
            } else {
                // Rollback subscription on failure
                if (existingChannel != null) {
                    channelDao.updateSubscriptionStatus(channelId, false)
                } else {
                    channelDao.deleteChannelById(channelId)
                }
                emit(ChannelResult.Error(response.body()?.message ?: "خطا در عضویت"))
            }
        } catch (e: Exception) {
            // Rollback on exception
            val existingChannel = channelDao.getChannelById(channelId)
            if (existingChannel != null) {
                channelDao.updateSubscriptionStatus(channelId, false)
            } else {
                channelDao.deleteChannelById(channelId)
            }
            emit(ChannelResult.Error("خطا در اتصال به سرور: ${e.message}"))
        }
    }
    
    suspend fun unsubscribe(channelId: String): Flow<ChannelResult<Unit>> = flow {
        emit(ChannelResult.Loading)
        
        // Store original channel state for rollback
        val originalChannel = channelDao.getChannelById(channelId)
        
        try {
            // Immediately remove from UI by deleting from DB
            channelDao.deleteChannelById(channelId)
            
            // Make API call
            val response = apiService.unsubscribeFromChannel(channelId)
            if (response.isSuccessful && response.body()?.success == true) {
                // Already deleted, just emit success
                emit(ChannelResult.Success(Unit))
            } else {
                // Rollback: restore original channel
                if (originalChannel != null) {
                    channelDao.insertChannel(originalChannel)
                }
                emit(ChannelResult.Error(response.body()?.message ?: "خطا در لغو عضویت"))
            }
        } catch (e: Exception) {
            // Rollback on exception
            if (originalChannel != null) {
                channelDao.insertChannel(originalChannel)
            }
            emit(ChannelResult.Error("خطا در اتصال به سرور: ${e.message}"))
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    // 📝 Posts Operations
    // ═══════════════════════════════════════════════════════════════════════════
    
    fun observePosts(channelId: String): Flow<List<ChannelPost>> = 
        channelPostDao.observePostsForChannel(channelId).map { posts ->
            posts.map { it.toDomain() }
        }.distinctUntilChanged()
    
    suspend fun getPosts(channelId: String, page: Int = 0): Flow<ChannelResult<List<ChannelPost>>> = flow {
        emit(ChannelResult.Loading)
        
        val cachedPosts = channelPostDao.getPostsForChannel(channelId, 50, page * 50)
        val cachedPostsMap = cachedPosts.associateBy { it.id }
        
        if (cachedPosts.isNotEmpty()) {
            emit(ChannelResult.Success(cachedPosts.map { it.toDomain() }))
        }
        
        try {
            val response = apiService.getChannelPosts(channelId, page)
            if (response.isSuccessful) {
                var postDtos = response.body()?.posts ?: emptyList()
                
                // CRITICAL FIX: Preserve poll data from cache when network returns incomplete data
                postDtos = postDtos.map { postDto ->
                    // Check if this is a POLL type post but network returned null/empty poll
                    val isPollType = postDto.type == "POLL"
                    val hasMissingPoll = postDto.poll == null || postDto.poll.options.isEmpty()
                    
                    if (isPollType && hasMissingPoll) {
                        android.util.Log.d("ChannelRepo", "Post ${postDto.id} is POLL type but has missing poll data")
                        
                        // First, try to get poll from local cache
                        val cachedPost = cachedPostsMap[postDto.id]
                        if (cachedPost != null && cachedPost.poll != null) {
                            android.util.Log.d("ChannelRepo", "Using poll data from local cache")
                            try {
                                val cachedPollDto = com.google.gson.Gson().fromJson(
                                    cachedPost.poll, 
                                    com.Kelasor.app.data.remote.dto.PollDto::class.java
                                )
                                if (cachedPollDto != null && cachedPollDto.options.isNotEmpty()) {
                                    android.util.Log.d("ChannelRepo", "Restored poll with ${cachedPollDto.options.size} options from cache")
                                    return@map postDto.copy(poll = cachedPollDto)
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("ChannelRepo", "Error parsing cached poll", e)
                            }
                        }
                        
                        // If cache doesn't have poll, try to fetch from API
                        val pollId = postDto.poll?.id
                        if (pollId != null) {
                            android.util.Log.d("ChannelRepo", "Fetching poll $pollId from API")
                            try {
                                val fullPollResult = pollRepository.getPoll(pollId)
                                fullPollResult.fold(
                                    onSuccess = { fullPoll ->
                                        if (fullPoll.options.isNotEmpty()) {
                                            android.util.Log.d("ChannelRepo", "Fetched full poll: ${fullPoll.options.size} options")
                                            return@map postDto.copy(poll = fullPoll)
                                        }
                                    },
                                    onFailure = { e ->
                                        android.util.Log.e("ChannelRepo", "Failed to fetch poll: ${e.message}")
                                    }
                                )
                            } catch (e: Exception) {
                                android.util.Log.e("ChannelRepo", "Error fetching poll", e)
                            }
                        }
                        
                        android.util.Log.w("ChannelRepo", "Could not recover poll data for post ${postDto.id}")
                    }
                    postDto
                }
                
                channelPostDao.insertPosts(postDtos.map { it.toEntity() })
                emit(ChannelResult.Success(postDtos.map { it.toDomain() }))
            }
        } catch (e: Exception) {
            if (cachedPosts.isEmpty()) {
                emit(ChannelResult.Error("خطا در اتصال به سرور: ${e.message}"))
            }
        }
    }
    
    suspend fun createPost(
        channelId: String,
        content: String,
        type: String = "TEXT",
        mediaUrl: String? = null,
        commentsEnabled: Boolean = true,
        amplitudes: List<Int>? = null
    ): Flow<ChannelResult<ChannelPost>> = flow {
        emit(ChannelResult.Loading)
        try {
            val response = apiService.createChannelPost(
                channelId,
                CreatePostRequest(content, mediaUrl, commentsEnabled, type, null, amplitudes)
            )
            if (response.isSuccessful && response.body()?.success == true) {
                val postDto = response.body()?.data
                if (postDto != null) {
                    // CRITICAL FIX: ALWAYS patch type, mediaUrl, and amplitudes from caller's values
                    // Backend may return incorrect type (e.g., TEXT instead of AUDIO/VIDEO)
                    val finalDto = postDto.copy(
                        type = type,
                        mediaUrl = mediaUrl ?: postDto.mediaUrl,
                        amplitudes = amplitudes ?: postDto.amplitudes
                    )
                    channelPostDao.insertPost(finalDto.toEntity())
                    emit(ChannelResult.Success(finalDto.toDomain()))
                } else {
                    emit(ChannelResult.Error("خطا در ایجاد پست"))
                }
            } else {
                emit(ChannelResult.Error(response.body()?.message ?: "خطا در ایجاد پست"))
            }
        } catch (e: Exception) {
            emit(ChannelResult.Error("خطا در اتصال به سرور: ${e.message}"))
        }
    }
    
    suspend fun sendPollMessage(channelId: String, poll: PollDto): Flow<ChannelResult<ChannelPost>> = flow {
        emit(ChannelResult.Loading)
        try {
            android.util.Log.d("ChannelRepo", "sendPollMessage: poll.id=${poll.id}, poll.options.size=${poll.options.size}")
            poll.options.forEachIndexed { i, opt ->
                android.util.Log.d("ChannelRepo", "  Option $i: id=${opt.id}, text=${opt.text}")
            }
            
            val response = apiService.createChannelPost(
                channelId,
                CreatePostRequest(
                    type = "POLL",
                    content = poll.question,
                    pollId = poll.id,
                    commentsEnabled = true
                )
            )
            if (response.isSuccessful && response.body()?.success == true) {
                val postDto = response.body()?.data
                if (postDto != null) {
                    android.util.Log.d("ChannelRepo", "Backend response: postDto.poll=${postDto.poll != null}")
                    if (postDto.poll != null) {
                        android.util.Log.d("ChannelRepo", "  Backend poll.options.size=${postDto.poll.options.size}")
                    }
                    
                    // CRITICAL FIX: ALWAYS use the input poll object to preserve all options
                    // Backend may return incomplete poll data (missing options)
                    android.util.Log.d("ChannelRepo", "Force using input poll object")
                    val finalDto = postDto.copy(poll = poll, type = "POLL")
                    
                    android.util.Log.d("ChannelRepo", "Final poll.options.size=${finalDto.poll?.options?.size}")
                    channelPostDao.insertPost(finalDto.toEntity())
                    emit(ChannelResult.Success(finalDto.toDomain()))
                } else {
                    emit(ChannelResult.Error("خطا در ارسال نظرسنجی"))
                }
            } else {
                emit(ChannelResult.Error(response.body()?.message ?: "خطا در ارسال نظرسنجی"))
            }
        } catch (e: Exception) {
            emit(ChannelResult.Error("خطا در اتصال به سرور: ${e.message}"))
        }
    }

    suspend fun editPost(
        channelId: String,
        postId: String,
        content: String,
        mediaUrl: String? = null,
        commentsEnabled: Boolean = true
    ): Flow<ChannelResult<ChannelPost>> = flow {
        emit(ChannelResult.Loading)
        try {
            val response = apiService.editChannelPost(
                channelId,
                postId,
                CreatePostRequest(content, mediaUrl, commentsEnabled)
            )
            if (response.isSuccessful && response.body()?.success == true) {
                val postDto = response.body()?.data
                if (postDto != null) {
                    channelPostDao.insertPost(postDto.toEntity())
                    emit(ChannelResult.Success(postDto.toDomain()))
                } else {
                    emit(ChannelResult.Error("خطا در ویرایش پست"))
                }
            } else {
                emit(ChannelResult.Error(response.body()?.message ?: "خطا در ویرایش پست"))
            }
        } catch (e: Exception) {
            emit(ChannelResult.Error("خطا در اتصال به سرور: ${e.message}"))
        }
    }

    suspend fun deletePost(
        channelId: String,
        postId: String,
        deleteForEveryone: Boolean = true
    ): Flow<ChannelResult<Unit>> = flow {
        emit(ChannelResult.Loading)
        try {
            val response = apiService.deleteChannelPost(channelId, postId, deleteForEveryone)
            if (response.isSuccessful && response.body()?.success == true) {
                channelPostDao.deletePostById(postId)
                emit(ChannelResult.Success(Unit))
            } else {
                emit(ChannelResult.Error(response.body()?.message ?: "خطا در حذف پست"))
            }
        } catch (e: Exception) {
            emit(ChannelResult.Error("خطا در اتصال به سرور: ${e.message}"))
        }
    }

    suspend fun reactToPost(
        channelId: String,
        postId: String,
        reaction: String
    ): Flow<ChannelResult<Unit>> = flow {
        emit(ChannelResult.Loading)
        try {
            val response = apiService.reactToChannelPost(channelId, postId, ReactionRequest(reaction))
            if (response.isSuccessful && response.body()?.success == true) {
                emit(ChannelResult.Success(Unit))
            } else {
                emit(ChannelResult.Error(response.body()?.message ?: "خطا در ثبت واکنش"))
            }
        } catch (e: Exception) {
            emit(ChannelResult.Error("خطا در اتصال به سرور: ${e.message}"))
        }
    }

    suspend fun sendComment(
        channelId: String,
        postId: String,
        content: String
    ): Flow<ChannelResult<ChannelPostCommentDto>> = flow {
        emit(ChannelResult.Loading)
        try {
            val response = apiService.addComment(channelId, postId, CreateCommentRequest(content))
            if (response.isSuccessful && response.body()?.success == true) {
                emit(ChannelResult.Success(response.body()!!.data!!))
            } else {
                emit(ChannelResult.Error(response.body()?.message ?: "خطا در ثبت نظر"))
            }
        } catch (e: Exception) {
            emit(ChannelResult.Error("خطا در اتصال به سرور: ${e.message}"))
        }
    }

    suspend fun getComments(
        channelId: String,
        postId: String,
        page: Int = 0
    ): Flow<ChannelResult<CommentListResponse>> = flow {
        emit(ChannelResult.Loading)
        try {
            val response = apiService.getComments(channelId, postId, page)
            if (response.isSuccessful && response.body() != null) {
                emit(ChannelResult.Success(response.body()!!))
            } else {
                emit(ChannelResult.Error("خطا در دریافت نظرات"))
            }
        } catch (e: Exception) {
            emit(ChannelResult.Error("خطا در اتصال به سرور: ${e.message}"))
        }
    }

    
    // ═══════════════════════════════════════════════════════════════════════════
    // 👥 Subscribers & Admin Management
    // ═══════════════════════════════════════════════════════════════════════════
    
    fun getSubscribers(channelId: String): Flow<ChannelResult<List<ChannelSubscriber>>> = kotlinx.coroutines.flow.channelFlow {
        send(ChannelResult.Loading)
        
        // Launch DB observation in parallel
        launch {
            channelSubscriberDao.observeSubscribersWithUsers(channelId).collect { relationList ->
                val domainList = relationList.map { 
                    it.subscriber.toDomain(it.user.toDomain()) 
                }
                send(ChannelResult.Success(domainList))
            }
        }
        
        // Fetch from network and update DB
        launch {
            try {
                val response = apiService.getChannelSubscribers(channelId)
                if (response.isSuccessful && response.body()?.success == true) {
                    val dtos = response.body()?.data ?: emptyList()
                    
                    // Save Users and Subscribers
                    val userEntities = dtos.map { it.user.toEntity() }
                    userDao.insertUsers(userEntities) 
                    
                    val subscriberEntities = dtos.map { it.toEntity(channelId) }
                    channelSubscriberDao.insertSubscribers(subscriberEntities)
                } 
                // We don't need to emit Error if local data exists. 
                // If API fails, we just don't update DB.
            } catch (e: Exception) {
                // Silent fail on network error, keep showing local
            }
        }
    }
    
    suspend fun addMembers(channelId: String, memberIds: List<String>): Flow<ChannelResult<Unit>> = flow {
        emit(ChannelResult.Loading)
        try {
            val response = apiService.addChannelMembers(channelId, AddChannelMembersRequest(memberIds))
            if (response.isSuccessful && response.body()?.success == true) {
                emit(ChannelResult.Success(Unit))
            } else {
                emit(ChannelResult.Error(response.body()?.message ?: "خطا در افزودن اعضا"))
            }
        } catch (e: Exception) {
            emit(ChannelResult.Error("خطا در اتصال به سرور: ${e.message}"))
        }
    }
    
    suspend fun addAdmin(channelId: String, userId: String): Flow<ChannelResult<Unit>> = flow {
        emit(ChannelResult.Loading)
        try {
            val response = apiService.addChannelAdmin(channelId, ChannelAdminRequest(userId))
            if (response.isSuccessful && response.body()?.success == true) {
                emit(ChannelResult.Success(Unit))
            } else {
                emit(ChannelResult.Error(response.body()?.message ?: "خطا در افزودن ادمین"))
            }
        } catch (e: Exception) {
            emit(ChannelResult.Error("خطا در اتصال به سرور: ${e.message}"))
        }
    }
    
    suspend fun removeAdmin(channelId: String, userId: String): Flow<ChannelResult<Unit>> = flow {
        emit(ChannelResult.Loading)
        try {
            val response = apiService.removeChannelAdmin(channelId, userId)
            if (response.isSuccessful && response.body()?.success == true) {
                emit(ChannelResult.Success(Unit))
            } else {
                emit(ChannelResult.Error(response.body()?.message ?: "خطا در حذف ادمین"))
            }
        } catch (e: Exception) {
            emit(ChannelResult.Error("خطا در اتصال به سرور: ${e.message}"))
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    // 🔗 Invite Link Management
    // ═══════════════════════════════════════════════════════════════════════════
    
    suspend fun toggleInviteLink(channelId: String, enabled: Boolean): Flow<ChannelResult<InviteLinkResponse>> = flow {
        emit(ChannelResult.Loading)
        try {
            val response = apiService.toggleChannelInviteLink(channelId, enabled)
            if (response.isSuccessful && response.body()?.success == true) {
                val result = response.body()?.data
                if (result != null) {
                    emit(ChannelResult.Success(result))
                }
            } else {
                emit(ChannelResult.Error(response.body()?.message ?: "خطا در تغییر لینک دعوت"))
            }
        } catch (e: Exception) {
            emit(ChannelResult.Error("خطا در اتصال به سرور: ${e.message}"))
        }
    }
    
    suspend fun regenerateInviteLink(channelId: String): Flow<ChannelResult<InviteLinkResponse>> = flow {
        emit(ChannelResult.Loading)
        try {
            val response = apiService.regenerateChannelInviteLink(channelId)
            if (response.isSuccessful && response.body()?.success == true) {
                val result = response.body()?.data
                if (result != null) {
                    emit(ChannelResult.Success(result))
                }
            } else {
                emit(ChannelResult.Error(response.body()?.message ?: "خطا در ایجاد لینک جدید"))
            }
        } catch (e: Exception) {
            emit(ChannelResult.Error("خطا در اتصال به سرور: ${e.message}"))
        }
    }
    
    suspend fun joinByInviteLink(inviteCode: String): Flow<ChannelResult<Channel>> = flow {
        emit(ChannelResult.Loading)
        try {
            val response = apiService.joinChannelByInviteLink(inviteCode)
            if (response.isSuccessful && response.body()?.success == true) {
                val channelDto = response.body()?.data
                if (channelDto != null) {
                    channelDao.insertChannel(channelDto.toEntity())
                    emit(ChannelResult.Success(channelDto.toDomain()))
                }
            } else {
                emit(ChannelResult.Error(response.body()?.message ?: "لینک نامعتبر یا غیرفعال است"))
            }
        } catch (e: Exception) {
            emit(ChannelResult.Error("خطا در اتصال به سرور: ${e.message}"))
        }
    }
    
    suspend fun archiveChannel(channelId: String): Flow<ChannelResult<Unit>> = flow {
        emit(ChannelResult.Loading)
        try {
            apiService.archiveChannel(channelId, true)
            channelDao.updateArchiveStatus(channelId, true)
            emit(ChannelResult.Success(Unit))
        } catch (e: Exception) {
            emit(ChannelResult.Error("خطا در بایگانی کانال: ${e.message}"))
        }
    }
    
    suspend fun unarchiveChannel(channelId: String): Flow<ChannelResult<Unit>> = flow {
        emit(ChannelResult.Loading)
        try {
            apiService.archiveChannel(channelId, false)
            channelDao.updateArchiveStatus(channelId, false)
            emit(ChannelResult.Success(Unit))
        } catch (e: Exception) {
            emit(ChannelResult.Error("خطا در خروج از بایگانی: ${e.message}"))
        }
    }
    
    suspend fun togglePin(channelId: String, isPinned: Boolean): Flow<ChannelResult<Unit>> = flow {
        emit(ChannelResult.Loading)
        try {
            apiService.pinChannel(channelId, isPinned)
            channelDao.updatePinStatus(channelId, isPinned)
            emit(ChannelResult.Success(Unit))
        } catch (e: Exception) {
            emit(ChannelResult.Error("خطا در سنجاق کردن: ${e.message}"))
        }
    }

    suspend fun toggleMute(channelId: String, isMuted: Boolean): Flow<ChannelResult<Unit>> = flow {
        emit(ChannelResult.Loading)
        try {
            apiService.muteChannel(channelId, isMuted)
            channelDao.updateMuteStatus(channelId, isMuted)
            emit(ChannelResult.Success(Unit))
        } catch (e: Exception) {
            emit(ChannelResult.Error("خطا در تغییر وضعیت بیصدا: ${e.message}"))
        }
    }
    
    fun observeArchivedChannels(): Flow<List<Channel>> = channelDao.observeArchivedChannels().map { channels ->
        channels.map { it.toDomain() }
    }.distinctUntilChanged()
}
