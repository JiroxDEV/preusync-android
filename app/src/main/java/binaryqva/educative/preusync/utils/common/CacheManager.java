/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: CacheManager.java
 * Versión: v4.0.0
 * Descripción: Gestor centralizado de caché. Refactorizado para usar cifrado
 *              AES-GCM (militar) con salt por equipo en toda la persistencia.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.utils.common;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import binaryqva.educative.preusync.debug.AppLogger;

/**
 * Administra la persistencia temporal de datos para soporte offline.
 * Todos los datos se almacenan cifrados para garantizar la integridad del usuario.
 */
public class CacheManager {

    private static final String TAG = "CacheManager";
    private static CacheManager instance;

    private final Gson gson = new Gson();
    private final ConcurrentHashMap<String, Long> cacheTimestamps = new ConcurrentHashMap<>();
    private Context appContext;

    /**
     * Enumeración mantenida por compatibilidad de firma, aunque ahora todo es SENSITIVE internamente.
     */
    public enum CacheSecurity {
        PUBLIC,
        SENSITIVE
    }

    private CacheManager() { }

    public static synchronized CacheManager getInstance() {
        if (instance == null) instance = new CacheManager();
        return instance;
    }

    public void init(Context context) {
        if (this.appContext == null) {
            this.appContext = context.getApplicationContext();
        }
    }

    // ==================== OPERACIONES DE ESCRITURA ====================

    public boolean saveCache(String cacheKey, Object data, CacheSecurity security) {
        if (cacheKey == null || data == null) return false;
        try {
            return saveRawCache(cacheKey, gson.toJson(data), security);
        } catch (Exception e) {
            AppLogger.e(TAG, "Error al serializar caché: " + cacheKey, e);
            return false;
        }
    }

    public boolean saveRawCache(String cacheKey, String data, CacheSecurity ignored) {
        if (cacheKey == null || data == null || appContext == null) return false;

        try {
            String filePath = getCacheFilePath(cacheKey);
            // UNIFICACIÓN: Usamos el cifrador de alta seguridad para TODO.
            String encoded = SensitiveDataCipher.encode(data, appContext);
            
            if (encoded == null) return false;
            FileUtils.writeFile(filePath, encoded);
            cacheTimestamps.put(cacheKey, System.currentTimeMillis());
            return true;
        } catch (Exception e) {
            AppLogger.e(TAG, "Error al escribir caché: " + cacheKey, e);
            return false;
        }
    }

    // ==================== OPERACIONES DE LECTURA ====================

    public <T> T loadCache(String cacheKey, CacheSecurity security, Type typeOfT) {
        String raw = loadRawCache(cacheKey, security);
        if (raw == null) return null;
        try {
            return gson.fromJson(raw, typeOfT);
        } catch (Exception e) {
            AppLogger.e(TAG, "Error al parsear caché: " + cacheKey, e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public ArrayList<HashMap<String, Object>> loadCache(String cacheKey, CacheSecurity security) {
        return loadCache(cacheKey, security, new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
    }

    public String loadRawCache(String cacheKey, CacheSecurity security) {
        if (cacheKey == null || appContext == null) return null;
        try {
            String filePath = getCacheFilePath(cacheKey);
            if (!FileUtils.isExistFile(filePath)) return null;
            
            String encoded = FileUtils.readFile(filePath);
            if (encoded == null || encoded.isEmpty()) return null;
            
            // Intentamos descifrar con SensitiveDataCipher (Nuevo Estándar).
            String decoded = SensitiveDataCipher.decode(encoded, appContext);
            
            // Fallback para caché antigua cifrada con DataCipher (si existiera).
            if (decoded == null) {
                String oldDecoded = DataCipher.decode(encoded, "");
                // Si logramos recuperar caché antigua, la remigramos al nuevo estándar.
                if (oldDecoded != null) {
                    saveRawCache(cacheKey, oldDecoded, security);
                    return oldDecoded;
                }
            }
            
            return decoded;
        } catch (Exception e) {
            AppLogger.e(TAG, "Error al leer caché: " + cacheKey, e);
            return null;
        }
    }

    public boolean hasCache(String cacheKey) {
        return FileUtils.isExistFile(getCacheFilePath(cacheKey));
    }

    public boolean invalidateCache(String cacheKey) {
        if (cacheKey == null) return false;
        try {
            String filePath = getCacheFilePath(cacheKey);
            if (FileUtils.isExistFile(filePath)) FileUtils.deleteFile(filePath);
            cacheTimestamps.remove(cacheKey);
            return true;
        } catch (Exception e) { return false; }
    }

    public boolean clearAllCache() {
        try {
            String cacheDir = getCacheDirectory();
            if (FileUtils.isExistFile(cacheDir)) {
                FileUtils.deleteFile(cacheDir);
                FileUtils.makeDir(cacheDir);
            }
            cacheTimestamps.clear();
            return true;
        } catch (Exception e) { return false; }
    }

    private String getCacheFilePath(String cacheKey) {
        return getCacheDirectory() + File.separator + cacheKey;
    }

    private String getCacheDirectory() {
        if (appContext == null) return "/data/data/binaryqva.educative.preusync/cache";
        return appContext.getCacheDir().getAbsolutePath();
    }
}


