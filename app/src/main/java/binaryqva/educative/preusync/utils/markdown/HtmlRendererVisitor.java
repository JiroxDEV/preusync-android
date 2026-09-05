/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: HtmlRendererVisitor.java
 * Versión: v1.8.9
 * Descripción: Visitador de nodos Markdown para generación de HTML.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.utils.markdown;

/**
 * Implementa el patrón Visitor para recorrer el árbol sintáctico de Markdown 
 * y convertir cada nodo (párrafos, títulos, listas) en su etiqueta HTML equivalente.
 */
public class HtmlRendererVisitor {

    private final HtmlWriter writer = new HtmlWriter();

    public void visitHeading(int level, String text) {
        writer.tag("h" + level); writer.text(text); writer.close("h" + level);
    }

    public void visitParagraph(String text) {
        writer.tag("p"); writer.text(text); writer.close("p");
    }

    public void visitCodeBlock(String code, String lang) {
        writer.text(SyntaxHighlighter.highlight(code, lang));
    }

    public String getHtml() { return writer.toString(); }
}


