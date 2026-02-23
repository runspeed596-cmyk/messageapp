package com.Kelasor.app.data.remote.api

import com.Kelasor.app.data.remote.dto.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit API service interface for all MessageApp endpoints.
 * Base URL: http://10.0.2.2:8080 (for emulator) or actual server IP
 */
interface ApiService {
    // ═══════════════════════════════════════════════════════════════════════════════
    // 🏠 Home Endpoints
    // ═══════════════════════════════════════════════════════════════════════════════
    @GET("api/home")
    suspend fun getHomeData(): Response<ApiResponse<HomeDataResponse>>

    // ═══════════════════════════════════════════════════════════════════════════════
    // 🔐 Authentication Endpoints
    // ═══════════════════════════════════════════════════════════════════════════════
    @POST("api/auth/send-otp")
    suspend fun sendOtp(@Body request: SendOtpRequest): Response<SendOtpResponse>
    @POST("api/auth/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): Response<AuthResponse>
    @POST("api/auth/refresh-token")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<AuthResponse>
    @POST("api/auth/logout")
    suspend fun logout(): Response<ApiResponse<Unit>>
    // ═══════════════════════════════════════════════════════════════════════════════
    // 👤 User Endpoints
    // ═══════════════════════════════════════════════════════════════════════════════
    @GET("api/users/me")
    suspend fun getCurrentUser(): Response<ApiResponse<UserDto>>
    @PUT("api/users/me")
    suspend fun updateUser(@Body request: UpdateUserRequest): Response<ApiResponse<UserDto>>
    @Multipart
    @POST("api/users/avatar")
    suspend fun uploadAvatar(@Part file: MultipartBody.Part): Response<ApiResponse<UserDto>>
    @GET("api/users/{id}")
    suspend fun getUserById(@Path("id") id: String): Response<ApiResponse<UserDto>>
    @GET("api/users/search")
    suspend fun searchUsers(
        @Query("query") query: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<UserSearchResult>
    @POST("api/users/contacts")
    suspend fun matchContacts(@Body phoneNumbers: List<String>): Response<UserSearchResult>
    
    @PUT("api/users/me/privacy")
    suspend fun updatePrivacy(@Body request: UpdatePrivacyRequest): Response<ApiResponse<UserDto>>
    
    @GET("api/users/me/privacy")
    suspend fun getPrivacy(): Response<ApiResponse<UserDto>>
    
    @GET("api/users/count")
    suspend fun getUserCount(): Response<ApiResponse<Long>>
    // ═══════════════════════════════════════════════════════════════════════════════
    // 💬 Chat Endpoints
    // ═══════════════════════════════════════════════════════════════════════════════
    @GET("api/chats")
    suspend fun getChats(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50
    ): Response<ChatListResponse>
    @POST("api/chats")
    suspend fun createChat(@Body request: CreateChatRequest): Response<ApiResponse<ChatDto>>
    @GET("api/chats/{id}")
    suspend fun getChatById(@Path("id") id: String): Response<ApiResponse<ChatDto>>
    @PUT("api/chats/{id}/pin")
    suspend fun pinChat(
        @Path("id") id: String,
        @Query("pinned") pinned: Boolean
    ): Response<ApiResponse<ChatDto>>
    @PUT("api/chats/{id}/mute")
    suspend fun muteChat(
        @Path("id") id: String,
        @Query("muted") muted: Boolean
    ): Response<ApiResponse<ChatDto>>
    @PUT("api/chats/{id}/archive")
    suspend fun archiveChat(
        @Path("id") id: String,
        @Query("archived") archived: Boolean
    ): Response<ApiResponse<ChatDto>>
    // ═══════════════════════════════════════════════════════════════════════════════
    // 📨 Message Endpoints
    // ═══════════════════════════════════════════════════════════════════════════════
    @GET("api/chats/{chatId}/messages")
    suspend fun getMessages(
        @Path("chatId") chatId: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50
    ): Response<MessageListResponse>
    @POST("api/chats/{chatId}/messages")
    suspend fun sendMessage(
        @Path("chatId") chatId: String,
        @Body request: SendMessageRequest
    ): Response<ApiResponse<MessageDto>>
    @PUT("api/messages/{id}")
    suspend fun editMessage(
        @Path("id") id: String,
        @Body request: EditMessageRequest
    ): Response<ApiResponse<MessageDto>>
    @DELETE("api/messages/{id}")
    suspend fun deleteMessage(
        @Path("id") id: String,
        @Query("deleteForEveryone") deleteForEveryone: Boolean = true
    ): Response<ApiResponse<Unit>>
    @POST("api/messages/{id}/read")
    suspend fun markAsRead(@Path("id") id: String): Response<ApiResponse<Unit>>
    @POST("api/messages/{id}/reactions")
    suspend fun reactToMessage(
        @Path("id") id: String,
        @Body request: ReactionRequest
    ): Response<ApiResponse<Unit>>

    @GET("api/chats/shared-content")
    suspend fun getSharedContent(
        @Query("targetId") targetId: String,
        @Query("scope") scope: String, // USER, GROUP, CHANNEL
        @Query("type") type: String,   // IMAGE, VIDEO, FILE, AUDIO, LINK, etc.
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50
    ): Response<ApiResponse<List<MessageDto>>> // Reusing MessageDto as it contains media info
    // ═══════════════════════════════════════════════════════════════════════════════
    // 👥 Group Endpoints
    // ═══════════════════════════════════════════════════════════════════════════════
    @GET("api/groups")
    suspend fun getGroups(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50
    ): Response<GroupListResponse>
    @POST("api/groups")
    suspend fun createGroup(@Body request: CreateGroupRequest): Response<ApiResponse<GroupDto>>
    @GET("api/groups/{id}")
    suspend fun getGroupById(@Path("id") id: String): Response<ApiResponse<GroupDto>>
    @PUT("api/groups/{id}")
    suspend fun updateGroup(
        @Path("id") id: String,
        @Body request: UpdateGroupRequest
    ): Response<ApiResponse<GroupDto>>
    @DELETE("api/groups/{id}")
    suspend fun deleteGroup(@Path("id") id: String): Response<ApiResponse<Unit>>
    @GET("api/groups/{id}/members")
    suspend fun getGroupMembers(
        @Path("id") id: String
    ): Response<ApiResponse<List<GroupMemberDto>>>
    @POST("api/groups/{id}/members")
    suspend fun addGroupMembers(
        @Path("id") id: String,
        @Body request: AddGroupMembersRequest
    ): Response<ApiResponse<Unit>>
    @DELETE("api/groups/{id}/members/{memberId}")
    suspend fun removeGroupMember(
        @Path("id") id: String,
        @Path("memberId") memberId: String
    ): Response<ApiResponse<Unit>>
    @PUT("api/groups/{id}/members/{memberId}/role")
    suspend fun changeGroupMemberRole(
        @Path("id") id: String,
        @Path("memberId") memberId: String,
        @Body request: ChangeRoleRequest
    ): Response<ApiResponse<Unit>>
    @GET("api/groups/{id}/messages")
    suspend fun getGroupMessages(
        @Path("id") id: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50
    ): Response<GroupMessageListResponse>
    @POST("api/groups/{id}/messages")
    suspend fun sendGroupMessage(
        @Path("id") id: String,
        @Body request: SendGroupMessageRequest
    ): Response<ApiResponse<GroupMessageDto>>
    @POST("api/groups/{id}/messages/{messageId}/reactions")
    suspend fun reactToGroupMessage(
        @Path("id") id: String, // groupId
        @Path("messageId") messageId: String,
        @Body request: ReactionRequest
    ): Response<ApiResponse<Unit>>

    @PUT("api/groups/{id}/messages/{messageId}")
    suspend fun editGroupMessage(
        @Path("id") id: String,
        @Path("messageId") messageId: String,
        @Body request: EditMessageRequest
    ): Response<ApiResponse<GroupMessageDto>>

    @DELETE("api/groups/{id}/messages/{messageId}")
    suspend fun deleteGroupMessage(
        @Path("id") id: String,
        @Path("messageId") messageId: String,
        @Query("deleteForEveryone") deleteForEveryone: Boolean = true
    ): Response<ApiResponse<Unit>>
    @PUT("api/groups/{id}/settings")
    suspend fun updateGroupSettings(
        @Path("id") id: String,
        @Body request: UpdateGroupSettingsRequest
    ): Response<ApiResponse<GroupDto>>
    @POST("api/groups/{id}/invite-link/toggle")
    suspend fun toggleGroupInviteLink(
        @Path("id") id: String,
        @Query("enabled") enabled: Boolean
    ): Response<ApiResponse<InviteLinkResponse>>
    @POST("api/groups/{id}/invite-link/regenerate")
    suspend fun regenerateGroupInviteLink(
        @Path("id") id: String
    ): Response<ApiResponse<InviteLinkResponse>>
    @POST("api/groups/join/{inviteCode}")
    suspend fun joinGroupByInviteLink(
        @Path("inviteCode") inviteCode: String
    ): Response<ApiResponse<GroupDto>>
    
    @PUT("api/groups/{id}/mute")
    suspend fun muteGroup(
        @Path("id") id: String,
        @Query("muted") muted: Boolean
    ): Response<ApiResponse<GroupDto>>
    @PUT("api/groups/{id}/pin")
    suspend fun pinGroup(
        @Path("id") id: String,
        @Query("pinned") pinned: Boolean
    ): Response<ApiResponse<GroupDto>>
    @PUT("api/groups/{id}/archive")
    suspend fun archiveGroup(
        @Path("id") id: String,
        @Query("archived") archived: Boolean
    ): Response<ApiResponse<GroupDto>>
    // ═══════════════════════════════════════════════════════════════════════════════
    // 📢 Channel Endpoints
    // ═══════════════════════════════════════════════════════════════════════════════
    @GET("api/channels")
    suspend fun getChannels(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50
    ): Response<ChannelListResponse>
    @POST("api/channels")
    suspend fun createChannel(@Body request: CreateChannelRequest): Response<ApiResponse<ChannelDto>>
    @GET("api/channels/{id}")
    suspend fun getChannelById(@Path("id") id: String): Response<ApiResponse<ChannelDto>>
    @PUT("api/channels/{id}")
    suspend fun updateChannel(
        @Path("id") id: String,
        @Body request: UpdateChannelRequest
    ): Response<ApiResponse<ChannelDto>>
    @DELETE("api/channels/{id}")
    suspend fun deleteChannel(@Path("id") id: String): Response<ApiResponse<Unit>>
    @POST("api/channels/{id}/subscribe")
    suspend fun subscribeToChannel(@Path("id") id: String): Response<ApiResponse<Unit>>
    @DELETE("api/channels/{id}/subscribe")
    suspend fun unsubscribeFromChannel(@Path("id") id: String): Response<ApiResponse<Unit>>
    @GET("api/channels/{id}/posts")
    suspend fun getChannelPosts(
        @Path("id") id: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50
    ): Response<PostListResponse>
    @POST("api/channels/{id}/posts")
    suspend fun createChannelPost(
        @Path("id") id: String,
        @Body request: CreatePostRequest
    ): Response<ApiResponse<ChannelPostDto>>

    @PUT("api/channels/{id}/posts/{postId}")
    suspend fun editChannelPost(
        @Path("id") channelId: String,
        @Path("postId") postId: String,
        @Body request: CreatePostRequest // Reusing CreatePostRequest as it likely has content/mediaUrl
    ): Response<ApiResponse<ChannelPostDto>>

    @DELETE("api/channels/{id}/posts/{postId}")
    suspend fun deleteChannelPost(
        @Path("id") channelId: String,
        @Path("postId") postId: String,
        @Query("deleteForEveryone") deleteForEveryone: Boolean = true
    ): Response<ApiResponse<Unit>>

    @POST("api/channels/{id}/posts/{postId}/reactions")
    suspend fun reactToChannelPost(
        @Path("id") channelId: String,
        @Path("postId") postId: String,
        @Body request: ReactionRequest
    ): Response<ApiResponse<Unit>>
    
    @POST("api/channels/{id}/posts/{postId}/comments")
    suspend fun addComment(
        @Path("id") channelId: String,
        @Path("postId") postId: String,
        @Body request: CreateCommentRequest
    ): Response<ApiResponse<ChannelPostCommentDto>>

    @GET("api/channels/{id}/posts/{postId}/comments")
    suspend fun getComments(
        @Path("id") channelId: String,
        @Path("postId") postId: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50
    ): Response<CommentListResponse>
    @GET("api/channels/search")
    suspend fun searchChannels(
        @Query("query") query: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ChannelListResponse>
    @POST("api/channels/{id}/admins")
    suspend fun addChannelAdmin(
        @Path("id") id: String,
        @Body request: ChannelAdminRequest
    ): Response<ApiResponse<Unit>>
    @DELETE("api/channels/{id}/admins/{userId}")
    suspend fun removeChannelAdmin(
        @Path("id") id: String,
        @Path("userId") userId: String
    ): Response<ApiResponse<Unit>>
    @POST("api/channels/{id}/members")
    suspend fun addChannelMembers(
        @Path("id") id: String,
        @Body request: AddChannelMembersRequest
    ): Response<ApiResponse<Unit>>
    @GET("api/channels/{id}/subscribers")
    suspend fun getChannelSubscribers(
        @Path("id") id: String
    ): Response<ApiResponse<List<ChannelSubscriberDto>>>
    @POST("api/channels/{id}/invite-link/toggle")
    suspend fun toggleChannelInviteLink(
        @Path("id") id: String,
        @Query("enabled") enabled: Boolean
    ): Response<ApiResponse<InviteLinkResponse>>
    @POST("api/channels/{id}/invite-link/regenerate")
    suspend fun regenerateChannelInviteLink(
        @Path("id") id: String
    ): Response<ApiResponse<InviteLinkResponse>>
    @POST("api/channels/join/{inviteCode}")
    suspend fun joinChannelByInviteLink(
        @Path("inviteCode") inviteCode: String
    ): Response<ApiResponse<ChannelDto>>

    @PUT("api/channels/{id}/mute")
    suspend fun muteChannel(
        @Path("id") id: String,
        @Query("muted") muted: Boolean
    ): Response<ApiResponse<ChannelDto>>
    @PUT("api/channels/{id}/pin")
    suspend fun pinChannel(
        @Path("id") id: String,
        @Query("pinned") pinned: Boolean
    ): Response<ApiResponse<ChannelDto>>
    @PUT("api/channels/{id}/archive")
    suspend fun archiveChannel(
        @Path("id") id: String,
        @Query("archived") archived: Boolean
    ): Response<ApiResponse<ChannelDto>>
    // ═══════════════════════════════════════════════════════════════════════════════
    // 📁 File Upload Endpoints
    // ═══════════════════════════════════════════════════════════════════════════════
    @Multipart
    @POST("api/files/upload")
    suspend fun uploadFile(@Part file: MultipartBody.Part): Response<ApiResponse<String>>
    // ═══════════════════════════════════════════════════════════════════════════════
    // 📋 Profile Details Endpoints
    // ═══════════════════════════════════════════════════════════════════════════════
    @GET("api/users/{userId}/profile-details")
    suspend fun getProfileDetails(@Path("userId") userId: String): Response<ApiResponse<ProfileDetailsDto>>
    @PUT("api/users/me/profile-details")
    suspend fun updateProfileDetails(@Body request: UpdateProfileDetailsRequest): Response<ApiResponse<ProfileDetailsDto>>
    @GET("api/users/me/profile-details")
    suspend fun getMyProfileDetails(): Response<ApiResponse<ProfileDetailsDto>>
    // ═══════════════════════════════════════════════════════════════════════════════
    // 👥 Follow Endpoints
    // ═══════════════════════════════════════════════════════════════════════════════
    @POST("api/users/{userId}/follow")
    suspend fun followUser(@Path("userId") userId: String): Response<ApiResponse<Boolean>>
    @DELETE("api/users/{userId}/follow")
    suspend fun unfollowUser(@Path("userId") userId: String): Response<ApiResponse<Boolean>>
    @GET("api/users/{userId}/followers")
    suspend fun getFollowers(
        @Path("userId") userId: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<FollowListResponse>
    @GET("api/users/{userId}/following")
    suspend fun getFollowing(
        @Path("userId") userId: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<FollowListResponse>
    @GET("api/users/{userId}/follow-counts")
    suspend fun getFollowCounts(@Path("userId") userId: String): Response<FollowCountsDto>
    @GET("api/users/{userId}/is-following")
    suspend fun isFollowing(@Path("userId") userId: String): Response<ApiResponse<Boolean>>
    // ═══════════════════════════════════════════════════════════════════════════════
    // 🤝 Collaboration Endpoints
    // ═══════════════════════════════════════════════════════════════════════════════
    @POST("api/collaboration/request")
    suspend fun sendCollaborationRequest(@Body request: SendCollaborationRequest): Response<ApiResponse<CollaborationRequestDto>>
    @POST("api/collaboration/{requestId}/accept")
    suspend fun acceptCollaborationRequest(@Path("requestId") requestId: String): Response<ApiResponse<CollaborationRequestDto>>
    @POST("api/collaboration/{requestId}/reject")
    suspend fun rejectCollaborationRequest(@Path("requestId") requestId: String): Response<ApiResponse<CollaborationRequestDto>>
    @GET("api/collaboration/received")
    suspend fun getReceivedCollaborationRequests(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<CollaborationListResponse>
    @GET("api/collaboration/sent")
    suspend fun getSentCollaborationRequests(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<CollaborationListResponse>
    @GET("api/collaboration/pending-count")
    suspend fun getCollaborationPendingCount(): Response<ApiResponse<Int>>
    // ═══════════════════════════════════════════════════════════════════════════════
    // 🔔 Notification Endpoints
    // ═══════════════════════════════════════════════════════════════════════════════
    @GET("api/notifications")
    suspend fun getNotifications(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<NotificationListResponse>
    @POST("api/notifications/{notificationId}/read")
    suspend fun markNotificationAsRead(@Path("notificationId") notificationId: String): Response<ApiResponse<Boolean>>
    @POST("api/notifications/read-all")
    suspend fun markAllNotificationsAsRead(): Response<ApiResponse<Int>>
    @GET("api/notifications/unread-count")
    suspend fun getUnreadNotificationCount(): Response<UnreadCountResponse>

    // ═══════════════════════════════════════════════════════════════════════════════
    // 📌 Message Pin Endpoints
    // ═══════════════════════════════════════════════════════════════════════════════
    @PUT("api/messages/{id}/pin")
    suspend fun pinMessage(
        @Path("id") id: String,
        @Query("pinned") pinned: Boolean
    ): Response<ApiResponse<MessageDto>>

    @GET("api/chats/{chatId}/messages/pinned")
    suspend fun getPinnedMessages(
        @Path("chatId") chatId: String
    ): Response<ApiResponse<List<MessageDto>>>

    @PUT("api/groups/{id}/messages/{messageId}/pin")
    suspend fun pinGroupMessage(
        @Path("id") groupId: String,
        @Path("messageId") messageId: String,
        @Query("pinned") pinned: Boolean
    ): Response<ApiResponse<GroupMessageDto>>

    @GET("api/groups/{id}/messages/pinned")
    suspend fun getPinnedGroupMessages(
        @Path("id") groupId: String
    ): Response<ApiResponse<List<GroupMessageDto>>>

    @PUT("api/channels/{id}/posts/{postId}/pin")
    suspend fun pinChannelPost(
        @Path("id") channelId: String,
        @Path("postId") postId: String,
        @Query("pinned") pinned: Boolean
    ): Response<ApiResponse<ChannelPostDto>>

    @GET("api/channels/{id}/posts/pinned")
    suspend fun getPinnedChannelPosts(
        @Path("id") channelId: String
    ): Response<ApiResponse<List<ChannelPostDto>>>

    // ═══════════════════════════════════════════════════════════════════════════════
    // 🔀 Message Forward Endpoints
    // ═══════════════════════════════════════════════════════════════════════════════
    @POST("api/messages/forward")
    suspend fun forwardMessages(
        @Body request: ForwardMessageRequest
    ): Response<ApiResponse<Unit>>

    // ═══════════════════════════════════════════════════════════════════════════════
    // ⏰ Message Schedule Endpoints
    // ═══════════════════════════════════════════════════════════════════════════════
    @POST("api/chats/{chatId}/messages/schedule")
    suspend fun scheduleMessage(
        @Path("chatId") chatId: String,
        @Body request: ScheduleMessageRequest
    ): Response<ApiResponse<MessageDto>>

    @POST("api/groups/{id}/messages/schedule")
    suspend fun scheduleGroupMessage(
        @Path("id") groupId: String,
        @Body request: ScheduleMessageRequest
    ): Response<ApiResponse<GroupMessageDto>>

    @POST("api/channels/{id}/posts/schedule")
    suspend fun scheduleChannelPost(
        @Path("id") channelId: String,
        @Body request: ScheduleMessageRequest
    ): Response<ApiResponse<ChannelPostDto>>

    // ═══════════════════════════════════════════════════════════════════════════════
    // 🆔 Username Management Endpoints
    // ═══════════════════════════════════════════════════════════════════════════════
    @PUT("api/users/me/username")
    suspend fun setUsername(
        @Body request: SetUsernameRequest
    ): Response<ApiResponse<UserDto>>

    @GET("api/users/username/check")
    suspend fun checkUsernameAvailability(
        @Query("username") username: String
    ): Response<UsernameAvailabilityResponse>

    // ═══════════════════════════════════════════════════════════════════════════════
    // ⭐ Special Folder Endpoints
    // ═══════════════════════════════════════════════════════════════════════════════
    @GET("api/special-folder")
    suspend fun getSpecialFolder(): Response<ApiResponse<SpecialFolderDto>>

    // ═══════════════════════════════════════════════════════════════════════════════
    // 📚 Reference Data Endpoints
    // ═══════════════════════════════════════════════════════════════════════════════
    @GET("api/reference-data")
    suspend fun getReferenceData(): Response<ApiResponse<ReferenceDataDto>>

    // ═══════════════════════════════════════════════════════════════════════════════
    // 🤖 AI Bot Chat Endpoints
    // ═══════════════════════════════════════════════════════════════════════════════
    @GET("api/special-folder/ai-bots/{botId}/messages")
    suspend fun getAiBotMessages(
        @Path("botId") botId: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50
    ): Response<ApiResponse<List<AiBotMessageDto>>>

    @POST("api/special-folder/ai-bots/{botId}/messages")
    suspend fun sendAiBotMessage(
        @Path("botId") botId: String,
        @Body request: SendAiBotMessageRequest
    ): Response<ApiResponse<List<AiBotMessageDto>>>

    // ═══════════════════════════════════════════════════════════════════════════════
    // 📍 Location Endpoints
    // ═══════════════════════════════════════════════════════════════════════════════
    @GET("api/locations/provinces/{country}")
    suspend fun getProvinces(
        @Path("country") country: String
    ): Response<ApiResponse<List<String>>>

    @GET("api/locations/cities/{province}")
    suspend fun getCities(
        @Path("province") province: String
    ): Response<ApiResponse<List<String>>>

    // ═══════════════════════════════════════════════════════════════════════════════
    // 📁 Smart Folder Endpoints
    // ═══════════════════════════════════════════════════════════════════════════════
    @GET("api/smart-folders")
    suspend fun getSmartFolders(): Response<ApiResponse<List<SmartFolderDto>>>

    // ═══════════════════════════════════════════════════════════════════════════════
    // 📚 Course Endpoints
    // ═══════════════════════════════════════════════════════════════════════════════
    @GET("api/mosbat-elm/courses")
    suspend fun getCourses(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiResponse<List<CourseDto>>>

    // ═══════════════════════════════════════════════════════════════════════════════
    // 📝 Exam Endpoints
    // ═══════════════════════════════════════════════════════════════════════════════
    @POST("api/exams")
    suspend fun createExam(
        @Body request: CreateExamRequest
    ): Response<ApiResponse<ExamDto>>

    @PUT("api/exams/{id}")
    suspend fun updateExam(
        @Path("id") examId: String,
        @Body request: CreateExamRequest
    ): Response<ApiResponse<ExamDto>>

    @POST("api/exams/{id}/activate")
    suspend fun activateExam(
        @Path("id") examId: String
    ): Response<ApiResponse<ExamDto>>

    @POST("api/exams/{id}/end")
    suspend fun endExam(
        @Path("id") examId: String
    ): Response<ApiResponse<ExamDto>>

    @GET("api/exams/{id}")
    suspend fun getExam(
        @Path("id") examId: String
    ): Response<ApiResponse<ExamDto>>

    @GET("api/exams/my")
    suspend fun getMyExams(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiResponse<PageResponse<ExamDto>>>

    @GET("api/exams/channel/{channelId}")
    suspend fun getChannelExams(
        @Path("channelId") channelId: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiResponse<PageResponse<ExamDto>>>

    @POST("api/exams/{id}/questions")
    suspend fun addExamQuestion(
        @Path("id") examId: String,
        @Body request: AddQuestionRequest
    ): Response<ApiResponse<ExamQuestionDto>>

    @DELETE("api/exams/questions/{questionId}")
    suspend fun deleteExamQuestion(
        @Path("questionId") questionId: String
    ): Response<ApiResponse<Unit>>

    @GET("api/exams/{id}/questions")
    suspend fun getExamQuestions(
        @Path("id") examId: String
    ): Response<ApiResponse<List<ExamQuestionDto>>>

    @POST("api/exams/{id}/start")
    suspend fun startExamAttempt(
        @Path("id") examId: String
    ): Response<ApiResponse<ExamAttemptDto>>

    @POST("api/exams/attempts/{attemptId}/answer")
    suspend fun submitExamAnswer(
        @Path("attemptId") attemptId: String,
        @Body request: SubmitAnswerRequest
    ): Response<ApiResponse<ExamAnswerDto>>

    @POST("api/exams/attempts/{attemptId}/submit")
    suspend fun submitExam(
        @Path("attemptId") attemptId: String
    ): Response<ApiResponse<ExamAttemptDto>>

    @GET("api/exams/attempts/{attemptId}/results")
    suspend fun getExamResults(
        @Path("attemptId") attemptId: String
    ): Response<ApiResponse<ExamResultDto>>

    @GET("api/exams/{id}/attempts")
    suspend fun getExamAttempts(
        @Path("id") examId: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiResponse<PageResponse<ExamAttemptDto>>>

    @POST("api/exams/answers/{answerId}/grade")
    suspend fun gradeExamAnswer(
        @Path("answerId") answerId: String,
        @Query("score") score: Double
    ): Response<ApiResponse<ExamAnswerDto>>

    // ═══════════════════════════════════════════════════════════════════════════════
    // 📢 Advertisement Endpoints
    // ═══════════════════════════════════════════════════════════════════════════════

    @POST("api/ads")
    suspend fun submitAdRequest(
        @Body request: CreateAdRequestDto
    ): Response<ApiResponse<AdRequestResponseDto>>

    @GET("api/channels/search")
    suspend fun searchPublicChannels(
        @Query("query") query: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ChannelListResponse>
}

