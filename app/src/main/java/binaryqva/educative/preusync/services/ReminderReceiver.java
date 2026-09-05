/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: ReminderReceiver.java
 * Versión: v1.0.3
 * Descripción: Receptor de alarmas para recordatorios de eventos.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import binaryqva.educative.preusync.debug.AppLogger;
import binaryqva.educative.preusync.ui.activities.AuthActivity;
import binaryqva.educative.preusync.ui.activities.HomeActivity;
import binaryqva.educative.preusync.utils.common.AuthManager;
import binaryqva.educative.preusync.utils.notification.NotificationHelper;

/**
 * Se dispara cuando una alarma de recordatorio programada vence.
 * Verifica la validez de la sesión antes de mostrar información sensible.
 */
public class ReminderReceiver extends BroadcastReceiver {

    private static final String TAG = "ReminderReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        AppLogger.d(TAG, "Recordatorio vencido: Procesando notificación");

        AuthManager auth = AuthManager.getInstance(context);

        // PROTECCIÓN DE DATOS: Si la sesión expiró, se avisa al usuario sin mostrar detalles del evento.
        if (!auth.isSessionValid()) {
            AppLogger.w(TAG, "Recordatorio ignorado por sesión expirada");
            showExpiredNotification(context);
            auth.handleExpiredSession(context);
            return;
        }

        String title = intent.getStringExtra("title");
        String msg = intent.getStringExtra("message");
        int eventId = intent.getIntExtra("eventId", 0);

        if (title == null || title.isEmpty()) title = "Recordatorio PreuSync";
        if (msg == null || msg.isEmpty()) msg = "Tienes un evento programado próximamente";

        AppLogger.i(TAG, "Mostrando recordatorio ID: " + eventId);

        // Configuración del Intent para abrir los detalles del evento al pulsar la notificación.
        Intent openIntent = new Intent(context, HomeActivity.class);
        openIntent.setAction(HomeActivity.ACTION_OPEN_EVENT);
        openIntent.putExtra("id", String.valueOf(eventId));
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        NotificationHelper.showNotification(context, title, msg, eventId, NotificationHelper.CHANNEL_REMINDERS, openIntent);
    }

    private void showExpiredNotification(Context context) {
        Intent authIntent = new Intent(context, AuthActivity.class);
        authIntent.putExtra("session_expired", true);
        authIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        NotificationHelper.showNotification(context, "Sesión Expirada", "Inicia sesión para ver tus recordatorios académicos.", 9999, NotificationHelper.CHANNEL_GENERAL, authIntent);
    }
}


