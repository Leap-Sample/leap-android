package is.leap.android.aui.ui.view.shapes;

import android.content.Context;
import android.graphics.Path;
import android.graphics.RectF;

import is.leap.android.aui.util.AppUtils;

public class RoundedCornerView extends ShapeOfView {

    private float cornerRadiusPx;
    private boolean enableTopLeftCorner = true;
    private boolean enableTopRightCorner = true;
    private boolean enableBottomRightCorner = true;
    private boolean enableBottomLeftCorner = true;
    private boolean roundAllCorners = false;

    public RoundedCornerView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        cornerRadiusPx = AppUtils.dpToPxInt(context, 10);
        shouldRoundAllCorners();
        super.setClipPathCreator(new ClipPathManager.ClipPathCreator() {
            @Override
            public Path createClipPath(int width, int height) {
                final RectF myRect = new RectF(0, 0, width, height);
                return drawRoundedCornerRect(myRect, cornerRadiusPx);
            }

            @Override
            public boolean requiresBitmap() {
                return false;
            }
        });
    }

    public void setCornerRadius(float cornerRadius) {
        this.cornerRadiusPx = cornerRadius;
        requiresShapeUpdate();
    }

    public void setEnableTopLeftCorner(boolean enableTopLeftCorner) {
        this.enableTopLeftCorner = enableTopLeftCorner;
        shouldRoundAllCorners();
        requiresShapeUpdate();
    }

    public void setEnableTopRightCorner(boolean enableTopRightCorner) {
        this.enableTopRightCorner = enableTopRightCorner;
        shouldRoundAllCorners();
        requiresShapeUpdate();
    }

    public void setEnableBottomRightCorner(boolean enableBottomRightCorner) {
        this.enableBottomRightCorner = enableBottomRightCorner;
        shouldRoundAllCorners();
        requiresShapeUpdate();
    }

    public void setEnableBottomLeftCorner(boolean enableBottomLeftCorner) {
        this.enableBottomLeftCorner = enableBottomLeftCorner;
        shouldRoundAllCorners();
        requiresShapeUpdate();
    }

    private void shouldRoundAllCorners() {
        roundAllCorners = enableTopLeftCorner && enableTopRightCorner && enableBottomRightCorner && enableBottomLeftCorner;
    }

    private Path drawRoundedCornerRect(RectF bounds, float radius) {
        final Path path = new Path();
        if (roundAllCorners) {
            path.addRoundRect(bounds, radius, radius, Path.Direction.CW);
            return path;
        }

        float[] radii = new float[8];
        if (enableTopLeftCorner) {
            radii[0] = radii[1] = radius;
        }

        if (enableTopRightCorner) {
            radii[2] = radii[3] = radius;
        }

        if (enableBottomRightCorner) {
            radii[4] = radii[5] = radius;
        }

        if (enableBottomLeftCorner) {
            radii[6] = radii[7] = radius;
        }

        path.addRoundRect(bounds, radii, Path.Direction.CW);
        return path;
    }
}

