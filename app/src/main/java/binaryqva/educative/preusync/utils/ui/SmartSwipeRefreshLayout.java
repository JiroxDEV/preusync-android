/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: SmartSwipeRefreshLayout.java
 * Versión: v4.1.9
 * Descripción: Layout de refresco táctil optimizado para evitar interferencias
 *              con la navegación horizontal (ViewPager2).
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.utils.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import binaryqva.educative.preusync.debug.AppLogger;

/**
 * Implementa una detección de gestos estricta. Requiere que la intención
 * de desplazamiento vertical sea significativamente superior a la horizontal
 * antes de activar el indicador de carga.
 */
public class SmartSwipeRefreshLayout extends CustomSwipeRefreshLayout {

    private static final String TAG = "SmartSwipeRefresh";

    private View targetScrollableView;
    private int touchSlop;
    private float startX, startY;
    private boolean isDragging;
    private int minVerticalDragPx;

    public SmartSwipeRefreshLayout(Context context) { super(context); init(context); }
    public SmartSwipeRefreshLayout(Context context, AttributeSet attrs) { super(context, attrs); init(context); }

    private void init(Context context) {
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        // Umbral de activación: 50dp de desplazamiento vertical descendente.
        minVerticalDragPx = (int) (50 * context.getResources().getDisplayMetrics().density);
    }

    /**
     * Vincula el componente de scroll objetivo (RecyclerView/ScrollView)
     * para consultar su capacidad de desplazamiento ascendente.
     */
    public void setTargetScrollableView(View v) { this.targetScrollableView = v; }
    public void setTargetRecyclerView(View v) { this.targetScrollableView = v; }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (targetScrollableView == null) return super.onInterceptTouchEvent(ev);

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = ev.getX(); startY = ev.getY(); isDragging = false;
                return false;

            case MotionEvent.ACTION_MOVE:
                float dx = Math.abs(ev.getX() - startX);
                float dy = ev.getY() - startY;

                if (Math.abs(dy) > minVerticalDragPx) isDragging = true;

                if (isDragging) {
                    // REGLA DE ORO: dy > dx * 3.0f (Gesto vertical puro hacia abajo)
                    // Y la vista objetivo debe estar en el tope superior.
                    if (dy > dx * 3.0f && !targetScrollableView.canScrollVertically(-1)) {
                        return super.onInterceptTouchEvent(ev);
                    }
                    return false; // Ignoramos si es un gesto diagonal u horizontal.
                }
                return false;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isDragging = false;
                break;
        }
        return false;
    }
}


