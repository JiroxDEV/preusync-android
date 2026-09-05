/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: AssetsGrammarLocator.java
 * Versión: v1.0.0
 * Descripción: Localizador de gramáticas Prism4j en la carpeta assets.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.utils.markdown;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.InputStream;
import java.util.Scanner;

/**
 * Carga definiciones de lenguajes de programación (.js) desde el sistema 
 * de archivos de la App para ser inyectadas en el motor de resaltado.
 */
public class AssetsGrammarLocator {

    private final Context context;

    public AssetsGrammarLocator(@NonNull Context context) { this.context = context; }

    /**
     * Recupera el código fuente de una gramática Prism específica.
     */
    @Nullable
    public String getGrammar(@NonNull String language) {
        try {
            InputStream is = context.getAssets().open("prism/prism-" + language + ".js");
            Scanner s = new Scanner(is).useDelimiter("\\A");
            return s.hasNext() ? s.next() : null;
        } catch (Exception e) { return null; }
    }
}


