/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: SimplePreprocessor.java
 * Versión: v1.0.0
 * Descripción: Preprocesador de texto Markdown. Realiza saneamiento y
 *              ajustes previos al renderizado final.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.utils.markdown;

/**
 * Corrige errores comunes de formato en el texto bruto antes de ser procesado 
 * por los parsers de tablas, LaTeX o código.
 */
public class SimplePreprocessor {

    /**
     * Limpia y normaliza el texto Markdown.
     */
    public static String process(String text) {
        if (text == null) return "";
        
        // 1. Unificación de saltos de línea (Windows/Unix).
        String body = text.replace("\r\n", "\n").replace("\r", "\n");

        // 2. Procesamiento de tablas (debe ocurrir antes que el renderizado de bloques).
        body = TableParser.parse(body);

        // 3. Procesamiento de LaTeX.
        body = LatexRenderer.render(body);

        // 4. Procesamiento de listas de tareas.
        body = TaskListParser.parse(body);

        return body;
    }
}


