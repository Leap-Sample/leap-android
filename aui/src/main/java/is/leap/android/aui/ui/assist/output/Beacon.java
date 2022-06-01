package is.leap.android.aui.ui.assist.output;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import is.leap.android.aui.ui.assist.view.RippleLayout;
import is.leap.android.aui.ui.listener.OnAnchorClickListener;
import is.leap.android.aui.ui.view.AnchorTouchFrameLayout;
import is.leap.android.aui.util.AppUtils;
import is.leap.android.core.Constants;
import is.leap.android.core.EventConstants;
import is.leap.android.core.data.model.DismissAction;
import is.leap.android.core.data.model.ExtraProps;
import is.leap.android.core.data.model.Style;

import java.util.ArrayList;

public class Beacon extends SameWindowAssist implements OnAnchorClickListener {

    private static final String DEF_RIPPLE_COLOR = "#FF0000";
    private static final float DEF_RIPPLE_ALPHA = 1.0f;
    public static final int DEF_RIPPLE_SIZE_DP = 18;
    private static final int DEFAULT_BEACON_ANIMATION_TIME = 2500;
    private static final int DEFAULT_BEACON_DELAY_TIME = 350;
    private AnchorTouchFrameLayout beaconRoot;
    private RippleLayout beaconRipple;
    private int rippleSize;

    public Beacon(Activity activity, View rootView, String accessibilityText) {
        super(activity, rootView);
        init(activity, accessibilityText);
    }

    @Override
    public void init(Activity activity, String accessibilityText) {
        beaconRoot = new AnchorTouchFrameLayout(getContext());
        beaconRipple = new RippleLayout(getContext());
        beaconRoot.addView(beaconRipple);
        beaconRoot.setOnAnchorClickListener(this);
        hide(false);
        addToRoot();
        initDefault();
        AppUtils.setContentDescription(beaconRipple, accessibilityText);
    }

    @Override
    public View getAssistView() {
        return beaconRoot;
    }

    @Override
    public void show() {
        super.show();
        beaconRoot.setVisibility(View.VISIBLE);
        beaconRipple.post(new Runnable() {
            @Override
            public void run() {
                beaconRipple.start();
            }
        });
    }

    @Override
    public void hide() {
        super.hide();
        beaconRoot.setVisibility(View.GONE);
        beaconRipple.hide();
    }

    @Override
    public void applyStyle(Style style) {
        if (style == null) return;
        prepareRipple(style.bgColor);

        ExtraProps extraProps = getAssistExtraProps();
        if (extraProps != null) {
            int beaconRippleSize = extraProps.getIntProp(Constants.ExtraProps.BEACON_RIPPLE_SIZE);
            int rippleSize = beaconRippleSize <= 0 ? DEF_RIPPLE_SIZE_DP : beaconRippleSize;
            this.rippleSize = AppUtils.dpToPxInt(getContext(), rippleSize);
        }

        beaconRipple.setWidth(rippleSize);
        beaconRipple.setAnimationTime(DEFAULT_BEACON_ANIMATION_TIME);
        beaconRipple.setDelayTime(DEFAULT_BEACON_DELAY_TIME);
    }

    @Override
    public void setUpLayoutAction(DismissAction action) {

    }

