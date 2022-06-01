package is.leap.android.aui.ui.view;

import android.content.Context;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import is.leap.android.aui.ui.listener.OnAnchorClickListener;

public class AnchorTouchFrameLayout extends FrameLayout {

    private Rect anchorBounds;
    private OnAnchorClickListener onAnchorClickListener;

    public AnchorTouchFrameLayout(@NonNull Context context) {
        super(context);
    }

    public void setAnchorBounds(Rect anchorBounds) {
        this.anchorBounds = anchorBounds;
    }

    public void setOnAnchorClickListener(OnAnchorClickListener onAnchorClickListener) {
        this.onAnchorClickListener = onAnchorClickListener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getRawX();
        float y = event.getRawY();
        if(onAnchorClickListener != null && isTouchInBounds(x,y))
            onAnchorClickListener.onAnchorClicked();
        return super.onTouchEvent(event);
    }

    boolean isTouchInBounds(float x, float y) {
        if(anchorBounds == null) return false;
        return x >= anchorBounds.left && x <= anchorBounds.right
                && y >= anchorBounds.top && y <= anchorBounds.bottom;
    }
}
