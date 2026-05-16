// ═══════════════════════════════════════════════════════════════
// کلاسور آنلاین — Room Logic (Custom UI Bridge Version)
// ═══════════════════════════════════════════════════════════════

(function() {
    'use strict';

    var params = new URLSearchParams(window.location.search);
    var roomName = params.get('room');
    var jwt = params.get('jwt');
    var displayName = params.get('name') || '\u0634\u0631\u06A9\u062A\u200C\u06A9\u0646\u0646\u062F\u0647';
    var isMod = !!jwt;
    
    var api = null;
    var participants = {};
    var SIGNAL_PREFIX = 'KEL_SIGNAL:';

    if (!roomName) return;

    // ── Initialize Jitsi API ──
    function initJitsi() {
        var domain = 'online.kelasorapp.ir';
        var options = {
            roomName: roomName,
            width: '100%',
            height: '100%',
            parentNode: document.querySelector('#jitsi-container'),
            jwt: jwt,
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
                toolbarButtons: isMod ? 
                    ['microphone', 'camera', 'desktop', 'fullscreen', 'fodeviceselection', 'hangup', 'raisehand', 'settings', 'videoquality', 'mute-everyone', 'security'] :
                    ['fullscreen', 'hangup', 'raisehand'], // Students have NO toolbar buttons for AV/Chat/Participants
                // Hide Jitsi internal panels entirely
                hideConferenceTimer: true,
                hideParticipantsPane: true,
                hideChatButton: true,
                disableTileView: true, // Disable the toggle button
            },
            interfaceConfigOverwrite: {
                SHOW_JITSI_WATERMARK: false,
                SHOW_WATERMARK_FOR_GUESTS: false,
                SHOW_BRAND_WATERMARK: false,
                SHOW_POWERED_BY: false,
                GENERATE_ROOMNAMES_ON_WELCOME_PAGE: false,
                DISPLAY_WELCOME_PAGE_CONTENT: false,
                MOBILE_APP_PROMO: false,
                // Ensure sidebar panels are never opened by default
                SETTINGS_SECTIONS: ['devices', 'language', 'profile'],
            },
            userInfo: { displayName: displayName }
        };

        api = new JitsiMeetExternalAPI(domain, options);
        setupEvents();
    }

    // ── Bridge Events ──
    function setupEvents() {
        api.addEventListeners({
            videoConferenceJoined: function(e) {
                document.getElementById('loading-overlay').style.display = 'none';
                updateParticipants();
            },
            participantJoined: updateParticipants,
            participantLeft: updateParticipants,
            displayNameChange: updateParticipants,
            incomingMessage: handleIncomingMessage,
            endpointTextMessageReceived: handleEndpointSignal,
        });

        // Chat Input
        document.getElementById('send-btn').onclick = sendMessage;
        document.getElementById('chat-input').onkeydown = function(e) { if (e.key === 'Enter') sendMessage(); };
    }

    function updateParticipants() {
        participants = api.getParticipantsInfo();
        var list = document.getElementById('participants-list');
        var count = document.getElementById('p-count');
        list.innerHTML = '';
        count.textContent = participants.length;

        participants.forEach(function(p) {
            var item = document.createElement('div');
            item.className = 'participant-item';
            item.innerHTML = 
                '<div class="participant-info">' +
                    '<div class="participant-avatar">' + (p.displayName ? p.displayName[0] : '?') + '</div>' +
                    '<div class="participant-name">' + p.displayName + '</div>' +
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

    // ── Chat Bridge ──
    function sendMessage() {
        var input = document.getElementById('chat-input');
        var text = input.value.trim();
        if (!text) return;

        api.executeCommand('sendChatMessage', text, '', true);
        addMessageToUI('local', '\u0645\u0646', text);
        input.value = '';
    }

    function handleIncomingMessage(e) {
        if (e.message.startsWith(SIGNAL_PREFIX)) {
            handleSignal(e.message.substring(SIGNAL_PREFIX.length), e.nick, e.from);
            return;
        }
        // Don't duplicate local messages
        if (e.nick === displayName) return; 
        addMessageToUI('remote', e.nick, e.message);
    }

    function addMessageToUI(type, sender, text) {
        var container = document.getElementById('chat-messages');
        var div = document.createElement('div');
        div.className = 'message ' + type;
        div.innerHTML = '<div class="message-sender">' + sender + '</div>' + text;
        container.appendChild(div);
        container.scrollTop = container.scrollHeight;
    }

    // ── Signaling (Moderator -> Student) ──
    function showMenu(e, id, name) {
        var menu = document.getElementById('custom-menu');
        menu.innerHTML = 
            '<div class="menu-item" onclick="sendSignal(\'MIC\', \'' + id + '\')">\uD83C\uDF99\uFE0F \u062F\u0631\u062E\u0648\u0627\u0633\u062A \u0645\u06CC\u06A9\u0631\u0641\u0648\u0646</div>' +
            '<div class="menu-item" onclick="sendSignal(\'CAM\', \'' + id + '\')">\uD83D\uDCF7 \u062F\u0631\u062E\u0648\u0627\u0633\u062A \u062A\u0635\u0648\u06CC\u0631</div>' +
            '<div class="menu-item" onclick="sendSignal(\'SCREEN\', \'' + id + '\')">\uD83D\uDDA5\uFE0F \u062F\u0631\u062E\u0648\u0627\u0633\u062A \u0627\u0634\u062A\u0631\u0627\u06A9 \u0635\u0641\u062D\u0647</div>' +
            '<div class="menu-item danger" onclick="kick(\'' + id + '\')">\u274C \u0627\u062E\u0631\u0627\u062C \u0627\u0632 \u06A9\u0644\u0627\u0633</div>';
        
        menu.style.display = 'block';
        menu.style.left = (e.clientX - 180) + 'px';
        menu.style.top = e.clientY + 'px';
        
        window.onclick = function() { menu.style.display = 'none'; };
        e.stopPropagation();
    }

    window.sendSignal = function(type, id) {
        // We use Chat as a transport for signals to reach everyone easily
        var msg = SIGNAL_PREFIX + type + ':' + id;
        api.executeCommand('sendChatMessage', msg, '', true);
    };

    window.kick = function(id) {
        if (confirm('\u0622\u06CC\u0627 \u0645\u0637\u0645\u0626\u0646\u06CC\u062F\u061F')) {
            api.executeCommand('kickParticipant', id);
        }
    };

    function handleSignal(signal, sender, fromId) {
        var parts = signal.split(':');
        var type = parts[0];
        var targetId = parts[1];
        
        // Only handle if targeted at me
        var myId = api._myID; 
        if (targetId !== myId) return;

        var popup = document.getElementById('request-popup');
        var text = document.getElementById('request-text');
        
        if (type === 'MIC') text.textContent = sender + ' \u0627\u0632 \u0634\u0645\u0627 \u062E\u0648\u0627\u0633\u062A \u06A9\u0647 \u0645\u06CC\u06A9\u0631\u0641\u0648\u0646 \u0631\u0627 \u0628\u0627\u0632 \u06A9\u0646\u06CC\u062F.';
        if (type === 'CAM') text.textContent = sender + ' \u0627\u0632 \u0634\u0645\u0627 \u062E\u0648\u0627\u0633\u062A \u06A9\u0647 \u062A\u0635\u0648\u06CC\u0631 \u062E\u0648\u0621 \u0631\u0627 \u0628\u0627\u0632 \u06A9\u0646\u06CC\u062F.';
        if (type === 'SCREEN') text.textContent = sender + ' \u0627\u0632 \u0634\u0645\u0627 \u062E\u0648\u0627\u0633\u062A \u06A9\u0647 \u0635\u0641\u062D\u0647 \u062E\u0648\u062F \u0631\u0627 \u0628\u0647 \u0627\u0634\u062A\u0631\u0627\u06A9 \u0628\u06AF\u0630\u0627\u0631\u06CC\u062F.';
        
        popup.style.display = 'flex';
        document.getElementById('request-accept').onclick = function() {
            popup.style.display = 'none';
        };
    }

    function handleEndpointSignal(e) {
        // Reserved for private signaling if needed
    }

    // ── Start ──
    if (window.JitsiMeetExternalAPI) {
        initJitsi();
    } else {
        var script = document.createElement('script');
        script.src = 'https://online.kelasorapp.ir/external_api.js';
        script.onload = initJitsi;
        document.head.appendChild(script);
    }

})();
