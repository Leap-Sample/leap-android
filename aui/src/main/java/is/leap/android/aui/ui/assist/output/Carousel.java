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
import is.leap.android.core.data.model.ExtraProps;
import is.leap.android.core.data.model.Style;

import static is.leap.android.aui.util.AnimUtils.executeAnimations;

public class Carousel extends DialogAssist {

    public final static String FULLSCREEN = "fullscreen";
    public final static String OVERLAY = "overlay";

    public Carousel(Activity activity, String accessibilityText) {
        super(activity, accessibilityText);
        leapWebView.shouldUpdateWidth(false);
        leapWebView.shouldUpdateHeight(false);
        roundedCornerWrapper.setCornerRadius(0);
    }

    @Override
    public void applyStyle(Style style) {
        ExtraProps extraProps = getAssistExtraProps();
        if (extraProps == null) return;
        String carouselType = extraProps.getStringProp(Constants.ExtraProps.CAROUSEL_TYPE);

        switch (carouselType) {
            case FULLSCREEN:
                // NO styles for full screen carousel
                return;
            case OVERLAY:
                super.applyStyle(style);
        }
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
     * ### Container / Overlay
     * <p>
     * **Opacity**
     * Beginning time: 0ms, Opacity is 0%
     * End time: 150ms, Opacity is 100%
     * Easing: standard
     * <p>
     * ### **Content**
     * <p>
     * **Opacity**
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
        ObjectAnimator overlayAlphaAnimator = AnimUtils.getAlphaAnimator(dialogContent,
                0f, 1f,
                150, null, 0, null);

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

        leapWebView.setAlpha(0);
        ObjectAnimator webViewAlphaAnimator = AnimUtils.getAlphaAnimator(leapWebView,
                0f, 1f,
                100, null, 100, null);

        iconView.setAlpha(0);
        ObjectAnimator iconAlphaAnimator =
                AnimUtils.getAlphaAnimator(iconView,
                        0f, 1f,
                        50, null, 150, null);

        Animator[] togetherAnimators = {containerAlphaAnimator, overlayAlphaAnimator, webViewAlphaAnimator};
        Animator[] sequenceAnimators = {containerAlphaAnimator, iconAlphaAnimator};
        executeAnimations(200, togetherAnimators, sequenceAnimators,
                new AnimatorEndListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        leapWebView.setAlpha(1);
                        iconView.setAlpha(1f);
                        if (animatorEndListener != null)
                            animatorEndListener.onAnimationEnd(animation);
                    }
                });
    }

    /**
     * ### Container, Content & Icon 1
     * <p>
     * **Opacity**
     * Beginning time: 0ms, Opacity is 100%
     * End time: 1ms, Opacity is 0%
     * Easing: standard
     * <p>
     * ### Icon 2
     * <p>
     * **Opacity**
     * Beginning time: 120ms, Opacity is 0%
     * End time: 200ms, Opacity is 100%
     * Easing: standard
     *
     * @param animatorEndListener
     */
    @Override
    public void performExitAnimation(final AnimatorEndListener animatorEndListener) {
        ObjectAnimator overlayAlphaAnimator = AnimUtils.getAlphaAnimator(
                dialogContent, 1f, 0f, 1, null, 0,
                new AnimatorEndListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (animatorEndListener != null)
                            animatorEndListener.onAnimationEnd(animation);
                    }
                }
        );
        overlayAlphaAnimator.start();
        if (appExecutors == null) return;
        appExecutors.mainThread().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (assistAnimationListener != null)
                    assistAnimationListener.onIndependentIconAnimationCanStart(Constants.Visual.VISUAL_TYPE_FULLSCREEN);
            }
        }, 120);
    }


    @Override
    void updateContentLayout(int pageWidth, int pageHeight) {

        ExtraProps extraProps = getAssistExtraProps();
        if (extraProps != null){
            String carouselType = extraProps.getStringProp(Constants.ExtraProps.CAROUSEL_TYPE);
            if (OVERLAY.equals(carouselType)) {
                // Update only height
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) roundedCornerWrapper.getLayoutParams();
                if (pageHeight > 0) layoutParams.height = pageHeight;
                roundedCornerWrapper.setLayoutParams(layoutParams);
            }
        }

        updateContentLayoutComplete();
    }
}
