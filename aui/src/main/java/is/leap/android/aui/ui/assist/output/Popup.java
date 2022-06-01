package is.leap.android.aui.ui.assist.output;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.view.Gravity;
import android.widget.FrameLayout;

import is.leap.android.aui.AUIConstants;
import is.leap.android.aui.data.LeapAUICache;
import is.leap.android.aui.ui.assist.listener.AnimatorEndListener;
import is.leap.android.aui.util.AnimUtils;
import is.leap.android.aui.util.AppUtils;
import is.leap.android.core.Constants;
import is.leap.android.core.data.model.Style;

import static is.leap.android.aui.util.AnimUtils.executeAnimations;

public class Popup extends DialogAssist {

    public final int POPUP_HEIGHT;

    public Popup(Activity activity, String accessibilityText) {
        super(activity, accessibilityText);
        leapWebView.shouldUpdateWidth(false);
        POPUP_HEIGHT = LeapAUICache.screenHeight - AppUtils.dpToPxInt(activity, AUIConstants.DP_96);
    }

    @Override
    public void applyStyle(Style style) {
        super.applyStyle(style);
        if (style == null) return;
        Context context = getContext();

        // Set Margin
        int contentMargin = style.contentMargin;
        if (contentMargin == -1) contentMargin = DEFAULT_DIALOG_CONTENT_MARGIN;
        contentMargin = AppUtils.dpToPxInt(context, contentMargin);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) roundedCornerWrapper.getLayoutParams();
        layoutParams.leftMargin = layoutParams.rightMargin = contentMargin;
        layoutParams.height = POPUP_HEIGHT;
        roundedCornerWrapper.setLayoutParams(layoutParams);
    }

    /**
     * ### Overlay
     * <p>
     * **Opacity**
     * Beginning time: 0 ms, Opacity is 0%
     * End time: 80 ms, Opacity is XX% (as selected by the user)
     * Easing: standard
     * <p>
     * ### Container
     * <p>
     * **Opacity**
     * Beginning time: 80 ms, Opacity is 0%
     * End time: 120 ms, Opacity is 100%
     * Easing: standard
     * <p>
     * **Scaling**
     * Beginning time: 80 ms, Size is 80%
     * End time: 120 ms, Size is 100%
     * Easing: standard
     * <p>
     * ### Icon 1
     * <p>
     * **Opacity**
     * Beginning time: 80 ms, Opacity is 0%
     * End time: 120 ms, Opacity is 100%
     * Easing: standard
     * <p>
     * ### Content
     * <p>
     * **Opacity**
     * Beginning time: 120 ms, Opacity is 0%
     * End time: 160 ms, Opacity is 100%
     * Easing: standard
     *
     * @param animatorEndListener
     */
    @Override
    public void performEnterAnimation(final AnimatorEndListener animatorEndListener) {
        showDialogOverlay();
        dialogContent.setAlpha(0);
        ObjectAnimator overlayAlphaAnimator = AnimUtils.getAlphaAnimator(dialogContent,
                0f, 1f, 80, null, 0, new AnimatorEndListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        showContent();
                        if (isIconEnabled()) showIcon();
                        roundedCornerWrapper.setScaleX(0.8f);
                        roundedCornerWrapper.setScaleY(0.8f);
                    }
                });

        roundedCornerWrapper.setAlpha(0);
        ObjectAnimator containerAlphaAnimator = AnimUtils.getAlphaAnimator(roundedCornerWrapper,
                0f, 1f, 40, null, 80, null);


        ObjectAnimator scaleContainer = AnimUtils.getScaleAnimator(roundedCornerWrapper,
                0.8f, 1f,
                0.8f, 1f,
                40, null, 80, null
        );

        iconView.setAlpha(0);
        ObjectAnimator iconAlphaAnimator = AnimUtils.getAlphaAnimator(iconView,
                0f, 1f, 40, null, 80, null);


        Animator[] togetherAnimators = {overlayAlphaAnimator, containerAlphaAnimator, scaleContainer, iconAlphaAnimator};
        executeAnimations(120, togetherAnimators, null,
                new AnimatorEndListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        dialogContent.setAlpha(1f);
                        roundedCornerWrapper.setAlpha(1);
                        roundedCornerWrapper.setScaleX(1f);
                        roundedCornerWrapper.setScaleY(1f);
                        iconView.setAlpha(1f);
                        if (animatorEndListener != null)
                            animatorEndListener.onAnimationEnd(animation);
                    }
                });
    }

    /**
     * ### Container
     * <p>
     * **Opacity**
     * Beginning time: 0 ms, Opacity is 100%
     * End time: instant , Opacity is 0%
     * Easing: standard
     * <p>
     * ### Content
     * <p>
     * **Opacity**
     * Beginning time: 0 ms, Opacity is 100%
     * End time: instant , Opacity is 0%
     * Easing: standard
     * <p>
     * ### Icon 1
     * <p>
     * **Opacity**
     * Beginning time: 0 ms, Opacity is 100%
     * End time: instant ms, Opacity is 0%
     * Easing: standard
     * <p>
     * ### Overlay
     * <p>
     * **Opacity**
     * Beginning time: 0 ms, Opacity is XX% (as selected by the user)
     * End time: 80 ms, Opacity is 0%
     * Easing: standard
     * <p>
     * ### Icon 2
     * <p>
     * **Opacity**
     * Beginning time: 0 ms, Opacity is 0%
     * End time: 80 ms, Opacity is 100%
     * Easing: standard
     * <p>
     * **Scaling**
     * Beginning time: 0 ms, size equal to icon 1
     * End time: 80 ms, size of icon 2
     * Easing: standard
     *
     * @param animatorEndListener
     */
    @Override
    public void performExitAnimation(final AnimatorEndListener animatorEndListener) {

        ObjectAnimator overlayAlphaAnimator = AnimUtils.getAlphaAnimator(dialogContent,
                1f, 0f, 80, null, 0, null);

        ObjectAnimator containerAlphaAnimator = AnimUtils.getAlphaAnimator(roundedCornerWrapper,
                1f, 0f, 80, null, 0, null);

        ObjectAnimator iconAlphaAnimator = AnimUtils.getAlphaAnimator(iconView,
                1f, 0f, 80, null, 0, null);

        Animator[] togetherAnimators = {overlayAlphaAnimator, containerAlphaAnimator, iconAlphaAnimator};
        executeAnimations(80, togetherAnimators, null, animatorEndListener);

        if (assistAnimationListener != null)
            assistAnimationListener.onIndependentIconAnimationCanStart(Constants.Visual.VISUAL_TYPE_POPUP);

    }

    @Override
    protected void alignIcon(Rect contentBounds) {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) iconView.getLayoutParams();
        float defTopGap = AppUtils.dpToPx(getContext(), AUIConstants.AUI_ASSOCIATE_ICON_MARGIN);
        int statusBarHeight = LeapAUICache.getStatusBarHeight(getContext());
        layoutParams.topMargin = (int) (contentBounds.bottom - statusBarHeight + defTopGap);
        if (isIconLeftAligned())
            layoutParams.leftMargin = contentBounds.left;
        else
            layoutParams.leftMargin = contentBounds.right - layoutParams.width;
        layoutParams.gravity = Gravity.START | Gravity.TOP;
        iconView.setLayoutParams(layoutParams);
    }

    @Override
    public void updateContentLayout(int pageWidth, int pageHeight) {
        // Update only height
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) roundedCornerWrapper.getLayoutParams();
        if (pageHeight > 0) {
            if(pageHeight > POPUP_HEIGHT)
                pageHeight = POPUP_HEIGHT;
            layoutParams.height = pageHeight;
        }
        roundedCornerWrapper.setLayoutParams(layoutParams);
        updateContentLayoutComplete();
    }
}
