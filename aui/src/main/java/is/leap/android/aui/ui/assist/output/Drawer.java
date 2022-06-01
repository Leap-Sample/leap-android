package is.leap.android.aui.ui.assist.output;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;

import is.leap.android.aui.AUIConstants;
import is.leap.android.aui.data.LeapAUICache;
import is.leap.android.aui.ui.assist.listener.AnimatorEndListener;
import is.leap.android.aui.util.AnimUtils;
import is.leap.android.aui.util.AppUtils;
import is.leap.android.core.Constants;
import is.leap.android.core.data.model.LayoutInfo;
import is.leap.android.core.data.model.Style;

import static is.leap.android.aui.util.AnimUtils.executeAnimations;

public class Drawer extends DialogAssist {

    private static final float DEFAULT_DRAWER_WIDTH_FRACTION = 0.8f;

    public Drawer(Activity activity, String accessibilityText) {
        super(activity, accessibilityText);
        leapWebView.shouldUpdateWidth(false);
        leapWebView.shouldUpdateHeight(false);
        roundedCornerWrapper.setCornerRadius(0);
    }

    @Override
    void applyAlignment(String alignment) {
        updateRadiusByAlignment(alignment);
        int gravity = LayoutInfo.getAlignment(alignment);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) roundedCornerWrapper.getLayoutParams();
        layoutParams.gravity = gravity;
        roundedCornerWrapper.setLayoutParams(layoutParams);
        updateContentLayoutComplete();
    }

    @Override
    public void applyStyle(Style style) {
        super.applyStyle(style);

        // Calculate drawer width
        int screenWidth = LeapAUICache.screenWidth;
        int drawerWidth = (int) (DEFAULT_DRAWER_WIDTH_FRACTION * screenWidth);

        // Update only width
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) roundedCornerWrapper.getLayoutParams();
        layoutParams.width = drawerWidth;
        roundedCornerWrapper.setLayoutParams(layoutParams);
    }

    @Override
    void updateContentLayout(int pageWidth, int pageHeight) {

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
     * End time: 180ms,  At relative position 80% of the screen
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
     * Same at beginning and end: 12px to the right of relative position 80% of the screen
     *
     * @param animatorEndListener
     */
    @Override
    public void performEnterAnimation(final AnimatorEndListener animatorEndListener) {
        showDialogOverlay();
        dialogContent.setAlpha(0);
        ObjectAnimator overlayAlphaAnimator = AnimUtils.getAlphaAnimator(dialogContent,
                0f, 1f,
                180, null, 0, new AnimatorEndListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        showContent();
                        if (isIconEnabled())
                            showIcon();
                    }
                });

        int fromValue = isLeftAligned()
                ? -roundedCornerWrapper.getWidth()
                : roundedCornerWrapper.getWidth();

        ObjectAnimator translationXContainer = AnimUtils.getTranslationXAnimator(
                roundedCornerWrapper, 180, new LinearInterpolator(), 0,
                null, fromValue, 0
        );

        iconView.setAlpha(0f);
        ObjectAnimator iconAlphaAnimation = AnimUtils.getAlphaAnimator(iconView,
                0f, 1f,
                40, null, 180, null);

        Animator[] togetherAnimators = {translationXContainer, iconAlphaAnimation};
        Animator[] sequenceAnimators = {overlayAlphaAnimator, translationXContainer};
        executeAnimations(220, togetherAnimators, sequenceAnimators, new AnimatorEndListener() {
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
     * Beginning time: 0ms, At relative position 80% of the screen
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
     * ### Icon 2
     * <p>
     * **Opacity**
     * Beginning time: 180ms, Opacity is 0%
     * End time: 260ms, Opacity is 100%
     * Easing: standard
     *
     * @param animatorEndListener
     */
    @Override
    public void performExitAnimation(final AnimatorEndListener animatorEndListener) {
        ObjectAnimator overlayAlphaAnimator =
                AnimUtils.getAlphaAnimator(dialogContent,
                        1f, 0f,
                        80, null, 180, null);

        ObjectAnimator translationXContainer =
                AnimUtils.getTranslationXAnimator(roundedCornerWrapper,
                        180, null, 0, null, isLeftAligned()
                                ? -roundedCornerWrapper.getWidth()
                                : roundedCornerWrapper.getWidth());

        ObjectAnimator iconAlphaAnimation = AnimUtils.getAlphaAnimator(iconView,
                1f, 0f,
                180, null, 0, new AnimatorEndListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (assistAnimationListener != null)
                            assistAnimationListener.onIndependentIconAnimationCanStart(Constants.Visual.VISUAL_TYPE_DRAWER);
                    }
                });

        Animator[] togetherAnimators = {overlayAlphaAnimator, translationXContainer, iconAlphaAnimation};
        executeAnimations(260, togetherAnimators, null, animatorEndListener);
    }

    @Override
    protected void alignIcon(Rect contentBounds) {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) iconView.getLayoutParams();
        float defTopGap = AppUtils.dpToPx(getContext(), AUIConstants.AUI_ASSOCIATE_ICON_MARGIN);

        layoutParams.bottomMargin = (int) defTopGap;
        if (isLeftAligned()) {
            layoutParams.leftMargin = (int) (contentBounds.right + defTopGap);
            layoutParams.gravity = Gravity.START | Gravity.BOTTOM;
        } else {
            layoutParams.rightMargin = (int) (contentBounds.width() + defTopGap);
            layoutParams.gravity = Gravity.END | Gravity.BOTTOM;
        }

        iconView.setLayoutParams(layoutParams);
    }

    private boolean isLeftAligned() {
        if (assistInfo == null || assistInfo.layoutInfo == null) return false;
        return isLeftAligned(assistInfo.layoutInfo.alignment);
    }

    private boolean isLeftAligned(String alignment) {
        switch (alignment) {
            case Constants.Alignment.LEFT:
            case Constants.Alignment.LEFT_CENTER:
            case Constants.Alignment.BOTTOM_LEFT:
            case Constants.Alignment.TOP_LEFT:
                return true;
        }
        return false;
    }

    private void updateRadiusByAlignment(String alignment) {
        if (isLeftAligned(alignment)) {
            roundedCornerWrapper.setEnableTopLeftCorner(false);
            roundedCornerWrapper.setEnableBottomLeftCorner(false);
            return;
        }
        roundedCornerWrapper.setEnableTopRightCorner(false);
        roundedCornerWrapper.setEnableBottomRightCorner(false);
    }
}
