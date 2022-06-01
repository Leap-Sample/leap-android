package is.leap.android.aui.ui.assist.output;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;

import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import is.leap.android.aui.data.LeapAUICache;
import is.leap.android.aui.ui.assist.listener.AnimatorEndListener;
import is.leap.android.aui.ui.assist.view.SwipeGestureView;
import is.leap.android.aui.util.AppUtils;
import is.leap.android.core.Constants;
import is.leap.android.core.data.model.DismissAction;
import is.leap.android.core.util.StringUtils;

import static is.leap.android.aui.util.AnimUtils.executeAnimations;

public class Finger extends SameWindowAssist {

    private static final int FINGER_SIZE = 100;
    private static final int GESTURE_FADE_OUT_DURATION = 150;
    private static final int FADEOUT_START_DELAY = 850;
    private static final int GESTURE_TRANSLATE_DURATION = 1000;
    private static final int GESTURE_FADE_IN_DURATION = 300;
    private static final int GESTURE_START_DELAY = 300;
    private String animationType;
    private SwipeGestureView swipeGestureView;
    private AnimatorSet animatorSet;

    public Finger(Activity activity, View rootView, String accessibilityText) {
        super(activity, rootView);
        init(activity, accessibilityText);
    }

    @Override
    public void init(Activity activity, String accessibilityText) {
        swipeGestureView = new SwipeGestureView(activity);
        hide(false);
        addToRoot();
        AppUtils.setContentDescription(swipeGestureView, accessibilityText);
    }

    @Override
    public View getAssistView() {
        return swipeGestureView;
    }

    @Override
    public void setUpLayoutAction(DismissAction action) {

    }

    @Override
    public void updateLayoutParams(Rect oldRect, Rect rect, String alignment) {
        FrameLayout.LayoutParams fl = (FrameLayout.LayoutParams) swipeGestureView.getLayoutParams();

        int width;
        int height;

        width = height = AppUtils.dpToPxInt(getContext(), FINGER_SIZE);
        int fingerHalfWidth = width / 2;
        fl.topMargin = rect.centerY() - fingerHalfWidth;
        fl.leftMargin = rect.centerX() - fingerHalfWidth;

        if (StringUtils.isNullOrEmpty(animationType)) return;
        int screenWidth = LeapAUICache.screenWidth;
        int halfScreenWidth = screenWidth / 2;

        switch (animationType) {
            case Constants.Visual.ANIMATION_SWIPE_LEFT:
                fl.leftMargin = fl.leftMargin + (halfScreenWidth / 2);
                break;
            case Constants.Visual.ANIMATION_SWIPE_RIGHT:
                fl.leftMargin = fl.leftMargin - (halfScreenWidth / 2);
                break;
            case Constants.Visual.ANIMATION_SWIPE_UP:
                fl.topMargin = fl.topMargin + (halfScreenWidth / 2);
                break;
            case Constants.Visual.ANIMATION_SWIPE_DOWN:
                fl.topMargin = fl.topMargin - (halfScreenWidth / 2);
                break;
        }
        fl.width = width;
        fl.height = height;

        fl.gravity = Gravity.TOP | Gravity.START;
        swipeGestureView.setLayoutParams(fl);
    }

    @Override
    public void hide(boolean withAnim) {
        super.hide(withAnim);
        stopAnimation();
    }

    @Override
    public void hide() {
        super.hide();
        stopAnimation();
    }

    @Override
    public void show() {
        super.show();
        swipeGestureView.show();
        animate();
    }

    @Override
    public void remove() {
        super.remove();
        stopAnimation();
    }

    private void animate() {
        if (animationType == null || animationType.isEmpty() || animatorSet != null && animatorSet.isRunning())
            return;
        swipeGestureView.hide();

        ObjectAnimator translateAnimator = getTranslateAnimator();
        if (translateAnimator == null) return;
        translateAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                swipeGestureView.show();
            }
        });

        final ObjectAnimator fadeInAnimator = ObjectAnimator.ofFloat(swipeGestureView, "alpha", 0f, 1f);
        fadeInAnimator.setDuration(GESTURE_FADE_IN_DURATION);
        fadeInAnimator.setInterpolator(new FastOutSlowInInterpolator());

        final ObjectAnimator fadeOutAnimator = ObjectAnimator.ofFloat(swipeGestureView, "alpha", 1f, 0f);
        fadeOutAnimator.setDuration(GESTURE_FADE_OUT_DURATION);
        fadeOutAnimator.setInterpolator(new FastOutSlowInInterpolator());
        fadeOutAnimator.setStartDelay(FADEOUT_START_DELAY);

        Animator[] togetherAnimators = {translateAnimator, fadeOutAnimator};
        Animator[] sequenceAnimators = {fadeInAnimator, translateAnimator};
        animatorSet = executeAnimations(-1, null, togetherAnimators, sequenceAnimators,
                GESTURE_START_DELAY, new AnimatorEndListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        swipeGestureView.hide();
                        if (animatorSet == null) return;
                        animatorSet.start();
                    }
                });

    }

    private ObjectAnimator getTranslateAnimator() {
        ObjectAnimator translateAnim;
        float toValue = LeapAUICache.screenWidth / 2f;
        switch (animationType) {
            case Constants.Visual.ANIMATION_SWIPE_RIGHT:
                translateAnim = ObjectAnimator.ofPropertyValuesHolder(
                        swipeGestureView,
                        PropertyValuesHolder.ofFloat("translationX", 0f, toValue));
                break;
            case Constants.Visual.ANIMATION_SWIPE_LEFT:
                translateAnim = ObjectAnimator.ofPropertyValuesHolder(
                        swipeGestureView,
                        PropertyValuesHolder.ofFloat("translationX", 0f, -toValue));
                break;
            case Constants.Visual.ANIMATION_SWIPE_UP:
                translateAnim = ObjectAnimator.ofPropertyValuesHolder(
                        swipeGestureView,
                        PropertyValuesHolder.ofFloat("translationY", 0f, -toValue));
                break;
            case Constants.Visual.ANIMATION_SWIPE_DOWN:
                translateAnim = ObjectAnimator.ofPropertyValuesHolder(
                        swipeGestureView,
                        PropertyValuesHolder.ofFloat("translationY", 0f, toValue));
                break;
            default:
                return null;
        }

        translateAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        translateAnim.setDuration(GESTURE_TRANSLATE_DURATION);
        return translateAnim;
    }

    public void setPointerAnimationType(String animationType) {
        this.animationType = animationType;
    }

    private void stopAnimation() {
        swipeGestureView.hide();
        if (animatorSet != null) {
            animatorSet.cancel();
            animatorSet = null;
        }
    }
}