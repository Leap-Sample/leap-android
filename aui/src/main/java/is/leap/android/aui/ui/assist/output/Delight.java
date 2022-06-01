package is.leap.android.aui.ui.assist.output;

import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import is.leap.android.aui.ui.assist.listener.AnimatorEndListener;
import is.leap.android.aui.ui.view.LeapWebView;
import is.leap.android.aui.util.AppUtils;
import is.leap.android.core.data.model.DismissAction;

public class Delight extends SameWindowWebContentAssist {

    LeapWebView delightView;
    FrameLayout delightRootView;

    public Delight(Activity activity, View rootView, String accessibilityText) {
        super(activity, rootView);
        init(activity, accessibilityText);
        delightView.shouldUpdateWidth(false);
        delightView.shouldUpdateHeight(false);
    }

    @Override
    public void init(Activity activity, String accessibilityText) {
        delightRootView = new FrameLayout(activity);
        delightView = new LeapWebView(activity);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        delightRootView.setLayoutParams(params);
        delightView.setLayoutParams(params);
        delightRootView.addView(delightView);

        AppUtils.setContentDescription(delightRootView, accessibilityText);
        setLeapWebView(delightView);
        initLeapRootView();
        hide(false);
        addToRoot();

        // To apply webView transparency
        applyStyle(null);
        // Enable click through the WebView
        enableClickThrough();
    }

    public void enableClickThrough() {
        View.OnTouchListener rootTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                View parent = (View) v.getParent();
                if (parent == null) return false;
                ViewGroup vg = (ViewGroup) parent;
                for (int i = 0; i < vg.getChildCount(); i++) {
                    View child = vg.getChildAt(i);
                    if (child == delightRootView) continue;
                    child.dispatchTouchEvent(event);
                }
                return true;
            }
        };
        delightRootView.setOnTouchListener(rootTouchListener);
        delightView.delegateTouchListener(rootTouchListener);
    }

    @Override
    public void setUpLayoutAction(DismissAction dismissAction) {

    }

    @Override
    void updateContentLayout(int pageWidth, int pageHeight) {
        if (assistInfo != null && assistInfo.enableHardwareAcceleration)
            delightView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        delightView.updateLayout(pageWidth, pageHeight);
    }

    // Since delight gets added to rootView, it can be considered as non anchored assist
    @Override
    public boolean isNonAnchorAssist() {
        return true;
    }

    @Override
    public void performEnterAnimation(final AnimatorEndListener animatorEndListener) {
        // DO nothing. No entry animation required
    }

    @Override
    public View getAssistView() {
        return delightRootView;
    }
}
