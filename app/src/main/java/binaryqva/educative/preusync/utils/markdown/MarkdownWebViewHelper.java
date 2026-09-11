/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: MarkdownWebViewHelper.java
 * Versión: v2.6.0
 * Descripción: Orquestador de visualización de Markdown.
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
import android.webkit.WebViewClient;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public class MarkdownWebViewHelper {

    private static final Map<String, String> htmlCache = new HashMap<>();

    public static void replaceWithWebView(Context context, TextView target, String markdown, String cacheKey, int loaderId) {
        if (target == null || markdown == null) return;

        ViewGroup parent = (ViewGroup) target.getParent();
        if (parent == null) return;

        int index = parent.indexOfChild(target);
        String html;

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
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);

        parent.removeView(target);
        parent.addView(webView, index);

        View loader = parent.findViewById(loaderId);
        if (loader != null) loader.setVisibility(View.VISIBLE);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                if (loader != null) loader.setVisibility(View.GONE);
            }
        });

        webView.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "UTF-8", null);
    }

    public static void invalidateCache() { htmlCache.clear(); }
    public static void invalidateCache(String key) { htmlCache.remove(key); }
}
