package is.leap.android.aui.ui.view;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import is.leap.android.aui.LeapAUILogger;

public class LeapShapeDrawable extends Drawable {

    static final String BORDER_COLOR = "#4DFFFFFF";
    public static final int CIRCLE = 0;
    public static final int CAPSULE = 1;
    public static final int RECT = 2;
    public static final int ARC = 3;

    private float borderWidth;

    private int borderColor;
    private int bgColor;
    private int shape;
    private float cornerRadius = 0;
    private float topLeftR = 0;
    private float topRightR = 0;
    private float bottomRightR = 0;
    private float bottomLeftR = 0;
    private float[] radii;

    private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Path path = new Path();
    private int width = 0;
    private int height = 0;

    public static float[] getRadiiFromRect(float topLeft, float topRight, float bottomRight, float bottomLeft) {
        return new float[]{topLeft, topLeft, topRight, topRight, bottomRight, bottomRight, bottomLeft, bottomLeft};
    }

    /**
     * @param bgColor
     * @param borderWidth
     * @param borderColor
     * @param shape       Shape of the Drawable
     */
    public LeapShapeDrawable(int bgColor, float borderWidth, int borderColor, int shape) {
        this.borderWidth = borderWidth;
        this.borderColor = borderColor;
        this.bgColor = bgColor;
        this.shape = shape;
        this.radii = getRadiiFromRect(topLeftR, topRightR, bottomRightR, bottomLeftR);
        init();
    }

    /**
     * @param bgColor
     * @param borderWidth
     * @param borderColor
     * @param shape        Shape of the Drawable
     * @param cornerRadius
     */
    public LeapShapeDrawable(int bgColor, float borderWidth, int borderColor, int shape,
                             float cornerRadius) {
        this.borderWidth = borderWidth;
        this.borderColor = borderColor;
        this.bgColor = bgColor;
        this.shape = shape;
        this.cornerRadius = cornerRadius;
        this.radii = getRadiiFromRect(topLeftR, topRightR, bottomRightR, bottomLeftR);
        init();
    }

    /**
     * @param bgColor
     * @param borderWidth
     * @param borderColor
     * @param shape        Shape of the Drawable
     * @param cornerRadius
     * @param topLeftR
     * @param topRightR
     * @param bottomRightR
     * @param bottomLeftR
     */
    public LeapShapeDrawable(int bgColor, float borderWidth, int borderColor, int shape,
                             float cornerRadius, float topLeftR, float topRightR,
                             float bottomRightR, float bottomLeftR) {
        this.borderWidth = borderWidth;
        this.borderColor = borderColor;
        this.bgColor = bgColor;
        this.shape = shape;
        this.cornerRadius = cornerRadius;
        this.topLeftR = topLeftR;
        this.topRightR = topRightR;
        this.bottomRightR = bottomRightR;
        this.bottomLeftR = bottomLeftR;
        this.radii = getRadiiFromRect(topLeftR, topRightR, bottomRightR, bottomLeftR);
        init();
    }

    private void init() {

        borderPaint.setStyle(Paint.Style.STROKE);

        if (borderWidth > 0) {
            borderPaint.setStrokeWidth(borderWidth / 2);
            borderPaint.setColor(borderColor);
        }

        bgPaint.setColor(bgColor);
    }


    public void setBorderWidth(float borderWidth) {
        this.borderWidth = borderWidth;
        borderPaint.setStrokeWidth(borderWidth);
        invalidateSelf();
    }

    public void setBorderColor(int borderColor) {
        this.borderColor = borderColor;
        borderPaint.setColor(borderColor);
        invalidateSelf();
    }

    void setBgColor(int bgColor) {
        this.bgColor = bgColor;
        bgPaint.setColor(bgColor);
        invalidateSelf();
    }

