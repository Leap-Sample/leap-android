package is.leap.android.aui.ui.assist.view;

import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;

public class TooltipHighlightBg extends HighlightView {

    private boolean highlightAnchor;

    public TooltipHighlightBg(Context context) {
        super(context);
        setHighlightAreaClickable(false);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!highlightAnchor) {
            float x = event.getX();
            float y = event.getY();
            boolean touchInBounds = isTouchInBounds(x, y);
            if (mProps.highlightAreaClickable && touchInBounds) {
                if (actionListener != null) actionListener.onHighlightClicked();
            }
            // return false so that click can be passed through
            return false;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (highlightAnchor)
            super.onDraw(canvas);
    }

    public void highlightAnchor() {
        this.highlightAnchor = true;
    }

}
