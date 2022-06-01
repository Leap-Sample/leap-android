package is.leap.android.aui.ui.view.shapes;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;

public abstract class ShapeOfView extends FrameLayout {

    private final Paint clipPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path clipPath = new Path();

    protected PorterDuffXfermode pdMode = new PorterDuffXfermode(PorterDuff.Mode.DST_OUT);
    private ClipManager clipManager = new ClipPathManager();
    private boolean requiersShapeUpdate = true;

    final Path rectView = new Path();
    protected float strokeWidth = 0f;

    public ShapeOfView(Context context) {
        super(context);
        init();
    }

    @Override
    public void setBackground(Drawable background) {
        //disabled here, please set a background to to this view child
        //super.setBackground(background);
    }

    @Override
    public void setBackgroundResource(int resid) {
        //disabled here, please set a background to to this view child
        //super.setBackgroundResource(resid);
    }

    @Override
    public void setBackgroundColor(int color) {
        //disabled here, please set a background to to this view child
        //super.setBackgroundColor(color);
    }

    private void init() {
        strokePaint.setStyle(Paint.Style.STROKE);

        setDrawingCacheEnabled(true);

        setWillNotDraw(false);

        clipPaint.setStyle(Paint.Style.FILL);
        clipPaint.setXfermode(pdMode);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    public void setStroke(float strokeWidth, int color) {
        if (strokeWidth <= 0) return;
        this.strokeWidth = strokeWidth;
        strokePaint.setStrokeWidth(strokeWidth);
        strokePaint.setColor(color);
        requiresShapeUpdate();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            requiresShapeUpdate();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        requiresShapeUpdate();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        if (requiersShapeUpdate) {
            calculateLayout(canvas.getWidth(), canvas.getHeight());
            requiersShapeUpdate = false;
        }

        if (strokeWidth > 0) canvas.drawPath(clipPath, strokePaint);
        canvas.drawPath(rectView, clipPaint);
    }

    private void calculateLayout(int width, int height) {
        rectView.reset();
        rectView.addRect(0f, 0f, 1f * getWidth(), 1f * getHeight(), Path.Direction.CW);

        if (clipManager != null) {
            if (width > 0 && height > 0) {
                clipManager.setupClipLayout(width, height);
                clipPath.reset();
                clipPath.set(clipManager.createMask(width, height));

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    rectView.op(clipPath, Path.Op.DIFFERENCE);
                }

                //this needs to be fixed for 25.4.0
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && getElevation() > 0f) {
                    try {
                        setOutlineProvider(getOutlineProvider());
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        postInvalidate();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public ViewOutlineProvider getOutlineProvider() {
        return new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                if (clipManager != null && !isInEditMode()) {
                    final Path shadowConvexPath = clipManager.getShadowConvexPath();
                    if (shadowConvexPath != null) {
                        try {
                            outline.setConvexPath(shadowConvexPath);
                        } catch (Exception ignored) {
                        }
                    }
                }

            }
        };
    }

    public void setClipPathCreator(ClipPathManager.ClipPathCreator createClipPath) {
        ((ClipPathManager) clipManager).setClipPathCreator(createClipPath);
        requiresShapeUpdate();
    }

    public void requiresShapeUpdate() {
        this.requiersShapeUpdate = true;
        postInvalidate();
    }

}
