/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: BackgroundServiceExecutorReceiver.java
 * Versión: v1.9.6
 * Descripción: Receptor puente para la ejecución forzada del servicio
 *              de sincronización mediante AlarmManager.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import binaryqva.educative.preusync.debug.AppLogger;
import binaryqva.educative.preusync.utils.scheduling.BackgroundServiceScheduler;

/**
 * Garantiza la ejecución de tareas de fondo incluso en estados de reposo
 * profundo (Doze Mode) mediante WakeLocks y disparos exactos.
 */
public class BackgroundServiceExecutorReceiver extends BroadcastReceiver {
    private static final String TAG = "ServiceExecutor";

    @Override
    public void onReceive(Context context, Intent intent) {
        AppLogger.d(TAG, "Despertador activado: Iniciando ciclo de sincronización");

        // Adquisición de WakeLock para evitar que la CPU se apague durante el encolado.
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "PreuSync:SyncExecution");
        wakeLock.acquire(5 * 60 * 1000L); // Límite de seguridad de 5 minutos.

        try {
            // Delegación de la tarea pesada al motor de WorkManager.
            WorkManager.getInstance(context).enqueue(new OneTimeWorkRequest.Builder(BackgroundServiceWorker.class).build());
            AppLogger.i(TAG, "Tarea de fondo encolada exitosamente");
        } catch (Exception e) { AppLogger.e(TAG, "Fallo al encolar tarea de fondo", e); }
        finally { if (wakeLock.isHeld()) wakeLock.release(); }

        // RE-PROGRAMACIÓN: Asegura la continuidad del ciclo infinito de chequeo.
        try { BackgroundServiceScheduler.scheduleNext(context); }
        catch (Exception e) { AppLogger.e(TAG, "Fallo al reprogramar el siguiente ciclo", e); }
    }
}


