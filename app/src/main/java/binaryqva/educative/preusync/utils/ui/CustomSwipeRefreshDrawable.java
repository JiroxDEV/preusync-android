/**
 * ============================================================================
 * Proyecto: PreuSync
 * Clase: CustomSwipeRefreshDrawable.java
 * Versión: v1.0.2
 * Descripción: Drawable animado para el indicador de refresco.
 * Autor: JiroxDEV
 * Licensed under the GNU Affero General Public License v3
 * ============================================================================
 */

package binaryqva.educative.preusync.utils.ui;

import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import binaryqva.educative.preusync.utils.common.AppUtils;

/**
 * Implementa una animación de arco rotativo con punta de flecha reactiva.
 * Soporta estados de arrastre manual y animación infinita durante la carga.
 */
public class CustomSwipeRefreshDrawable extends Drawable implements Animatable {

    private final Paint paint;
    private final Path arrowPath = new Path();
    private final RectF arcRect = new RectF();
    private float startAngle = 0f, sweepAngle = 300f, arrowAlpha = 1f;
    
    private ValueAnimator rotationAnim, fadeOutAnim, sweepAnim, fadeOutDrawableAnim;
    private boolean isRunning = false, isRefreshing = false, isFadingOut = false;
    
    private final float strokeWidthPx, radiusPx, arrowSizePx;
    private final int intrinsicSizePx;
    private final Context context;

    public CustomSwipeRefreshDrawable(Context ctx, int color, float strokeDp, float sizeDp) {
        this.context = ctx;
        float d = ctx.getResources().getDisplayMetrics().density;
        this.strokeWidthPx = strokeDp * d;
        this.intrinsicSizePx = (int) (sizeDp * d);
        this.radiusPx = intrinsicSizePx / 2f;
        this.arrowSizePx = strokeWidthPx * 1.8f;

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(strokeWidthPx);
        paint.setColor(color); paint.setStrokeCap(Paint.Cap.ROUND); paint.setStrokeJoin(Paint.Join.ROUND);
    }

    @Override
    protected void onBoundsChange(Rect b) {
        super.onBoundsChange(b);
        arcRect.set(b.centerX() - radiusPx, b.centerY() - radiusPx, b.centerX() + radiusPx, b.centerY() + radiusPx);
    }

    @Override
    public void draw(@NonNull Canvas c) {
        c.drawArc(arcRect, startAngle, sweepAngle, false, paint);

        // Dibujado de la flecha de indicación de sentido.
        if (arrowAlpha > 0.01f && !isRefreshing) {
            int oldA = paint.getAlpha(); paint.setAlpha((int) (oldA * arrowAlpha));
            float rad = (float) Math.toRadians(startAngle + sweepAngle);
            float bX = (float) (arcRect.centerX() + radiusPx * Math.cos(rad));
            float bY = (float) (arcRect.centerY() + radiusPx * Math.sin(rad));

            float tA = rad + (float) Math.PI / 2;
            float dX = (float) Math.cos(tA), dY = (float) Math.sin(tA);
            float tipX = bX + dX * arrowSizePx, tipY = bY + dY * arrowSizePx;

            arrowPath.reset(); arrowPath.moveTo(tipX, tipY);
            arrowPath.lineTo(bX - dY * (arrowSizePx * 0.65f), bY + dX * (arrowSizePx * 0.65f));
            arrowPath.moveTo(tipX, tipY);
            arrowPath.lineTo(bX + dY * (arrowSizePx * 0.65f), bY - dX * (arrowSizePx * 0.65f));
            c.drawPath(arrowPath, paint); paint.setAlpha(oldA);
        }
    }

    public void setDragProgress(float p) {
        if (isRefreshing) return;
        startAngle = p * 360f; sweepAngle = 10f + p * 290f; invalidateSelf();
    }

    public void startRefreshingAnimation() {
        if (isRefreshing) return;
        isRefreshing = true;
        if (rotationAnim != null) rotationAnim.cancel();

        fadeOutAnim = ValueAnimator.ofFloat(1f, 0f);
        fadeOutAnim.setDuration(AppUtils.getScaledDuration(context, 200));
        fadeOutAnim.addUpdateListener(a -> { arrowAlpha = (float) a.getAnimatedValue(); invalidateSelf(); });
        fadeOutAnim.start();

        sweepAnim = ValueAnimator.ofFloat(30f, 330f);
        sweepAnim.setDuration(AppUtils.getScaledDuration(context, 800));
        sweepAnim.setRepeatCount(ValueAnimator.INFINITE); sweepAnim.setRepeatMode(ValueAnimator.REVERSE);
        sweepAnim.addUpdateListener(a -> { sweepAngle = (float) a.getAnimatedValue(); invalidateSelf(); });
        sweepAnim.start();

        start();
    }

    public void stopRefreshingAnimation() {
        isRefreshing = false; isRunning = false;
        if (fadeOutAnim != null) fadeOutAnim.cancel();
        if (sweepAnim != null) sweepAnim.cancel();
        if (rotationAnim != null) rotationAnim.cancel();
        startAngle = 0f; sweepAngle = 300f; arrowAlpha = 1f; invalidateSelf();
    }

    public void fadeOutDrawable(long dur, @Nullable Runnable end) {
        isFadingOut = true;
        fadeOutDrawableAnim = ValueAnimator.ofInt(255, 0);
        fadeOutDrawableAnim.setDuration(AppUtils.getScaledDuration(context, (int) dur));
        fadeOutDrawableAnim.addUpdateListener(a -> setAlpha((int) a.getAnimatedValue()));
        fadeOutDrawableAnim.addListener(new AnimatorListenerAdapter() { @Override public void onAnimationEnd(android.animation.Animator a) { isFadingOut = false; if (end != null) end.run(); } });
        fadeOutDrawableAnim.start();
    }

    public void resetAlpha() { if (fadeOutDrawableAnim != null) fadeOutDrawableAnim.cancel(); isFadingOut = false; setAlpha(255); }

    @Override public void start() {
        if (isRunning) return; isRunning = true;
        rotationAnim = ValueAnimator.ofFloat(0f, 360f);
        rotationAnim.setDuration(AppUtils.getScaledDuration(context, 1200));
        rotationAnim.setRepeatCount(ValueAnimator.INFINITE);
        rotationAnim.addUpdateListener(a -> { startAngle = (float) a.getAnimatedValue(); invalidateSelf(); });
        rotationAnim.start();
    }

    @Override public void stop() { isRunning = false; if (rotationAnim != null) rotationAnim.cancel(); invalidateSelf(); }
    @Override public boolean isRunning() { return isRunning; }
    @Override public void setAlpha(int a) { paint.setAlpha(a); invalidateSelf(); }
    @Override public void setColorFilter(@Nullable ColorFilter f) { paint.setColorFilter(f); invalidateSelf(); }
    @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
    @Override public int getIntrinsicWidth() { return intrinsicSizePx; }
    @Override public int getIntrinsicHeight() { return intrinsicSizePx; }
}


