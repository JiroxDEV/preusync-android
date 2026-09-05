/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: OfflineSnackbarManager.java
 * Versión: v1.0.2
 * Descripción: Gestor del Snackbar para avisos de modo fuera de línea.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.utils.common;

import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

import binaryqva.educative.preusync.R;

/**
 * Controla la aparición automática de alertas persistentes cuando la App 
 * detecta falta de red pero posee datos en la memoria caché.
 */
public class OfflineSnackbarManager {

    private final Fragment fragment;
    private final View anchorView;
    private final Runnable onRetryAction;
    private final NetworkErrorManager errorManager;

    private Snackbar currentSnackbar;
    private boolean isShowing = false;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable pendingCheck;

    public OfflineSnackbarManager(@NonNull Fragment fragment, @NonNull View anchor, Runnable retry) {
        this.fragment = fragment; this.anchorView = anchor;
        this.onRetryAction = retry; this.errorManager = NetworkErrorManager.getInstance();
    }

    /**
     * Evalúa el estado de red y caché para mostrar u ocultar la alerta.
     */
    public void checkAndUpdate() {
        if (fragment.getContext() == null || !fragment.isAdded()) return;
        if (pendingCheck != null) handler.removeCallbacks(pendingCheck);

        pendingCheck = () -> {
            boolean offline = !errorManager.isNetworkAvailable(fragment.getContext());
            boolean cached = errorManager.hasAnyCache();
            if (offline && cached) show(); else dismiss();
        };
        handler.postDelayed(pendingCheck, 300);
    }

    private void show() {
        if (isShowing || anchorView == null || anchorView.getParent() == null) return;

        currentSnackbar = Snackbar.make(anchorView, R.string.message_offline_mode, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.button_retry, v -> { if (onRetryAction != null) onRetryAction.run(); dismiss(); })
                .addCallback(new Snackbar.Callback() {
                    @Override public void onShown(Snackbar sb) { isShowing = true; }
                    @Override public void onDismissed(Snackbar sb, int e) { isShowing = false; currentSnackbar = null; }
                });
        currentSnackbar.show();
    }

    public void dismiss() { if (currentSnackbar != null) currentSnackbar.dismiss(); isShowing = false; }
    public void onDestroy() { dismiss(); handler.removeCallbacksAndMessages(null); }
    public void setSectionHasCache(String s, boolean h) { errorManager.setSectionHasCache(s, h); }
    public void resetCacheFlags() { errorManager.reset(); }
}


