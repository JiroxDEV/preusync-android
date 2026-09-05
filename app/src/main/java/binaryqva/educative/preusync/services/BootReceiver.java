/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: BootReceiver.java
 * Versión: v1.0.6
 * Descripción: Receptor de arranque del sistema. Restaura los servicios de
 *              sincronización y notificaciones tras el reinicio del equipo.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import binaryqva.educative.preusync.debug.AppLogger;
import binaryqva.educative.preusync.utils.common.AuthManager;
import binaryqva.educative.preusync.utils.common.PreferenceManager;
import binaryqva.educative.preusync.utils.scheduling.BackgroundServiceScheduler;

/**
 * Escucha el evento BOOT_COMPLETED para asegurar que PreuSync permanezca 
 * activo y sincronizado sin intervención manual del usuario.
 */
public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;

        AppLogger.d(TAG, "Detección de arranque: Inicializando componentes core");

        PreferenceManager prefs = PreferenceManager.getInstance(context);
        AuthManager auth = AuthManager.getInstance(context);

        // VALIDACIÓN DE SESIÓN: Evita arrancar servicios si el token es inválido.
        if (!auth.isSessionValid()) {
            AppLogger.w(TAG, "Sesión no válida tras reinicio. Abortando servicios");
            return;
        }

        // COMPROBACIÓN DE AJUSTES: El usuario debe permitir la ejecución en segundo plano.
        if (!prefs.isBgSyncEnabled()) {
            AppLogger.d(TAG, "Sincronización desactivada por el usuario");
            return;
        }

        // 1. Restaurar el Servicio Persistente (Notificaciones Resumen).
        try {
            Intent serviceIntent = new Intent(context, PersistentService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) context.startForegroundService(serviceIntent);
            else context.startService(serviceIntent);
        } catch (Exception e) { AppLogger.e(TAG, "Fallo al restaurar PersistentService", e); }

        // 2. Reprogramar la Alarma de Sincronización Periódica.
        try { BackgroundServiceScheduler.scheduleFirst(context); }
        catch (Exception e) { AppLogger.e(TAG, "Fallo al reprogramar BackgroundServiceScheduler", e); }

        AppLogger.i(TAG, "Restauración post-arranque completada");
    }
}


