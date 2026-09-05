/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: LatexRenderer.java
 * Versión: v1.1.4
 * Descripción: Procesador de sintaxis matemática LaTeX.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.utils.markdown;

/**
 * Detecta y formatea bloques de ecuaciones para su posterior interpretación
 * por motores de renderizado web como MathJax.
 */
public class LatexRenderer {

    /**
     * Envuelve las expresiones LaTeX detectadas en etiquetas compatibles.
     */
    public static String render(String text) {
        if (text == null) return "";
        // Soporte para ecuaciones en bloque ($$...$$) y en línea ($...$).
        return text.replaceAll("\\$\\$(.*?)\\$\\$", "<div class='math-block'>\\[$1\\]</div>")
                   .replaceAll("\\$(.*?)\\$", "<span class='math-inline'>\\($1\\)</span>");
    }
}


