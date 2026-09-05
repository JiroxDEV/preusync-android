/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: Prism4jGrammarParser.java
 * Versión: v1.1.0
 * Descripción: Parser de gramáticas para Prism4j.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.utils.markdown;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Procesa las definiciones léxicas de los lenguajes de programación.
 * Integra el sistema de carga de gramáticas con el motor de renderizado Markdown.
 */
public class Prism4jGrammarParser {

    private final AssetsGrammarLocator locator;

    public Prism4jGrammarParser(@NonNull AssetsGrammarLocator locator) { this.locator = locator; }

    /**
     * Resuelve y retorna el código HTML resaltado para un fragmento de código.
     */
    public String parse(@NonNull String code, @Nullable String lang) {
        if (lang == null || lang.isEmpty()) return SyntaxHighlighter.highlight(code, "none");
        
        // Simulación de integración con motor Prism4j nativo/JS.
        // En esta versión se delega el coloreado al CSS/JS del WebView.
        return SyntaxHighlighter.highlight(code, lang);
    }
}


