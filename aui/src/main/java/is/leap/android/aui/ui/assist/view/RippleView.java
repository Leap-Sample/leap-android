package is.leap.android.aui.ui.assist.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import is.leap.android.aui.util.AppUtils;

public class RippleView extends View {
    public static final int STROKE_WIDTH = 1;
    private Paint mPaint;
    private float mRadius;
    private Paint strokePaint;

    public float getRadius() {
        return mRadius;
    }

    public RippleView(Context context) {
        super(context);
        initPaint(context);
    }

    public RippleView(Context context, Paint p) {
        super(context);
        mPaint = p;
    }

    public void setRadius(float radius) {
        mRadius = radius;
        invalidate();
    }

    public void initPaint(Context context) {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.FILL);

        strokePaint = new Paint();
        strokePaint.setAntiAlias(true);
        strokePaint.setColor(Color.WHITE);
        strokePaint.setStyle(Paint.Style.STROKE);
        float strokeWidth = AppUtils.dpToPx(context, STROKE_WIDTH);
        strokePaint.setStrokeWidth(strokeWidth);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        if (mPaint != null) canvas.drawCircle(centerX, centerY, mRadius, mPaint);
        if (strokePaint != null) canvas.drawCircle(centerX, centerY, mRadius, strokePaint);
    }

    public void hide() {
        this.setVisibility(INVISIBLE);
    }

    public void show() {
        this.setVisibility(VISIBLE);
    }

}