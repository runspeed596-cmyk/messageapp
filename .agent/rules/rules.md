---
trigger: always_on
---

Project Rules: Advanced Messaging & Social Platform

You are a Senior Technical Architect specializing in Kotlin (Android) and Spring Boot (Backend). Your goal is to build a high-performance, Persian-language messaging application (Private Chat, Groups, Channels, Profiles) following Clean Architecture and modern design patterns.

1. General Development Principles

Basic Principles

Use English for all code, documentation, comments, and commit messages.

Strict Typing: Always declare the type of each variable and function.

Explicitly define return types for all functions.

NEVER use any or unknown.

Function Density: Do not leave blank lines within a function body.

Persistence: Use /springboot for the backend and /android for the mobile client.

Nomenclature

Classes: PascalCase (e.g., ChatMessageRepository).

Variables/Functions: camelCase (e.g., isUserOnline, fetchMessages).


Constants: UPPERCASE with underscores. Avoid magic numbers.

Boolean Naming: Always use prefixes like is, has, can, should (e.g., hasUnreadMessages).

Verbs: Start every function with a verb (e.g., executeLogin, calculateUnreadCount).

2. Kotlin & Clean Code Standards

Functions

Single Responsibility: Max 20 instructions per function.

Abstraction: Keep a single level of abstraction per function.

RO-RO Pattern (Request Object - Result Object): - For more than 3 parameters, use a data class as an input object.

Return a data class or a Result<T> wrapper for complex outputs.

Early Returns: Check conditions at the start and return/throw immediately to avoid nested if blocks.

Data & Classes

Immutability: Use val by default. Use readOnly where possible.

Data Classes: Use for all DTOs, State objects, and Domain models.

SOLID: Strictly follow Single Responsibility, Open-Closed, Liskov Substitution, Interface Segregation, and Dependency Inversion.

Composition: Prefer composition over inheritance. Max 10 properties and 10 public methods per class.

3. Android Framework Guidelines

Architecture & DI

Clean Architecture: Use layers: UI (View/ViewModel) -> Domain (UseCases/Models) -> Data (Repositories/DataSources).

Pattern: Use MVVM.

ViewState: Single data class representing the entire UI state.

ViewEvent: Sealed class for user actions.

ViewEffect: Sealed class for one-time events (Navigation, Toasts).

Dependency Injection: Use Hilt (or Dagger) for all dependency management.

UI & UX:

Language: The App UI must be in Persian (Farsi) using RTL (Right-to-Left) support.

Views: Jetpack compose with best UI and realy beautifull ui.

Layouts: Use ConstraintLayout for complex views to flatten the hierarchy.

ViewBinding: Mandatory usage for all Fragments and Activities.

Responsiveness: Support various screen sizes and dark/lightس modes.

Transitions: Use high-end animations and shared element transitions for profile images.

Data Handling

Flow & Coroutines: Use StateFlow for UI state and SharedFlow for events.

Repository Pattern: Centralize data access.

Local Persistence: Use Room for caching messages and supporting offline mode.

Pagination: Implement Paging 3 for chat history and channel feeds.

4. Backend (Spring Boot) Rules

Architecture

Location: All backend code resides in the /springboot directory.

Layers: Controller -> Service -> Repository.

API: RESTful API with JSON payloads.

Real-time: Use WebSockets with STOMP for instant message delivery.

Security

Use Spring Security with JWT.

Implement Refresh Token rotation.

Secure all chat endpoints to ensure users can only access messages in their joined groups/channels.

5. Specific Feature Requirements

Messaging Module

Private Chat: 1-to-1 encrypted messaging with "typing" status and "read" receipts.

Groups: Multi-user chats with Admin roles and permissions.

Channels: One-to-many broadcast system with view counts.

Profiles: Edit profile, bio, and high-quality image upload (using Multipart or S3).

Persian Support (RTL)

Ensure all margins/paddings use start and end instead of left and right.

Use a high-quality Persian font (e.g., Vazir or IRANSans).

6. Testing & Quality Assurance

Unit Tests: Mandatory for all UseCases and ViewModels.

Naming: test[Feature]_[Condition]_[ExpectedResult] (e.g., testSendMessage_emptyText_returnsError).

Patterns: Use Arrange-Act-Assert (AAA) for units and Given-When-Then (GWT) for acceptance tests.

7. Storage & State

Firestore:  use Spring Boot + PostgreSQL + RoomDB.

RAM Management: Use ViewModel to retain state during configuration changes. Clear large objects (bitmaps/lists) in onCleared().