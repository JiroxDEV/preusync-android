/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: MarkdownHtmlGenerator.java
 * Versión: v2.1.0
 * Descripción: Generador de envoltorios HTML para contenido Markdown.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.utils.markdown;

import android.content.Context;
import binaryqva.educative.preusync.utils.theme.ThemeManager;

/**
 * Construye la estructura base del documento HTML, incluyendo CSS dinámico
 * adaptado al tema actual y scripts para renderizado de fórmulas.
 */
public class MarkdownHtmlGenerator {

    /**
     * Produce un documento HTML5 completo listo para ser cargado en un WebView.
     */
    public static String generate(Context ctx, String markdown) {
        // Preprocesamiento para corregir inconsistencias del parser.
        String body = SimplePreprocessor.process(markdown);
        
        // Extracción de colores del tema activo para inyección en el CSS.
        int tC = ThemeManager.getThemeColor(ctx, android.R.attr.textColorPrimary);
        String textColor = String.format("#%06X", (0xFFFFFF & tC));

        return "<!DOCTYPE html><html><head>" +
               "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, user-scalable=no\">" +
               "<link rel=\"stylesheet\" href=\"file:///android_asset/markdown.css\">" +
               "<script src=\"file:///android_asset/mathjax/tex-chtml.js\" async></script>" +
               "<style>body { color: " + textColor + "; background-color: transparent; font-family: sans-serif; }</style>" +
               "</head><body>" + body + "</body></html>";
    }
}


