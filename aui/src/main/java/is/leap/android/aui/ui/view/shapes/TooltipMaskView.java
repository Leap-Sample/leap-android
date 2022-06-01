package is.leap.android.aui.ui.view.shapes;

import android.content.Context;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import is.leap.android.aui.util.AppUtils;

import static is.leap.android.aui.ui.view.shapes.TooltipMaskView.TipAlignment.BOTTOM;
import static is.leap.android.aui.ui.view.shapes.TooltipMaskView.TipAlignment.TOP;

public class TooltipMaskView extends ShapeOfView {

    public static final int TOOLTIP_TIP_HEIGHT = 12;
    public static final int TOOLTIP_TIP_WIDTH = 20;
    @TipAlignment
    private int position = BOTTOM;

    private float cornerRadiusPx;
    private float arrowHeightPx;
    private float arrowWidthPx;
    private float tipX;
    private float tipY;

    public TooltipMaskView(Context context) {
        super(context);
        init();
    }

    private void init() {
        Context context = getContext();
        cornerRadiusPx = AppUtils.dpToPx(context, 10);
        arrowHeightPx = AppUtils.dpToPx(context, TOOLTIP_TIP_HEIGHT);
        arrowWidthPx = AppUtils.dpToPx(context, TOOLTIP_TIP_WIDTH);
        super.setClipPathCreator(new ClipPathManager.ClipPathCreator() {
            @Override
            public Path createClipPath(int width, int height) {
                final RectF myRect = new RectF(0, 0, width, height);
                return drawBubble(myRect, cornerRadiusPx);
            }

            @Override
            public boolean requiresBitmap() {
                return false;
            }
        });
    }

    public void setPosition(@TipAlignment int position) {
        this.position = position;
        requiresShapeUpdate();
    }

    public void setCornerRadius(float cornerRadius) {
        this.cornerRadiusPx = cornerRadius;
        requiresShapeUpdate();
    }

    public float getArrowHeightPx() {
        return arrowHeightPx;
    }

    public float getArrowWidthPx() {
        return arrowWidthPx;
    }

    public void setArrowHeight(float arrowHeight) {
        this.arrowHeightPx = arrowHeight;
        requiresShapeUpdate();
    }

    public void setArrowWidth(float arrowWidth) {
        this.arrowWidthPx = arrowWidth;
        requiresShapeUpdate();
    }

    public void setTipXY(float x, float y) {
        this.tipX = x;
        this.tipY = y;
        invalidate();
    }

    private Path drawBubble(RectF bounds, float radius) {
        final Path path = new Path();
        if (strokeWidth <= 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                path.addRoundRect(arrowHeightPx, arrowHeightPx,
                        bounds.right - arrowHeightPx, bounds.bottom - arrowHeightPx,
                        radius, radius, Path.Direction.CW);
            }
        }

        switch (position) {
            case BOTTOM: {
                createBottomTip(bounds, path);
                if (strokeWidth <= 0) break;

                // For stroke we need to draw in a single path. Else you get a tooltip like this
                // https://drive.google.com/file/d/1C2WfBGe4Gy30KZ3vp-YanWlHw5OVYPK5/view?usp=sharing
                // The drawing is done in clock wise direction
                makeLineToBottomLeft(bounds, radius, path);
                makeBottomLeftCorner(bounds, radius, path);

                makeLineToTopLeftCorner(radius, path);
                makeTopLeftCorner(radius, path);

                makeLineToTopRightCorner(bounds, radius, path);
                makeTopRightCorner(bounds, radius, path);

                makeLineToBottomRight(bounds, radius, path);
                makeBottomRightCorner(bounds, radius, path);

                path.lineTo(tipX + arrowWidthPx / 2, bounds.bottom - arrowHeightPx);
                break;
            }
            case TOP: {
                createTopTip(bounds, path);
                if (strokeWidth <= 0) break;

                makeLineToTopRightCorner(bounds, radius, path);
                makeTopRightCorner(bounds, radius, path);

                makeLineToBottomRight(bounds, radius, path);
                makeBottomRightCorner(bounds, radius, path);

                makeLineToBottomLeft(bounds, radius, path);
                makeBottomLeftCorner(bounds, radius, path);

                makeLineToTopLeftCorner(radius, path);
                makeTopLeftCorner(radius, path);

                path.lineTo(tipX - arrowWidthPx / 2, bounds.top + arrowHeightPx);
                break;
            }
        }

