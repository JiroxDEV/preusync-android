/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: AppearanceSettingsActivity.java
 * Versión: v1.8.1
 * Descripción: Gestiona ajustes visuales globales de la aplicación: tema, tipografía, tamaño y elementos del Home.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.ui.activities;

import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import binaryqva.educative.preusync.R;
import binaryqva.educative.preusync.utils.common.PreferenceManager;
import binaryqva.educative.preusync.utils.settings.HeaderItem;
import binaryqva.educative.preusync.utils.settings.SelectItem;
import binaryqva.educative.preusync.utils.settings.SettingsItem;
import binaryqva.educative.preusync.utils.settings.SwitchItem;
import binaryqva.educative.preusync.utils.theme.ThemeManager;

/**
 * Gestiona la personalización estética de la aplicación.
 * Permite al usuario ajustar la experiencia visual según sus preferencias.
 */
public class AppearanceSettingsActivity extends BaseSettingsActivity {

    private PreferenceManager prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefs = PreferenceManager.getInstance(getApplicationContext());
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getTitleResId() {
        return R.string.title_appearance;
    }

    /**
     * Define la jerarquía de opciones estéticas organizadas por secciones.
     */
    @Override
    protected List<SettingsItem> buildItems() {
        List<SettingsItem> items = new ArrayList<>();

        // SECCIÓN: TEMA VISUAL
        items.add(new HeaderItem(getString(R.string.section_theme)));
        items.add(buildThemeSelectItem());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            items.add(buildDynamicColorsSwitchItem());
        }

        // SECCIÓN: TIPOGRAFÍA Y TEXTO
        items.add(new HeaderItem(getString(R.string.section_typography)));
        items.add(buildFontSizeSelectItem());
        items.add(buildFontFamilySelectItem());

        // SECCIÓN: PERSONALIZACIÓN DEL HOME
        items.add(new HeaderItem(getString(R.string.section_home_screen)));
        items.add(buildShowFeaturedNewsSwitchItem());
        items.add(buildShowFeaturedPostsSwitchItem());
        items.add(buildShowDailyEphemerisSwitchItem());
        items.add(buildShowCurrentShiftSwitchItem());

        // SECCIÓN: RENDIMIENTO GRÁFICO
        items.add(new HeaderItem(getString(R.string.section_performance)));
        items.add(buildBlurSwitchItem());
        items.add(buildAnimationSpeedSelectItem());

