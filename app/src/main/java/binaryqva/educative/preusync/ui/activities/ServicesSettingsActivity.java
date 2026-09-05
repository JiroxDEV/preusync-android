/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: ServicesSettingsActivity.java
 * Versión: v1.9.9
 * Descripción: Configuración de servicios en segundo plano y notificaciones.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.ui.activities;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import binaryqva.educative.preusync.R;
import binaryqva.educative.preusync.utils.common.PreferenceManager;
import binaryqva.educative.preusync.utils.scheduling.BackgroundServiceScheduler;
import binaryqva.educative.preusync.utils.settings.HeaderItem;
import binaryqva.educative.preusync.utils.settings.SelectItem;
import binaryqva.educative.preusync.utils.settings.SettingsItem;
import binaryqva.educative.preusync.utils.settings.SwitchItem;

/**
 * Configura los procesos que se ejecutan de forma invisible para el usuario.
 * Permite optimizar el consumo de batería y la inmediatez de las noticias.
 */
public class ServicesSettingsActivity extends BaseSettingsActivity {

    private PreferenceManager prefs;
    private MediaPlayer previewMediaPlayer;
    private SwitchItem batteryOptimizationSwitchItem;
    private static final int REQUEST_IGNORE_BATTERY_OPTIMIZATION = 2297;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefs = PreferenceManager.getInstance(getApplicationContext());
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (previewMediaPlayer != null) {
            previewMediaPlayer.release();
            previewMediaPlayer = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Sincronización del estado de batería al regresar de los ajustes del sistema.
        if (batteryOptimizationSwitchItem != null) {
            boolean isGranted = isIgnoringBatteryOptimizations();
            if (batteryOptimizationSwitchItem.currentValue != isGranted) {
                batteryOptimizationSwitchItem.setCurrentValue(isGranted);
                refreshItem(batteryOptimizationSwitchItem);
            }
        }
    }

    @Override
    protected int getTitleResId() {
        return R.string.title_services;
    }

    /**
     * Construye las secciones de configuración de servicios core.
     */
    @Override
    protected List<SettingsItem> buildItems() {
        List<SettingsItem> items = new ArrayList<>();

        // SECCIÓN: SINCRONIZACIÓN
        items.add(new HeaderItem(getString(R.string.section_background_sync)));
        items.add(buildBgSyncSwitchItem());
        items.add(buildWifiOnlyBackgroundSwitchItem());

        // SECCIÓN: FRECUENCIAS INDIVIDUALES
        items.add(new HeaderItem(getString(R.string.section_sync_frequencies)));
        addSyncFreqItems(items);

        // SECCIÓN: NOTIFICACIONES POR CATEGORÍA
        items.add(new HeaderItem(getString(R.string.section_notifications)));
        addNotificationSwitches(items);
        addNotificationSoundSelectors(items);

        // SECCIÓN: RECORDATORIOS DE EVENTOS
        items.add(new HeaderItem(getString(R.string.section_reminders)));
        items.add(buildReminderSwitchItem());
        items.add(buildReminderLeadTimeSelectItem());
        items.add(buildSoundSelectItem(getString(R.string.reminder_sound), prefs.getReminderSound(), prefs::setReminderSound));

        // SECCIÓN: ACTUALIZACIONES AUTOMÁTICAS
        items.add(new HeaderItem(getString(R.string.section_auto_updates)));
        items.add(buildAutoUpdatesSwitchItem());
        items.add(buildAutoUpdatesFreqSelectItem());
        items.add(buildAutoUpdatesWifiOnlySwitchItem());
        items.add(buildSoundSelectItem(getString(R.string.auto_updates_sound), prefs.getAutoUpdatesSound(), prefs::setAutoUpdatesSound));

        // SECCIÓN: OPTIMIZACIÓN DE ENERGÍA
        items.add(new HeaderItem(getString(R.string.section_battery)));
        items.add(buildBatteryOptimizationSwitchItem());

        return items;
    }

    private void addSyncFreqItems(List<SettingsItem> items) {
        items.add(buildSyncFreqSelectItem(getString(R.string.sync_freq_news), prefs.getSyncFreqNews(), prefs::setSyncFreqNews));
        items.add(buildSyncFreqSelectItem(getString(R.string.sync_freq_posts), prefs.getSyncFreqPosts(), prefs::setSyncFreqPosts));
        items.add(buildSyncFreqSelectItem(getString(R.string.sync_freq_school_events), prefs.getSyncFreqSchoolEvents(), prefs::setSyncFreqSchoolEvents));
        items.add(buildSyncFreqSelectItem(getString(R.string.sync_freq_external_events), prefs.getSyncFreqExternalEvents(), prefs::setSyncFreqExternalEvents));
        items.add(buildSyncFreqSelectItem(getString(R.string.sync_freq_ephemerides), prefs.getSyncFreqEphemerides(), prefs::setSyncFreqEphemerides));
        items.add(buildSyncFreqSelectItem(getString(R.string.sync_freq_schedule), prefs.getSyncFreqSchedule(), prefs::setSyncFreqSchedule));
    }