        return path;
    }

    private void createTopTip(RectF bounds, Path path) {
        path.moveTo(tipX - arrowWidthPx / 2, bounds.top + arrowHeightPx);
        // Make curved tip
        path.lineTo(tipX - arrowWidthPx / 7, bounds.top + arrowHeightPx / 4);
        path.cubicTo(tipX - arrowWidthPx / 7, bounds.top + arrowHeightPx / 4,
                tipX, bounds.top,
                tipX + arrowWidthPx / 7, bounds.top + arrowHeightPx / 4);

        path.lineTo(tipX + arrowWidthPx / 2, bounds.top + arrowHeightPx);
    }

    private void createBottomTip(RectF bounds, Path path) {
        path.moveTo(tipX + arrowWidthPx / 2, bounds.bottom - arrowHeightPx);
        // Make curved tip
        path.lineTo(tipX + arrowWidthPx / 7, bounds.bottom - arrowHeightPx / 4);
        path.cubicTo(tipX + arrowWidthPx / 7, bounds.bottom - arrowHeightPx / 4,
                tipX, bounds.bottom,
                tipX - arrowWidthPx / 7, bounds.bottom - arrowHeightPx / 4);
        path.lineTo(tipX - arrowWidthPx / 2, bounds.bottom - arrowHeightPx);
    }

    private void makeBottomRightCorner(RectF bounds, float radius, Path path) {
        path.cubicTo(bounds.right - arrowHeightPx, bounds.bottom - arrowHeightPx - radius,
                bounds.right - arrowHeightPx, bounds.bottom - arrowHeightPx,
                bounds.right - arrowHeightPx - radius, bounds.bottom - arrowHeightPx);
    }

    private void makeLineToBottomRight(RectF bounds, float radius, Path path) {
        path.lineTo(bounds.right - arrowHeightPx, bounds.bottom - arrowHeightPx - radius);
    }

    private void makeTopRightCorner(RectF bounds, float radius, Path path) {
        path.cubicTo(bounds.right - arrowHeightPx - radius, arrowHeightPx,
                bounds.right - arrowHeightPx, arrowHeightPx,
                bounds.right - arrowHeightPx, arrowHeightPx + radius);
    }

    private void makeLineToTopRightCorner(RectF bounds, float radius, Path path) {
        path.lineTo(bounds.right - arrowHeightPx - radius, arrowHeightPx);
    }

    private void makeTopLeftCorner(float radius, Path path) {
        path.cubicTo(arrowHeightPx, arrowHeightPx + radius,
                arrowHeightPx, arrowHeightPx,
                arrowHeightPx + radius, arrowHeightPx);
    }

    private void makeLineToTopLeftCorner(float radius, Path path) {
        path.lineTo(arrowHeightPx, arrowHeightPx + radius);
    }

    private void makeBottomLeftCorner(RectF bounds, float radius, Path path) {
        path.cubicTo(arrowHeightPx + radius, bounds.bottom - arrowHeightPx,
                arrowHeightPx, bounds.bottom - arrowHeightPx,
                arrowHeightPx, bounds.bottom - arrowHeightPx - radius);
    }

    private void makeLineToBottomLeft(RectF bounds, float radius, Path path) {
        path.lineTo(arrowHeightPx + radius, bounds.bottom - arrowHeightPx);
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface TipAlignment {
        int TOP = 3;
        int BOTTOM = 4;
    }
}
