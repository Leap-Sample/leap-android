package is.leap.android.aui.ui.assist.output;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.Rect;
import android.view.Gravity;
import android.widget.FrameLayout;

import is.leap.android.aui.AUIConstants;
import is.leap.android.aui.ui.assist.listener.AnimatorEndListener;
import is.leap.android.aui.util.AnimUtils;
import is.leap.android.aui.util.AppUtils;
import is.leap.android.core.Constants;
import is.leap.android.core.data.model.Style;

import static is.leap.android.aui.util.AnimUtils.executeAnimations;

public class FullScreen extends DialogAssist {

    public FullScreen(Activity activity, String accessibilityText) {
        super(activity, accessibilityText);
        leapWebView.shouldUpdateWidth(false);
        leapWebView.shouldUpdateHeight(false);
        roundedCornerWrapper.setCornerRadius(0);
    }

    @Override
    public void applyStyle(Style style) {
        // NO styles for full screen
    }

    @Override
    protected void alignIcon(Rect contentBounds) {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) iconView.getLayoutParams();
        int defGap = AppUtils.dpToPxInt(getContext(), AUIConstants.AUI_ASSOCIATE_ICON_MARGIN);
        layoutParams.topMargin = defGap;
        if (isIconLeftAligned()) {
            layoutParams.leftMargin = defGap;
            layoutParams.gravity = Gravity.START | Gravity.TOP;
        } else {
            layoutParams.rightMargin = defGap;
            layoutParams.gravity = Gravity.END | Gravity.TOP;
        }
        iconView.setLayoutParams(layoutParams);
    }

    /**
     * ### **Container**
     * <p>
     * **Opacity**
     * Beginning time: 0ms, Opacity is 0%
     * End time: 150ms, Opacity is 100%
     * Easing: standard
     * <p>
     * **Scaling**
     * Beginning time: 0ms, Scale is 80%
     * End time: 100ms, Scale is 100%
     * Easing: standard
     * <p>
     * ### Content
     * <p>
     * **Opacity**
     * <p>
     * Beginning time: 100ms, Opacity is 0%
     * End time: 200ms, Opacity is 100%
     * Easing: standard
     * <p>
     * ### Icon 1
     * <p>
     * **Opacity**
     * Beginning time: 150ms, Opacity is 0%
     * End time: 200ms, Opacity is 100%
     * Easing: standard
     *
     * @param animatorEndListener
     */
    @Override
    public void performEnterAnimation(final AnimatorEndListener animatorEndListener) {
        showDialogOverlay();
        showContent();

        roundedCornerWrapper.setAlpha(0);
        ObjectAnimator containerAlphaAnimator = AnimUtils.getAlphaAnimator(roundedCornerWrapper,
                0f, 1f,
                150, null, 0, new AnimatorEndListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        roundedCornerWrapper.setAlpha(1);
                        if (isIconEnabled()) showIcon();
                    }
                });

        roundedCornerWrapper.setScaleX(0.8f);
        roundedCornerWrapper.setScaleY(0.8f);
        ObjectAnimator scaleContainer = AnimUtils.getScaleAnimator(roundedCornerWrapper,
                0.8f, 1f,
                0.8f, 1f,
                100, null, 0, new AnimatorEndListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        roundedCornerWrapper.setScaleX(1f);
                        roundedCornerWrapper.setScaleY(1f);
                    }
                }
        );

        leapWebView.setAlpha(0);
        ObjectAnimator webViewAlphaAnimator = AnimUtils.getAlphaAnimator(leapWebView,
                0f, 1f,
                100, null, 100, null);

        iconView.setAlpha(0);
        ObjectAnimator iconAlphaAnimator =
                AnimUtils.getAlphaAnimator(iconView,
                        0f, 1f,
                        50, null, 150, null);

        Animator[] togetherAnimators = {containerAlphaAnimator, scaleContainer, webViewAlphaAnimator};
        Animator[] sequenceAnimators = {containerAlphaAnimator, iconAlphaAnimator};
        executeAnimations(200, togetherAnimators, sequenceAnimators, new AnimatorEndListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                leapWebView.setAlpha(1);
                iconView.setAlpha(1f);
                if (animatorEndListener != null)
                    animatorEndListener.onAnimationEnd(animation);
            }
        });
    }

    @Override
    public void performExitAnimation(final AnimatorEndListener animatorEndListener) {
        ObjectAnimator overlayAlphaAnimator = AnimUtils.getAlphaAnimator(
                dialogContent, 1f, 0f, 0, null, 0,
                new AnimatorEndListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (assistAnimationListener != null)
                            assistAnimationListener.onIndependentIconAnimationCanStart(Constants.Visual.VISUAL_TYPE_FULLSCREEN);
                        if (animatorEndListener != null)
                            animatorEndListener.onAnimationEnd(animation);
                    }
                }
        );
        overlayAlphaAnimator.start();
    }


    @Override
    void updateContentLayout(int pageWidth, int pageHeight) {
        updateContentLayoutComplete();
    }
}
