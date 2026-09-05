/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: CustomCircularProgressDrawable.java
 * Versión: v1.0.0
 * Descripción: Indicador de progreso circular personalizado para Glide.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.utils.ui;

import binaryqva.educative.preusync.R;
import binaryqva.educative.preusync.utils.common.AppUtils;
import binaryqva.educative.preusync.utils.theme.ThemeManager;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Reemplaza los spinners genéricos por uno que sigue la línea estética de la App.
 * Utilizado como placeholder durante la carga de imágenes remotas.
 */
public class CustomCircularProgressDrawable extends Drawable implements Animatable {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF arcRect = new RectF();
    private float startAngle = 0f, sweepAngle = 0f;
    private ValueAnimator rotationAnim, sweepAnim;
    private boolean isRunning = false;
    private final float strokeWidth, radius;
    private final int intrinsicSize;
    private final Context context;

    public CustomCircularProgressDrawable(Context ctx, int color, float strokeDp, float sizeDp) {
        this.context = ctx;
        float d = ctx.getResources().getDisplayMetrics().density;
        this.strokeWidth = strokeDp * d;
        this.intrinsicSize = (int) (sizeDp * d);
        this.radius = this.intrinsicSize / 2.2f;

        paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(this.strokeWidth);
        paint.setColor(color); paint.setStrokeCap(Paint.Cap.ROUND);
    }

    public CustomCircularProgressDrawable(Context ctx) {
        this(ctx, ThemeManager.getThemeColor(ctx, R.attr.colorAccent), 4f, 64f);
    }

    @Override
    public void draw(@NonNull Canvas c) {
        arcRect.set(getBounds().centerX() - radius, getBounds().centerY() - radius, getBounds().centerX() + radius, getBounds().centerY() + radius);
        c.drawArc(arcRect, startAngle, sweepAngle, false, paint);
    }

    @Override public void start() {
        if (isRunning) return; isRunning = true;
        rotationAnim = ValueAnimator.ofFloat(0f, 360f);
        rotationAnim.setDuration(AppUtils.getScaledDuration(context, 2000));
        rotationAnim.setRepeatCount(ValueAnimator.INFINITE); rotationAnim.setInterpolator(new LinearInterpolator());
        rotationAnim.addUpdateListener(a -> { startAngle = (float) a.getAnimatedValue(); invalidateSelf(); });
        rotationAnim.start();

        sweepAnim = ValueAnimator.ofFloat(0f, 270f);
        sweepAnim.setDuration(AppUtils.getScaledDuration(context, 1000));
        sweepAnim.setRepeatCount(ValueAnimator.INFINITE); sweepAnim.setRepeatMode(ValueAnimator.REVERSE);
        sweepAnim.setInterpolator(new DecelerateInterpolator());
        sweepAnim.addUpdateListener(a -> { sweepAngle = (float) a.getAnimatedValue(); invalidateSelf(); });
        sweepAnim.start();
    }

    @Override public void stop() {
        isRunning = false;
        if (rotationAnim != null) rotationAnim.cancel();
        if (sweepAnim != null) sweepAnim.cancel();
        startAngle = 0f; sweepAngle = 0f; invalidateSelf();
    }

    @Override public boolean isRunning() { return isRunning; }
    @Override public void setAlpha(int a) { paint.setAlpha(a); invalidateSelf(); }
    @Override public void setColorFilter(@Nullable ColorFilter f) { paint.setColorFilter(f); invalidateSelf(); }
    @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
    @Override public int getIntrinsicWidth() { return intrinsicSize; }
    @Override public int getIntrinsicHeight() { return intrinsicSize; }
}


