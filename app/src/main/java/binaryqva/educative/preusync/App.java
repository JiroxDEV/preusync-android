/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: App.java
 * Versión: v6.2.0
 * Descripción: Clase principal de la aplicación (Application). Centraliza la
 *              inicialización de servicios globales, caché y gestión de temas.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Process;

import binaryqva.educative.preusync.debug.AppLogger;
import binaryqva.educative.preusync.debug.CrashReportActivity;
import binaryqva.educative.preusync.utils.common.CacheManager;
import binaryqva.educative.preusync.utils.common.PreferenceManager;
import binaryqva.educative.preusync.utils.theme.ThemeManager;

/**
 * Punto de entrada del proceso de la App. Orquestra la configuración inicial
 * antes de desplegar cualquier componente visual.
 */
public class App extends Application {

    private static final String TAG = "PreuSync_App";
    private static Context applicationContext;

    public static Context getContext() { return applicationContext; }

    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext = getApplicationContext();

        // INICIALIZACIÓN DE PREFERENCIAS Y MOTOR DE TEMAS:
        PreferenceManager prefs = PreferenceManager.getInstance(this);
        prefs.initDefaultsIfNeeded();
        ThemeManager.applyTheme(prefs.getTheme());

        // INICIALIZACIÓN DEL GESTOR DE PERSISTENCIA (CACHÉ):
        CacheManager.getInstance().init(this);

        // MANEJADOR GLOBAL DE CRASHES (EXCEPCIONES NO CAPTURADAS):
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            String trace = AppLogger.getStackTraceString(throwable);
            AppLogger.e(TAG, "ERROR CRÍTICO DETECTADO", throwable);

            // Redirección a la pantalla de informe de error.
            Intent intent = new Intent(getApplicationContext(), CrashReportActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("error", trace);
            startActivity(intent);

            Process.killProcess(Process.myPid());
            System.exit(1);
        });

        AppLogger.i(TAG, "Entorno PreuSync inicializado correctamente");
    }
}


