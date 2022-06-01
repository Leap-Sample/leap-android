package is.leap.android.aui.ui.assist.output;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;

import is.leap.android.aui.AUIConstants;
import is.leap.android.aui.data.LeapAUICache;
import is.leap.android.aui.ui.assist.listener.AnimatorEndListener;
import is.leap.android.aui.util.AnimUtils;
import is.leap.android.aui.util.AppUtils;
import is.leap.android.core.Constants;
import is.leap.android.core.data.model.Style;

import static is.leap.android.aui.util.AnimUtils.executeAnimations;

public class BottomUp extends DialogAssist {

    private static final float DEFAULT_MAX_HEIGHT = 0.8f;

    public BottomUp(Activity activity, String accessibilityText) {
        super(activity, accessibilityText);

        roundedCornerWrapper.setEnableBottomLeftCorner(false);
        roundedCornerWrapper.setEnableBottomRightCorner(false);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) roundedCornerWrapper.getLayoutParams();
        layoutParams.gravity = Gravity.BOTTOM;
        roundedCornerWrapper.setLayoutParams(layoutParams);

        leapWebView.shouldUpdateWidth(false);
    }

    /**
     * ### **Overlay**
     * <p>
     * **Opacity**
     * Beginning time: 0ms, Opacity is 0%
     * End time: 180ms, Opacity is XX% (as selected by the user)
     * Easing: standard
     * <p>
     * ### **Container & content**
     * <p>
     * **Position**
     * Beginning time: 0ms, At relative position 0% of the screen
     * End time: 180ms,  At relative position XX% of the screen
     * Easing: standard
     * <p>
     * ### Icon 1
     * <p>
     * **Opacity**
     * Beginning time: 180ms, Opacity is 0%
     * End time: 220ms, Opacity is 100%
     * Easing: standard
     * <p>
     * **Position**
     * Same at beginning and end: 12px to the top of final position of container
     *
     * @param animatorEndListener
     */
    @Override
    public void performEnterAnimation(final AnimatorEndListener animatorEndListener) {
        showDialogOverlay();
        dialogContent.setAlpha(0);
        ObjectAnimator overlayAlphaAnimator =
                AnimUtils.getAlphaAnimator(dialogContent,
                        0f, 1f, 180, null, 0,
                        new AnimatorEndListener() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                showContent();
                                if (isIconEnabled()) showIcon();
                                iconView.setAlpha(0);
                            }
                        });

        ObjectAnimator translateYContent = AnimUtils.getTranslationYAnimator(roundedCornerWrapper,
                180, new LinearInterpolator(), 0, null,
                roundedCornerWrapper.getLayoutParams().height, 0);

        ObjectAnimator iconAlphaAnimator = AnimUtils.getAlphaAnimator(iconView,
                0f, 1f, 40, null, 180, null);

        Animator[] sequenceAnimators = {overlayAlphaAnimator, translateYContent};
        Animator[] togetherAnimators = {translateYContent, iconAlphaAnimator};
        executeAnimations(220, togetherAnimators, sequenceAnimators,
                new AnimatorEndListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        dialogContent.setAlpha(1f);
                        iconView.setAlpha(1f);
                        if (animatorEndListener != null)
                            animatorEndListener.onAnimationEnd(animation);
                    }
                });
    }

    /**
     * ### **Overlay**
     * <p>
     * **Opacity**
     * Beginning time: 180ms, Opacity is XX% (as selected by the user)
     * End time: 260ms, Opacity is 0%
     * Easing: standard
     * <p>
     * ### **Container & content**
     * <p>
     * **Position**
     * Beginning time: 0ms, At relative position xx% of the screen (Final position as defined in framework)
     * End time: 180ms,  At relative position 0% of the screen
     * Easing: standard
     * <p>
     * ### Icon 1
     * <p>
     * **Opacity**
     * Beginning time: 0ms, Opacity is 100%
     * End time: instant, Opacity is 0%
     * Easing: standard
     * <p>
     * **Position**
     * Same at beginning and end: 12px to the top of final position of container
     * <p>
     * ### Icon 2
     * <p>
     * **Opacity**
     * Beginning time: 180ms, Opacity is 0%
     * End time: 260ms, Opacity is 100%
     * Easing: standard
     * <p>
     * **Position**
     * Same at beginning and end: 28px above the bottom-left corner of screen
     *
     * @param animatorEndListener
     */
    @Override
    public void performExitAnimation(AnimatorEndListener animatorEndListener) {
        ObjectAnimator overlayAlphaAnimator = AnimUtils.getAlphaAnimator(dialogContent,
                1f, 0f, 80, null, 180, null);

        ObjectAnimator scaleContainer = AnimUtils.getTranslationYAnimator(roundedCornerWrapper,
                180, null, 0,
                new AnimatorEndListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (assistAnimationListener != null)
                            assistAnimationListener.onIndependentIconAnimationCanStart(Constants.Visual.VISUAL_TYPE_BOTTOMUP);
                    }
                },
                roundedCornerWrapper.getHeight()
        );

        iconView.setVisibility(View.GONE);

        executeAnimations(260, new Animator[]{overlayAlphaAnimator, scaleContainer},
                null, animatorEndListener);
    }

    @Override
    protected void alignIcon(Rect contentBounds) {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) iconView.getLayoutParams();
        float defTopGap = AppUtils.dpToPx(getContext(), AUIConstants.AUI_ASSOCIATE_ICON_MARGIN);
        layoutParams.bottomMargin = (int) (contentBounds.height() + defTopGap);

        if (isIconLeftAligned())
            layoutParams.leftMargin = (int) (contentBounds.left + defTopGap);
        else
            layoutParams.leftMargin = (int) (contentBounds.right - defTopGap - layoutParams.width);

        layoutParams.gravity = Gravity.START | Gravity.BOTTOM;

        iconView.setLayoutParams(layoutParams);
    }

    @Override
    public void updateContentLayout(int pageWidth, int pageHeight) {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) roundedCornerWrapper.getLayoutParams();
        layoutParams.height = pageHeight;
        roundedCornerWrapper.setLayoutParams(layoutParams);
        updateContentLayoutComplete();
    }

    @Override
    public void applyStyle(Style style) {
        super.applyStyle(style);
        if (style == null) return;

        float contentMaxHeight = style.maxHeight;
        ViewGroup.LayoutParams layoutParams = roundedCornerWrapper.getLayoutParams();
        if (contentMaxHeight > 0 && contentMaxHeight <= DEFAULT_MAX_HEIGHT) {
            float maxHeight = LeapAUICache.screenHeight * contentMaxHeight;
            layoutParams.height = (int) maxHeight;
            return;
        }

        if (contentMaxHeight > 0 && contentMaxHeight > DEFAULT_MAX_HEIGHT) {
            float maxHeight = LeapAUICache.screenHeight * DEFAULT_MAX_HEIGHT;
            layoutParams.height = (int) maxHeight;
        }
    }
}
