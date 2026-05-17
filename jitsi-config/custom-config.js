// ═══════════════════════════════════════════════════════════════
// کلاسور آنلاین — Jitsi Server-Side Config Overrides
// These apply when Jitsi loads inside the IFrame
// ═══════════════════════════════════════════════════════════════

// CRITICAL: Guest domain for anonymous access with JWT auth
config.hosts = config.hosts || {};
config.hosts.anonymousdomain = 'guest.meet.jitsi';

// Disable prejoin
config.prejoinConfig = config.prejoinConfig || {};
config.prejoinConfig.enabled = false;

// No camera/mic request on load
config.disableInitialGUM = true;

// Join muted
config.startWithAudioMuted = true;
config.startWithVideoMuted = true;

// AV Moderation
config.startAudioModerationEnabled = true;
config.startVideoModerationEnabled = true;

// Persian
config.defaultLanguage = 'fa';

// Deep linking off
config.disableDeepLinking = true;

// Display names
config.defaultLocalDisplayName = 'من';
config.defaultRemoteDisplayName = 'شرکت‌کننده';

// Completely disable invite (prevents sharing internal Jitsi URLs)
config.disableInviteFunctions = true;

// Force tile view (grid view) instead of stage/filmstrip view
config.defaultTiling = true;

// ── Mobile-specific optimizations ──
// Disable filmstrip entirely (we use external sidebar)
config.filmstrip = config.filmstrip || {};
config.filmstrip.enabled = false;

// Hide dominant speaker badge (saves space on mobile)
config.hideDominantSpeakerBadge = true;

// Disable 1-on-1 mode (keeps tile layout even with 2 participants)
config.disable1On1Mode = true;

// Responsive tiles
config.disableResponsiveTiles = false;

// ── Welcome & Redirect Overrides ──
config.welcomePage = config.welcomePage || {};
config.welcomePage.disabled = true;

// Default Language
config.defaultLanguage = 'fa';

