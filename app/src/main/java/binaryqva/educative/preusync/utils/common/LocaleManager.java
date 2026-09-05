/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: LocaleManager.java
 * Versión: v1.0.0
 * Descripción: Gestor de internacionalización. Controla el cambio dinámico 
 *              de idioma (Español/Inglés) sin necesidad de reiniciar la App.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.utils.common;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;

import java.util.Locale;

/**
 * Proporciona métodos para inyectar configuraciones de idioma en el Contexto.
 * Asegura compatibilidad con las APIs de recursos de Android N y superiores.
 */
public class LocaleManager {

    /**
     * Aplica un idioma específico al contexto recibido.
     * @param context Contexto base (Activity o Application).
     * @param languageCode Código ISO del idioma (ej. "es", "en").
     * @return Nuevo contexto con la configuración regional aplicada.
     */
    public static Context setLocale(Context context, String languageCode) {
        if (languageCode == null || languageCode.isEmpty()) return context;

        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        
        Configuration config = new Configuration(context.getResources().getConfiguration());
        
        // Gestión de Locale según la versión de Android.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale);
        } else {
            config.locale = locale;
        }
        
        // Retorna un contexto envuelto para que los recursos carguen el idioma correcto.
        return context.createConfigurationContext(config);
    }

    /**
     * Identifica el idioma que está utilizando actualmente el sistema de recursos.
     */
    public static String getCurrentLanguage(Context context) {
        Configuration config = context.getResources().getConfiguration();
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = config.getLocales().get(0);
        } else {
            locale = config.locale;
        }
        return locale.getLanguage();
    }
}


