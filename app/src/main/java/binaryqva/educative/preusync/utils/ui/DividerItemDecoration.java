/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: DividerItemDecoration.java
 * Versión: v1.0.0
 * Descripción: Decorador de listas para el dibujado de divisores personalizados.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.utils.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import binaryqva.educative.preusync.utils.theme.ThemeManager;

/**
 * Añade una línea de separación entre los elementos de un RecyclerView, 
 * con soporte para márgenes laterales y colores dinámicos del tema.
 */
public class DividerItemDecoration extends RecyclerView.ItemDecoration {

    private final Drawable divider;
    private final int leftMargin, rightMargin;

    public DividerItemDecoration(Context ctx, int attrColor, int leftDp, int rightDp) {
        this.divider = new ColorDrawable(ThemeManager.getThemeColor(ctx, attrColor));
        float d = ctx.getResources().getDisplayMetrics().density;
        this.leftMargin = (int) (leftDp * d);
        this.rightMargin = (int) (rightDp * d);
    }

    @Override
    public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView p, @NonNull RecyclerView.State s) {
        if (divider == null) return;

        int count = p.getChildCount();
        for (int i = 0; i < count - 1; i++) {
            View child = p.getChildAt(i);
            if (child == null) continue;

            int top = child.getBottom();
            int bottom = top + 2; // Altura fija de 2px para el divisor.

            int left = p.getPaddingLeft() + leftMargin;
            int right = p.getWidth() - p.getPaddingRight() - rightMargin;

            divider.setBounds(left, top, right, bottom);
            divider.draw(c);
        }
    }
}


