/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: AppLogger.java
 * Versión: v2.2.0
 * Descripción: Sistema de logs personalizado. (DESHABILITADO PARA PRODUCCIÓN)
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.debug;

import android.util.Log;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AppLogger {
    public enum LogLevel { DEBUG, INFO, WARN, ERROR }

    private static final int MAX_LOGS = 200;
    private static final List<LogEntry> logs = new ArrayList<>();
    private static LogListener listener;

    public interface LogListener { void onLogAdded(LogEntry entry); }
    public static void setListener(LogListener l) { listener = l; }

    public static void d(String tag, String msg) { 
        // addLog(LogLevel.DEBUG, tag, msg); 
    }
    public static void i(String tag, String msg) { 
        // addLog(LogLevel.INFO, tag, msg); 
    }
    public static void w(String tag, String msg) { 
        // addLog(LogLevel.WARN, tag, msg); 
    }
    public static void e(String tag, String msg) { 
        Log.e(tag, msg); 
        // addLog(LogLevel.ERROR, tag, msg); 
    }
    public static void e(String tag, String msg, Throwable t) { 
        Log.e(tag, msg, t); 
        // addLog(LogLevel.ERROR, tag, msg + " | " + t.getMessage()); 
    }

    public static String getStackTraceString(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }

    private static synchronized void addLog(LogLevel level, String tag, String msg) {
        LogEntry entry = new LogEntry(level, tag, msg);
        logs.add(entry);
        if (logs.size() > MAX_LOGS) logs.remove(0);
        if (listener != null) listener.onLogAdded(entry);
    }

    public static List<LogEntry> getLogs() { return new ArrayList<>(logs); }
    public static void clearLogs() { logs.clear(); }

    public static class LogEntry {
        public final LogLevel level;
        public final String tag, message, timestamp;
        public LogEntry(LogLevel l, String t, String m) {
            this.level = l; this.tag = t; this.message = m;
            this.timestamp = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        }
    }
}
