/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: MarkdownWebViewHelper.java
 * Versión: v2.7.0
 * Descripción: Orquestador de visualización de Markdown con scroll mejorado.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.utils.markdown;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.MotionEvent;
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

    @SuppressLint("ClickableViewAccessibility")
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

        // BLOQUEO DE INTERCEPCIÓN DE TOUCH: Garantiza scroll fluido dentro de ViewPager2
        webView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                    break;
            }
            return false;
        });

        webView.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "UTF-8", null);
    }

    public static void invalidateCache() { htmlCache.clear(); }
    public static void invalidateCache(String key) { htmlCache.remove(key); }
}
