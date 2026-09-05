/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: ImageBlurHelper.java
 * Versión: v1.1.0
 * Descripción: Ayudante para la aplicación de efectos de desenfoque (Blur).
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.utils.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.renderscript.Toolkit;

import binaryqva.educative.preusync.R;
import binaryqva.educative.preusync.debug.AppLogger;
import binaryqva.educative.preusync.utils.common.PreferenceManager;
import binaryqva.educative.preusync.utils.theme.ThemeManager;

/**
 * Implementa el efecto de desenfoque Gaussiano sobre componentes de UI.
 * Utiliza Renderscript Toolkit para un rendimiento óptimo de procesamiento.
 */
public class ImageBlurHelper {
    private static final float MAX_RADIUS = 25f, MIN_RADIUS = 1f;
    private final Context context;

    public ImageBlurHelper(Context context) {
        if (context == null) throw new IllegalArgumentException("Contexto obligatorio");
        this.context = context.getApplicationContext();
    }

    /**
     * Aplica desenfoque al fondo de un contenedor LinearLayout.
     */
    public void applyBlurToLinearLayout(LinearLayout layout, float radius, float downscale) {
        if (layout == null) return;
        if (!PreferenceManager.getInstance(context).isBlurEnabled()) {
            layout.setBackgroundColor(ThemeManager.getThemeColor(context, R.attr.colorBackgroundCard));
            return;
        }

        Bitmap blurred = captureAndBlur(layout, radius, downscale);
        if (blurred != null) layout.setBackground(new BitmapDrawable(context.getResources(), blurred));
    }

    /**
     * Aplica desenfoque directamente al contenido de un ImageView.
     */
    public void applyBlurToImageView(ImageView iv, float radius, float downscale) {
        if (iv == null || iv.getDrawable() == null || !PreferenceManager.getInstance(context).isBlurEnabled()) return;

        Bitmap raw = drawableToBitmap(iv.getDrawable());
        Bitmap scaled = Bitmap.createScaledBitmap(raw, (int)(raw.getWidth()/downscale), (int)(raw.getHeight()/downscale), false);
        iv.setImageBitmap(applyToolkitBlur(scaled, radius));
    }

    /**
     * Procesa la captura de la pantalla bajo una vista para generar un efecto de cristal.
     */
    private Bitmap captureAndBlur(View target, float radius, float downscale) {
        View root = target.getRootView();
        if (root == null || target.getWidth() <= 0) return null;

        int[] loc = new int[2]; target.getLocationOnScreen(loc);
        Bitmap bmp = Bitmap.createBitmap((int)(target.getWidth()/downscale), (int)(target.getHeight()/downscale), Bitmap.Config.ARGB_8888);
        
        Canvas canvas = new Canvas(bmp);
        canvas.scale(1f/downscale, 1f/downscale);
        canvas.translate(-loc[0], -loc[1]);
        root.draw(canvas);

        return applyToolkitBlur(bmp, radius);
    }

    private Bitmap applyToolkitBlur(Bitmap bmp, float r) {
        if (bmp == null) return null;
        try { return Toolkit.INSTANCE.blur(bmp, (int)Math.max(MIN_RADIUS, Math.min(MAX_RADIUS, r))); }
        catch (Exception e) { AppLogger.e("BlurError", "Fallo al aplicar Renderscript", e); return bmp; }
    }

    private Bitmap drawableToBitmap(Drawable d) {
        if (d instanceof BitmapDrawable) return ((BitmapDrawable) d).getBitmap();
        int w = Math.max(1, d.getIntrinsicWidth()), h = Math.max(1, d.getIntrinsicHeight());
        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp); d.setBounds(0, 0, w, h); d.draw(c);
        return bmp;
    }
}


