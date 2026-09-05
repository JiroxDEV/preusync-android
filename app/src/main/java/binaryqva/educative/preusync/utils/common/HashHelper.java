/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: HashHelper.java
 * Versión: v1.0.0
 * Descripción: Utilidad para la generación de hashes SHA-256.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.utils.common;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Provee métodos estáticos para el cifrado unidireccional de datos sensibles.
 */
public class HashHelper {

    private static final String SHA_256 = "SHA-256";

    /**
     * Calcula la huella SHA-256 de una cadena de texto.
     * @return Hash hexadecimal de 64 caracteres.
     */
    public static String sha256(String input) {
        if (input == null) return null;
        try {
            MessageDigest digest = MessageDigest.getInstance(SHA_256);
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    /**
     * Verifica la integridad de un texto comparándolo con su hash esperado.
     */
    public static boolean verifySha256(String plainText, String hash) {
        if (plainText == null || hash == null) return false;
        String computed = sha256(plainText);
        return computed != null && computed.equals(hash);
    }
}


