/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: ThemeManager.java
 * Versión: v2.0.2
 * Descripción: Motor de gestión de temas y colores dinámicos. Implementa
 *              soporte para Material You y cambio de tema en caliente.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.utils.theme;

import binaryqva.educative.preusync.utils.common.PreferenceManager;
import binaryqva.educative.preusync.utils.markdown.MarkdownWebViewHelper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Build;
import android.util.TypedValue;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.content.res.AppCompatResources;

import com.google.android.material.color.DynamicColors;

/**
 * Controla la estética visual de la aplicación. Gestiona la paleta de colores,
 * el modo nocturno y la integración con las APIs de colores dinámicos de Android.
 */
public class ThemeManager {

    public static final String THEME_DEFAULT = "theme_default";
    public static final String THEME_LIGHT = "theme_light";
    public static final String THEME_DARK = "theme_dark";

    /**
     * Aplica el modo nocturno basándose en el identificador de tema guardado.
     * Invalida la caché de Markdown para asegurar el refresco de estilos CSS.
     */
    public static void applyTheme(String theme) {
        MarkdownWebViewHelper.invalidateCache(); 

        switch (theme != null ? theme : THEME_DEFAULT) {
            case THEME_LIGHT: AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); break;
            case THEME_DARK: AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES); break;
            default: AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM); break;
        }
    }

    /**
     * Habilita los colores dinámicos de Material You (Android 12+) si el usuario lo prefiere.
     */
    public static void applyDynamicColorsIfEnabled(Activity activity) {
        if (PreferenceManager.getInstance(activity).isDynamicColorsEnabled() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            DynamicColors.applyToActivityIfAvailable(activity);
        }
    }

    /**
     * Actualiza la preferencia de tema y fuerza el reinicio total de la App.
     */
    public static void setThemeAndRestart(Activity activity, String theme) {
        PreferenceManager.getInstance(activity).setTheme(theme);
        applyTheme(theme);
        restartApp(activity);
    }

    public static void setDynamicColorsEnabled(Activity activity, boolean enabled) {
        PreferenceManager.getInstance(activity).setDynamicColorsEnabled(enabled);
        restartApp(activity);
    }

    /**
     * Realiza un reinicio "limpio" de la aplicación para aplicar cambios estructurales.
     */
    public static void restartApp(Activity activity) {
        Intent i = activity.getPackageManager().getLaunchIntentForPackage(activity.getPackageName());
        if (i != null) {
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.startActivity(i);
        }
        activity.finishAffinity();
    }

    // ==================== UTILIDADES DE EXTRACCIÓN DE COLORES ====================

    /**
     * Resuelve un color desde un atributo del tema activo (ej: ?attr/colorAccent).
     */
    @ColorInt
    public static int getThemeColor(Context context, @AttrRes int attrResId) {
        TypedValue val = new TypedValue();
        context.getTheme().resolveAttribute(attrResId, val, true);

        if (val.resourceId != 0) {
            ColorStateList csl = AppCompatResources.getColorStateList(context, val.resourceId);
            if (csl != null) return csl.getDefaultColor();
        }
        return val.data;
    }

    @ColorInt public static int getColorPrimary(Context ctx) { return getThemeColor(ctx, androidx.appcompat.R.attr.colorPrimary); }
    @ColorInt public static int getColorAccent(Context ctx) { return getThemeColor(ctx, androidx.appcompat.R.attr.colorAccent); }
    @ColorInt public static int getColorControlHighlight(Context ctx) { return getThemeColor(ctx, androidx.appcompat.R.attr.colorControlHighlight); }
}


