/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: AvatarHelper.java
 * Versión: v1.0.1
 * Descripción: Utilidad para la generación dinámica de avatares de usuario
 *              basados en iniciales de nombre o iconos representativos de rol.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.utils.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import binaryqva.educative.preusync.R;
import binaryqva.educative.preusync.utils.theme.ThemeManager;

/**
 * Proporciona métodos para crear representaciones gráficas temporales de perfil
 * cuando el usuario no ha subido una fotografía personalizada.
 */
public class AvatarHelper {

    /**
     * Genera un círculo de color sólido con la inicial del nombre centrada.
     * @param context Contexto de la App.
     * @param initial Letra inicial a dibujar.
     * @param accentColor Color de fondo del avatar.
     * @return Bitmap de 256x256 píxeles con el avatar generado.
     */
    public static Bitmap generateInitialAvatar(Context context, String initial, int accentColor) {
        int size = 256;
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        
        // Dibujo del fondo circular.
        paint.setAntiAlias(true);
        paint.setColor(accentColor);
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint);
        
        // Configuración de tipografía para la inicial.
        paint.setColor(ThemeManager.getThemeColor(context, R.attr.colorBackground));
        paint.setTextSize(size * 0.5f);
        paint.setTextAlign(Paint.Align.CENTER);
        
        Rect bounds = new Rect();
        paint.getTextBounds(initial, 0, 1, bounds);
        float x = size / 2f;
        float y = size / 2f + (bounds.height() / 2f);
        
        canvas.drawText(initial, x, y, paint);
        return bitmap;
    }

    /**
     * Crea un avatar circular que contiene un icono representativo del rol del usuario.
     * @param roleValue Identificador técnico del rol (student, teacher, etc).
     */
    public static Bitmap generateRoleAvatar(Context context, String roleValue, int accentColor) {
        int size = 256;
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        
        paint.setAntiAlias(true);
        paint.setColor(accentColor);
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint);

        // Obtención del recurso de icono asociado al rol.
        int iconRes = RoleHelper.getRoleIconResId(roleValue);
        Drawable drawable = ContextCompat.getDrawable(context, iconRes);
        
        if (drawable != null) {
            // Aplicación de filtro de color para contraste con el fondo.
            drawable.setColorFilter(ThemeManager.getThemeColor(context, R.attr.colorBackground), PorterDuff.Mode.MULTIPLY);
            // Ajuste del margen interno (padding del 20%) para el icono.
            int padding = (20 * size / 100);
            drawable.setBounds(padding, padding, size - padding, size - padding);
            drawable.draw(canvas);
        }
        return bitmap;
    }
}


