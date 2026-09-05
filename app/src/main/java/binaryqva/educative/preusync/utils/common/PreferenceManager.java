/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: PreferenceManager.java
 * Versión: v2.1.9
 * Descripción: Gestor centralizado de persistencia local mediante SharedPreferences.
 *              Implementa almacenamiento cifrado para datos sensibles.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.utils.common;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;

import binaryqva.educative.preusync.R;
import binaryqva.educative.preusync.debug.AppLogger;

/**
 * Proporciona métodos seguros y tipados para acceder a las preferencias del usuario.
 * Divide los datos en múltiples archivos físicos según su nivel de sensibilidad.
 * 
 * 🔒 CUENTA: Tokens e IDs se cifran vía Android Keystore (API 23+) mediante SecurePreferencesHelper.
 * 🧂 CACHÉ: El salt para derivación de claves se almacena en el archivo de ajustes generales.
 */
public final class PreferenceManager {

    private static final String TAG = "PreferenceManager";
    private static PreferenceManager instance;
    private final Context appContext;

    // Archivos físicos de preferencias (.xml)
    private final SharedPreferences settingsPrefs;      // Ajustes generales (UI, comportamiento).
    private final SecurePreferencesHelper secureAccountPrefs; // Datos de cuenta CIFRADOS.
    private final SharedPreferences backgroundPrefs;    // Frecuencias de sincronización.
    private final SharedPreferences schedulePrefs;      // Datos del horario local.

    public static final String KEY_CACHE_SENSITIVE_SALT = "cache_sensitive_salt";

    private PreferenceManager(Context context) {
        this.appContext = context.getApplicationContext();
        
        settingsPrefs = appContext.getSharedPreferences(PreferenceConstants.FILE_SETTINGS, Context.MODE_PRIVATE);
        
        // Inicialización del wrapper de cifrado para el archivo de cuenta.
        secureAccountPrefs = new SecurePreferencesHelper(appContext, PreferenceConstants.FILE_ACCOUNT);
        
        backgroundPrefs = appContext.getSharedPreferences(PreferenceConstants.FILE_BACKGROUND, Context.MODE_PRIVATE);
        schedulePrefs = appContext.getSharedPreferences(PreferenceConstants.FILE_SCHEDULE, Context.MODE_PRIVATE);
    }

    public static synchronized PreferenceManager getInstance(Context context) {
        if (instance == null) {
            instance = new PreferenceManager(context);
        }
        return instance;
    }

    // ==================== GENERIC ====================
    public void clearAll() {
        settingsPrefs.edit().clear().apply();
        secureAccountPrefs.clear();
        backgroundPrefs.edit().clear().apply();
        schedulePrefs.edit().clear().apply();
    }

    // ==================== SETTINGS (SharedPreferences normal) ====================
    public boolean isDynamicColorsEnabled() {
        return settingsPrefs.getBoolean(PreferenceConstants.KEY_DYNAMIC_COLORS, true);
    }

    public void setDynamicColorsEnabled(boolean enabled) {
        settingsPrefs.edit().putBoolean(PreferenceConstants.KEY_DYNAMIC_COLORS, enabled).apply();
    }

    @Nullable
    public String getLanguage() {
        return settingsPrefs.getString(PreferenceConstants.KEY_LANGUAGE, null);
    }

    public void setLanguage(@Nullable String languageCode) {
        settingsPrefs.edit().putString(PreferenceConstants.KEY_LANGUAGE, languageCode).apply();
    }

    @NonNull
    public String getTheme() {
        return settingsPrefs.getString(PreferenceConstants.KEY_THEME, "theme_default");
    }

    public void setTheme(@NonNull String theme) {
        settingsPrefs.edit().putString(PreferenceConstants.KEY_THEME, theme).apply();
    }

    public boolean isOnboardingCompleted() {
        return settingsPrefs.getBoolean(PreferenceConstants.KEY_ONBOARDING_COMPLETED, false);
    }

    public void setOnboardingCompleted(boolean completed) {
        settingsPrefs.edit().putBoolean(PreferenceConstants.KEY_ONBOARDING_COMPLETED, completed).apply();
    }

    public boolean hasAccount() {
        return settingsPrefs.getBoolean(PreferenceConstants.KEY_HAS_ACCOUNT, false);
    }

    public void setHasAccount(boolean hasAccount) {
        settingsPrefs.edit().putBoolean(PreferenceConstants.KEY_HAS_ACCOUNT, hasAccount).apply();
    }

    public int getAppOpenCount() {
        return settingsPrefs.getInt(PreferenceConstants.KEY_APP_OPEN_COUNT, 0);
    }

