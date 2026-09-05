/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: BackgroundServiceScheduler.java
 * Versión: v1.0.1
 * Descripción: Programador de tareas en segundo plano. Gestiona el ciclo
 *              de sincronización periódica mediante AlarmManager.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.utils.scheduling;

import binaryqva.educative.preusync.services.BackgroundServiceExecutorReceiver;
import binaryqva.educative.preusync.utils.common.PreferenceManager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

/**
 * Orquestra la ejecución exacta de los servicios de sincronización.
 * Adapta el comportamiento según las restricciones de batería de Android 12+.
 */
public class BackgroundServiceScheduler {

    private static final int REQUEST_CODE = 999;

    private BackgroundServiceScheduler() {}

    /**
     * Inicia el primer ciclo de alarmas tras el arranque o login.
     */
    public static void scheduleFirst(Context ctx) { scheduleNext(ctx); }

    /**
     * Programa la siguiente ejecución exacta basándose en el intervalo configurado.
     */
    public static void scheduleNext(Context ctx) {
        PreferenceManager prefs = PreferenceManager.getInstance(ctx);
        long intervalMillis = prefs.getBackgroundIntervalMinutes() * 60 * 1000L;

        // Soporte para API 31+: Verificación de permiso para alarmas exactas.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
            if (!am.canScheduleExactAlarms()) { scheduleInexact(ctx, intervalMillis); return; }
        }

        PendingIntent pi = getPendingIntent(ctx);
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        long next = System.currentTimeMillis() + intervalMillis;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next, pi);
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) am.setExact(AlarmManager.RTC_WAKEUP, next, pi);
        else am.set(AlarmManager.RTC_WAKEUP, next, pi);
    }

    /**
     * Fallback para dispositivos con restricciones de ahorro de energía estrictas.
     */
    private static void scheduleInexact(Context ctx, long millis) {
        PendingIntent pi = getPendingIntent(ctx);
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        long start = System.currentTimeMillis() + millis;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) am.setInexactRepeating(AlarmManager.RTC_WAKEUP, start, millis, pi);
        else am.setRepeating(AlarmManager.RTC_WAKEUP, start, millis, pi);
    }

    public static void cancel(Context ctx) {
        PendingIntent pi = PendingIntent.getBroadcast(ctx, REQUEST_CODE, new Intent(ctx, BackgroundServiceExecutorReceiver.class), PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
        if (pi != null) { ((AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE)).cancel(pi); pi.cancel(); }
    }

    private static PendingIntent getPendingIntent(Context ctx) {
        Intent i = new Intent(ctx, BackgroundServiceExecutorReceiver.class);
        return PendingIntent.getBroadcast(ctx, REQUEST_CODE, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    /**
     * Actualiza el intervalo de chequeo y reinicia la cola de alarmas.
     */
    public static void setInterval(Context ctx, long mins) {
        PreferenceManager.getInstance(ctx).setBackgroundIntervalMinutes(Math.max(1, mins));
        cancel(ctx); scheduleFirst(ctx);
    }
}


