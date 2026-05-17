// ═══════════════════════════════════════════════════════════════
// کلاسور آنلاین — Room Logic (Desktop + Mobile Responsive)
// ═══════════════════════════════════════════════════════════════

(function() {
    'use strict';

    var params = new URLSearchParams(window.location.search);
    var roomName = params.get('room');
    var jwt = params.get('jwt');
    var displayName = params.get('name') || '\u0634\u0631\u06A9\u062A\u200C\u06A9\u0646\u0646\u062F\u0647';
    var isMod = !!jwt;
    var api = null;
    var participants = [];
    var SIGNAL_PREFIX = 'KEL_SIG:';
    var isMobile = window.innerWidth <= 768;
    var sidebarOpen = false;
    var unreadCount = 0;
    var touchStartY = 0;
    var touchCurrentY = 0;
    var isDragging = false;

    if (!roomName) return;

    // ── Mobile Sidebar Functions ──

    window.toggleMobileSidebar = function() {
        var sidebar = document.getElementById('kelasor-sidebar');
        var backdrop = document.getElementById('mobile-backdrop');
        var fab = document.getElementById('mobile-fab');
        if (!sidebar) return;
        sidebarOpen = !sidebarOpen;
        if (sidebarOpen) {
            sidebar.classList.add('sidebar-open');
            backdrop.style.display = 'block';
            requestAnimationFrame(function() {
                backdrop.classList.add('visible');
            });
            fab.innerHTML = '✕';
            // Reset unread count
            unreadCount = 0;
            updateFabBadge();
        } else {
            closeMobileSidebar();
        }
    };

    function closeMobileSidebar() {
        var sidebar = document.getElementById('kelasor-sidebar');
        var backdrop = document.getElementById('mobile-backdrop');
        var fab = document.getElementById('mobile-fab');
        if (!sidebar) return;
        sidebarOpen = false;
        sidebar.classList.remove('sidebar-open');
        backdrop.classList.remove('visible');
        setTimeout(function() {
            backdrop.style.display = 'none';
        }, 300);
        fab.innerHTML = '💬<span class="fab-badge" id="fab-badge">' + (unreadCount > 0 ? unreadCount : '') + '</span>';
        updateFabBadge();
    }

    window.switchMobileTab = function(tab) {
        var sidebar = document.getElementById('kelasor-sidebar');
        var tabs = document.querySelectorAll('#mobile-tabs .mobile-tab');
        if (!sidebar) return;
        // Toggle class
        sidebar.classList.remove('tab-chat', 'tab-participants');
        sidebar.classList.add('tab-' + tab);
        // Active tab styling
        tabs.forEach(function(t) {
            t.classList.toggle('active', t.getAttribute('data-tab') === tab);
        });
    };

    function updateFabBadge() {
        var badge = document.getElementById('fab-badge');
        if (!badge) return;
        if (unreadCount > 0 && !sidebarOpen) {
            badge.style.display = 'flex';
            badge.textContent = unreadCount > 9 ? '9+' : unreadCount;
        } else {
            badge.style.display = 'none';
        }
    }

    function updateMobileParticipantCount() {
        var el = document.getElementById('mobile-p-count');
        if (el) {
            el.textContent = participants.length;
        }
    }

    // ── Touch Swipe to Close ──

    function setupSwipeGesture() {
        var handle = document.getElementById('mobile-drag-handle');
        var sidebar = document.getElementById('kelasor-sidebar');
        if (!handle || !sidebar) return;
        handle.addEventListener('touchstart', function(e) {
            isDragging = true;
            touchStartY = e.touches[0].clientY;
            sidebar.style.transition = 'none';
        }, { passive: true });
        handle.addEventListener('touchmove', function(e) {
            if (!isDragging) return;
            touchCurrentY = e.touches[0].clientY;
            var deltaY = touchCurrentY - touchStartY;
            if (deltaY > 0) {
                sidebar.style.transform = 'translateY(' + deltaY + 'px)';
            }
        }, { passive: true });
        handle.addEventListener('touchend', function() {
            if (!isDragging) return;
            isDragging = false;
            sidebar.style.transition = '';
            var deltaY = touchCurrentY - touchStartY;
            if (deltaY > 80) {
                closeMobileSidebar();
            } else {
                sidebar.style.transform = '';
                if (sidebarOpen) {
                    sidebar.classList.add('sidebar-open');
                }
            }
        }, { passive: true });
    }

    // ── Backdrop click to close ──

    function setupBackdropClose() {
        var backdrop = document.getElementById('mobile-backdrop');
        if (backdrop) {
            backdrop.addEventListener('click', function() {
                closeMobileSidebar();
            });
        }
    }

    // ── Resize handler ──

    function handleResize() {
        var wasMobile = isMobile;
        isMobile = window.innerWidth <= 768;
        if (wasMobile && !isMobile) {
            // Switched to desktop: reset sidebar
            var sidebar = document.getElementById('kelasor-sidebar');
            var backdrop = document.getElementById('mobile-backdrop');
            if (sidebar) {
                sidebar.classList.remove('sidebar-open');
                sidebar.style.transform = '';
            }
            if (backdrop) {
                backdrop.style.display = 'none';
                backdrop.classList.remove('visible');
            }
            sidebarOpen = false;
        }
    }

    window.addEventListener('resize', handleResize);

    // ── Jitsi Init ──

    function initJitsi() {
        var domain = 'online.kelasorapp.ir';
        var options = {
            roomName: roomName,
            width: '100%',
            height: '100%',
            parentNode: document.querySelector('#jitsi-container'),
            jwt: jwt,
            lang: 'fa',
            configOverwrite: {
                prejoinPageEnabled: false,
                disableInitialGUM: true,
                startWithAudioMuted: true,
                startWithVideoMuted: true,
                startAudioModerationEnabled: true,
                startVideoModerationEnabled: true,
                defaultLanguage: 'fa',
                disableDeepLinking: true,
                disableProfile: true,
                disableInviteFunctions: true,
                defaultTiling: true,
                disableSelfView: false,
                hideConferenceTimer: true,
                hideParticipantsPane: true,
                hideChatButton: true,
                disableTileView: true,
                welcomePage: {
                    disabled: true
                },
                filmstrip: {
                    enabled: false
                },
                verticalFilmstrip: false,
                buttonsWithNotifyClick: [
                    {
                        key: 'hangup',
                        preventExecution: true
                    },
                    {
                        key: 'hangup-menu',
                        preventExecution: true
                    },
                    {
                        key: 'end-meeting',
                        preventExecution: true
                    }
                ],
                toolbarButtons: isMod ? 
                    ['microphone', 'camera', 'desktop', 'fullscreen', 'fodeviceselection', 'hangup', 'raisehand', 'settings', 'videoquality', 'mute-everyone', 'security'] :
                    ['fullscreen', 'hangup', 'raisehand'],
            },
            interfaceConfigOverwrite: {
                SHOW_JITSI_WATERMARK: false,
                SHOW_WATERMARK_FOR_GUESTS: false,
                SHOW_BRAND_WATERMARK: false,
                SHOW_POWERED_BY: false,
                MOBILE_APP_PROMO: false,
                DISPLAY_WELCOME_PAGE_CONTENT: false,
                GENERATE_ROOMNAMES_ON_WELCOME_PAGE: false,
            },
            userInfo: { displayName: displayName }
        };
        api = new JitsiMeetExternalAPI(domain, options);
        setupEvents();
    }

    function setupEvents() {
        api.addEventListeners({
            videoConferenceJoined: function() {
                document.getElementById('loading-overlay').style.display = 'none';
                updateParticipants();
            },
            participantJoined: updateParticipants,
            participantLeft: updateParticipants,
            displayNameChange: updateParticipants,
            incomingMessage: handleIncomingMessage,
            endpointTextMessageReceived: handleEndpointSignal,
            videoConferenceLeft: function() {
                handleExit();
            },
            readyToClose: function() {
                handleExit();
            },
            dialogClosed: function(dialog) {
                if (dialog && (dialog.name === 'FeedbackDialog' || dialog.titleKey === 'dialog.sessTerminatedReason')) {
                    handleExit();
                }
            },
            toolbarButtonClicked: function(e) {
                var key = e.key || e.name || e.id;
                if (key === 'hangup' || key === 'hangup-menu') {
                    if (isMod) {
                        openExitModal();
                    } else {
                        api.executeCommand('hangup');
                    }
                } else if (key === 'end-meeting') {
                    api.executeCommand('endConference');
                }
            }
        });

        function handleExit() {
            var overlay = document.getElementById('termination-overlay');
            if (overlay) {
                overlay.style.display = 'flex';
            }
            // Attempt auto-redirect
            window.top.location.href = 'https://online.kelasorapp.ir';
        }
        document.getElementById('send-btn').onclick = sendMessage;
        document.getElementById('chat-input').onkeydown = function(e) { if (e.key === 'Enter') sendMessage(); };
    }

    function updateParticipants() {
        participants = api.getParticipantsInfo();
        var list = document.getElementById('participants-list');
        var count = document.getElementById('p-count');
        list.innerHTML = '';
        count.textContent = participants.length;
        updateMobileParticipantCount();
        participants.forEach(function(p) {
            var item = document.createElement('div');
            item.className = 'participant-item';
            item.innerHTML = 
                '<div class="participant-info">' +
                    '<div class="participant-avatar">' + (p.displayName ? p.displayName[0] : '?') + '</div>' +
                    '<div class="participant-name">' + (p.formattedDisplayName || p.displayName) + '</div>' +
                '</div>' +
                (isMod ? '<div class="participant-actions" data-id="' + p.participantId + '" data-name="' + p.displayName + '">\u22EE</div>' : '');
            if (isMod) {
                item.querySelector('.participant-actions').onclick = function(e) {
                    showMenu(e, p.participantId, p.displayName);
                };
            }
            list.appendChild(item);
        });
    }

    function sendMessage() {
        var input = document.getElementById('chat-input');
        var text = input.value.trim();
        if (!text) return;
        api.executeCommand('sendChatMessage', text, '', true);
        addMessageToUI('local', '\u0645\u0646', text);
        input.value = '';
    }

    function handleIncomingMessage(e) {
        if (e.message.startsWith('KEL_SIGNAL:') || e.message.startsWith('KEL_SIG:')) return; 
        if (e.nick === displayName) return; 
        addMessageToUI('remote', e.nick, e.message);
        // Mobile: increment unread badge if sidebar is closed
        if (isMobile && !sidebarOpen) {
            unreadCount++;
            updateFabBadge();
        }
    }

    function addMessageToUI(type, sender, text) {
        var container = document.getElementById('chat-messages');
        var div = document.createElement('div');
        div.className = 'message ' + type;
        div.innerHTML = '<div class="message-sender">' + sender + '</div>' + text;
        container.appendChild(div);
        container.scrollTop = container.scrollHeight;
    }

    function showMenu(e, id, name) {
        var menu = document.getElementById('custom-menu');
        menu.innerHTML = 
            '<div class="menu-item" onclick="sendSilentSignal(\'' + id + '\', \'MIC\')">\uD83C\uDF99\uFE0F \u062F\u0631\u062E\u0648\u0627\u0633\u062A \u0645\u06CC\u06A9\u0631\u0641\u0648\u0646</div>' +
            '<div class="menu-item" onclick="sendSilentSignal(\'' + id + '\', \'CAM\')">\uD83D\uDCF7 \u062F\u0631\u062E\u0648\u0627\u0633\u062A \u062A\u0635\u0648\u06CC\u0631</div>' +
            '<div class="menu-item" onclick="sendSilentSignal(\'' + id + '\', \'SCREEN\')">\uD83D\uDDA5\uFE0F \u062F\u0631\u062E\u0648\u0627\u0633\u062A \u0627\u0634\u062A\u0631\u0627\u06A9 \u0635\u0641\u062D\u0647</div>' +
            '<div class="menu-item danger" onclick="kick(\'' + id + '\')">\u274C \u0627\u062E\u0631\u0627\u062C \u0627\u0632 \u06A9\u0644\u0627\u0633</div>';
        menu.style.display = 'block';
        if (isMobile) {
            // Center on mobile
            menu.style.left = '50%';
            menu.style.top = '50%';
            menu.style.transform = 'translate(-50%, -50%)';
            menu.style.bottom = 'auto';
        } else {
            menu.style.left = (e.clientX - 180) + 'px';
            menu.style.top = e.clientY + 'px';
            menu.style.transform = '';
        }
        window.onclick = function() { menu.style.display = 'none'; };
        e.stopPropagation();
    }

    window.sendSilentSignal = function(toId, type) {
        api.executeCommand('sendEndpointTextMessage', toId, SIGNAL_PREFIX + type);
    };

    function handleEndpointSignal(e) {
        var data = e.data.eventData;
        if (!data || !data.text || !data.text.startsWith(SIGNAL_PREFIX)) return;
        var type = data.text.substring(SIGNAL_PREFIX.length);
        var popup = document.getElementById('request-popup');
        var text = document.getElementById('request-text');
        var acceptBtn = document.getElementById('request-accept');
        popup.style.background = '#1E1B4B';
        popup.style.border = '2px solid #6366F1';
        if (type === 'MIC') {
            text.textContent = '\u0645\u062F\u06CC\u0631 \u0627\u0632 \u0634\u0645\u0627 \u062E\u0648\u0627\u0633\u062A \u06A9\u0647 \u0635\u062F\u0627 \u0631\u0627 \u0648\u0635\u0644 \u06A9\u0646\u06CC\u062F.';
            acceptBtn.textContent = '\u0648\u0635\u0644 \u0635\u062F\u0627';
            acceptBtn.onclick = function() { api.executeCommand('toggleAudio'); popup.style.display = 'none'; };
        } else if (type === 'CAM') {
            text.textContent = '\u0645\u062F\u06CC\u0631 \u0627\u0632 \u0634\u0645\u0627 \u062E\u0648\u0627\u0633\u062A \u06A9\u0647 \u062A\u0635\u0648\u06CC\u0631 \u0631\u0627 \u0648\u0635\u0644 \u06A9\u0646\u06CC\u062F.';
            acceptBtn.textContent = '\u0648\u0635\u0644 \u062A\u0635\u0648\u06CC\u0631';
            acceptBtn.onclick = function() { api.executeCommand('toggleVideo'); popup.style.display = 'none'; };
        } else if (type === 'SCREEN') {
            text.textContent = '\u0645\u062F\u06CC\u0631 \u0627\u0632 \u0634\u0645\u0627 \u062E\u0648\u0627\u0633\u062A \u06A9\u0647 \u0635\u0641\u062D\u0647 \u0631\u0627 \u0627\u0634\u062A\u0631\u0627\u06A9 \u0628\u06AF\u0630\u0627\u0631\u06CC\u062F.';
            acceptBtn.textContent = '\u0627\u0634\u062A\u0631\u0627\u06A9 \u0635\u0641\u062D\u0647';
            acceptBtn.onclick = function() { api.executeCommand('toggleShareScreen'); popup.style.display = 'none'; };
        }
        popup.style.display = 'flex';
    }

    window.kick = function(id) {
        if (confirm('\u0622\u06CC\u0627 \u0645\u0637\u0645\u0626\u0646\u06CC\u062F\u061F')) {
            api.executeCommand('kickParticipant', id);
        }
    };

    // ── Initialize ──

    function initMobile() {
        setupSwipeGesture();
        setupBackdropClose();
        // On mobile, sidebar starts closed (CSS handles it)
        if (isMobile) {
            var sidebar = document.getElementById('kelasor-sidebar');
            if (sidebar) sidebar.classList.remove('sidebar-open');
        }
    }

    function boot() {
        initJitsi();
        initMobile();
    }

    if (window.JitsiMeetExternalAPI) {
        boot();
    } else {
        var script = document.createElement('script');
        script.src = 'https://online.kelasorapp.ir/external_api.js';
        script.onload = boot;
        document.head.appendChild(script);
    }

    // ── Exit Modal Controllers ──
    window.openExitModal = function() {
        var modal = document.getElementById('exit-modal');
        if (modal) modal.style.display = 'flex';
    };

    window.closeExitModal = function() {
        var modal = document.getElementById('exit-modal');
        if (modal) modal.style.display = 'none';
    };

    window.confirmLeaveClass = function() {
        window.closeExitModal();
        if (api) api.executeCommand('hangup');
    };

    window.confirmEndClass = function() {
        window.closeExitModal();
        if (api) api.executeCommand('endConference');
    };

})();
