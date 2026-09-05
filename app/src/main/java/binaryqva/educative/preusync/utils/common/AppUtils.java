/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: AppUtils.java
 * Versión: v2.0.7
 * Descripción: Utilidades generales para la aplicación (red, teclado, Toast, UI).
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.utils.common;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.android.material.color.MaterialColors;

/**
 * Proporciona métodos estáticos reutilizables para tareas comunes de sistema y UI.
 */
public class AppUtils {

    /**
     * Comprueba si el dispositivo tiene acceso a internet activo.
     */
    public static boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network network = cm.getActiveNetwork();
            if (network == null) return false;
            NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
            // Verifica específicamente la capacidad de navegar por internet.
            return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        } else {
            // Fallback para versiones antiguas de Android.
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        }
    }

    /**
     * Oculta el teclado virtual de la pantalla.
     */
    public static void hideKeyboard(Context context) {
        try {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            View view = ((Activity) context).getCurrentFocus();
            if (view == null) return;
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } catch (Exception ignored) {}
    }

    /**
     * Fuerza la visualización del teclado virtual.
     */
    public static void showKeyboard(Context context) {
        try {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            View view = ((Activity) context).getCurrentFocus();
            if (view == null) {
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
            } else {
                imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
            }
        } catch (Exception ignored) {}
    }

    /**
     * Muestra un mensaje Toast corto en pantalla.
     */
    public static void showMessage(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Obtiene un color definido en el tema de Material Design de la aplicación.
     */
    public static int getMaterialColor(Context context, int resourceId) {
        return MaterialColors.getColor(context, resourceId, "getMaterialColor");
    }

    /**
     * Convierte valores de DP (Density-independent Pixels) a píxeles reales según la pantalla.
     */
    public static float getDip(Context context, int input) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, input,
                context.getResources().getDisplayMetrics());
    }

    public static int getDisplayWidthPixels(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    public static int getDisplayHeightPixels(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    /**
     * Ajusta la duración de una animación basándose en la preferencia de velocidad del usuario.
     */
    public static int getScaledDuration(Context context, int baseDuration) {
        PreferenceManager prefs = PreferenceManager.getInstance(context);
        float speed = prefs.getAnimationSpeed();
        return Math.max(1, (int) (baseDuration / speed));
    }

    /**
     * Obtiene el nombre de la versión actual de la aplicación (ej. "1.0.0").
     */
    public static String getAppVersionName(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Exception e) {
            return "Desconocida";
        }
    }
}


