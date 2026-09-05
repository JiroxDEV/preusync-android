/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: NotificationHelper.java
 * Versión: v6.0.1
 * Descripción: Sistema de gestión de notificaciones. Controla canales,
 *              estilos y el despliegue de avisos del sistema.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.utils.notification;

import binaryqva.educative.preusync.R;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.provider.Settings;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.RemoteInput;

/**
 * Centraliza la creación y emisión de notificaciones. Implementa el estándar
 * de canales de Android Oreo (API 26+) y soporta configuraciones avanzadas.
 */
public class NotificationHelper {
	
	// Identificadores técnicos de canales para categorizar avisos.
	public static final String CHANNEL_GENERAL = "channel_general";
	public static final String CHANNEL_REMINDERS = "channel_reminders";
	public static final String CHANNEL_FOREGROUND_SERVICE = "channel_foreground_service";
	public static final String CHANNEL_NEWS = "channel_news";
	public static final String CHANNEL_POSTS = "channel_posts";
	public static final String CHANNEL_EVENTS = "channel_events";
	public static final String CHANNEL_EPHEMERIDES = "channel_ephemerides";
	public static final String CHANNEL_SCHEDULE = "channel_schedule";
	
	public static final String KEY_TEXT_REPLY = "key_text_reply";
	
	/**
	 * Registra todos los canales de la aplicación en el sistema operativo.
	 */
	public static void createChannels(Context ctx) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
		
		NotificationManager m = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
		if (m == null) return;
		
		// Registro modular de categorías.
		m.createNotificationChannel(createChan(CHANNEL_GENERAL, ctx.getString(R.string.notification_channel_general_name), ctx.getString(R.string.notification_channel_general_desc), NotificationManager.IMPORTANCE_DEFAULT));
		m.createNotificationChannel(createChan(CHANNEL_REMINDERS, ctx.getString(R.string.notification_channel_reminders_name), ctx.getString(R.string.notification_channel_reminders_desc), NotificationManager.IMPORTANCE_HIGH));
		m.createNotificationChannel(createChan(CHANNEL_FOREGROUND_SERVICE, ctx.getString(R.string.notification_channel_foreground_name), ctx.getString(R.string.notification_channel_foreground_desc), NotificationManager.IMPORTANCE_LOW));
		m.createNotificationChannel(createChan(CHANNEL_NEWS, ctx.getString(R.string.notification_channel_news_name), ctx.getString(R.string.notification_channel_news_desc), NotificationManager.IMPORTANCE_HIGH));
		m.createNotificationChannel(createChan(CHANNEL_POSTS, ctx.getString(R.string.notification_channel_posts_name), ctx.getString(R.string.notification_channel_posts_desc), NotificationManager.IMPORTANCE_DEFAULT));
		m.createNotificationChannel(createChan(CHANNEL_EVENTS, ctx.getString(R.string.notification_channel_events_name), ctx.getString(R.string.notification_channel_events_desc), NotificationManager.IMPORTANCE_DEFAULT));
		m.createNotificationChannel(createChan(CHANNEL_EPHEMERIDES, ctx.getString(R.string.notification_channel_ephemerides_name), ctx.getString(R.string.notification_channel_ephemerides_desc), NotificationManager.IMPORTANCE_DEFAULT));
		m.createNotificationChannel(createChan(CHANNEL_SCHEDULE, ctx.getString(R.string.notification_channel_schedule_name), ctx.getString(R.string.notification_channel_schedule_desc), NotificationManager.IMPORTANCE_HIGH));
	}

    private static NotificationChannel createChan(String id, String name, String desc, int imp) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return null;
        NotificationChannel c = new NotificationChannel(id, name, imp);
        c.setDescription(desc); c.setSound(null, null); // El sonido se gestiona dinámicamente por ítem.
        return c;
    }
	
	public static void showNotification(Context ctx, String title, String msg, int id, String chan, Intent clickIntent) {
        showNotification(ctx, title, msg, id, chan, clickIntent, null);
    }

	public static void showNotification(Context ctx, String title, String msg, int id, String chan, Intent clickIntent, NotificationOptions opts) {
		if (Build.VERSION.SDK_INT >= 33 && !NotificationManagerCompat.from(ctx).areNotificationsEnabled()) return;
		
		try {
			NotificationManager m = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
			if (m == null) return;
			
			if (opts == null) opts = new NotificationOptions();
			
			NotificationCompat.Builder b = new NotificationCompat.Builder(ctx, chan)
			.setSmallIcon(opts.icon != 0 ? opts.icon : android.R.drawable.ic_dialog_info)
			.setContentTitle(title).setContentText(msg).setPriority(opts.priority)
			.setAutoCancel(opts.autoCancel).setColor(opts.color).setVibrate(opts.vibrate).setSound(opts.sound)
			.setLights(opts.ledColor, opts.ledOnMs, opts.ledOffMs).setShowWhen(opts.showWhen).setLocalOnly(opts.localOnly)
			.setBadgeIconType(opts.badgeIconType).setNumber(opts.number);
			
			if (opts.bigPicture != null) b.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(opts.bigPicture).setBigContentTitle(title).setSummaryText(msg));
			else if (opts.bigText != null) b.setStyle(new NotificationCompat.BigTextStyle().bigText(opts.bigText));
			
			if (opts.actions != null) for (NotificationCompat.Action a : opts.actions) b.addAction(a);
			
			if (clickIntent != null && opts.pendingIntent == null) {
				b.setContentIntent(PendingIntent.getActivity(ctx, id, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE));
			} else if (opts.pendingIntent != null) b.setContentIntent(opts.pendingIntent);
			
			m.notify(id, b.build());
		} catch (Exception ignored) {}
	}
	
	public static void cancelNotification(Context ctx, int id) {
		try { ((NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(id); } catch (Exception ignored) {}
	}
	
	public static void cancelAll(Context ctx) {
		try { ((NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE)).cancelAll(); } catch (Exception ignored) {}
	}

    public static Uri getSoundUri(Context ctx, String soundPref) {
        if (soundPref == null || soundPref.equals("default")) return Settings.System.DEFAULT_NOTIFICATION_URI;
        if (soundPref.equals("silent")) return null;
        try { return Uri.parse(soundPref); } catch (Exception e) { return Settings.System.DEFAULT_NOTIFICATION_URI; }
    }
	
	/**
	 * Opciones de personalización para el constructor de notificaciones.
	 */
	public static class NotificationOptions {
		public int icon = 0, priority = NotificationCompat.PRIORITY_DEFAULT, color = Color.TRANSPARENT, number = 0, ledColor = 0, ledOnMs = 0, ledOffMs = 0;
		public String group = null; public boolean autoCancel = true, showWhen = true, localOnly = false, groupSummary = false;
		public long[] vibrate = null; public Uri sound = null; public PendingIntent pendingIntent = null, deleteIntent = null;
		public int badgeIconType = NotificationCompat.BADGE_ICON_NONE;
		public Bitmap bigPicture = null; public String bigText = null;
		public NotificationCompat.Action[] actions = null;
		
		public NotificationOptions setIcon(int i) { this.icon = i; return this; }
		public NotificationOptions setPriority(int p) { this.priority = p; return this; }
		public NotificationOptions setAutoCancel(boolean a) { this.autoCancel = a; return this; }
		public NotificationOptions setSound(Uri s) { this.sound = s; return this; }
		public NotificationOptions setBigText(String t) { this.bigText = t; return this; }
		public NotificationOptions setBigPicture(Bitmap b) { this.bigPicture = b; return this; }
	}
}