        return items;
    }

    // ==================== GENERACIÓN DE ÍTEMS DE CONFIGURACIÓN ====================

    private SwitchItem buildShowFeaturedNewsSwitchItem() {
        boolean show = prefs.isShowFeaturedNews();
        return new SwitchItem(getString(R.string.show_featured_news), show,
                isChecked -> {
                    // REGLA DE NEGOCIO: Evita que el usuario desactive todos los elementos del Home.
                    if (!isChecked && !atLeastOneHomeElementActive()) {
                        SwitchItem item = (SwitchItem) getItemByTitle(getString(R.string.show_featured_news));
                        if (item != null) item.setCurrentValue(true);
                        Toast.makeText(this, R.string.toast_at_least_one_element_required, Toast.LENGTH_SHORT).show();
                        refreshItem(getItemByTitle(getString(R.string.show_featured_news)));
                        return;
                    }
                    prefs.setShowFeaturedNews(isChecked);
                });
    }

    private SwitchItem buildShowFeaturedPostsSwitchItem() {
        boolean show = prefs.isShowFeaturedPosts();
        return new SwitchItem(getString(R.string.show_featured_posts), show,
                isChecked -> {
                    if (!isChecked && !atLeastOneHomeElementActive()) {
                        SwitchItem item = (SwitchItem) getItemByTitle(getString(R.string.show_featured_posts));
                        if (item != null) item.setCurrentValue(true);
                        Toast.makeText(this, R.string.toast_at_least_one_element_required, Toast.LENGTH_SHORT).show();
                        refreshItem(getItemByTitle(getString(R.string.show_featured_posts)));
                        return;
                    }
                    prefs.setShowFeaturedPosts(isChecked);
                });
    }

    private SwitchItem buildShowDailyEphemerisSwitchItem() {
        boolean show = prefs.isShowDailyEphemeris();
        return new SwitchItem(getString(R.string.show_daily_ephemeris), show,
                isChecked -> {
                    if (!isChecked && !atLeastOneHomeElementActive()) {
                        SwitchItem item = (SwitchItem) getItemByTitle(getString(R.string.show_daily_ephemeris));
                        if (item != null) item.setCurrentValue(true);
                        Toast.makeText(this, R.string.toast_at_least_one_element_required, Toast.LENGTH_SHORT).show();
                        refreshItem(getItemByTitle(getString(R.string.show_daily_ephemeris)));
                        return;
                    }
                    prefs.setShowDailyEphemeris(isChecked);
                });
    }

    private SwitchItem buildShowCurrentShiftSwitchItem() {
        boolean show = prefs.isShowCurrentShift();
        return new SwitchItem(getString(R.string.show_current_shift), show,
                isChecked -> {
                    if (!isChecked && !atLeastOneHomeElementActive()) {
                        SwitchItem item = (SwitchItem) getItemByTitle(getString(R.string.show_current_shift));
                        if (item != null) item.setCurrentValue(true);
                        Toast.makeText(this, R.string.toast_at_least_one_element_required, Toast.LENGTH_SHORT).show();
                        refreshItem(getItemByTitle(getString(R.string.show_current_shift)));
                        return;
                    }
                    prefs.setShowCurrentShift(isChecked);
                });
    }

    /**
     * Valida la integridad de la pantalla de inicio para asegurar que no quede vacía.
     */
    private boolean atLeastOneHomeElementActive() {
        return prefs.isShowFeaturedNews() || prefs.isShowFeaturedPosts() || 
               prefs.isShowDailyEphemeris() || prefs.isShowCurrentShift();
    }

    private SettingsItem getItemByTitle(String title) {
        if (adapter == null) return null;
        for (SettingsItem item : adapter.getItems()) {
            if (item instanceof SwitchItem && ((SwitchItem) item).title.equals(title)) return item;
        }
        return null;
    }

    private SelectItem buildThemeSelectItem() {
        String current = prefs.getTheme();
        List<SelectItem.SelectOption> options = Arrays.asList(
                new SelectItem.SelectOption(getString(R.string.theme_default), ThemeManager.THEME_DEFAULT),
                new SelectItem.SelectOption(getString(R.string.theme_light), ThemeManager.THEME_LIGHT),
                new SelectItem.SelectOption(getString(R.string.theme_dark), ThemeManager.THEME_DARK)
        );
        return new SelectItem(getString(R.string.title_theme), options, current,
                selected -> { if (!selected.equals(current)) ThemeManager.setThemeAndRestart(this, selected); });
    }

    private SwitchItem buildDynamicColorsSwitchItem() {
        return new SwitchItem(getString(R.string.title_dynamic_colors), prefs.isDynamicColorsEnabled(),
                isChecked -> {
                    prefs.setDynamicColorsEnabled(isChecked);
                    ThemeManager.restartApp(this);
                });
    }

    private SelectItem buildFontSizeSelectItem() {
        float currentScale = prefs.getFontScale();
        String currentValue = currentScale >= 1.35f ? "xlarge" : (currentScale >= 1.15f ? "large" : (currentScale <= 0.85f ? "small" : "medium"));

        List<SelectItem.SelectOption> options = Arrays.asList(
                new SelectItem.SelectOption(getString(R.string.font_size_small), "small"),
                new SelectItem.SelectOption(getString(R.string.font_size_medium), "medium"),
                new SelectItem.SelectOption(getString(R.string.font_size_large), "large"),
                new SelectItem.SelectOption(getString(R.string.font_size_xlarge), "xlarge")
        );
        return new SelectItem(getString(R.string.font_size), options, currentValue,
                selected -> {
                    float newScale;
                    switch (selected) {
                        case "small": newScale = 0.85f; break;
                        case "large": newScale = 1.15f; break;
                        case "xlarge": newScale = 1.35f; break;
                        default: newScale = 1.0f;
                    }
                    prefs.setFontScale(newScale);
                    ThemeManager.restartApp(this);
                });
    }

    private SelectItem buildFontFamilySelectItem() {
        String current = prefs.getFontFamily();
        List<SelectItem.SelectOption> options = Arrays.asList(
                new SelectItem.SelectOption(getString(R.string.font_family_system), "system"),
                new SelectItem.SelectOption(getString(R.string.font_family_sans), "sans-serif"),
                new SelectItem.SelectOption(getString(R.string.font_family_serif), "serif"),
                new SelectItem.SelectOption(getString(R.string.font_family_monospace), "monospace")
        );
        return new SelectItem(getString(R.string.font_family), options, current,
                selected -> { prefs.setFontFamily(selected); ThemeManager.restartApp(this); });
    }

    private SwitchItem buildBlurSwitchItem() {
        return new SwitchItem(getString(R.string.blur_effect), prefs.isBlurEnabled(), isChecked -> prefs.setBlurEnabled(isChecked));
    }

    private SelectItem buildAnimationSpeedSelectItem() {
        float current = prefs.getAnimationSpeed();
        String currentValue = current <= 0.5f ? "fast" : (current >= 2.0f ? "slow" : "normal");

        List<SelectItem.SelectOption> options = Arrays.asList(
                new SelectItem.SelectOption(getString(R.string.anim_fast), "fast"),
                new SelectItem.SelectOption(getString(R.string.anim_normal), "normal"),
                new SelectItem.SelectOption(getString(R.string.anim_slow), "slow")
        );
        return new SelectItem(getString(R.string.animation_speed), options, currentValue,
                selected -> {
                    float newSpeed = selected.equals("fast") ? 0.5f : (selected.equals("slow") ? 2.0f : 1.0f);
                    prefs.setAnimationSpeed(newSpeed);
                    ThemeManager.restartApp(this);
                });
    }
}


