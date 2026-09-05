/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: NumberFormatUtil.java
 * Versión: v1.0.0
 * Descripción: Utilidad para el formateo de números grandes (K, M, B).
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.utils.common;

import java.text.DecimalFormat;

/**
 * Compacta cifras numéricas para su correcta visualización en interfaces móviles.
 */
public class NumberFormatUtil {
    
    /**
     * Convierte un número largo en una cadena legible (ej: 1500 -> 1.5 K).
     */
    public static String formatNumber(long number) {
        if (number < 1000) return String.valueOf(number);
        
        String[] units = {"", "K", "M", "B", "T"};
        int index = (int) (Math.log10(number) / 3);
        double val = number / Math.pow(1000, index);
        
        DecimalFormat df = new DecimalFormat(val < 10 ? "#.##" : "#");
        return df.format(val) + " " + units[index];
    }
}


