package is.leap.android.aui.util;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.util.Property;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

import is.leap.android.aui.ui.assist.listener.AnimatorEndListener;

public class AnimUtils {

    public static void animateViewByTranslationX(View view, float deltaFromX, float deltaToX,
                                                 int duration, Interpolator interpolator,
                                                 Animation.AnimationListener animCallback) {
        animateViewByTranslation(view, deltaFromX, deltaToX, 0, 0, duration, interpolator, animCallback);
    }

    public static void animateViewByTranslation(View view, float deltaFromX, float deltaToX, float deltaFromY, float deltaToY,
                                                int duration, Interpolator interpolator,
                                                Animation.AnimationListener animCallback) {
        Animation translateAnimation = new TranslateAnimation(deltaFromX, deltaToX, deltaFromY, deltaToY);
        translateAnimation.setDuration(duration);
        translateAnimation.setInterpolator(interpolator);
        if (animCallback != null)
            translateAnimation.setAnimationListener(animCallback);
        view.startAnimation(translateAnimation);
    }

    public static Animation getZoomInAnimation(View view, float fromX, float fromY, float pivotX, float pivotY,
                                               int duration, Interpolator interpolator) {

        Animation scaleAnimation = new ScaleAnimation(fromX, 1, fromY, 1,
                Animation.RELATIVE_TO_SELF, pivotX, Animation.RELATIVE_TO_SELF, pivotY);
        scaleAnimation.setDuration(duration);
        scaleAnimation.setInterpolator(interpolator);
        view.setAnimation(scaleAnimation);
        return scaleAnimation;
    }

    public static ObjectAnimator getTranslationXAnimator(View view,
                                                         int duration, Interpolator interpolator,
                                                         int startDelayTime,
                                                         Animator.AnimatorListener animatorListener,
                                                         float... values) {
        return getTranslationAnimator(view, View.TRANSLATION_X, duration,
                interpolator, startDelayTime, animatorListener, values);
    }

    public static ObjectAnimator getTranslationYAnimator(View view,
                                                         int duration, Interpolator interpolator,
                                                         int startDelayTime,
                                                         Animator.AnimatorListener animatorListener,
                                                         float... values) {
        return getTranslationAnimator(view, View.TRANSLATION_Y, duration,
                interpolator, startDelayTime, animatorListener, values);
    }

    private static ObjectAnimator getTranslationAnimator(View view, Property<View, Float> property,
                                                         int duration, Interpolator interpolator,
                                                         int startDelayTime,
                                                         Animator.AnimatorListener animatorListener,
                                                         float... values) {

        ObjectAnimator translationYAnimator = ObjectAnimator.ofFloat(view, property, values);
        translationYAnimator.setDuration(duration);
        if (interpolator != null) translationYAnimator.setInterpolator(interpolator);
        if (startDelayTime > 0) translationYAnimator.setStartDelay(startDelayTime);
        if (animatorListener != null) translationYAnimator.addListener(animatorListener);
        return translationYAnimator;
    }

    public static ObjectAnimator getScaleAnimator(View view, float valueX1, float valueX2,
                                                  float valueY1, float valueY2,
                                                  int duration, Interpolator interpolator,
                                                  int startDelayTime, Animator.AnimatorListener animatorListener) {

        ObjectAnimator scaleAnimator = ObjectAnimator.ofPropertyValuesHolder(
                view,
                PropertyValuesHolder.ofFloat(View.SCALE_X, valueX1, valueX2),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, valueY1, valueY2)
        );
        scaleAnimator.setDuration(duration);
        if (interpolator != null) scaleAnimator.setInterpolator(interpolator);
        if (startDelayTime > 0) scaleAnimator.setStartDelay(startDelayTime);
        if (animatorListener != null) scaleAnimator.addListener(animatorListener);
        return scaleAnimator;
    }

    public static ObjectAnimator getAlphaAnimator(View view, float value1, float value2,
                                                  int duration, Interpolator interpolator,
                                                  int startDelayTime, Animator.AnimatorListener animatorListener) {
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(view, View.ALPHA, value1, value2);
        alphaAnimator.setDuration(duration);
        if (interpolator != null) alphaAnimator.setInterpolator(interpolator);
        if (startDelayTime > 0) alphaAnimator.setStartDelay(startDelayTime);
        if (animatorListener != null) alphaAnimator.addListener(animatorListener);
        return alphaAnimator;
    }

    public static void executeAnimations(int duration, Animator[] togetherAnimators,
                                         Animator[] sequenceAnimators,
                                         AnimatorEndListener animationEndListener) {
        executeAnimations(duration, new LinearInterpolator(), togetherAnimators, sequenceAnimators,
                0,
                animationEndListener);
    }

    public static AnimatorSet executeAnimations(int duration, Interpolator interpolator,
                                                Animator[] togetherAnimators, Animator[] sequenceAnimators,
                                                long startDelay,
                                                AnimatorEndListener animationEndListener) {
        AnimatorSet animatorSet = new AnimatorSet();
        // If value is less than 0 don't set the duration. If not set the duration for the
        // corresponding child animations will be taken during animations
        if (duration > 0)
            animatorSet.setDuration(duration);
        if (interpolator != null)
            animatorSet.setInterpolator(interpolator);
        if (togetherAnimators != null)
            animatorSet.playTogether(togetherAnimators);
        if (sequenceAnimators != null)
            animatorSet.playSequentially(sequenceAnimators);
        if (animationEndListener != null)
            animatorSet.addListener(animationEndListener);
        if (startDelay > 0)
            animatorSet.setStartDelay(startDelay);
        animatorSet.start();
        return animatorSet;
    }
}