    public void incrementAppOpenCount() {
        int count = getAppOpenCount() + 1;
        settingsPrefs.edit().putInt(PreferenceConstants.KEY_APP_OPEN_COUNT, count).apply();
    }

    public int getLoginSessionCount() {
        return settingsPrefs.getInt(PreferenceConstants.KEY_LOGIN_SESSION_COUNT, 0);
    }

    public void incrementLoginSessionCount() {
        int count = getLoginSessionCount() + 1;
        settingsPrefs.edit().putInt(PreferenceConstants.KEY_LOGIN_SESSION_COUNT, count).apply();
    }

    public float getFontScale() {
        return settingsPrefs.getFloat(PreferenceConstants.KEY_FONT_SCALE, 1.0f);
    }

    public void setFontScale(float scale) {
        settingsPrefs.edit().putFloat(PreferenceConstants.KEY_FONT_SCALE, scale).apply();
    }

    public String getFontFamily() {
        return settingsPrefs.getString(PreferenceConstants.KEY_FONT_FAMILY, "system");
    }

    public void setFontFamily(String family) {
        settingsPrefs.edit().putString(PreferenceConstants.KEY_FONT_FAMILY, family).apply();
    }

    public boolean isBlurEnabled() {
        return settingsPrefs.getBoolean(PreferenceConstants.KEY_BLUR_ENABLED, true);
    }

    public void setBlurEnabled(boolean enabled) {
        settingsPrefs.edit().putBoolean(PreferenceConstants.KEY_BLUR_ENABLED, enabled).apply();
    }

    public boolean isShowFeaturedNews() {
        return settingsPrefs.getBoolean(PreferenceConstants.KEY_SHOW_FEATURED_NEWS, true);
    }

    public void setShowFeaturedNews(boolean show) {
        settingsPrefs.edit().putBoolean(PreferenceConstants.KEY_SHOW_FEATURED_NEWS, show).apply();
    }

    public boolean isShowFeaturedPosts() {
        return settingsPrefs.getBoolean(PreferenceConstants.KEY_SHOW_FEATURED_POSTS, true);
    }

    public void setShowFeaturedPosts(boolean show) {
        settingsPrefs.edit().putBoolean(PreferenceConstants.KEY_SHOW_FEATURED_POSTS, show).apply();
    }

    public boolean isShowDailyEphemeris() {
        return settingsPrefs.getBoolean(PreferenceConstants.KEY_SHOW_DAILY_EPHEMERIS, true);
    }

    public void setShowDailyEphemeris(boolean show) {
        settingsPrefs.edit().putBoolean(PreferenceConstants.KEY_SHOW_DAILY_EPHEMERIS, show).apply();
    }

    public boolean isShowCurrentShift() {
        return settingsPrefs.getBoolean(PreferenceConstants.KEY_SHOW_CURRENT_SHIFT, true);
    }

    public void setShowCurrentShift(boolean show) {
        settingsPrefs.edit().putBoolean(PreferenceConstants.KEY_SHOW_CURRENT_SHIFT, show).apply();
    }

    public float getAnimationSpeed() {
        return settingsPrefs.getFloat(PreferenceConstants.KEY_ANIMATION_SPEED, 1.0f);
    }

    public void setAnimationSpeed(float speed) {
        settingsPrefs.edit().putFloat(PreferenceConstants.KEY_ANIMATION_SPEED, speed).apply();
    }

    public int getDefaultStartTab() {
        return settingsPrefs.getInt(PreferenceConstants.KEY_DEFAULT_START_TAB, 0);
    }

    public void setDefaultStartTab(int tabIndex) {
        settingsPrefs.edit().putInt(PreferenceConstants.KEY_DEFAULT_START_TAB, tabIndex).apply();
    }

    public boolean isConfirmExit() {
        return settingsPrefs.getBoolean(PreferenceConstants.KEY_CONFIRM_EXIT, false);
    }

    public void setConfirmExit(boolean confirm) {
        settingsPrefs.edit().putBoolean(PreferenceConstants.KEY_CONFIRM_EXIT, confirm).apply();
    }

    public boolean isDataSaverMode() {
        return settingsPrefs.getBoolean(PreferenceConstants.KEY_DATA_SAVER_MODE, false);
    }

    public void setDataSaverMode(boolean enabled) {
        settingsPrefs.edit().putBoolean(PreferenceConstants.KEY_DATA_SAVER_MODE, enabled).apply();
    }

    // ==================== MÉTODOS PARA EL SALT DE CACHÉ SENSIBLE ====================
    /**
     * Obtiene el salt almacenado para la caché sensible.
     * Si no existe, devuelve null (SensitiveDataCipher lo generará).
     */
    @Nullable
    public String getSensitiveCacheSalt() {
        return settingsPrefs.getString(KEY_CACHE_SENSITIVE_SALT, null);
    }

