package is.leap.android.aui.ui.assist.output;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;

import is.leap.android.aui.AUIConstants;
import is.leap.android.aui.data.LeapAUICache;
import is.leap.android.aui.ui.assist.listener.AnimatorEndListener;
import is.leap.android.aui.ui.assist.view.HighlightDescView;
import is.leap.android.aui.ui.assist.view.HighlightView;
import is.leap.android.aui.ui.view.LeapWebView;
import is.leap.android.aui.util.AnimUtils;
import is.leap.android.aui.util.AppUtils;

import static is.leap.android.aui.util.AnimUtils.executeAnimations;
import static is.leap.android.core.Constants.Visual.VISUAL_TYPE_HIGHLIGHT_WITH_DESC;

public class Spot extends HighlightDescriptionAssist implements HighlightView.HighlightActionListener {

    private static final int START_DELAY_TIME = 200;
    private static final int ANIM_DURATION = 80;
    private static final float FROM_ALPHA = 0f;
    private static final float TO_ALPHA = 1f;

    public Spot(Activity activity, View rootView, String accessibilityText) {
        super(activity, rootView, accessibilityText);
        init(activity, accessibilityText);
    }

    @Override
    public void updateLayoutParams(Rect oldRect, Rect rect, String alignment) {
        super.updateLayoutParams(oldRect, rect, alignment);
        if (!isWebRendered) return;
        alignIcon(descViewPosition);
    }

    @Override
    public void performEnterAnimation(final AnimatorEndListener animatorEndListener) {
        if (isIconEnabled()) showIcon();
        //Below will animate using canvas drawCircle() i.e. outer circle in 200ms
        highlightDescView.animateOuterCircle();
        ObjectAnimator leapWebViewAlphaAnimator = null;
        final LeapWebView leapWebView = highlightDescView.getLeapWebView();
        leapWebView.setAlpha(FROM_ALPHA);
        leapWebViewAlphaAnimator = AnimUtils.getAlphaAnimator(leapWebView,
                FROM_ALPHA, TO_ALPHA, ANIM_DURATION, new LinearInterpolator(),
                START_DELAY_TIME, new AnimatorEndListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        leapWebView.setAlpha(TO_ALPHA);
                    }
                });

        if (leapWebViewAlphaAnimator == null) return;
        if (!isIconEnabled()) {
            leapWebViewAlphaAnimator.start();
            return;
        }

        iconView.setAlpha(FROM_ALPHA);
        ObjectAnimator iconAlphaAnimator = AnimUtils.getAlphaAnimator(iconView,
                FROM_ALPHA, TO_ALPHA, ANIM_DURATION, new LinearInterpolator(),
                START_DELAY_TIME, new AnimatorEndListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        iconView.setAlpha(TO_ALPHA);
                    }
                });

        Animator[] togetherAnimators = {leapWebViewAlphaAnimator, iconAlphaAnimator};
        executeAnimations(280, togetherAnimators, null, animatorEndListener);
    }

    @Override
    public void performExitAnimation(final AnimatorEndListener animationEndListener) {
        if (assistAnimationListener != null && isActionTakenForExit())
            assistAnimationListener.onIndependentIconAnimationCanStart(VISUAL_TYPE_HIGHLIGHT_WITH_DESC);

        final float bgAlpha = highlightDescView.getBgAlpha();
        ObjectAnimator overlayAlphaAnimator = AnimUtils.getAlphaAnimator(highlightDescView,
                bgAlpha, 0f, 120, new LinearInterpolator(),
                0, new AnimatorEndListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        highlightDescView.hide();
                        if (animationEndListener != null)
                            animationEndListener.onAnimationEnd(animation);
                    }
                });
        overlayAlphaAnimator.start();
    }

    @Override
    protected void alignIcon(Rect contentBounds) {
        if (contentBounds == null) return;
        if (!isIconEnabled()) return;
        // Tooltip at top
        showIcon();
        updateIconPosition(contentBounds);
    }

    private void updateIconPosition(Rect contentBounds) {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) iconView.getLayoutParams();
        float defIconMargin = AppUtils.dpToPx(getContext(), AUIConstants.AUI_ASSOCIATE_ICON_MARGIN);
        int iconHeight = AppUtils.dpToPxInt(getContext(), AUIConstants.ASSOCIATE_ICON_HEIGHT);
        layoutParams.width = layoutParams.height = iconHeight;
        float associateIconSize = AppUtils.dpToPx(getContext(), AUIConstants.ASSOCIATE_ICON_HEIGHT);

        if (isIconLeftAligned())
            layoutParams.leftMargin = contentBounds.left;
        else
            layoutParams.leftMargin = (int) (contentBounds.right - associateIconSize);

        if (HighlightDescView.ConnectorAlignment.BOTTOM == connectorAlignment) {
            int screenHeight = LeapAUICache.screenHeight;
            layoutParams.bottomMargin = (int) (screenHeight - contentBounds.bottom - defIconMargin);
            layoutParams.gravity = Gravity.START | Gravity.BOTTOM;
            iconView.setLayoutParams(layoutParams);
            return;
        }
        layoutParams.topMargin = (int) (contentBounds.top - (defIconMargin * 3.5));
        layoutParams.gravity = Gravity.START | Gravity.TOP;
        iconView.setLayoutParams(layoutParams);
    }
}