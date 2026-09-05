/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: NetworkErrorManager.java
 * Versión: v1.1.0
 * Descripción: Gestor centralizado de disponibilidad de red y estados de caché.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.utils.common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;

import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

import binaryqva.educative.preusync.ui.viewmodels.HomeViewModel;

/**
 * Orquestra el diagnóstico de conectividad y la validez de la persistencia local.
 * Determina si la aplicación debe operar en modo degradado (offline).
 */
public class NetworkErrorManager {

    private static NetworkErrorManager instance;
    private final Map<String, Boolean> sectionHasCache = new HashMap<>();
    private boolean hasAnyCache = false;

    private NetworkErrorManager() {}

    public static synchronized NetworkErrorManager getInstance() {
        if (instance == null) instance = new NetworkErrorManager();
        return instance;
    }

    /**
     * Evalúa si el dispositivo cuenta con acceso activo a Internet.
     */
    public boolean isNetworkAvailable(Context context) {
        if (context == null) return true;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network n = cm.getActiveNetwork();
            if (n == null) return false;
            NetworkCapabilities c = cm.getNetworkCapabilities(n);
            return c != null && c.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        } else {
            NetworkInfo info = cm.getActiveNetworkInfo();
            return info != null && info.isConnected();
        }
    }

    /**
     * Registra la existencia de datos en caché para una sección específica.
     */
    public void setSectionHasCache(String section, boolean hasCache) {
        sectionHasCache.put(section, hasCache);
        updateGlobalCacheFlag();
    }

    private void updateGlobalCacheFlag() {
        hasAnyCache = false;
        for (boolean v : sectionHasCache.values()) if (v) { hasAnyCache = true; break; }
    }

    public boolean hasAnyCache() { return hasAnyCache; }

    public void reset() { sectionHasCache.clear(); hasAnyCache = false; }

    /**
     * Lógica de decisión para mostrar errores visuales. 
     * Si hay caché, el error se oculta para priorizar la visualización offline.
     */
    public boolean shouldShowError(int state, boolean cache, boolean network) {
        if (state == HomeViewModel.STATE_ERROR) {
            if (!cache && !network) return true; // Offline total sin caché.
            if (cache) return false; // Tenemos caché, evitamos el layout de error.
            return network; // Si hay red y falló, mostramos error.
        }
        return false;
    }
}



