/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: TaskListParser.java
 * Versión: v1.0.0
 * Descripción: Procesador de listas de tareas Markdown ([ ] y [x]).
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.utils.markdown;

/**
 * Transforma indicadores de checkbox en elementos visuales HTML.
 * Ideal para listas de estudio o pasos a seguir en publicaciones.
 */
public class TaskListParser {

    /**
     * Sustituye la sintaxis de tarea por checkboxes HTML de solo lectura.
     */
    public static String parse(String html) {
        if (html == null) return "";
        return html.replace("[ ]", "<input type='checkbox' disabled>")
                   .replace("[x]", "<input type='checkbox' checked disabled>")
                   .replace("[X]", "<input type='checkbox' checked disabled>");
    }
}