    /**
     * Almacena el salt para la caché sensible.
     * Normalmente lo genera SensitiveDataCipher en la primera ejecución.
     */
    public void setSensitiveCacheSalt(@Nullable String salt) {
        settingsPrefs.edit().putString(KEY_CACHE_SENSITIVE_SALT, salt).apply();
    }

    // ==================== GESTIÓN DE CUENTA (Cifrado) ====================
    
    // --- Supabase Auth ---
    /**
     * Recupera el token de acceso (JWT) cifrado desde el almacenamiento seguro.
     */
    @Nullable
    public String getSupabaseAccessToken() {
        return secureAccountPrefs.getString(PreferenceConstants.KEY_SUPABASE_ACCESS_TOKEN, null);
    }

    /**
     * Almacena de forma cifrada el token de acceso obtenido del servidor.
     */
    public void setSupabaseAccessToken(@Nullable String token) {
        secureAccountPrefs.putString(PreferenceConstants.KEY_SUPABASE_ACCESS_TOKEN, token);
    }

    @Nullable
    public String getSupabaseRefreshToken() {
        return secureAccountPrefs.getString(PreferenceConstants.KEY_SUPABASE_REFRESH_TOKEN, null);
    }

    public void setSupabaseRefreshToken(@Nullable String token) {
        secureAccountPrefs.putString(PreferenceConstants.KEY_SUPABASE_REFRESH_TOKEN, token);
    }

    @Nullable
    public String getSupabaseUserId() {
        return secureAccountPrefs.getString(PreferenceConstants.KEY_SUPABASE_USER_ID, null);
    }

    public void setSupabaseUserId(@Nullable String userId) {
        secureAccountPrefs.putString(PreferenceConstants.KEY_SUPABASE_USER_ID, userId);
    }

    @Nullable
    public String getSchoolId() {
        return secureAccountPrefs.getString(PreferenceConstants.KEY_SCHOOL_ID, null);
    }

    public void setSchoolId(@Nullable String schoolId) {
        secureAccountPrefs.putString(PreferenceConstants.KEY_SCHOOL_ID, schoolId);
    }

    // --- Métodos heredados (deprecated) ---
    @Deprecated
    @Nullable
    public String getSessionToken() {
        return getSupabaseAccessToken();
    }

    @Deprecated
    public void setSessionToken(@Nullable String token) {
        setSupabaseAccessToken(token);
    }

    @Deprecated
    @Nullable
    public String getUserId() {
        return getSupabaseUserId();
    }

    @Deprecated
    public void setUserId(@Nullable String userId) {
        setSupabaseUserId(userId);
    }

    @Deprecated
    @Nullable
    public String getSessionId() {
        return getSupabaseUserId();
    }

    @Deprecated
    public void setSessionId(@Nullable String sessionId) {
        setSupabaseUserId(sessionId);
    }

    // --- Datos de perfil ---
    @Nullable
    public String getUsername() {
        return secureAccountPrefs.getString(PreferenceConstants.KEY_USERNAME, null);
    }

    public void setUsername(@Nullable String username) {
        secureAccountPrefs.putString(PreferenceConstants.KEY_USERNAME, username);
    }

    @Nullable
    public String getUserGroup() {
        return secureAccountPrefs.getString(PreferenceConstants.KEY_USER_GROUP, null);
    }

    public void setUserGroup(@Nullable String group) {
        secureAccountPrefs.putString(PreferenceConstants.KEY_USER_GROUP, group);
    }

    /**
     * Borra permanentemente los datos cifrados de la cuenta actual.
     */
    public void clearAccount() {
        secureAccountPrefs.clear();
        setHasAccount(false);
    }

    // ==================== UTILIDADES DE RED ====================
    /**
     * Construye un mapa de cabeceras incluyendo el token de autorización si existe.
     */
    public HashMap<String, Object> getAuthHeaders() {
        HashMap<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        String token = getSupabaseAccessToken();
        if (token != null && !token.isEmpty()) {
            headers.put("Authorization", "Bearer " + token);
        }
        return headers;
    }

    /**
     * Verifica rápidamente si hay un token almacenado.
     */
    public boolean isAuthenticated() {
        String token = getSupabaseAccessToken();
        return token != null && !token.isEmpty();
    }

    // ==================== BACKGROUND SYNC (SharedPreferences normal) ====================
    public long getSyncFreqNews() {
        return backgroundPrefs.getLong(PreferenceConstants.KEY_SYNC_FREQ_NEWS, 60);
    }

