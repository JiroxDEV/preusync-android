/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: SplashActivity.java
 * Versión: v5.4.1
 * Descripción: Pantalla de arranque: inicializa servicios, verifica sesión y
 *              dirige el flujo inicial; configura workers y servicios
 *              persistentes.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.ui.activities;

import binaryqva.educative.preusync.R;
import binaryqva.educative.preusync.services.PersistentService;
import binaryqva.educative.preusync.services.UpdateWorker;
import binaryqva.educative.preusync.utils.common.AuthManager;
import binaryqva.educative.preusync.utils.common.PreferenceManager;
import binaryqva.educative.preusync.utils.notification.NotificationHelper;
import binaryqva.educative.preusync.utils.scheduling.BackgroundServiceScheduler;
import binaryqva.educative.preusync.utils.theme.ThemeManager;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import binaryqva.educative.preusync.debug.AppLogger;

/**
 * Pantalla de bienvenida encargada de preparar el entorno antes de la navegación principal.
 * Utiliza la API de SplashScreen de Android 12+ para una transición fluida.
 */
public class SplashActivity extends BaseActivity {

    private final Timer timer = new Timer();
    private TimerTask currentTimerTask;
    private PreferenceManager preferencesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Instalación de la API de SplashScreen para compatibilidad con Android 12+.
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        
        // Personalización de la animación de salida de la pantalla de carga.
        splashScreen.setOnExitAnimationListener(splashScreenViewProvider -> {
            View splashView = splashScreenViewProvider.getView();
            ObjectAnimator fadeOut = ObjectAnimator.ofFloat(splashView, View.ALPHA, 1f, 0f);
            fadeOut.setDuration(500);
            fadeOut.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    splashScreenViewProvider.remove();
                }
            });
            fadeOut.start();
        });

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);

        preferencesManager = PreferenceManager.getInstance(this);
        initialize();
        initializeLogic();
    }

    /**
     * Configuración inicial de componentes básicos.
     */
    private void initialize() {
        // Aseguramos la existencia de los canales de notificación.
        NotificationHelper.createChannels(this);
    }

    /**
     * Lógica central de decisión: Navegar al Home o al Login basándose en la sesión.
     */
    private void initializeLogic() {
        int colorBackground = ThemeManager.getThemeColor(this, R.attr.colorBackground);
        getWindow().setNavigationBarColor(colorBackground);

        // Registro de métrica de apertura.
        preferencesManager.incrementAppOpenCount();

        // FLUJO DE AUTENTICACIÓN:
        if (AuthManager.getInstance(this).hasValidToken()) {
            // El token local aún es vigente, procedemos directamente.
            navigateToHomeDelayed();
        } else if (preferencesManager.getSupabaseRefreshToken() != null && !preferencesManager.getSupabaseRefreshToken().isEmpty()) {
            // Token expirado pero tenemos llave de renovación; intentamos refresh silencioso.
            AuthManager.getInstance(this).refreshSession(new AuthManager.SessionCallback() {
                @Override
                public void onSessionValid() {
                    navigateToHomeDelayed();
                }

                @Override
                public void onSessionInvalid() {
                    preferencesManager.clearAccount();
                    navigateToAuthDelayed();
                }
            });
        } else {
            // Sin credenciales vigentes, redirigimos al registro/login.
            navigateToAuthDelayed();
        }

        // INICIALIZACIÓN DE SERVICIOS EN SEGUNDO PLANO:
        initializeBackgroundWorkers();
    }

    private void initializeBackgroundWorkers() {
        try {
            WorkManager.getInstance(this);
        } catch (IllegalStateException e) {
            WorkManager.initialize(this, new androidx.work.Configuration.Builder().build());
        }

        // Programación de comprobación de actualizaciones semanales.
        PeriodicWorkRequest updateCheckRequest = new PeriodicWorkRequest.Builder(
                UpdateWorker.class, 7, TimeUnit.DAYS)
                .setInitialDelay(1, TimeUnit.HOURS)
                .addTag("update_check")
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "periodic_update",
                ExistingPeriodicWorkPolicy.KEEP,
                updateCheckRequest
        );

        // Lanzamiento del sincronizador de contenido.
        BackgroundServiceScheduler.scheduleFirst(this);

        // Inicio del servicio persistente si se cuenta con el permiso de notificaciones (Android 13+).
        if (canStartForegroundService()) {
            if (!isServiceRunning(PersistentService.class)) {
                Intent serviceIntent = new Intent(this, PersistentService.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent);
                } else {
                    startService(serviceIntent);
                }
            }
        }
    }

    /**
     * Transición hacia la actividad principal tras un breve retardo visual.
     */
    private void navigateToHomeDelayed() {
        if (currentTimerTask != null) currentTimerTask.cancel();
        currentTimerTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                    intent.putExtra("page", "default");
                    startActivity(intent);
                    finish();
                });
            }
        };
        timer.schedule(currentTimerTask, 2000);
    }

    /**
     * Transición hacia la actividad de autenticación u Onboarding si es la primera vez.
     */
    private void navigateToAuthDelayed() {
        if (currentTimerTask != null) currentTimerTask.cancel();
        currentTimerTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    if (!preferencesManager.isOnboardingCompleted()) {
                        startActivity(new Intent(getApplicationContext(), OnboardingActivity.class));
                    } else {
                        Intent intent = new Intent(getApplicationContext(), AuthActivity.class);
                        intent.putExtra("registrando", String.valueOf(!preferencesManager.hasAccount()));
                        startActivity(intent);
                    }
                    finish();
                });
            }
        };
        timer.schedule(currentTimerTask, 2000);
    }

    private boolean canStartForegroundService() {
        if (Build.VERSION.SDK_INT >= 33) {
            return ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (currentTimerTask != null) currentTimerTask.cancel();
        if (timer != null) timer.cancel();
    }
}


