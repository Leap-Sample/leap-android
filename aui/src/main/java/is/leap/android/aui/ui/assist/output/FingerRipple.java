package is.leap.android.aui.ui.assist.output;

import android.app.Activity;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import is.leap.android.aui.LeapAUIInternal;
import is.leap.android.aui.ui.assist.view.FingerRippleView;
import is.leap.android.aui.ui.listener.OnAnchorClickListener;
import is.leap.android.aui.util.AppUtils;
import is.leap.android.core.EventConstants;
import is.leap.android.core.data.model.DismissAction;

public class FingerRipple extends SameWindowAssist implements OnAnchorClickListener {

    public static final int FINGER_WIDTH = 100;
    private FingerRippleView rootFingerRippleView;

    public FingerRipple(Activity activity, View rootView, String accessibilityText) {
        super(activity, rootView);
        init(activity, accessibilityText);
    }

    @Override
    public void init(Activity activity, String accessibilityText) {
        rootFingerRippleView = new FingerRippleView(LeapAUIInternal.getInstance().getContext());
        rootFingerRippleView.setOnAnchorClickListener(this);
        hide(false);
        addToRoot();
        AppUtils.setContentDescription(rootFingerRippleView, accessibilityText);
    }

    @Override
    public void setUpLayoutAction(DismissAction action) {

    }

    @Override
    public void show() {
        super.show();
        rootFingerRippleView.show();
    }

    @Override
    public void hide(boolean withAnim) {
        super.hide(withAnim);
        if (rootFingerRippleView.getVisibility() != View.VISIBLE) return;
        rootFingerRippleView.hide();
    }

    @Override
    public void hide() {
        super.hide();
        if (rootFingerRippleView.getVisibility() != View.VISIBLE) return;
        rootFingerRippleView.hide();
    }

    @Override
    public View getAssistView() {
        return rootFingerRippleView;
    }

    @Override
    public void updateLayoutParams(Rect oldRect, final Rect rect, String alignment) {
        int height = Math.max(rect.height(), AppUtils.dpToPxInt(getContext(), FINGER_WIDTH));
        int width = Math.max(rect.width(), AppUtils.dpToPxInt(getContext(), FINGER_WIDTH));
        FrameLayout.LayoutParams fl = new FrameLayout.LayoutParams(width, height);
        fl.topMargin = rect.centerY() - (height / 2);
        fl.leftMargin = rect.centerX() - (width / 2);
        fl.gravity = Gravity.TOP | Gravity.START;
        rootFingerRippleView.setLayoutParams(fl);
        View topWindowView = getTopWindowView();
        Rect newAnchorBounds = getAbsoluteBounds(rect, topWindowView);
        rootFingerRippleView.setAnchorBounds(newAnchorBounds);
    }

    @Override
    public void onAnchorClicked() {
        if(!shouldTrackAnchorTouch) return;
        if (assistActionListener != null)
            assistActionListener.onAssistActionPerformed(EventConstants.ANCHOR_CLICK);
    }
}