// ═══════════════════════════════════════════════════════════════
// کلاسور آنلاین — Frontend Logic (Jitsi Meet Integration)
// ═══════════════════════════════════════════════════════════════

// API base URL — auto-detect based on current host
const API_BASE = window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1'
    ? 'http://localhost:8080/api/kelasor-online'
    : window.location.origin + '/api/kelasor-online';

// ── Tab Switching ──

function switchTab(tab) {
    document.getElementById('tab-organizer').classList.toggle('active', tab === 'organizer');
    document.getElementById('tab-guest').classList.toggle('active', tab === 'guest');
    document.getElementById('panel-organizer').classList.toggle('active', tab === 'organizer');
    document.getElementById('panel-guest').classList.toggle('active', tab === 'guest');
    // Hide success panel when switching tabs
    document.getElementById('panel-success').style.display = 'none';
}

// ── Organizer Login ──

async function handleOrganizerLogin(event) {
    event.preventDefault();
    var username = document.getElementById('username').value.trim();
    var password = document.getElementById('password').value.trim();
    var roomName = document.getElementById('room-name').value.trim();
    if (!username || !password) {
        showToast('لطفاً نام کاربری و رمز عبور را وارد کنید', 'error');
        return;
    }
    setButtonLoading('btn-login', true);
    try {
        var response = await fetch(API_BASE + '/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                username: username,
                password: password,
                roomName: roomName || 'کلاس آنلاین'
            })
        });
        var data = await response.json();
        if (data.success && data.data) {
            // Save room to local history
            saveRoomToHistory(data.data, username);
            
            // Show success panel with room details
            showSuccessPanel(data.data);
            showToast('اتاق با موفقیت ایجاد شد!', 'success');
        } else {
            showToast(data.message || 'خطا در ورود', 'error');
        }
    } catch (error) {
        console.error('Login error:', error);
        showToast('خطا در اتصال به سرور. لطفاً دوباره تلاش کنید.', 'error');
    } finally {
        setButtonLoading('btn-login', false);
    }
}

// ── Guest Join ──

async function handleGuestJoin(event) {
    event.preventDefault();
    var guestName = document.getElementById('guest-name').value.trim();
    var roomId = document.getElementById('room-id').value.trim();
    if (!guestName || !roomId) {
        showToast('لطفاً نام و کد اتاق را وارد کنید', 'error');
        return;
    }
    setButtonLoading('btn-guest', true);
    try {
        var response = await fetch(API_BASE + '/guest-join', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                guestName: guestName,
                roomId: roomId
            })
        });
        var data = await response.json();
        if (data.success && data.data) {
            showToast('در حال ورود به کلاس...', 'success');
            // Redirect to Jitsi room with JWT
            setTimeout(function() {
                window.location.href = data.data.joinUrl;
            }, 800);
        } else {
            showToast(data.message || 'خطا در ورود به کلاس', 'error');
        }
    } catch (error) {
        console.error('Guest join error:', error);
        showToast('خطا در اتصال به سرور. لطفاً دوباره تلاش کنید.', 'error');
    } finally {
        setButtonLoading('btn-guest', false);
    }
}

// ── Success Panel ──

function showSuccessPanel(roomInfo) {
    document.getElementById('panel-organizer').classList.remove('active');
    document.getElementById('panel-guest').classList.remove('active');
    var successPanel = document.getElementById('panel-success');
    successPanel.style.display = 'block';
    successPanel.classList.add('active');
    document.getElementById('success-room-name').textContent = roomInfo.roomName;
    
    // Set organizer link (joinUrl is an absolute path, so prepend origin)
    var organizerLink = window.location.origin + roomInfo.joinUrl;
    var orgLinkDisplay = document.getElementById('organizer-link-display');
    if (orgLinkDisplay) {
        orgLinkDisplay.textContent = organizerLink;
    }
    window._currentOrganizerLink = organizerLink;
    
    // Set join link to Jitsi room (same origin)
    var joinLink = document.getElementById('join-link');
    if (joinLink) {
        joinLink.href = roomInfo.joinUrl;
    }
    
    // Set share link
    var shareUrl = window.location.origin + '/?room=' + roomInfo.roomId;
    var shareLinkDisplay = document.getElementById('room-link-display');
    if (shareLinkDisplay) {
        shareLinkDisplay.textContent = shareUrl;
    }
    window._currentShareUrl = shareUrl;
    
    // Fallback for old cached HTML
    var oldRoomCodeDisplay = document.getElementById('room-code-display');
    if (oldRoomCodeDisplay) {
        oldRoomCodeDisplay.textContent = roomInfo.roomId;
    }
    window._currentRoomId = roomInfo.roomId;
}

