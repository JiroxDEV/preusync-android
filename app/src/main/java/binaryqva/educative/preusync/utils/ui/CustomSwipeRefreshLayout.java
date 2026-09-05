/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: CustomSwipeRefreshLayout.java
 * Versión: v1.0.0
 * Descripción: Extensión de SwipeRefreshLayout con hooks de progreso.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.utils.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.lang.reflect.Field;

/**
 * Permite capturar el progreso exacto del gesto de arrastre (0.0 a 1.0) 
 * mediante reflexión de campos privados de la clase base.
 */
public class CustomSwipeRefreshLayout extends SwipeRefreshLayout {

    public interface OnDragProgressListener { void onDragProgress(float progress); }

    private OnDragProgressListener listener;
    private int spinnerOffsetEnd = 0, originalOffsetTop = 0;

    public CustomSwipeRefreshLayout(@NonNull Context ctx) { super(ctx); }
    public CustomSwipeRefreshLayout(@NonNull Context ctx, @Nullable AttributeSet attrs) { super(ctx, attrs); }

    public void setOnDragProgressListener(OnDragProgressListener l) { this.listener = l; }

    @Override
    public void onNestedScroll(View t, int dxC, int dyC, int dxU, int dyU, int type, int[] consumed) {
        super.onNestedScroll(t, dxC, dyC, dxU, dyU, type, consumed);
        if (!isRefreshing() && listener != null) listener.onDragProgress(calculateDragProgress());
    }

    /**
     * Extrae dinámicamente las coordenadas del indicador para computar el porcentaje de carga.
     */
    private float calculateDragProgress() {
        try {
            if (spinnerOffsetEnd == 0) {
                Field eF = SwipeRefreshLayout.class.getDeclaredField("mSpinnerOffsetEnd"); eF.setAccessible(true);
                spinnerOffsetEnd = eF.getInt(this);
                Field sF = SwipeRefreshLayout.class.getDeclaredField("mOriginalOffsetTop"); sF.setAccessible(true);
                originalOffsetTop = sF.getInt(this);
            }

            Field cF = SwipeRefreshLayout.class.getDeclaredField("mCircleView"); cF.setAccessible(true);
            View v = (View) cF.get(this);

            int dist = spinnerOffsetEnd - originalOffsetTop;
            if (dist > 0) return Math.max(0, Math.min(1, (float) (v.getTop() - originalOffsetTop) / dist));
        } catch (Exception ignored) {}
        return 0f;
    }
}