    public void setSyncFreqNews(long minutes) {
        backgroundPrefs.edit().putLong(PreferenceConstants.KEY_SYNC_FREQ_NEWS, minutes).apply();
    }

    public long getSyncFreqPosts() {
        return backgroundPrefs.getLong(PreferenceConstants.KEY_SYNC_FREQ_POSTS, 60);
    }

    public void setSyncFreqPosts(long minutes) {
        backgroundPrefs.edit().putLong(PreferenceConstants.KEY_SYNC_FREQ_POSTS, minutes).apply();
    }

    public long getSyncFreqSchoolEvents() {
        return backgroundPrefs.getLong(PreferenceConstants.KEY_SYNC_FREQ_SCHOOL_EVENTS, 60);
    }

    public void setSyncFreqSchoolEvents(long minutes) {
        backgroundPrefs.edit().putLong(PreferenceConstants.KEY_SYNC_FREQ_SCHOOL_EVENTS, minutes).apply();
    }

    public long getSyncFreqExternalEvents() {
        return backgroundPrefs.getLong(PreferenceConstants.KEY_SYNC_FREQ_EXTERNAL_EVENTS, 60);
    }

    public void setSyncFreqExternalEvents(long minutes) {
        backgroundPrefs.edit().putLong(PreferenceConstants.KEY_SYNC_FREQ_EXTERNAL_EVENTS, minutes).apply();
    }

    public long getSyncFreqEphemerides() {
        return backgroundPrefs.getLong(PreferenceConstants.KEY_SYNC_FREQ_EPHEMERIDES, 1440);
    }

    public void setSyncFreqEphemerides(long minutes) {
        backgroundPrefs.edit().putLong(PreferenceConstants.KEY_SYNC_FREQ_EPHEMERIDES, minutes).apply();
    }

    public long getSyncFreqSchedule() {
        return backgroundPrefs.getLong(PreferenceConstants.KEY_SYNC_FREQ_SCHEDULE, 30);
    }

    public void setSyncFreqSchedule(long minutes) {
        backgroundPrefs.edit().putLong(PreferenceConstants.KEY_SYNC_FREQ_SCHEDULE, minutes).apply();
    }

    public boolean isBgSyncEnabled() {
        return backgroundPrefs.getBoolean(PreferenceConstants.KEY_BG_SYNC_ENABLED, true);
    }

    public void setBgSyncEnabled(boolean enabled) {
        backgroundPrefs.edit().putBoolean(PreferenceConstants.KEY_BG_SYNC_ENABLED, enabled).apply();
    }

    public boolean isWifiOnlyBackground() {
        return backgroundPrefs.getBoolean(PreferenceConstants.KEY_WIFI_ONLY_BACKGROUND, false);
    }

    public void setWifiOnlyBackground(boolean wifiOnly) {
        backgroundPrefs.edit().putBoolean(PreferenceConstants.KEY_WIFI_ONLY_BACKGROUND, wifiOnly).apply();
    }

    // ==================== NOTIFICATIONS ENABLED ====================
    public boolean isNotifNewsEnabled() {
        return backgroundPrefs.getBoolean(PreferenceConstants.KEY_NOTIF_NEWS_ENABLED, true);
    }

    public void setNotifNewsEnabled(boolean enabled) {
        backgroundPrefs.edit().putBoolean(PreferenceConstants.KEY_NOTIF_NEWS_ENABLED, enabled).apply();
    }

    public boolean isNotifPostsEnabled() {
        return backgroundPrefs.getBoolean(PreferenceConstants.KEY_NOTIF_POSTS_ENABLED, true);
    }

    public void setNotifPostsEnabled(boolean enabled) {
        backgroundPrefs.edit().putBoolean(PreferenceConstants.KEY_NOTIF_POSTS_ENABLED, enabled).apply();
    }

    public boolean isNotifSchoolEventsEnabled() {
        return backgroundPrefs.getBoolean(PreferenceConstants.KEY_NOTIF_SCHOOL_EVENTS_ENABLED, true);
    }

    public void setNotifSchoolEventsEnabled(boolean enabled) {
        backgroundPrefs.edit().putBoolean(PreferenceConstants.KEY_NOTIF_SCHOOL_EVENTS_ENABLED, enabled).apply();
    }

    public boolean isNotifExternalEventsEnabled() {
        return backgroundPrefs.getBoolean(PreferenceConstants.KEY_NOTIF_EXTERNAL_EVENTS_ENABLED, true);
    }

    public void setNotifExternalEventsEnabled(boolean enabled) {
        backgroundPrefs.edit().putBoolean(PreferenceConstants.KEY_NOTIF_EXTERNAL_EVENTS_ENABLED, enabled).apply();
    }

