/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: ReminderManager.java
 * Versión: v1.0.0
 * Descripción: Gestor de alarmas y recordatorios. Envuelve AlarmManager
 *              para soportar disparos exactos en diferentes APIs de Android.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.utils.notification;

import binaryqva.educative.preusync.services.ReminderReceiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

/**
 * Provee métodos estáticos para programar ejecuciones en momentos precisos.
 */
public class ReminderManager {

    /**
     * Agenda una notificación de recordatorio para un evento académico.
     */
    public static void scheduleReminder(Context ctx, long millis, int id, String title, String msg) {
        Intent intent = new Intent(ctx, ReminderReceiver.class);
        intent.putExtra("title", title); intent.putExtra("message", msg); intent.putExtra("eventId", id);

        PendingIntent pi = PendingIntent.getBroadcast(ctx, id, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);

        // Uso de setExactAndAllowWhileIdle para ignorar restricciones de ahorro de energía en Android 6.0+.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pi);
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) am.setExact(AlarmManager.RTC_WAKEUP, millis, pi);
        else am.set(AlarmManager.RTC_WAKEUP, millis, pi);
    }

    /**
     * Programa una tarea recurrente con margen de error (más eficiente para la batería).
     */
    public static void scheduleInexactPeriodicTask(Context ctx, long interval, int req, PendingIntent pi) {
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        long start = System.currentTimeMillis() + interval;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) am.setInexactRepeating(AlarmManager.RTC_WAKEUP, start, interval, pi);
        else am.setRepeating(AlarmManager.RTC_WAKEUP, start, interval, pi);
    }

    /**
     * Programa una tarea recurrente forzando precisión absoluta.
     */
    public static void scheduleExactPeriodicTask(Context ctx, long interval, int req, PendingIntent pi) {
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        long start = System.currentTimeMillis() + interval;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, start, pi);
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) am.setExact(AlarmManager.RTC_WAKEUP, start, pi);
        else am.set(AlarmManager.RTC_WAKEUP, start, pi);
    }

    public static void cancelAlarm(Context ctx, PendingIntent pi) {
        if (pi != null) { ((AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE)).cancel(pi); pi.cancel(); }
    }

    public static void cancelAlarm(Context ctx, int req, Class<?> receiver) {
        PendingIntent pi = PendingIntent.getBroadcast(ctx, req, new Intent(ctx, receiver), PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
        if (pi != null) cancelAlarm(ctx, pi);
    }
}


