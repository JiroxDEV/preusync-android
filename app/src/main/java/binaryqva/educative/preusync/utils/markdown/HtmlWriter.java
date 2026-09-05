/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: HtmlWriter.java
 * Versión: v1.0.5
 * Descripción: Escritor secuencial de HTML para el renderizador.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.utils.markdown;

import androidx.annotation.NonNull;

/**
 * Provee un búfer eficiente para la construcción de cadenas HTML, 
 * facilitando la indentación y el escapado de caracteres.
 */
public class HtmlWriter {

    private final StringBuilder sb = new StringBuilder();

    public void tag(@NonNull String name) { sb.append("<").append(name).append(">"); }
    public void tag(@NonNull String name, @NonNull String attr) { sb.append("<").append(name).append(" ").append(attr).append(">"); }
    public void close(@NonNull String name) { sb.append("</").append(name).append(">"); }
    public void text(@NonNull String text) { sb.append(text); }

    @NonNull @Override public String toString() { return sb.toString(); }
}


