package is.leap.android.aui.ui.assist.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import is.leap.android.aui.AUIConstants;
import is.leap.android.aui.LeapAUIInternal;
import is.leap.android.aui.LeapAUILogger;
import is.leap.android.aui.R;
import is.leap.android.aui.util.AppUtils;
import is.leap.android.core.Constants;
import is.leap.android.core.util.StringUtils;

public class HighlightView extends FrameLayout {

    private static final long RIPPLE_ANIMATION_TIME = 1000;
    public static final float DEFAULT_RIPPLE_WIDTH_FRACTION = 0.6f;
    public static final float DEFAULT_ANIMATED_HALF_VALUE = 0.5f;
    public static final int FULL_ALPHA_VALUE = 255;
    public static final int OUTER_CIRCLE_DRAW_DURATION = 200;

    Props mProps;
    Context mContext;
    Canvas mCanvas;
    Bitmap mBitmap;
    // To make sure we don't redraw when the oldHeight and width doesn't change.
    int mOldHeight, mOldWidth;
    Paint maskRipplePaint;
    Paint mEraserPaint;
    Paint outerCirclePaint;
    private float rectRippleLeft, rectRippleTop, rectRippleRight, rectRippleBottom;
    private float rectTargetLeft, rectTargetTop, rectTargetRight, rectTargetBottom;
    boolean shouldInvalidate = true;
    boolean animateHighlight = false;
    private float cornerRadius;
    private float rectRippleRx, rectRippleRy;
    private int highlightPadding;
    HighlightActionListener actionListener;
    boolean outsideDismiss;
    private float rippleRadius;
    private float rippleAlpha;
    private static float NEGATIVE_CIRCLE_FIXED_RADIUS;
    private static int CIRCLE_HIGHLIGHT_PADDING;
    private float targetCircleRadius;
    protected Rect contentRect;
    private float outerCircleRadius;
    private float outerCircleCenterX;
    private float outerCircleCenterY;
    private ValueAnimator rippleAnimator;
    private ValueAnimator revealAnimator;

    void init(Context context) {
        mProps = getDefaultProps();
        setWillNotDraw(false);
        setVisibility(VISIBLE);
        setClickable(true);
        setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        extractCornerValues(mProps.bounds);
        mContext = context;
        mEraserPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mEraserPaint.setColor(0xFFFFFFFF);
        maskRipplePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        maskRipplePaint.setColor(Color.WHITE);
        mEraserPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        outerCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        outerCirclePaint.setStyle(Paint.Style.FILL);
        cornerRadius = highlightPadding = AppUtils.dpToPxInt(context, AUIConstants.DEFAULT_MARGIN_5);
        CIRCLE_HIGHLIGHT_PADDING = AppUtils.dpToPxInt(getContext(), AUIConstants.DEFAULT_MARGIN_10);
        resetBounds();
    }

    public HighlightView(Context context) {
        super(context);
        init(context);
    }

    public static Props getDefaultProps() {
        Rect rect = new Rect();
        int bgColor = LeapAUIInternal.getInstance()
                .getResources()
                .getColor(R.color.leap_overlay_bg);
        return new Props(Props.RECT, Props.NONE, rect, bgColor, false);
    }

    private void extractCornerValues(Rect bounds) {
        rectRippleLeft = rectTargetLeft = bounds.left;
        rectRippleTop = rectTargetTop = bounds.top;
        rectRippleRight = rectTargetRight = bounds.right;
        rectRippleBottom = rectTargetBottom = bounds.bottom;
        if (bounds.width() != 0f && bounds.height() != 0f) {
            rectRippleLeft = rectTargetLeft -= highlightPadding;
            rectRippleTop = rectTargetTop -= highlightPadding;
            rectRippleRight = rectTargetRight += highlightPadding;
            rectRippleBottom = rectTargetBottom += highlightPadding;
        }
    }

    public void setAnimateHighlight(boolean animateHighlight) {
        this.animateHighlight = animateHighlight;
    }

    public int getHighlightPadding() {
        if (Props.CIRCLE != mProps.shape)
            return highlightPadding;

        int diff = mProps.bounds.width() - mProps.bounds.height();
        if (diff > 0)
            return (int) ((diff / 2f) + CIRCLE_HIGHLIGHT_PADDING);
        return CIRCLE_HIGHLIGHT_PADDING;
    }