function copyOrganizerLink() {
    var link = window._currentOrganizerLink;
    if (!link) return;
    if (navigator.clipboard) {
        navigator.clipboard.writeText(link).then(function() {
            showToast('لینک برگزارکننده کپی شد!', 'success');
        });
    } else {
        var textarea = document.createElement('textarea');
        textarea.value = link;
        document.body.appendChild(textarea);
        textarea.select();
        document.execCommand('copy');
        document.body.removeChild(textarea);
        showToast('لینک برگزارکننده کپی شد!', 'success');
    }
}

function copyRoomLink() {
    var link = window._currentShareUrl;
    if (!link) return;
    if (navigator.clipboard) {
        navigator.clipboard.writeText(link).then(function() {
            showToast('لینک اتاق کپی شد!', 'success');
        });
    } else {
        var textarea = document.createElement('textarea');
        textarea.value = link;
        document.body.appendChild(textarea);
        textarea.select();
        document.execCommand('copy');
        document.body.removeChild(textarea);
        showToast('لینک اتاق کپی شد!', 'success');
    }
}

// ── Past Rooms Management ──

function saveRoomToHistory(roomInfo, username) {
    try {
        var key = 'kelasor_past_rooms_' + username;
        var pastRooms = JSON.parse(localStorage.getItem(key)) || [];
        
        // Remove if exists to push to top
        pastRooms = pastRooms.filter(function(r) { return r.roomId !== roomInfo.roomId; });
        
        // Add to top
        pastRooms.unshift({
            roomId: roomInfo.roomId,
            roomName: roomInfo.roomName,
            joinUrl: roomInfo.joinUrl,
            timestamp: new Date().getTime()
        });
        
        // Keep only last 10 rooms
        if (pastRooms.length > 10) {
            pastRooms = pastRooms.slice(0, 10);
        }
        
        localStorage.setItem(key, JSON.stringify(pastRooms));
        
        // Save last username
        localStorage.setItem('kelasor_last_username', username);
        
        loadPastRooms(); // Refresh the list
    } catch (e) {
        console.error('Error saving room history', e);
    }
}

function loadPastRooms() {
    try {
        var username = localStorage.getItem('kelasor_last_username');
        if (!username) return;
        
        var key = 'kelasor_past_rooms_' + username;
        var pastRooms = JSON.parse(localStorage.getItem(key)) || [];
        
        var container = document.getElementById('past-rooms-container');
        var list = document.getElementById('past-rooms-list');
        
        if (!container || !list) return;
        
        if (pastRooms.length === 0) {
            container.style.display = 'none';
            return;
        }
        
        list.innerHTML = ''; // Clear current
        
        pastRooms.forEach(function(room) {
            var date = new Date(room.timestamp).toLocaleDateString('fa-IR');
            
            var item = document.createElement('div');
            item.style.cssText = 'background: rgba(255,255,255,0.05); border: 1px solid rgba(255,255,255,0.1); border-radius: 8px; padding: 12px; display: flex; flex-direction: column; gap: 8px;';
            
            var header = document.createElement('div');
            header.style.cssText = 'display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid rgba(255,255,255,0.1); padding-bottom: 8px;';
            header.innerHTML = '<span style="font-weight: bold; color: #E2E8F0;">' + room.roomName + '</span>' +
                               '<span style="font-size: 0.8rem; color: #94A3B8;">' + date + '</span>';
                               
            var actions = document.createElement('div');
            actions.style.cssText = 'display: flex; gap: 8px; justify-content: space-between; align-items: center; margin-top: 5px;';
            
            var shareUrl = window.location.origin + '/?room=' + room.roomId;
            var orgLink = window.location.origin + room.joinUrl;
            
            actions.innerHTML = 
                '<div style="display: flex; gap: 5px;">' +
                    '<button onclick="window.copyAnyText(\'' + orgLink + '\')" style="background: #3B82F6; color: white; border: none; padding: 5px 10px; border-radius: 4px; font-size: 0.8rem; cursor: pointer;">📋 کپی لینک شما</button>' +
                    '<button onclick="window.copyAnyText(\'' + shareUrl + '\')" style="background: #10B981; color: white; border: none; padding: 5px 10px; border-radius: 4px; font-size: 0.8rem; cursor: pointer;">🔗 کپی لینک دانشجو</button>' +
                '</div>' +
                '<a href="' + room.joinUrl + '" target="_blank" style="background: #8B5CF6; color: white; text-decoration: none; padding: 5px 15px; border-radius: 4px; font-size: 0.8rem; font-weight: bold;">ورود</a>';
            
            item.appendChild(header);
            item.appendChild(actions);
            list.appendChild(item);
        });
        
        container.style.display = 'block';
    } catch (e) {
        console.error('Error loading room history', e);
    }
}