    @Override
    protected void onBoundsChange(Rect bounds) {

        height = bounds.height();
        width = bounds.width();

        path.reset();
        switch (shape) {
            case ARC:
                calculateArcPath();
                break;
            case CIRCLE:
                path.addCircle(width / 2f, height / 2f, Math.min((width - borderWidth) / 2f, (height - borderWidth) / 2f), Path.Direction.CW);
                break;
            case RECT:
                RectF rect = new RectF(0, 0, width, height);
                path.addRoundRect(rect, radii, Path.Direction.CW);
                break;
            case CAPSULE:
                if (width < height) {
                    LeapAUILogger.errorAUI("Width cannot be less than height in creating a Capsule view");
                    return;
                }

                float _half_border_width = borderWidth / 2;
                RectF leftRect = new RectF(0 + _half_border_width, 0 + _half_border_width, height - _half_border_width, height - _half_border_width);
                path.addArc(leftRect, 90, 180);
                path.lineTo(width - (height / 2f) + _half_border_width, 0 + _half_border_width);
                RectF rightRect = new RectF(width - height + _half_border_width, 0 + _half_border_width, width - _half_border_width, height - _half_border_width);
                path.addArc(rightRect, 270, 180);
                path.lineTo(height / 2f - _half_border_width, height - _half_border_width);
        }

    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawPath(path, bgPaint);
        if (borderWidth > 0) {
            canvas.drawPath(path, borderPaint);
        }
    }

    @Override
    public void setAlpha(int alpha) {
        bgPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        bgPaint.setColorFilter(colorFilter);
    }

    @SuppressLint("NewApi")
    @Override
    public void getOutline(Outline outline) {
        if (width == 0 && height == 0) return;
        Rect rect = new Rect(0, 0, width, height);
        switch (shape) {
            case ARC:
                calculateArcPath();
                outline.setConvexPath(path);
                return;
            case CIRCLE:
                int diameter = Math.min((width), (height));
                outline.setOval(0, 0, diameter, diameter);
                return;
            case RECT: {
                // round all corners
                if (cornerRadius > 0) {
                    outline.setRoundRect(rect, cornerRadius);
                }
                // round partial corners
                else {
                    // No corners
                    if (topLeftR == 0 && topRightR == 0 && bottomRightR == 0 && bottomLeftR == 0) {
                        outline.setRect(rect);
                        break;
                    }
                    // no bottom corners
                    else if (bottomRightR == 0 && bottomLeftR == 0 && (topLeftR != 0 || topRightR != 0)) {
                        float radius = topLeftR == 0 ? topRightR : topLeftR;
                        outline.setRoundRect(0, 0, width, (int) (height + radius), radius);
                    }
                    // no top corners
                    else if (topLeftR == 0 && topRightR == 0 && (bottomRightR != 0 || bottomLeftR != 0)) {
                        float radius = bottomRightR == 0 ? bottomLeftR : bottomRightR;
                        outline.setRoundRect(0, (int) -radius, width, height, radius);
                    }
                    // no right corners
                    else if (bottomRightR == 0 && topRightR == 0 && (topLeftR != 0 || bottomLeftR != 0)) {
                        float radius = topLeftR == 0 ? bottomLeftR : topLeftR;
                        outline.setRoundRect(0, 0, (int) (width + radius), height, radius);
                    }
                    // no left corners
                    else if (bottomLeftR == 0 && topLeftR == 0 && (bottomRightR != 0 || topRightR != 0)) {
                        float radius = bottomRightR == 0 ? topRightR : bottomRightR;
                        outline.setRoundRect((int) -radius, 0, width, height, radius);
                    }
                }
                return;
            }
            case CAPSULE:
                outline.setRoundRect(0, 0, width, height, height / 2);
                return;
        }
    }

    private void calculateArcPath() {
        int curvatureHeight = 100;
        path.reset();
        path.moveTo(0, 0);
        path.lineTo(0, height - curvatureHeight);
        path.quadTo(width / 2, height + curvatureHeight,
                width, height - curvatureHeight);
        path.lineTo(width, 0);
        path.lineTo(0, 0);
        path.close();
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }


    public void setCornerRadius(int cornerRadius) {
        this.cornerRadius = cornerRadius;
        topLeftR = 0;
        topRightR = 0;
        bottomRightR = 0;
        bottomLeftR = 0;
        invalidateSelf();
    }

    public void setCornerRadius(float topLeftR, float topRightR, float bottomRightR, float bottomLeftR) {
        this.cornerRadius = 0;
        this.topLeftR = topLeftR;
        this.topRightR = topRightR;
        this.bottomRightR = bottomRightR;
        this.bottomLeftR = bottomLeftR;
        invalidateSelf();
    }

    public void setShape(int shape) {
        this.shape = shape;
        invalidateSelf();
    }
}