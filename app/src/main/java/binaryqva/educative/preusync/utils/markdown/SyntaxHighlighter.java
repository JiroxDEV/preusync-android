/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: SyntaxHighlighter.java
 * Versión: v1.0.5
 * Descripción: Sistema de resaltado de sintaxis para bloques de código.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.utils.markdown;

/**
 * Prepara los bloques de código Markdown para ser coloreados mediante
 * librerías externas como Prism.js en el entorno del WebView.
 */
public class SyntaxHighlighter {

    /**
     * Transforma bloques de código en estructuras HTML con clases de lenguaje.
     */
    public static String highlight(String code, String lang) {
        if (code == null) return "";
        String l = (lang == null || lang.isEmpty()) ? "none" : lang.toLowerCase();
        return "<pre><code class=\"language-" + l + "\">" + escapeHtml(code) + "</code></pre>";
    }

    private static String escapeHtml(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                   .replace("\"", "&quot;").replace("'", "&#039;");
    }
}


