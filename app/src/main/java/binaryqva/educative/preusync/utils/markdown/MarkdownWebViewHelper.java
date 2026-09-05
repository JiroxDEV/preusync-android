/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: MarkdownWebViewHelper.java
 * Versión: v2.5.0
 * Descripción: Orquestador de visualización de Markdown. Convierte texto plano
 *              en HTML enriquecido con soporte para fórmulas y código.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.utils.markdown;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import binaryqva.educative.preusync.utils.theme.ThemeManager;

/**
 * Gestiona la sustitución de TextViews por WebViews dinámicos. 
 * Implementa caché de renderizado para evitar conversiones redundantes y mejorar el scroll.
 */
public class MarkdownWebViewHelper {

    private static final Map<String, String> htmlCache = new HashMap<>();

    /**
     * Reemplaza un TextView por un WebView que renderiza contenido Markdown.
     * @param context Contexto de la actividad.
     * @param target TextView a sustituir.
     * @param markdown Texto en formato Markdown.
     * @param cacheKey Identificador único para el contenido (ID del post).
     * @param loaderId ID del componente de carga (ProgressBar).
     */
    public static void replaceWithWebView(Context context, TextView target, String markdown, String cacheKey, int loaderId) {
        if (target == null || markdown == null) return;

        ViewGroup parent = (ViewGroup) target.getParent();
        if (parent == null) return;

        int index = parent.indexOfChild(target);
        String html;

        // Recuperación desde caché para optimizar rendimiento.
        if (cacheKey != null && htmlCache.containsKey(cacheKey)) {
            html = htmlCache.get(cacheKey);
        } else {
            html = MarkdownHtmlGenerator.generate(context, markdown);
            if (cacheKey != null) htmlCache.put(cacheKey, html);
        }

        WebView webView = new WebView(context);
        webView.setLayoutParams(target.getLayoutParams());
        webView.setBackgroundColor(Color.TRANSPARENT);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true); // Necesario para MathJax (LaTeX).
        settings.setDomStorageEnabled(true);

        parent.removeView(target);
        parent.addView(webView, index);

        // Inyección del HTML procesado.
        webView.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "UTF-8", null);

        // Gestión del indicador de carga.
        View loader = parent.findViewById(loaderId);
        if (loader != null) webView.setWebViewClient(new android.webkit.WebViewClient() {
            @Override public void onPageFinished(WebView v, String url) { loader.setVisibility(View.GONE); }
        });
    }

    public static void invalidateCache() { htmlCache.clear(); }
    public static void invalidateCache(String key) { htmlCache.remove(key); }
}


