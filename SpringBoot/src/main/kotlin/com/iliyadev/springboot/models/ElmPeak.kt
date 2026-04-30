package com.iliyadev.springboot.models

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

enum class ElmEventType {
    COMPETITION, STARTUP, CONGRESS
}

@Entity
@Table(name = "elm_events")
class ElmEvent(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @Column(nullable = false)
    var title: String = "",

    @Column(columnDefinition = "TEXT")
    var description: String = "",

    var date: String = "",
    var location: String = "",
    var imageUrl: String? = null,
    var organizer: String? = null,
    var reward: String? = null,

    @Enumerated(EnumType.STRING)
    var type: ElmEventType = ElmEventType.COMPETITION,

    var isExternal: Boolean = false,
    var link: String? = null,
    
    // Admin approval workflow
    @Column(name = "is_approved", nullable = false)
    var isApproved: Boolean = false,
    var submittedByUserId: UUID? = null,
    
    var createdAt: Instant = Instant.now()
)

@Entity
@Table(name = "startup_ideas")
class StartupIdea(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @Column(nullable = false)
    var title: String = "",

    @Column(columnDefinition = "TEXT")
    var description: String = "",

    var contactInfo: String = "",
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User? = null,

    var createdAt: Instant = Instant.now()
)

@Entity
@Table(name = "event_reports")
class EventReport(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @Column(nullable = false)
    var title: String = "",

    @Column(columnDefinition = "TEXT")
    var description: String = "",

    var date: String = "",
    var location: String = "",
    var link: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User? = null,

    var isVerified: Boolean = false,
    var pointsAwarded: Int = 0,
    
    var createdAt: Instant = Instant.now()
)

// DTOs moved to Dtos.kt
