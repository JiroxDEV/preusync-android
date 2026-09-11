/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: MarkdownHtmlGenerator.java
 * Versión: v2.2.0
 * Descripción: Generador de envoltorios HTML para contenido Markdown.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.utils.markdown;

import android.content.Context;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import binaryqva.educative.preusync.utils.theme.ThemeManager;

public class MarkdownHtmlGenerator {

    public static String generate(Context ctx, String markdown) {
        if (markdown == null) return "";
        
        // 1. Preprocesamiento básico
        String body = markdown.replace("\r\n", "\n").replace("\r", "\n");
        
        // 2. Conversión a HTML
        Parser parser = Parser.builder().build();
        Node document = parser.parse(body);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        String htmlContent = renderer.render(document);

        // 3. Adaptación de Estilo
        int tC = ThemeManager.getThemeColor(ctx, android.R.attr.textColorPrimary);
        String textColor = String.format("#%06X", (0xFFFFFF & tC));

        return "<!DOCTYPE html><html><head>" +
               "<meta charset=\"UTF-8\">" +
               "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, user-scalable=no\">" +
               "<link rel=\"stylesheet\" href=\"file:///android_asset/markdown-parser/html/custom.css\">" +
               "<style>" +
               "body { color: " + textColor + "; background-color: transparent; font-family: -apple-system, sans-serif; padding: 16px; line-height: 1.6; word-wrap: break-word; }" +
               "h1, h2, h3 { color: " + textColor + "; margin-top: 24px; margin-bottom: 12px; }" +
               "p { margin-bottom: 16px; }" +
               "ul, ol { padding-left: 24px; margin-bottom: 16px; }" +
               "li { margin-bottom: 8px; }" +
               "strong { font-weight: bold; }" +
               "em { font-style: italic; }" +
               "</style></head><body>" + htmlContent + "</body></html>";
    }
}
