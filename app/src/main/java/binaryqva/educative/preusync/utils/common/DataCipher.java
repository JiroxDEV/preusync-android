/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: DataCipher.java
 * Versión: v2.0.0
 * Descripción: Cifrador simétrico AES-GCM para datos de caché pública. 
 *              Sustituye al antiguo XOR para garantizar confidencialidad.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.utils.common;

import binaryqva.educative.preusync.debug.AppLogger;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import java.util.Base64;

/**
 * Implementa cifrado de grado militar (AES-256) para la persistencia local de datos.
 * Utiliza PBKDF2 para la derivación de claves y GCM para protección contra manipulación.
 */
public class DataCipher {

    private static final String TAG = "DataCipher";
    private static final String SECRET_SEED = "PreuSync2025SecureSeed!";
    private static final int ITERATION_COUNT = 10000;
    private static final int KEY_LENGTH = 256;
    private static final int GCM_IV_LENGTH = 12; // 96 bits recomendado para GCM.
    private static final int GCM_TAG_LENGTH = 128;

    private static SecretKey cachedKey;

    /**
     * Genera o recupera la clave simétrica derivada de la semilla interna.
     */
    private static synchronized SecretKey getDerivedKey() {
        if (cachedKey != null) return cachedKey;
        try {
            // Salt estático para la clave de dominio público.
            byte[] salt = "PreuSyncSalt2025".getBytes(StandardCharsets.UTF_8);
            KeySpec spec = new PBEKeySpec(SECRET_SEED.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] keyBytes = factory.generateSecret(spec).getEncoded();
            cachedKey = new SecretKeySpec(keyBytes, "AES");
            return cachedKey;
        } catch (Exception e) {
            AppLogger.e(TAG, "Fallo crítico en derivación de clave AES", e);
            throw new RuntimeException("Entorno criptográfico no disponible");
        }
    }

    /**
     * Cifra una cadena de texto plano.
     * @param plainText Texto a proteger.
     * @param ignoredKey Parámetro obsoleto mantenido para retrocompatibilidad con firmas antiguas.
     * @return Cadena Base64 que contiene [IV + Datos Cifrados].
     */
    public static String encode(String plainText, String ignoredKey) {
        if (plainText == null) return null;
        try {
            SecretKey key = getDerivedKey();
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            
            // Generación de Vector de Inicialización (IV) aleatorio para cada operación.
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);
            
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);

            byte[] plainBytes = plainText.getBytes(StandardCharsets.UTF_8);
            byte[] encrypted = cipher.doFinal(plainBytes);

            // Empaquetado de IV y datos cifrados en una única estructura binaria.
            ByteBuffer buffer = ByteBuffer.allocate(iv.length + encrypted.length);
            buffer.put(iv);
            buffer.put(encrypted);
            
            return Base64.getEncoder().encodeToString(buffer.array());
        } catch (Exception e) {
            AppLogger.e(TAG, "Error durante el cifrado de datos", e);
            return null;
        }
    }

    /**
     * Descifra una cadena previamente cifrada con este algoritmo.
     */
    public static String decode(String encodedData, String ignoredKey) {
        if (encodedData == null) return null;
        try {
            byte[] combined = Base64.getDecoder().decode(encodedData);
            if (combined.length < GCM_IV_LENGTH) {
                AppLogger.e(TAG, "Cifrado inválido: longitud insuficiente", null);
                return null;
            }

            // Segmentación de la estructura binaria.
            ByteBuffer buffer = ByteBuffer.wrap(combined);
            byte[] iv = new byte[GCM_IV_LENGTH];
            buffer.get(iv);
            byte[] encrypted = new byte[buffer.remaining()];
            buffer.get(encrypted);

            SecretKey key = getDerivedKey();
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);

            byte[] plainBytes = cipher.doFinal(encrypted);
            return new String(plainBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            AppLogger.e(TAG, "Fallo al descifrar caché pública", e);
            return null;
        }
    }
}