    private void addNotificationSwitches(List<SettingsItem> items) {
        items.add(new SwitchItem(getString(R.string.notif_news), prefs.isNotifNewsEnabled(), prefs::setNotifNewsEnabled));
        items.add(new SwitchItem(getString(R.string.notif_posts), prefs.isNotifPostsEnabled(), prefs::setNotifPostsEnabled));
        items.add(new SwitchItem(getString(R.string.notif_school_events), prefs.isNotifSchoolEventsEnabled(), prefs::setNotifSchoolEventsEnabled));
        items.add(new SwitchItem(getString(R.string.notif_external_events), prefs.isNotifExternalEventsEnabled(), prefs::setNotifExternalEventsEnabled));
        items.add(new SwitchItem(getString(R.string.notif_ephemerides), prefs.isNotifEphemeridesEnabled(), prefs::setNotifEphemeridesEnabled));
        items.add(new SwitchItem(getString(R.string.notif_schedule), prefs.isNotifScheduleEnabled(), prefs::setNotifScheduleEnabled));
    }

    private void addNotificationSoundSelectors(List<SettingsItem> items) {
        String label = getString(R.string.label_sound);
        items.add(buildSoundSelectItem(getString(R.string.notif_news) + " - " + label, prefs.getNotifSoundNews(), prefs::setNotifSoundNews));
        items.add(buildSoundSelectItem(getString(R.string.notif_posts) + " - " + label, prefs.getNotifSoundPosts(), prefs::setNotifSoundPosts));
        items.add(buildSoundSelectItem(getString(R.string.notif_school_events) + " - " + label, prefs.getNotifSoundSchoolEvents(), prefs::setNotifSoundSchoolEvents));
        items.add(buildSoundSelectItem(getString(R.string.notif_external_events) + " - " + label, prefs.getNotifSoundExternalEvents(), prefs::setNotifSoundExternalEvents));
        items.add(buildSoundSelectItem(getString(R.string.notif_ephemerides) + " - " + label, prefs.getNotifSoundEphemerides(), prefs::setNotifSoundEphemerides));
        items.add(buildSoundSelectItem(getString(R.string.notif_schedule) + " - " + label, prefs.getNotifSoundSchedule(), prefs::setNotifSoundSchedule));
    }

    // ==================== MÉTODOS DE CONSTRUCCIÓN ESPECÍFICOS ====================

    private SwitchItem buildBgSyncSwitchItem() {
        return new SwitchItem(getString(R.string.enable_background_sync), prefs.isBgSyncEnabled(), isChecked -> {
            prefs.setBgSyncEnabled(isChecked);
            if (isChecked) BackgroundServiceScheduler.scheduleFirst(this);
            else BackgroundServiceScheduler.cancel(this);
        });
    }

    private SwitchItem buildWifiOnlyBackgroundSwitchItem() {
        return new SwitchItem(getString(R.string.wifi_only_background), prefs.isWifiOnlyBackground(), isChecked -> prefs.setWifiOnlyBackground(isChecked));
    }

    private SelectItem buildSyncFreqSelectItem(String title, long currentMin, java.util.function.Consumer<Long> onSelected) {
        List<SelectItem.SelectOption> options = Arrays.asList(
                new SelectItem.SelectOption("30 minutos", "30"), new SelectItem.SelectOption("1 hora", "60"),
                new SelectItem.SelectOption("2 horas", "120"), new SelectItem.SelectOption("4 horas", "240"),
                new SelectItem.SelectOption("12 horas", "720"), new SelectItem.SelectOption("24 horas", "1440")
        );
        return new SelectItem(title, options, String.valueOf(currentMin), selected -> {
            long newMin = Long.parseLong(selected);
            if (newMin != currentMin) { onSelected.accept(newMin); Toast.makeText(this, R.string.sync_freq_changed, Toast.LENGTH_SHORT).show(); }
        });
    }

    private SelectItem buildSoundSelectItem(String title, String currentUri, java.util.function.Consumer<String> onSelected) {
        List<SelectItem.SelectOption> options = new ArrayList<>();
        options.add(new SelectItem.SelectOption(getString(R.string.sound_default), "default"));
        options.add(new SelectItem.SelectOption(getString(R.string.sound_silence), "silence"));
        options.add(new SelectItem.SelectOption("Alerta", "android.resource://" + getPackageName() + "/" + R.raw.alerta));
        options.add(new SelectItem.SelectOption("Hecho", "android.resource://" + getPackageName() + "/" + R.raw.hecho));
        options.add(new SelectItem.SelectOption("Ondas", "android.resource://" + getPackageName() + "/" + R.raw.tono_2));

        return new SelectItem(title, options, currentUri, onSelected::accept);
    }

