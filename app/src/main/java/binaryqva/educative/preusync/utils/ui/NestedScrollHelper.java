/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: NestedScrollHelper.java
 * Versión: v2.0.3
 * Descripción: Ayudante para la gestión de scroll anidado. Resuelve conflictos
 *              de gestos entre carruseles horizontales y ViewPagers.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.utils.ui;

import android.view.MotionEvent;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Implementa lógica de intercepción de gestos para priorizar el scroll 
 * del componente interno (hijo) sobre el contenedor (padre).
 */
public class NestedScrollHelper {

    /**
     * Configura un RecyclerView horizontal para que tome control exclusivo del 
     * gesto táctil mientras el usuario desliza sobre sus elementos.
     */
    public static void applyHorizontalScrollPriority(@NonNull RecyclerView recyclerView) {
        recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                // Al detectar el inicio del toque, bloqueamos la intercepción del padre (ViewPager2).
                if (e.getAction() == MotionEvent.ACTION_DOWN) rv.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }

            @Override public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {}
            @Override public void onRequestDisallowInterceptTouchEvent(boolean disallow) {}
        });
    }
}


