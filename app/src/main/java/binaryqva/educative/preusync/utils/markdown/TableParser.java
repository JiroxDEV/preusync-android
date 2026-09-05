/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: TableParser.java
 * Versión: v1.0.0
 * Descripción: Parser manual de tablas GFM (GitHub Flavored Markdown).
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.utils.markdown;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Convierte estructuras de texto delimitadas por tuberías (|) en tablas HTML.
 * Asegura la correcta visualización de datos tabulares en los posts académicos.
 */
public class TableParser {

    private static final String TABLE_ROW = "^\\|(.*)\\|\\s*$";

    /**
     * Analiza una porción de texto y retorna la representación HTML de la tabla.
     */
    public static String parse(String text) {
        if (text == null || !text.contains("|")) return text;

        StringBuilder sb = new StringBuilder();
        String[] lines = text.split("\n");
        boolean inTable = false;

        for (String line : lines) {
            if (line.matches(TABLE_ROW)) {
                if (!inTable) { sb.append("<table class='m-table'>"); inTable = true; }
                sb.append("<tr>");
                String[] cells = line.split("\\|");
                for (String cell : cells) {
                    if (cell.trim().isEmpty() && cells.length > 1) continue;
                    sb.append("<td>").append(cell.trim()).append("</td>");
                }
                sb.append("</tr>");
            } else {
                if (inTable) { sb.append("</table>"); inTable = false; }
                sb.append(line).append("\n");
            }
        }
        if (inTable) sb.append("</table>");
        return sb.toString();
    }
}