    private SwitchItem buildReminderSwitchItem() {
        return new SwitchItem(getString(R.string.enable_reminders), prefs.isReminderEnabled(), isChecked -> prefs.setReminderEnabled(isChecked));
    }

    private SelectItem buildReminderLeadTimeSelectItem() {
        int currentHours = prefs.getReminderLeadTimeHours();
        List<SelectItem.SelectOption> options = Arrays.asList(
                new SelectItem.SelectOption("1 hora", "1"), new SelectItem.SelectOption("12 horas", "12"),
                new SelectItem.SelectOption("1 día", "24"), new SelectItem.SelectOption("2 días", "48")
        );
        return new SelectItem(getString(R.string.reminder_lead_time), options, String.valueOf(currentHours), selected -> {
            int hours = Integer.parseInt(selected);
            if (hours != currentHours) { prefs.setReminderLeadTimeHours(hours); Toast.makeText(this, R.string.reminder_lead_time_changed, Toast.LENGTH_SHORT).show(); }
        });
    }

    private SwitchItem buildAutoUpdatesSwitchItem() {
        return new SwitchItem(getString(R.string.enable_auto_updates), prefs.isAutoUpdatesEnabled(), isChecked -> prefs.setAutoUpdatesEnabled(isChecked));
    }

    private SelectItem buildAutoUpdatesFreqSelectItem() {
        int currentDays = prefs.getAutoUpdatesFreqDays();
        List<SelectItem.SelectOption> options = Arrays.asList(
                new SelectItem.SelectOption("3 días", "3"), new SelectItem.SelectOption("7 días", "7"),
                new SelectItem.SelectOption("15 días", "15"), new SelectItem.SelectOption("30 días", "30")
        );
        return new SelectItem(getString(R.string.auto_updates_freq), options, String.valueOf(currentDays), selected -> {
            int days = Integer.parseInt(selected);
            if (days != currentDays) { prefs.setAutoUpdatesFreqDays(days); Toast.makeText(this, R.string.auto_updates_freq_changed, Toast.LENGTH_SHORT).show(); }
        });
    }

    private SwitchItem buildAutoUpdatesWifiOnlySwitchItem() {
        return new SwitchItem(getString(R.string.auto_updates_wifi_only), prefs.isAutoUpdatesWifiOnly(), isChecked -> prefs.setAutoUpdatesWifiOnly(isChecked));
    }

    // ==================== BATERÍA ====================

    private SwitchItem buildBatteryOptimizationSwitchItem() {
        batteryOptimizationSwitchItem = new SwitchItem(getString(R.string.allow_battery_optimization), isIgnoringBatteryOptimizations(), isChecked -> {
            boolean real = isIgnoringBatteryOptimizations();
            if (batteryOptimizationSwitchItem.currentValue != real) { batteryOptimizationSwitchItem.setCurrentValue(real); refreshItem(batteryOptimizationSwitchItem); }
            if (isChecked && !real) requestIgnoreBatteryOptimization();
            else if (!isChecked && real) { Toast.makeText(this, R.string.toast_disable_battery_optimization_manually, Toast.LENGTH_LONG).show(); openAppBatterySettings(); }
        });
        return batteryOptimizationSwitchItem;
    }

    private boolean isIgnoringBatteryOptimizations() {
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        return pm.isIgnoringBatteryOptimizations(getPackageName());
    }

    private void requestIgnoreBatteryOptimization() {
        startActivityForResult(new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).setData(Uri.parse("package:" + getPackageName())), REQUEST_IGNORE_BATTERY_OPTIMIZATION);
    }

    private void openAppBatterySettings() {
        startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.parse("package:" + getPackageName())));
    }

    public void playPreviewSound(String uriString) {
        stopPreviewSound();
        try {
            Uri uri = uriString.equals("default") ? Settings.System.DEFAULT_NOTIFICATION_URI : Uri.parse(uriString);
            if (uri == null) return;
            previewMediaPlayer = MediaPlayer.create(this, uri);
            if (previewMediaPlayer != null) previewMediaPlayer.start();
        } catch (Exception ignored) {}
    }

    public void stopPreviewSound() {
        if (previewMediaPlayer != null) {
            try { previewMediaPlayer.stop(); previewMediaPlayer.release(); } catch (Exception ignored) {}
            previewMediaPlayer = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IGNORE_BATTERY_OPTIMIZATION && batteryOptimizationSwitchItem != null) {
            boolean isGranted = isIgnoringBatteryOptimizations();
            if (batteryOptimizationSwitchItem.currentValue != isGranted) { batteryOptimizationSwitchItem.setCurrentValue(isGranted); refreshItem(batteryOptimizationSwitchItem); }
        }
    }
}