    public static void setBgColor(HighlightView highlightView, String bgColor) {
        try {
            highlightView.setBgColor(Color.parseColor(bgColor));
        } catch (IllegalArgumentException e) {
            highlightView.setBgColor(LeapAUIInternal.getInstance().getResources().getColor(R.color.leap_overlay_bg));
        }
    }

    public void setBgColor(int bgColor) {
        mProps.bgColor = bgColor;
        invalidate();
    }

    public void setAlpha(float alpha) {
        mProps.alpha = alpha;
        invalidate();
    }

    float getDecrementedValueFromHalfDuration(float animatedValue) {
        if (animatedValue < DEFAULT_ANIMATED_HALF_VALUE) {
            return animatedValue / DEFAULT_ANIMATED_HALF_VALUE;
        }

        return (1.0f - animatedValue) / DEFAULT_ANIMATED_HALF_VALUE;
    }

    float getIncrementedValueFromHalfDuration(float animatedValue) {
        if (animatedValue < DEFAULT_ANIMATED_HALF_VALUE) {
            return 0.0f;
        }

        return animatedValue / 1.0f;
    }

    public void animateRipple() {
        rippleAnimator = ValueAnimator.ofFloat(0f, 1f);
        rippleAnimator.setDuration(RIPPLE_ANIMATION_TIME);
        rippleAnimator.setInterpolator(new LinearInterpolator());
        rippleAnimator.setRepeatCount(ValueAnimator.INFINITE);
        rippleAlpha = 1;
        rippleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatedValue = (float) animation.getAnimatedValue();
                float delayedPulseValue = getIncrementedValueFromHalfDuration(animatedValue);
                if (mProps.bgShape != Props.CIRCLE)
                    rippleAlpha = (int) ((1.0f - delayedPulseValue) * FULL_ALPHA_VALUE);
                float decrementedValueFromHalfDuration = getDecrementedValueFromHalfDuration(animatedValue);
                switch (mProps.shape) {
                    case Props.CIRCLE:
                        final int TARGET_PULSE_RADIUS = (int) (0.1f * NEGATIVE_CIRCLE_FIXED_RADIUS);
                        rippleRadius = (DEFAULT_RIPPLE_WIDTH_FRACTION + delayedPulseValue) * NEGATIVE_CIRCLE_FIXED_RADIUS;
                        targetCircleRadius = NEGATIVE_CIRCLE_FIXED_RADIUS + decrementedValueFromHalfDuration * TARGET_PULSE_RADIUS;
                        break;
                    case Props.RECT:
                    case Props.CAPSULE:
                        int width = mProps.bounds.width();
                        int height = mProps.bounds.height();
                        float widthFraction = (delayedPulseValue * DEFAULT_RIPPLE_WIDTH_FRACTION) * width;
                        float heightFraction = (delayedPulseValue * DEFAULT_RIPPLE_WIDTH_FRACTION) * height;
                        extractCornerValues(mProps.bounds);
                        rectRippleLeft -= widthFraction;
                        rectRippleTop -= heightFraction;
                        rectRippleRight += widthFraction;
                        rectRippleBottom += heightFraction;

                        if (Props.CAPSULE == mProps.shape)
                            rectRippleRx = rectRippleRy = (rectRippleRight - rectRippleLeft) / 2f;
                        else
                            rectRippleRx = rectRippleRy = (cornerRadius / mProps.bounds.width()) * (rectRippleRight - rectRippleLeft);

                        final int TARGET_PULSE_WIDTH_FRACTION = (int) (0.1f * width);
                        final int TARGET_PULSE_HEIGHT_FRACTION = (int) (0.1f * height);

                        float rectTargetXFraction = decrementedValueFromHalfDuration * TARGET_PULSE_WIDTH_FRACTION;
                        float rectTargetYFraction = decrementedValueFromHalfDuration * TARGET_PULSE_HEIGHT_FRACTION;
                        rectTargetLeft -= rectTargetXFraction;
                        rectTargetTop -= rectTargetYFraction;
                        rectTargetRight += rectTargetXFraction;
                        rectTargetBottom += rectTargetYFraction;
                        break;
                }
                shouldInvalidate = true;
                invalidate();
            }
        });

        rippleAnimator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();
        float pointerWidth = mProps.bounds.right - mProps.bounds.left;
        float pointerHeight = mProps.bounds.bottom - mProps.bounds.top;
        float cx = mProps.bounds.left + pointerWidth / 2;
        float cy = mProps.bounds.top + pointerHeight / 2;
        if (width <= 0 || height <= 0)
            return;

        if (mCanvas == null || mOldHeight != height || mOldWidth != width || shouldInvalidate) {
            if (mBitmap != null) mBitmap.recycle();
            mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
            mOldHeight = height;
            mOldWidth = width;
            if (mProps.bgShape == Props.CIRCLE) {
                drawOuterCircle();
            } else {
                mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                mCanvas.drawColor(mProps.bgColor);
            }
            switch (mProps.shape) {
                case Props.CIRCLE:
                    maskRipplePaint.setAlpha((int) rippleAlpha);
                    if (animateHighlight)
                        mCanvas.drawCircle(cx, cy, rippleRadius, maskRipplePaint);
                    mCanvas.drawCircle(cx, cy, targetCircleRadius, mEraserPaint);
                    break;
                case Props.CAPSULE:
                case Props.RECT:
                    maskRipplePaint.setAlpha((int) (rippleAlpha));
                    if (animateHighlight)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            mCanvas.drawRoundRect(rectRippleLeft, rectRippleTop,
                                    rectRippleRight, rectRippleBottom,
                                    rectRippleRx, rectRippleRy, maskRipplePaint);
                        }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        mCanvas.drawRoundRect(rectTargetLeft, rectTargetTop,
                                rectTargetRight, rectTargetBottom,
                                cornerRadius, cornerRadius, mEraserPaint);
                    }
                    break;
            }
        }
        canvas.drawBitmap(mBitmap, 0, 0, null);
        shouldInvalidate = false;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (rippleAnimator != null) rippleAnimator.cancel();
        if (revealAnimator != null) revealAnimator.cancel();
    }

    private void drawOuterCircle() {
        mCanvas.drawCircle(outerCircleCenterX, outerCircleCenterY, outerCircleRadius, outerCirclePaint);
    }

    private void evaluateOuterCircle() {
        int minLeft = Math.min(contentRect.left, mProps.bounds.left);
        int maxRight = Math.max(contentRect.right, mProps.bounds.right);
        int minTop = Math.min(contentRect.top, mProps.bounds.top);
        int maxBottom = Math.max(contentRect.bottom, mProps.bounds.bottom);

        outerCircleCenterX = (minLeft + maxRight) / 2.0f;
        outerCircleCenterY = (minTop + maxBottom) / 2.0f;

        int length = maxRight - minLeft;
        int width = maxBottom - minTop;

        //outerCircleRadius is the diagonal of the above rectangle
        outerCircleRadius = (float) Math.sqrt(length * length + width * width);
        outerCirclePaint.setColor(mProps.bgColor);
    }

    public void animateOuterCircle() {
        evaluateOuterCircle();
        revealAnimator = ValueAnimator.ofFloat(0, outerCircleRadius);
        revealAnimator.setDuration(OUTER_CIRCLE_DRAW_DURATION);
        revealAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                outerCircleRadius = (float) animation.getAnimatedValue();
                shouldInvalidate = true;
                invalidate();
            }
        });
        revealAnimator.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_DOWN)
            return super.onTouchEvent(event);
        float x = event.getX();
        float y = event.getY();
        boolean touchInBounds = isTouchInBounds(x, y);
        if (mProps.highlightAreaClickable && touchInBounds) {
            if (actionListener != null) actionListener.onHighlightClicked();
            // return false so that click can be passed through
            return false;
        }

        if (outsideDismiss && !touchInBounds && actionListener != null) {
            actionListener.onNonHighlightClicked();
        }

        return true;
    }

    // Check if the touch is in bounds of highlight view or the anchor view
    boolean isTouchInBounds(float x, float y) {
        return x >= mProps.bounds.left && x <= mProps.bounds.right
                && y >= mProps.bounds.top && y <= mProps.bounds.bottom;
    }

    public void updateBounds(boolean isHighlightAnchor, Rect newBounds) {
        if (isHighlightAnchor) {
            updateBounds(newBounds);
            return;
        }
        mProps.bounds = newBounds;
        invalidate();
    }

    public void updateBounds(final Rect newBounds) {
        shouldInvalidate = true;
        NEGATIVE_CIRCLE_FIXED_RADIUS = targetCircleRadius = (Math.max(newBounds.width(), newBounds.height()) / 2f) + CIRCLE_HIGHLIGHT_PADDING;
        extractCornerValues(newBounds);
        mProps.bounds = newBounds;
        calculateCapsuleCornerRadius();
        if (animateHighlight) animateRipple();
        invalidate();
    }

    public void setShape(@Shape int shape) {
        mProps.shape = shape;
    }

    public void setBgShape(@Shape int shape) {
        mProps.bgShape = shape;
    }

    public void setCornerRadius(float cornerRadius) {
        this.cornerRadius = cornerRadius;
        calculateCapsuleCornerRadius();
    }

    private void calculateCapsuleCornerRadius() {
        if (Props.CAPSULE == mProps.shape)
            this.cornerRadius = mProps.bounds.width() / 2f;
    }

    public void hide() {
        setVisibility(INVISIBLE);
        resetBounds();
        //hide the button if visible and reset the boudns
    }

    private void resetBounds() {
        Rect rect = new Rect();
        mProps.bounds = new Rect();
        extractCornerValues(rect);
    }

    public void show() {
        setVisibility(VISIBLE);
    }

    public void setHighlightAreaClickable(boolean shouldHighlightAreaBeClickable) {
        mProps.highlightAreaClickable = shouldHighlightAreaBeClickable;
    }

    public void setHighlightActionListener(HighlightActionListener actionListener) {
        this.actionListener = actionListener;
    }

    public void setDismissOnOutsideClick(boolean outsideDismiss) {
        this.outsideDismiss = outsideDismiss;
    }

    public @Shape
    int getShapeProps(String shape) {
        if (shape == null) return Props.CIRCLE;
        switch (shape) {
            case Constants.ExtraProps.HIGHLIGHT_CAPSULE_TYPE:
                return Props.CAPSULE;
            case Constants.ExtraProps.HIGHLIGHT_RECT_TYPE:
                return Props.RECT;
            case Constants.ExtraProps.HIGHLIGHT_CIRCLE_TYPE:
            default:
                return Props.CIRCLE;
        }
    }

    public float getBgAlpha() {
        return Color.alpha(mProps.bgColor) / 255f;
    }

    public @Shape
    int getBgShapeProps(String shape) {
        if (StringUtils.isNullOrEmpty(shape)) return Props.NONE;
        if (Constants.ExtraProps.HIGHLIGHT_CIRCLE_TYPE.equals(shape)) return Props.CIRCLE;
        else return Props.NONE;
    }

    @Retention(RetentionPolicy.SOURCE)
    @interface Shape {
        int NONE = -1;
        int CIRCLE = 0;
        int RECT = 1;
        int CAPSULE = 2;
    }

    public static class Props {
        final static int NONE = -1;
        final static int CIRCLE = 0;
        final static int RECT = 1;
        final static int CAPSULE = 2;
        public Rect bounds;
        @Shape
        public int shape;
        @Shape
        public int bgShape;
        public int bgColor;
        public float alpha;
        boolean highlightAreaClickable;


        Props(@Shape int shape, @Shape int bgShape, Rect bounds, int bgColor,
              boolean highlightAreaClickable) {
            this(shape, bgShape, bounds, bgColor, 1, highlightAreaClickable);
        }

        Props(@Shape int shape, @Shape int bgShape, Rect bounds, int bgColor,
              int alpha, boolean highlightAreaClickable) {
            this.shape = shape;
            this.bgShape = bgShape;
            this.bounds = bounds;
            this.bgColor = bgColor;
            this.alpha = alpha;
            this.highlightAreaClickable = highlightAreaClickable;
        }
    }

    public interface HighlightActionListener {
        void onHighlightClicked();

        void onNonHighlightClicked();
    }

}