    @Override
    public void updateLayoutParams(Rect oldRect, Rect rect, String alignment) {
        FrameLayout.LayoutParams beaconLayoutParams = (FrameLayout.LayoutParams) beaconRipple.getLayoutParams();
        if (beaconLayoutParams == null) {
            beaconLayoutParams = new FrameLayout.LayoutParams(rippleSize, rippleSize);
        } else {
            beaconLayoutParams.width = rippleSize;
            beaconLayoutParams.height = rippleSize;
        }
        int halfRipple = rippleSize / 2;
        int topMargin = 0;
        int leftMargin = 0;
        int rippleGravity = Gravity.CENTER;
        switch (alignment) {
            case Constants.Alignment.CENTER: {
                topMargin = rect.centerY() - halfRipple;
                leftMargin = rect.centerX() - halfRipple;
                rippleGravity = Gravity.CENTER;
            }
            break;

            case Constants.Alignment.RIGHT_CENTER: {
                topMargin = rect.centerY() - halfRipple;
                leftMargin = rect.left + rect.width() - halfRipple;
                rippleGravity = Gravity.CENTER | Gravity.END;
            }
            break;

            case Constants.Alignment.LEFT_CENTER: {
                topMargin = rect.centerY() - halfRipple;
                leftMargin = rect.left - halfRipple;
                rippleGravity = Gravity.CENTER | Gravity.START;
            }
            break;

            case Constants.Alignment.BOTTOM_LEFT: {
                topMargin = rect.top + rect.height() - halfRipple;
                leftMargin = rect.left - halfRipple;
                rippleGravity = Gravity.BOTTOM | Gravity.START;
            }
            break;

            case Constants.Alignment.BOTTOM_RIGHT: {
                topMargin = rect.bottom - halfRipple;
                leftMargin = rect.left + rect.width() - halfRipple;
                rippleGravity = Gravity.BOTTOM | Gravity.END;
            }
            break;

            case Constants.Alignment.BOTTOM:
            case Constants.Alignment.BOTTOM_CENTER: {
                topMargin = rect.bottom - halfRipple;
                leftMargin = rect.centerX() - halfRipple;
                rippleGravity = Gravity.BOTTOM | Gravity.CENTER;
            }
            break;

            case Constants.Alignment.LEFT:
            case Constants.Alignment.TOP_LEFT: {
                topMargin = rect.top - halfRipple;
                leftMargin = rect.left - halfRipple;
                rippleGravity = Gravity.START| Gravity.TOP;
            }

            break;
            case Constants.Alignment.RIGHT:
            case Constants.Alignment.TOP_RIGHT: {
                topMargin = rect.top - halfRipple;
                leftMargin = rect.left + rect.width() - halfRipple;
                rippleGravity = Gravity.END| Gravity.TOP;
            }

            break;
            case Constants.Alignment.TOP:
            case Constants.Alignment.TOP_CENTER: {
                topMargin = rect.top - halfRipple;
                leftMargin = rect.centerX() - halfRipple;
                rippleGravity = Gravity.TOP| Gravity.CENTER;
            }
            break;
        }
        beaconLayoutParams.gravity = rippleGravity;
        beaconRipple.setLayoutParams(beaconLayoutParams);

        Rect newAnchorBounds = new Rect();
        newAnchorBounds.left = Math.min(leftMargin,rect.left);
        newAnchorBounds.top = Math.min(topMargin,rect.top);
        newAnchorBounds.right = Math.max(leftMargin + rippleSize,rect.right);
        newAnchorBounds.bottom = Math.max(topMargin + rippleSize,rect.bottom);

        FrameLayout.LayoutParams beaconRootLayoutParams = (FrameLayout.LayoutParams) beaconRoot.getLayoutParams();
        if (beaconRootLayoutParams == null) {
            beaconRootLayoutParams = new FrameLayout.LayoutParams(newAnchorBounds.width(), newAnchorBounds.height());
        } else {
            beaconRootLayoutParams.width = newAnchorBounds.width();
            beaconRootLayoutParams.height = newAnchorBounds.height();
        }
        beaconRootLayoutParams.topMargin = newAnchorBounds.top;
        beaconRootLayoutParams.leftMargin = newAnchorBounds.left;
        beaconRoot.setLayoutParams(beaconRootLayoutParams);
        newAnchorBounds = getAbsoluteBounds(newAnchorBounds, getTopWindowView());
        beaconRoot.setAnchorBounds(newAnchorBounds);
    }

    private void prepareRipple(String bgColor) {
        ArrayList<Integer> rippleColors = new ArrayList<>();
        Integer color = Color.parseColor(bgColor);
        rippleColors.add(color);
        rippleColors.add(color);

        beaconRipple.setRippleCount(2);
        beaconRipple.setRippleColors(rippleColors);
    }

    private void initDefault() {
        prepareRipple(DEF_RIPPLE_COLOR);
        beaconRipple.setAlpha(DEF_RIPPLE_ALPHA);
        rippleSize = AppUtils.dpToPxInt(getContext(), DEF_RIPPLE_SIZE_DP);
    }

    @Override
    public void onAnchorClicked() {
        if(!shouldTrackAnchorTouch) return;
        if (assistActionListener != null)
            assistActionListener.onAssistActionPerformed(EventConstants.ANCHOR_CLICK);
    }
}
