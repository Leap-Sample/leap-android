package is.leap.android.aui.ui.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;
import android.view.animation.LinearInterpolator;

public class LeapIconLoader extends View {

    public static final int PROGRESS_COLOR = 0xF2FFFFFF;
    public static final int LOADER_DURATION = 1000;
    public static final double STROKE_WIDTH_RATIO = 0.10;
    private final Paint progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float rotationInDegrees = 0;
    private RectF RECT_PROGRESS = new RectF();
    private ValueAnimator valueAnimator;

    public LeapIconLoader(Context context) {
        super(context);
        init();
    }

    private void init() {
        // 90% opacity color
        progressPaint.setColor(PROGRESS_COLOR);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);

        start();
    }

    private void start() {
        valueAnimator = ValueAnimator.ofFloat(0, 360);
        valueAnimator.setDuration(LOADER_DURATION);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                rotationInDegrees = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator.start();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measuredWidth = getMeasuredWidth();
        RECT_PROGRESS = new RectF(0, 0, measuredWidth, measuredWidth);
        progressPaint.setStrokeWidth((float) (STROKE_WIDTH_RATIO * measuredWidth));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // startAngle starts at 3 o'clock on a watch.
        float startAngle = rotationInDegrees;
        float sweepAngle = 60;
        canvas.drawArc(RECT_PROGRESS, startAngle, sweepAngle, false, progressPaint);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (valueAnimator == null) return;
        valueAnimator.cancel();
    }
}
