/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: Validator.java
 * Versión: v1.0.8
 * Descripción: Motor de validación de datos de entrada. Incluye reglas para
 *              contraseñas seguras, nombres de usuario e IDs cubanos.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.utils.common;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Centraliza la lógica de validación de formularios.
 * Implementa listas negras de contraseñas y patrones Regex estrictos.
 */
public class Validator {

    // ==================== VALIDACIÓN DE CONTRASEÑAS ====================
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 128;
    
    // Requiere: 1 Mayúscula, 1 Minúscula, 1 Número y 1 Carácter Especial.
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*()\\-_=+{};:,<.>/?]).+$"
    );
    
    private static final Set<String> commonPasswords = new HashSet<>();
    private static boolean commonPasswordsLoaded = false;
    private static Context applicationContext;

    /**
     * Carga el diccionario de contraseñas vulnerables desde assets en segundo plano.
     */
    public static void initializePasswordValidator(Context context) {
        applicationContext = context.getApplicationContext();
        loadCommonPasswords();
    }

    private static void loadCommonPasswords() {
        new Thread(() -> {
            try {
                InputStream inputStream = applicationContext.getAssets().open("top_10k_common_passwords.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    commonPasswords.add(line.trim().toLowerCase());
                }
                reader.close();
                commonPasswordsLoaded = true;
            } catch (IOException e) {
                // Fallback: Lista básica de seguridad si falla la carga del archivo.
                commonPasswords.add("123456"); commonPasswords.add("password");
                commonPasswordsLoaded = true;
            }
        }).start();
    }

    /**
     * Verifica si la contraseña cumple con los estándares de seguridad modernos.
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.isEmpty()) return false;
        if (password.length() < MIN_PASSWORD_LENGTH || password.length() > MAX_PASSWORD_LENGTH) return false;
        if (!PASSWORD_PATTERN.matcher(password).matches()) return false;
        
        // Rechaza si es una contraseña trivial (ej. qwerty).
        if (commonPasswordsLoaded && commonPasswords.contains(password.toLowerCase())) return false;
        
        return hasNoRepeatedCharacters(password) && hasNoPersonalInfo(password);
    }

    private static boolean hasNoRepeatedCharacters(String password) {
        // Rechaza más de 2 caracteres idénticos consecutivos (ej. aaa).
        return !password.matches(".*(.)\\1{2,}.*");
    }

    private static boolean hasNoPersonalInfo(String password) {
        String[] forbidden = {"nombre", "apellido", "usuario", "fecha"};
        for (String word : forbidden) if (password.toLowerCase().contains(word)) return false;
        return true;
    }

    // ==================== VALIDACIÓN DE IDENTIDAD ====================

    // Formato alfanumérico básico para usernames (5-30 chars).
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{5,30}$");

    public static boolean isValidUsername(String username) {
        return username != null && USERNAME_PATTERN.matcher(username).matches();
    }

    // Patrón para nombres y apellidos con soporte de caracteres Unicode (tildes, eñes).
    private static final Pattern NAME_PATTERN = Pattern.compile("^[\\p{L}\\-'\\s]{2,40}$");

    public static boolean isValidFirstName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) return false;
        String normalized = fullName.trim().replaceAll("\\s+", " ");
        if (normalized.length() < 2 || normalized.length() > 80) return false;
        return NAME_PATTERN.matcher(normalized).matches();
    }

    public static boolean isValidLastName(String lastName) {
        return isValidFirstName(lastName);
    }

    /**
     * Valida el número de identidad de Cuba (11 dígitos o nuevo formato extendido).
     */
    public static boolean isValidCubanCI(String ciNumber) {
        if (ciNumber == null || ciNumber.isEmpty()) return false;
        String cleanCI = ciNumber.trim().replaceAll("[\\s-]", "").toUpperCase();
        
        // Expresión para 11 dígitos o 11 dígitos + 2 letras finales.
        Pattern ciPattern = Pattern.compile("^(\\d{11}|\\d{11}[A-Z]{2})$");
        if (!ciPattern.matcher(cleanCI).matches()) return false;
        
        return true;
    }
}


