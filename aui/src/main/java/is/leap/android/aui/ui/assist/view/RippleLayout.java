package is.leap.android.aui.ui.assist.view;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Paint;
import android.view.Gravity;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import java.util.ArrayList;

import is.leap.android.aui.LeapAUIInternal;

import static is.leap.android.aui.util.AnimUtils.executeAnimations;

public class RippleLayout extends FrameLayout {
    public static final int ANIMATION_TIME = 1500;
    public static final int DELAY_TIME = 900;

    public static final String RIPPLE_TYPE_FILL = "0";
    public static final String RIPPLE_TYPE_STROKE = "1";
    public static final String ANIMATE_PROPERTY_RADIUS = "radius";
    public static final String ANIMATE_PROPERTY_ALPHA = "alpha";
    private int rippleCount = 1;
    private int animationTime = ANIMATION_TIME;
    private int delayTime = DELAY_TIME;
    private String rippleType = RIPPLE_TYPE_FILL;
    private float strokeWidth = 0;
    private ArrayList<Integer> rippleColors;
    private int rippleWidth;

    public RippleLayout(Context context) {
        super(context);
    }

    public void setRippleCount(int rippleCount) {
        this.rippleCount = rippleCount;
    }

    public void setRippleType(String rippleType) {
        this.rippleType = rippleType;
    }

    public void setStrokeWidth(float strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    public void setRippleColors(ArrayList<Integer> rippleColors) {
        this.rippleColors = rippleColors;
    }

    public void setAnimationTime(int animationTime) {
        this.animationTime = animationTime;
    }

    public void setDelayTime(int delayTime) {
        this.delayTime = delayTime;
    }

    public void setWidth(int width) {
        this.rippleWidth = width;
    }

    public void start() {
        removeAllViews();
        int width = rippleWidth == 0
                ? getWidth()
                : rippleWidth;
        for (int index = 0; index < rippleCount; index++) {
            Integer rippleColor = rippleColors.get(index);
            Paint paint = getPaint(rippleColor, rippleType, strokeWidth);
            View rippleView = new RippleView(LeapAUIInternal.getInstance().getApp(), paint);
            LayoutParams params = new LayoutParams(width, width);
            params.gravity = Gravity.CENTER;
            addView(rippleView, params);

            float radius = width / 2f;
            float rippleRadiusPoint = radius / 4f;
            ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(rippleView, ANIMATE_PROPERTY_RADIUS, rippleRadiusPoint, radius);
            scaleXAnimator.setRepeatCount(ValueAnimator.INFINITE);

            ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(rippleView, ANIMATE_PROPERTY_ALPHA, 1f, 0f);
            alphaAnimator.setRepeatCount(ValueAnimator.INFINITE);

            Animator[] togetherAnimators = {scaleXAnimator, alphaAnimator};
            executeAnimations(animationTime, new DecelerateInterpolator(), togetherAnimators,
                    null, index * delayTime, null);
        }
    }

    public void show() {
        setVisibility(VISIBLE);
    }

    public void hide() {
        setVisibility(GONE);
    }

    private Paint getPaint(int color, String rippleType, float strokeWidth) {
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setAntiAlias(true);
        if (rippleType.equals(RIPPLE_TYPE_STROKE)) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(strokeWidth);
        } else {
            paint.setStyle(Paint.Style.FILL);
            paint.setStrokeWidth(0);
        }
        return paint;
    }

}