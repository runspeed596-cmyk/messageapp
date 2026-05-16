// ═══════════════════════════════════════════════════════════════
// کلاسور آنلاین — Jitsi Custom Interface Config
// ═══════════════════════════════════════════════════════════════

if (typeof interfaceConfig !== 'undefined') {
    // Rebrand to Kelasor Online
    interfaceConfig.APP_NAME = 'کلاسور آنلاین';
    interfaceConfig.NATIVE_APP_NAME = 'کلاسور آنلاین';
    interfaceConfig.PROVIDER_NAME = 'Kelasor';

    // Hide all Jitsi branding
    interfaceConfig.SHOW_JITSI_WATERMARK = false;
    interfaceConfig.SHOW_BRAND_WATERMARK = false;
    interfaceConfig.SHOW_WATERMARK_FOR_GUESTS = false;
    interfaceConfig.SHOW_POWERED_BY = false;
    interfaceConfig.SHOW_CHROME_EXTENSION_BANNER = false;

    // Links
    interfaceConfig.JITSI_WATERMARK_LINK = 'https://online.kelasorapp.ir';
    interfaceConfig.BRAND_WATERMARK_LINK = 'https://online.kelasorapp.ir';
    interfaceConfig.SUPPORT_URL = 'https://kelasorapp.ir';

    // Disable language auto-detection — always Persian
    interfaceConfig.LANG_DETECTION = false;

    // Disable mobile app promo
    interfaceConfig.MOBILE_APP_PROMO = false;

    // UI tweaks
    interfaceConfig.DEFAULT_BACKGROUND = '#0F0B1E';
    interfaceConfig.HIDE_INVITE_MORE_HEADER = true;
    interfaceConfig.GENERATE_ROOMNAMES_ON_WELCOME_PAGE = false;
    interfaceConfig.DISPLAY_WELCOME_FOOTER = false;
    interfaceConfig.DISPLAY_WELCOME_PAGE_CONTENT = false;
    interfaceConfig.DISPLAY_WELCOME_PAGE_ADDITIONAL_CARD = false;
    interfaceConfig.DISPLAY_WELCOME_PAGE_TOOLBAR_ADDITIONAL_CONTENT = false;

    // Filmstrip
    interfaceConfig.VERTICAL_FILMSTRIP = true;
    interfaceConfig.FILM_STRIP_MAX_HEIGHT = 150;
}