    public boolean isNotifEphemeridesEnabled() {
        return backgroundPrefs.getBoolean(PreferenceConstants.KEY_NOTIF_EPHEMERIDES_ENABLED, true);
    }

    public void setNotifEphemeridesEnabled(boolean enabled) {
        backgroundPrefs.edit().putBoolean(PreferenceConstants.KEY_NOTIF_EPHEMERIDES_ENABLED, enabled).apply();
    }

    public boolean isNotifScheduleEnabled() {
        return backgroundPrefs.getBoolean(PreferenceConstants.KEY_NOTIF_SCHEDULE_ENABLED, true);
    }

    public void setNotifScheduleEnabled(boolean enabled) {
        backgroundPrefs.edit().putBoolean(PreferenceConstants.KEY_NOTIF_SCHEDULE_ENABLED, enabled).apply();
    }

    // ==================== NOTIFICATION SOUNDS ====================
    public String getNotifSoundNews() {
        return backgroundPrefs.getString(PreferenceConstants.KEY_NOTIF_SOUND_NEWS, "default");
    }

    public void setNotifSoundNews(String soundUri) {
        backgroundPrefs.edit().putString(PreferenceConstants.KEY_NOTIF_SOUND_NEWS, soundUri).apply();
    }

    public String getNotifSoundPosts() {
        return backgroundPrefs.getString(PreferenceConstants.KEY_NOTIF_SOUND_POSTS, "default");
    }

    public void setNotifSoundPosts(String soundUri) {
        backgroundPrefs.edit().putString(PreferenceConstants.KEY_NOTIF_SOUND_POSTS, soundUri).apply();
    }

    public String getNotifSoundSchoolEvents() {
        return backgroundPrefs.getString(PreferenceConstants.KEY_NOTIF_SOUND_SCHOOL_EVENTS, "default");
    }

    public void setNotifSoundSchoolEvents(String soundUri) {
        backgroundPrefs.edit().putString(PreferenceConstants.KEY_NOTIF_SOUND_SCHOOL_EVENTS, soundUri).apply();
    }

    public String getNotifSoundExternalEvents() {
        return backgroundPrefs.getString(PreferenceConstants.KEY_NOTIF_SOUND_EXTERNAL_EVENTS, "default");
    }

    public void setNotifSoundExternalEvents(String soundUri) {
        backgroundPrefs.edit().putString(PreferenceConstants.KEY_NOTIF_SOUND_EXTERNAL_EVENTS, soundUri).apply();
    }

    public String getNotifSoundEphemerides() {
        return backgroundPrefs.getString(PreferenceConstants.KEY_NOTIF_SOUND_EPHEMERIDES, "default");
    }

    public void setNotifSoundEphemerides(String soundUri) {
        backgroundPrefs.edit().putString(PreferenceConstants.KEY_NOTIF_SOUND_EPHEMERIDES, soundUri).apply();
    }

    public String getNotifSoundSchedule() {
        return backgroundPrefs.getString(PreferenceConstants.KEY_NOTIF_SOUND_SCHEDULE, "default");
    }

    public void setNotifSoundSchedule(String soundUri) {
        backgroundPrefs.edit().putString(PreferenceConstants.KEY_NOTIF_SOUND_SCHEDULE, soundUri).apply();
    }

    // ==================== REMINDERS ====================
    public boolean isReminderEnabled() {
        return backgroundPrefs.getBoolean(PreferenceConstants.KEY_REMINDER_ENABLED, false);
    }

    public void setReminderEnabled(boolean enabled) {
        backgroundPrefs.edit().putBoolean(PreferenceConstants.KEY_REMINDER_ENABLED, enabled).apply();
    }

    public int getReminderLeadTimeHours() {
        return backgroundPrefs.getInt(PreferenceConstants.KEY_REMINDER_LEAD_TIME_HOURS, 24);
    }

    public void setReminderLeadTimeHours(int hours) {
        backgroundPrefs.edit().putInt(PreferenceConstants.KEY_REMINDER_LEAD_TIME_HOURS, hours).apply();
    }

    public String getReminderSound() {
        return backgroundPrefs.getString(PreferenceConstants.KEY_REMINDER_SOUND, "default");
    }

    public void setReminderSound(String soundUri) {
        backgroundPrefs.edit().putString(PreferenceConstants.KEY_REMINDER_SOUND, soundUri).apply();
    }

    // ==================== AUTO UPDATES ====================
    public boolean isAutoUpdatesEnabled() {
        return backgroundPrefs.getBoolean(PreferenceConstants.KEY_AUTO_UPDATES_ENABLED, true);
    }

