package is.leap.android.aui.ui.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Build;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import is.leap.android.aui.util.AppUtils;

public class LeapBgShapeView extends FrameLayout {

    LeapShapeDrawable shapeDrawable;

    private static final int DEF_BORDER_WIDTH = 0;
    private static final int DEF_BORDER_COLOR = Color.parseColor(LeapShapeDrawable.BORDER_COLOR);
    private static final int DEF_ELEVATION = 0;
    private static final int DEF_RADIUS = 0;
    private static final int DEF_RADIUS_TOP_LEFT = 0;
    private static final int DEF_RADIUS_TOP_RIGHT = 0;
    private static final int DEF_RADIUS_BOTTOM_RIGHT = 0;
    private static final int DEF_RADIUS_BOTTOM_LEFT = 0;

    public LeapBgShapeView(Context context, int widthInDp, int heightInDp, int shape,
                           int bgColor) {
        super(context);
        setSize(widthInDp, heightInDp);
        init(shape, bgColor, DEF_BORDER_COLOR, DEF_BORDER_WIDTH);
    }

    public LeapBgShapeView(Context context, int widthInDp, int heightInDp, int shape,
                           int bgColor, int borderColor, int borderWidth) {
        super(context);
        setSize(widthInDp, heightInDp);
        init(shape, bgColor, borderColor, borderWidth);
    }

    private void init(int shape, int bgColor, int borderColor, int borderWidth) {
        shapeDrawable = new LeapShapeDrawable(bgColor, borderWidth, borderColor,
                shape, DEF_RADIUS, DEF_RADIUS_TOP_LEFT, DEF_RADIUS_TOP_RIGHT,
                DEF_RADIUS_BOTTOM_RIGHT, DEF_RADIUS_BOTTOM_LEFT);
        setViewElevation(DEF_ELEVATION);
        setWillNotDraw(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setClipToOutline(true);
        }
        setBackground(shapeDrawable);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        shapeDrawable.draw(canvas);
        setBackground(shapeDrawable);
    }

    public void setBorderWidth(float borderWidth) {
        shapeDrawable.setBorderWidth(borderWidth);
        setBackground(shapeDrawable);
    }

    public void setBorderColor(int borderColor) {
        shapeDrawable.setBorderColor(borderColor);
        setBackground(shapeDrawable);
    }

    public void setBgColor(int bgColor) {
        shapeDrawable.setBgColor(bgColor);
        setBackground(shapeDrawable);
    }

    @SuppressLint("NewApi")
    public void setViewElevation(int elevation) {
        if (elevation < 0) {
            elevation = 0;
        }
        setElevation(elevation);
    }

    public void hide() {
        setVisibility(GONE);
    }

    public void show() {
        setVisibility(VISIBLE);
    }

    public void setShape(int shape) {
        if (shapeDrawable == null) return;
        shapeDrawable.setShape(shape);
    }

    public void setRadius(int radius) {
        shapeDrawable.setCornerRadius(radius);
        setBackground(shapeDrawable);
    }

    public void setCornerRadius(float topLeftR, float topRightR, float bottomRightR, float bottomLeftR) {
        shapeDrawable.setCornerRadius(topLeftR, topRightR, bottomRightR, bottomLeftR);
        setBackground(shapeDrawable);
    }

    public void setSize(int widthInDp, int heightInDp) {
        int width = AppUtils.dpToPxInt(getContext(), widthInDp);
        int height = AppUtils.dpToPxInt(getContext(), heightInDp);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(width, height);
        setLayoutParams(layoutParams);
    }
}