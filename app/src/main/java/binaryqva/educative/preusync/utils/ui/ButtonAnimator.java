/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: ButtonAnimator.java
 * Versión: v1.1.0
 * Descripción: Utilidad para la animación reactiva de botones. Gestiona el
 *              cambio dinámico de radio de bordes al presionar.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.utils.ui;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import binaryqva.educative.preusync.R;
import binaryqva.educative.preusync.utils.common.AppUtils;

/**
 * Provee un efecto visual de "morfismo" en los botones Material, suavizando
 * los bordes durante la interacción táctil.
 */
public class ButtonAnimator {

    private static int normalRadius = -1;
    private static int pressedRadius = -1;

    /**
     * Aplica recursivamente animaciones de interacción a todos los botones
     * encontrados dentro de una jerarquía de vistas.
     */
    public static void applyTo(View root) {
        if (root == null) return;

        // Carga de dimensiones estándar desde recursos (Lazy Init).
        if (normalRadius == -1) {
            try {
                normalRadius = (int) root.getResources().getDimension(R.dimen.button_corner_radius_normal);
                pressedRadius = (int) root.getResources().getDimension(R.dimen.button_corner_radius_pressed);
            } catch (Exception e) {
                float d = root.getResources().getDisplayMetrics().density;
                normalRadius = (int) (14 * d); pressedRadius = (int) (4 * d);
            }
        }
        applyToView(root);
    }

    private static void applyToView(View view) {
        // Omitimos componentes que ya tienen sus propias animaciones complejas o gestión de bordes especial.
        if (view instanceof ExtendedFloatingActionButton) return;
        if (view.getParent() instanceof MaterialButtonToggleGroup) return;

        if (view instanceof MaterialButton) {
            setupAnimation((MaterialButton) view);
        } else if (view instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) view;
            for (int i = 0; i < g.getChildCount(); i++) {
                applyToView(g.getChildAt(i));
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private static void setupAnimation(MaterialButton button) {
        // Evitamos reaplicar la animación si ya está configurada.
        if (button.getTag(R.id.tag_animation_applied) != null) return;
        button.setTag(R.id.tag_animation_applied, true);

        button.setOnTouchListener((v, event) -> {
            Context ctx = button.getContext();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    animateCornerRadius(button, normalRadius, pressedRadius, AppUtils.getScaledDuration(ctx, 100));
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    animateCornerRadius(button, pressedRadius, normalRadius, AppUtils.getScaledDuration(ctx, 150));
                    break;
            }
            return false;
        });
    }

    private static void animateCornerRadius(MaterialButton button, int from, int to, int duration) {
        // Cancelamos cualquier animación previa en este botón para evitar conflictos visuales.
        Object oldAnim = button.getTag(R.id.tag_current_animator);
        if (oldAnim instanceof ValueAnimator) {
            ((ValueAnimator) oldAnim).cancel();
        }

        ValueAnimator anim = ValueAnimator.ofInt(from, to);
        anim.setDuration(duration);
        anim.addUpdateListener(animation -> button.setCornerRadius((int) animation.getAnimatedValue()));
        
        button.setTag(R.id.tag_current_animator, anim);
        anim.start();
    }
}