    public void setAutoUpdatesEnabled(boolean enabled) {
        backgroundPrefs.edit().putBoolean(PreferenceConstants.KEY_AUTO_UPDATES_ENABLED, enabled).apply();
    }

    public int getAutoUpdatesFreqDays() {
        return backgroundPrefs.getInt(PreferenceConstants.KEY_AUTO_UPDATES_FREQ_DAYS, 7);
    }

    public void setAutoUpdatesFreqDays(int days) {
        backgroundPrefs.edit().putInt(PreferenceConstants.KEY_AUTO_UPDATES_FREQ_DAYS, days).apply();
    }

    public boolean isAutoUpdatesWifiOnly() {
        return backgroundPrefs.getBoolean(PreferenceConstants.KEY_AUTO_UPDATES_WIFI_ONLY, true);
    }

    public void setAutoUpdatesWifiOnly(boolean wifiOnly) {
        backgroundPrefs.edit().putBoolean(PreferenceConstants.KEY_AUTO_UPDATES_WIFI_ONLY, wifiOnly).apply();
    }

    public String getAutoUpdatesSound() {
        return backgroundPrefs.getString(PreferenceConstants.KEY_AUTO_UPDATES_SOUND, "default");
    }

    public void setAutoUpdatesSound(String soundUri) {
        backgroundPrefs.edit().putString(PreferenceConstants.KEY_AUTO_UPDATES_SOUND, soundUri).apply();
    }

    // ==================== POST VOTES ====================
    public SharedPreferences getPostVotesPrefs(String sessionId) {
        String fileName = PreferenceConstants.FILE_POST_VOTES_PREFIX + sessionId;
        return appContext.getSharedPreferences(fileName, Context.MODE_PRIVATE);
    }

    // ==================== BACKGROUND SERVICE (legacy) ====================
    public long getBackgroundIntervalMinutes() {
        return backgroundPrefs.getLong(PreferenceConstants.KEY_INTERVAL_MINUTES, 30L);
    }

    public void setBackgroundIntervalMinutes(long minutes) {
        backgroundPrefs.edit().putLong(PreferenceConstants.KEY_INTERVAL_MINUTES, minutes).apply();
    }

    public long getLastNewsTimestamp() {
        return backgroundPrefs.getLong(PreferenceConstants.KEY_LAST_NEWS, 0L);
    }

    public void setLastNewsTimestamp(long timestamp) {
        backgroundPrefs.edit().putLong(PreferenceConstants.KEY_LAST_NEWS, timestamp).apply();
    }

    public long getLastPostsTimestamp() {
        return backgroundPrefs.getLong(PreferenceConstants.KEY_LAST_POSTS, 0L);
    }

    public void setLastPostsTimestamp(long timestamp) {
        backgroundPrefs.edit().putLong(PreferenceConstants.KEY_LAST_POSTS, timestamp).apply();
    }

    public long getLastEventsTimestamp() {
        return backgroundPrefs.getLong(PreferenceConstants.KEY_LAST_EVENTS, 0L);
    }

    public void setLastEventsTimestamp(long timestamp) {
        backgroundPrefs.edit().putLong(PreferenceConstants.KEY_LAST_EVENTS, timestamp).apply();
    }

    public long getLastEphemeridesTimestamp() {
        return backgroundPrefs.getLong(PreferenceConstants.KEY_LAST_EPHEMERIDES, 0L);
    }

    public void setLastEphemeridesTimestamp(long timestamp) {
        backgroundPrefs.edit().putLong(PreferenceConstants.KEY_LAST_EPHEMERIDES, timestamp).apply();
    }

    public long getLastScheduleTimestamp() {
        return backgroundPrefs.getLong(PreferenceConstants.KEY_LAST_SCHEDULE, 0L);
    }

    public void setLastScheduleTimestamp(long timestamp) {
        backgroundPrefs.edit().putLong(PreferenceConstants.KEY_LAST_SCHEDULE, timestamp).apply();
    }

    @Nullable
    public String getNotifiedIdsNews() {
        return backgroundPrefs.getString(PreferenceConstants.KEY_NOTIFIED_IDS_NEWS, null);
    }

    public void setNotifiedIdsNews(@Nullable String jsonArray) {
        backgroundPrefs.edit().putString(PreferenceConstants.KEY_NOTIFIED_IDS_NEWS, jsonArray).apply();
    }

    @Nullable
    public String getNotifiedIdsPosts() {
        return backgroundPrefs.getString(PreferenceConstants.KEY_NOTIFIED_IDS_POSTS, null);
    }

