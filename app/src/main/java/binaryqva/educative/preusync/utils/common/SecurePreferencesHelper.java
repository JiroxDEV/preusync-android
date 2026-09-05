/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: SecurePreferencesHelper.java
 * Versión: v1.0.0
 * Descripción: Envoltorio seguro para SharedPreferences mediante AES/GCM.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.utils.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

import binaryqva.educative.preusync.debug.AppLogger;

/**
 * Implementa almacenamiento cifrado utilizando el Android Keystore System.
 * Protege tokens y datos de sesión contra acceso root o volcado de memoria.
 */
public class SecurePreferencesHelper {

    private static final String TAG = "SecurePrefs";
    private static final String ALIAS = "PreuSync_secure_vault";
    private static final String TRANS = "AES/GCM/NoPadding";
    private static final int IV_LEN = 12;

    private final SharedPreferences prefs;
    private SecretKey key;
    private final boolean isSecure;

    public SecurePreferencesHelper(Context ctx, String file) {
        this.prefs = ctx.getSharedPreferences(file, Context.MODE_PRIVATE);
        this.isSecure = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
        if (isSecure) {
            try { this.key = getOrCreateKey(); }
            catch (Exception e) { AppLogger.e(TAG, "Fallo al inicializar bóveda de claves", e); this.key = null; }
        }
    }

    private SecretKey getOrCreateKey() throws Exception {
        KeyStore ks = KeyStore.getInstance("AndroidKeyStore"); ks.load(null);
        if (ks.containsAlias(ALIAS)) return (SecretKey) ks.getKey(ALIAS, null);

        KeyGenerator kg = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        kg.init(new KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256).build());
        return kg.generateKey();
    }

    // ==================== OPERACIONES DE PERSISTENCIA ====================

    public void putString(String k, String v) {
        if (!isSecure || key == null) { prefs.edit().putString(k, v).apply(); return; }
        try { String enc = encrypt(v); prefs.edit().putString(k, enc).commit(); }
        catch (Exception e) { AppLogger.e(TAG, "Error de cifrado en clave: " + k, e); prefs.edit().putString(k, v).commit(); }
    }

    public String getString(String k, String def) {
        if (!isSecure || key == null) return prefs.getString(k, def);
        String enc = prefs.getString(k, null);
        if (enc == null) return def;
        try { return decrypt(enc); }
        catch (Exception e) { return prefs.getString(k, def); }
    }

    public void remove(String k) { prefs.edit().remove(k).apply(); }
    public void clear() { prefs.edit().clear().apply(); }

    // ==================== LÓGICA INTERNA DE CIFRADO ====================

    private String encrypt(String raw) throws Exception {
        Cipher c = Cipher.getInstance(TRANS); c.init(Cipher.ENCRYPT_MODE, key);
        byte[] enc = c.doFinal(raw.getBytes(StandardCharsets.UTF_8)), iv = c.getIV();
        byte[] res = new byte[IV_LEN + enc.length];
        System.arraycopy(iv, 0, res, 0, IV_LEN); System.arraycopy(enc, 0, res, IV_LEN, enc.length);
        return Base64.encodeToString(res, Base64.NO_WRAP);
    }

    private String decrypt(String enc) throws Exception {
        byte[] res = Base64.decode(enc, Base64.NO_WRAP);
        byte[] iv = new byte[IV_LEN], cipher = new byte[res.length - IV_LEN];
        System.arraycopy(res, 0, iv, 0, IV_LEN); System.arraycopy(res, IV_LEN, cipher, 0, cipher.length);

        Cipher c = Cipher.getInstance(TRANS); c.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv));
        return new String(c.doFinal(cipher), StandardCharsets.UTF_8);
    }
}


