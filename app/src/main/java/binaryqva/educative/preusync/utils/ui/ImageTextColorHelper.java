/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: ImageTextColorHelper.java
 * Versión: v1.0.2
 * Descripción: Ayudante para el contraste adaptativo de texto sobre imágenes.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.utils.ui;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Analiza la luminancia de los píxeles de una imagen para decidir el color
 * óptimo del texto superpuesto, asegurando la legibilidad (WCAG).
 */
public class ImageTextColorHelper {

    /**
     * Ajusta dinámicamente el color de un TextView basándose en el fondo.
     */
    public static void adjustTextColor(ImageView backgroundImage, TextView textView) {
        if (backgroundImage == null || textView == null) return;

        try {
            if (backgroundImage.getDrawable() instanceof BitmapDrawable) {
                Bitmap bitmap = ((BitmapDrawable) backgroundImage.getDrawable()).getBitmap();
                if (bitmap == null) return;

                // Muestreo de 9 puntos cardinales para evaluar la luminancia media.
                int[] points = new int[9];
                int w = bitmap.getWidth(), h = bitmap.getHeight();
                points[0] = bitmap.getPixel(w/4, h/4); points[1] = bitmap.getPixel(w/2, h/4); points[2] = bitmap.getPixel(3*w/4, h/4);
                points[3] = bitmap.getPixel(w/4, h/2); points[4] = bitmap.getPixel(w/2, h/2); points[5] = bitmap.getPixel(3*w/4, h/2);
                points[6] = bitmap.getPixel(w/4, 3*h/4); points[7] = bitmap.getPixel(w/2, 3*h/4); points[8] = bitmap.getPixel(3*w/4, 3*h/4);

                double luminance = 0;
                for (int c : points) {
                    double r = linearize(Color.red(c)), g = linearize(Color.green(c)), b = linearize(Color.blue(c));
                    luminance += (0.2126 * r + 0.7152 * g + 0.0722 * b);
                }
                double avg = luminance / points.length;

                // Aplicación de color y sombra para maximizar contraste.
                if (avg > 0.5) applyTheme(textView, Color.BLACK, Color.WHITE);
                else applyTheme(textView, Color.WHITE, Color.BLACK);
            }
        } catch (Exception e) {
            applyTheme(textView, Color.BLACK, Color.WHITE);
        }
    }

    private static double linearize(int val) {
        double v = val / 255.0;
        return (v <= 0.03928) ? v / 12.92 : Math.pow((v + 0.055) / 1.055, 2.4);
    }

    private static void applyTheme(TextView tv, int text, int shadow) {
        tv.setTextColor(text);
        tv.setShadowLayer(2, 1, 1, shadow);
    }
}