    public void setNotifiedIdsPosts(@Nullable String jsonArray) {
        backgroundPrefs.edit().putString(PreferenceConstants.KEY_NOTIFIED_IDS_POSTS, jsonArray).apply();
    }

    @Nullable
    public String getNotifiedIdsEvents() {
        return backgroundPrefs.getString(PreferenceConstants.KEY_NOTIFIED_IDS_EVENTS, null);
    }

    public void setNotifiedIdsEvents(@Nullable String jsonArray) {
        backgroundPrefs.edit().putString(PreferenceConstants.KEY_NOTIFIED_IDS_EVENTS, jsonArray).apply();
    }

    @Nullable
    public String getLastEphemerisDate() {
        return backgroundPrefs.getString(PreferenceConstants.KEY_LAST_EPHEMERIS_DATE, null);
    }

    public void setLastEphemerisDate(@Nullable String date) {
        backgroundPrefs.edit().putString(PreferenceConstants.KEY_LAST_EPHEMERIS_DATE, date).apply();
    }

    @Nullable
    public String getScheduleHash() {
        return backgroundPrefs.getString(PreferenceConstants.KEY_SCHEDULE_HASH, null);
    }

    public void setScheduleHash(@Nullable String hash) {
        backgroundPrefs.edit().putString(PreferenceConstants.KEY_SCHEDULE_HASH, hash).apply();
    }

    // ==================== SCHEDULE ====================
    @Nullable
    public String getLastScheduleGroup() {
        return schedulePrefs.getString(PreferenceConstants.KEY_LAST_SCHEDULE_GROUP, null);
    }

    public void setLastScheduleGroup(@Nullable String group) {
        schedulePrefs.edit().putString(PreferenceConstants.KEY_LAST_SCHEDULE_GROUP, group).apply();
    }

    // ==================== IMAGE LOAD SIZE ====================
    public int getImageLoadSize() {
        if (isDataSaverMode()) {
            return 200;
        } else {
            return 1024;
        }
    }

