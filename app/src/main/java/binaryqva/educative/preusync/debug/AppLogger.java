/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: AppLogger.java
 * Versión: v2.0.0
 * Descripción: Sistema de logging centralizado con buffer circular en memoria.
 *              Sustituye a android.util.Log para depuración profesional.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.debug;

import android.util.Log;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Gestiona el registro de eventos de la aplicación. Mantiene una bitácora de 
 * los últimos 500 registros para ser consultados desde la Consola de Logs.
 */
public class AppLogger {

    private static final String DEFAULT_TAG = "PreuSync_Core";
    private static final int MAX_LOGS = 500;
    private static final LinkedList<LogEntry> logBuffer = new LinkedList<>();

    public enum Level { VERBOSE, DEBUG, INFO, WARN, ERROR }

    /**
     * Representa una entrada individual en la bitácora de depuración.
     */
    public static class LogEntry {
        public final long timestamp;
        public final Level level;
        public final String tag;
        public final String message;

        public LogEntry(Level level, String tag, String message) {
            this.timestamp = System.currentTimeMillis();
            this.level = level;
            this.tag = tag;
            this.message = message;
        }
    }

    private static synchronized void addLog(Level level, String tag, String message) {
        if (logBuffer.size() >= MAX_LOGS) logBuffer.removeFirst();
        logBuffer.addLast(new LogEntry(level, tag, message));
    }

    public static synchronized List<LogEntry> getLogs() { return new ArrayList<>(logBuffer); }
    public static synchronized void clearLogs() { logBuffer.clear(); }
    public static String getStackTraceString(Throwable tr) { return Log.getStackTraceString(tr); }

    // ==================== NIVEL: VERBOSE ====================
    public static void v(String tag, String msg) { Log.v(tag, msg); addLog(Level.VERBOSE, tag, msg); }

    // ==================== NIVEL: DEBUG ====================
    public static void d(String msg) { d(DEFAULT_TAG, msg); }
    public static void d(String tag, String msg) { Log.d(tag, msg); addLog(Level.DEBUG, tag, msg); }

    // ==================== NIVEL: INFO ====================
    public static void i(String msg) { i(DEFAULT_TAG, msg); }
    public static void i(String tag, String msg) { Log.i(tag, msg); addLog(Level.INFO, tag, msg); }

    // ==================== NIVEL: WARN ====================
    public static void w(String msg) { w(DEFAULT_TAG, msg); }
    public static void w(String tag, String msg) { Log.w(tag, msg); addLog(Level.WARN, tag, msg); }
    public static void w(String tag, String msg, Throwable tr) {
        String full = msg + (tr != null ? "\n" + Log.getStackTraceString(tr) : "");
        Log.w(tag, full); addLog(Level.WARN, tag, full);
    }

    // ==================== NIVEL: ERROR ====================
    public static void e(String msg) { e(DEFAULT_TAG, msg); }
    public static void e(String tag, String msg) { Log.e(tag, msg); addLog(Level.ERROR, tag, msg); }
    public static void e(String msg, Throwable tr) { e(DEFAULT_TAG, msg, tr); }
    public static void e(String tag, String msg, Throwable tr) {
        String full = msg + (tr != null ? "\n" + Log.getStackTraceString(tr) : "");
        Log.e(tag, full); addLog(Level.ERROR, tag, full);
    }
}