function clearPastRooms() {
    if (confirm('آیا از حذف تاریخچه اتاق‌ها اطمینان دارید؟')) {
        var username = localStorage.getItem('kelasor_last_username');
        if (username) {
            localStorage.removeItem('kelasor_past_rooms_' + username);
            loadPastRooms();
        }
    }
}

window.copyAnyText = function(text) {
    if (navigator.clipboard) {
        navigator.clipboard.writeText(text).then(function() { showToast('لینک کپی شد', 'success'); });
    } else {
        var textarea = document.createElement('textarea');
        textarea.value = text;
        document.body.appendChild(textarea);
        textarea.select();
        document.execCommand('copy');
        document.body.removeChild(textarea);
        showToast('لینک کپی شد', 'success');
    }
};

// ── UI Helpers ──

function setButtonLoading(btnId, isLoading) {
    var btn = document.getElementById(btnId);
    var btnText = btn.querySelector('.btn-text');
    var btnLoader = btn.querySelector('.btn-loader');
    if (isLoading) {
        btnText.style.display = 'none';
        btnLoader.style.display = 'flex';
        btn.disabled = true;
    } else {
        btnText.style.display = 'inline';
        btnLoader.style.display = 'none';
        btn.disabled = false;
    }
}

function showToast(message, type) {
    var toast = document.getElementById('toast');
    var toastIcon = document.getElementById('toast-icon');
    var toastText = document.getElementById('toast-text');
    toastText.textContent = message;
    toast.className = 'toast ' + type;
    toastIcon.textContent = type === 'error' ? '!' : 'OK';
    toast.classList.add('show');
    clearTimeout(window._toastTimer);
    window._toastTimer = setTimeout(function() {
        toast.classList.remove('show');
    }, 4000);
}

// ── Background Particles ──

function createParticles() {
    var container = document.getElementById('particles');
    if (!container) return;
    for (var i = 0; i < 15; i++) {
        var particle = document.createElement('div');
        particle.className = 'particle';
        var size = Math.random() * 4 + 2;
        particle.style.width = size + 'px';
        particle.style.height = size + 'px';
        particle.style.left = Math.random() * 100 + '%';
        particle.style.animationDuration = (Math.random() * 15 + 10) + 's';
        particle.style.animationDelay = (Math.random() * 10) + 's';
        particle.style.opacity = Math.random() * 0.5 + 0.1;
        container.appendChild(particle);
    }
}

function initApp() {
    createParticles();
    
    // Auto-fill room from URL params
    var urlParams = new URLSearchParams(window.location.search);
    var roomParam = urlParams.get('room');
    if (roomParam) {
        // Switch to guest tab
        switchTab('guest');
        // Fill room ID input
        document.getElementById('room-id').value = roomParam;
        // Focus name input
        setTimeout(function() {
            var nameInput = document.getElementById('guest-name');
            if (nameInput) nameInput.focus();
        }, 100);
        // Clean URL to not confuse users
        window.history.replaceState({}, document.title, window.location.pathname);
    }
    
    // Load past rooms if available
    loadPastRooms();
}

document.addEventListener('DOMContentLoaded', initApp);