    // ==================== INICIALIZACIÓN DE VALORES POR DEFECTO ====================
    /**
     * Establece los valores iniciales de la aplicación en la primera ejecución.
     */
    public void initDefaultsIfNeeded() {
        if (settingsPrefs.getBoolean("defaults_initialized", false)) {
            return;
        }

        // --- AJUSTES (prefs_settings) ---
        if (!settingsPrefs.contains(PreferenceConstants.KEY_DYNAMIC_COLORS)) setDynamicColorsEnabled(true);
        if (!settingsPrefs.contains(PreferenceConstants.KEY_LANGUAGE)) setLanguage(null);
        if (!settingsPrefs.contains(PreferenceConstants.KEY_THEME)) setTheme("theme_default");
        if (!settingsPrefs.contains(PreferenceConstants.KEY_ONBOARDING_COMPLETED)) setOnboardingCompleted(false);
        if (!settingsPrefs.contains(PreferenceConstants.KEY_HAS_ACCOUNT)) setHasAccount(false);
        if (!settingsPrefs.contains(PreferenceConstants.KEY_APP_OPEN_COUNT)) settingsPrefs.edit().putInt(PreferenceConstants.KEY_APP_OPEN_COUNT, 0).apply();
        if (!settingsPrefs.contains(PreferenceConstants.KEY_LOGIN_SESSION_COUNT)) settingsPrefs.edit().putInt(PreferenceConstants.KEY_LOGIN_SESSION_COUNT, 0).apply();
        if (!settingsPrefs.contains(PreferenceConstants.KEY_FONT_SCALE)) setFontScale(1.0f);
        if (!settingsPrefs.contains(PreferenceConstants.KEY_FONT_FAMILY)) setFontFamily("system");
        if (!settingsPrefs.contains(PreferenceConstants.KEY_BLUR_ENABLED)) setBlurEnabled(true);
        if (!settingsPrefs.contains(PreferenceConstants.KEY_SHOW_FEATURED_NEWS)) setShowFeaturedNews(true);
        if (!settingsPrefs.contains(PreferenceConstants.KEY_SHOW_FEATURED_POSTS)) setShowFeaturedPosts(true);
        if (!settingsPrefs.contains(PreferenceConstants.KEY_SHOW_DAILY_EPHEMERIS)) setShowDailyEphemeris(true);
        if (!settingsPrefs.contains(PreferenceConstants.KEY_SHOW_CURRENT_SHIFT)) setShowCurrentShift(true);
        if (!settingsPrefs.contains(PreferenceConstants.KEY_ANIMATION_SPEED)) setAnimationSpeed(1.0f);
        if (!settingsPrefs.contains(PreferenceConstants.KEY_DEFAULT_START_TAB)) setDefaultStartTab(0);
        if (!settingsPrefs.contains(PreferenceConstants.KEY_CONFIRM_EXIT)) setConfirmExit(false);
        if (!settingsPrefs.contains(PreferenceConstants.KEY_DATA_SAVER_MODE)) setDataSaverMode(false);

        // --- SINCRONIZACIÓN (prefs_background) ---
        if (!backgroundPrefs.contains(PreferenceConstants.KEY_BG_SYNC_ENABLED)) setBgSyncEnabled(true);
        if (!backgroundPrefs.contains(PreferenceConstants.KEY_WIFI_ONLY_BACKGROUND)) setWifiOnlyBackground(false);
        if (!backgroundPrefs.contains(PreferenceConstants.KEY_SYNC_FREQ_NEWS)) setSyncFreqNews(60);
        if (!backgroundPrefs.contains(PreferenceConstants.KEY_SYNC_FREQ_POSTS)) setSyncFreqPosts(60);
        if (!backgroundPrefs.contains(PreferenceConstants.KEY_SYNC_FREQ_SCHOOL_EVENTS)) setSyncFreqSchoolEvents(60);
        if (!backgroundPrefs.contains(PreferenceConstants.KEY_SYNC_FREQ_EXTERNAL_EVENTS)) setSyncFreqExternalEvents(60);
        if (!backgroundPrefs.contains(PreferenceConstants.KEY_SYNC_FREQ_EPHEMERIDES)) setSyncFreqEphemerides(1440);
        if (!backgroundPrefs.contains(PreferenceConstants.KEY_SYNC_FREQ_SCHEDULE)) setSyncFreqSchedule(30);
        if (!backgroundPrefs.contains(PreferenceConstants.KEY_NOTIF_NEWS_ENABLED)) setNotifNewsEnabled(true);
        if (!backgroundPrefs.contains(PreferenceConstants.KEY_NOTIF_POSTS_ENABLED)) setNotifPostsEnabled(true);
        if (!backgroundPrefs.contains(PreferenceConstants.KEY_NOTIF_SCHOOL_EVENTS_ENABLED)) setNotifSchoolEventsEnabled(true);
        if (!backgroundPrefs.contains(PreferenceConstants.KEY_NOTIF_EXTERNAL_EVENTS_ENABLED)) setNotifExternalEventsEnabled(true);
        if (!backgroundPrefs.contains(PreferenceConstants.KEY_NOTIF_EPHEMERIDES_ENABLED)) setNotifEphemeridesEnabled(true);
        if (!backgroundPrefs.contains(PreferenceConstants.KEY_NOTIF_SCHEDULE_ENABLED)) setNotifScheduleEnabled(true);
        
        // Asignación de tonos de notificación por defecto desde recursos internos.
        if (!backgroundPrefs.contains(PreferenceConstants.KEY_NOTIF_SOUND_NEWS)) setNotifSoundNews("android.resource://" + appContext.getPackageName() + "/" + R.raw.tono_1);
        if (!backgroundPrefs.contains(PreferenceConstants.KEY_NOTIF_SOUND_POSTS)) setNotifSoundPosts("android.resource://" + appContext.getPackageName() + "/" + R.raw.tono_3);
        if (!backgroundPrefs.contains(PreferenceConstants.KEY_NOTIF_SOUND_SCHOOL_EVENTS)) setNotifSoundSchoolEvents("android.resource://" + appContext.getPackageName() + "/" + R.raw.tono_2);
        if (!backgroundPrefs.contains(PreferenceConstants.KEY_NOTIF_SOUND_EXTERNAL_EVENTS)) setNotifSoundExternalEvents("android.resource://" + appContext.getPackageName() + "/" + R.raw.tono_4);
        
        if (!backgroundPrefs.contains(PreferenceConstants.KEY_REMINDER_ENABLED)) setReminderEnabled(false);
        if (!backgroundPrefs.contains(PreferenceConstants.KEY_REMINDER_LEAD_TIME_HOURS)) setReminderLeadTimeHours(24);
        if (!backgroundPrefs.contains(PreferenceConstants.KEY_AUTO_UPDATES_ENABLED)) setAutoUpdatesEnabled(true);

        // --- HORARIO (prefs_schedule) ---
        if (!schedulePrefs.contains(PreferenceConstants.KEY_LAST_SCHEDULE_GROUP)) schedulePrefs.edit().putString(PreferenceConstants.KEY_LAST_SCHEDULE_GROUP, null).apply();

        // Marcamos la inicialización como completada.
        settingsPrefs.edit().putBoolean("defaults_initialized", true).apply();
    }
}

