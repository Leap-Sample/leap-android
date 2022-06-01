package is.leap.android.aui.ui.assist.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import is.leap.android.aui.LeapAUIInternal;
import is.leap.android.aui.R;
import is.leap.android.aui.ui.LeapCustomViewGroup;
import is.leap.android.aui.ui.assist.listener.AnimatorEndListener;
import is.leap.android.aui.ui.listener.OnAnchorClickListener;
import is.leap.android.aui.util.AppUtils;

import static is.leap.android.aui.util.AnimUtils.executeAnimations;

public class FingerRippleView extends FrameLayout implements LeapCustomViewGroup {

    private static final int RIPPLE_DURATION = 900;
    private static final int SCALE_FINGER_DURATION = 300;
    private static final int RIPPLE_ALPHA_DURATION = 750;
    private static final int RIPPLE_ALPHA_START_DELAY = 150;
    private static final int RIPPLE_RADIUS = 35;
    private static final int FINGER_PIVOT = 18;
    public static final int START_DELAY = 200;
    public static final int FINGER_ROOT_SIZE = 100;
    private RippleView ripple;
    private View finger;
    private AnimatorSet animatorSet;
    private FrameLayout fingerRootLayout;
    private OnAnchorClickListener onAnchorClickListener;
    private Rect anchorBounds;

    public FingerRippleView(Context context) {
        super(context);
        init(context);
    }

    public void init(Context context) {
        fingerRootLayout = (FrameLayout) LeapAUIInternal.getInstance().inflate(R.layout.leap_finger_ripple_layout);
        setClipChildren(false);
        setClipToPadding(false);

        setupView(context);

        finger = fingerRootLayout.findViewById(R.id.finger_view);

        finger.setPivotY(0);
        finger.setPivotX(AppUtils.dpToPx(context, FINGER_PIVOT));

        addView(fingerRootLayout);
        FrameLayout.LayoutParams layoutParams = (LayoutParams) fingerRootLayout.getLayoutParams();
        layoutParams.width = layoutParams.height = AppUtils.dpToPxInt(context, FINGER_ROOT_SIZE);
        layoutParams.gravity = Gravity.CENTER;
        fingerRootLayout.setLayoutParams(layoutParams);
        fingerRootLayout.setClipChildren(false);
        fingerRootLayout.setClipToPadding(false);
        ripple.hide();
    }

    @Override
    public void setupView(Context context) {
        ripple = new RippleView(context);
        int size = AppUtils.dpToPxInt(context, 70);
        LayoutParams params = new LayoutParams(size, size);
        params.gravity = Gravity.CENTER;
        ripple.setLayoutParams(params);
        fingerRootLayout.addView(ripple, 0);
    }

    public void show() {
        setVisibility(VISIBLE);
        finger.setVisibility(VISIBLE);

        ObjectAnimator scaleFingerDown = ObjectAnimator.ofPropertyValuesHolder(
                finger,
                PropertyValuesHolder.ofFloat("scaleX", 1f, 0.66f),
                PropertyValuesHolder.ofFloat("scaleY", 1f, 0.66f)
        );
        scaleFingerDown.setInterpolator(new FastOutSlowInInterpolator());
        scaleFingerDown.addListener(new AnimatorEndListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                ripple.show();
            }
        });
        scaleFingerDown.setDuration(SCALE_FINGER_DURATION);

        ObjectAnimator scaleFingerUp = ObjectAnimator.ofPropertyValuesHolder(
                finger,
                PropertyValuesHolder.ofFloat("scaleX", 0.66f, 1f),
                PropertyValuesHolder.ofFloat("scaleY", 0.66f, 1f)
        );
        scaleFingerUp.setDuration(SCALE_FINGER_DURATION);
        scaleFingerUp.setInterpolator(new FastOutSlowInInterpolator());

        float radius = AppUtils.dpToPx(getContext(), RIPPLE_RADIUS);
        final ObjectAnimator rippleAnim = ObjectAnimator.ofFloat(ripple, "radius", 0f, radius);
        rippleAnim.setDuration(RIPPLE_DURATION);
        rippleAnim.setInterpolator(new FastOutSlowInInterpolator());

        final ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(ripple, View.ALPHA, 1f, 0f);
        alphaAnimator.setDuration(RIPPLE_ALPHA_DURATION);
        alphaAnimator.setInterpolator(new FastOutSlowInInterpolator());
        alphaAnimator.setStartDelay(RIPPLE_ALPHA_START_DELAY);

        Animator[] togetherAnimators = {scaleFingerUp, rippleAnim, alphaAnimator};
        Animator[] sequenceAnimators = {scaleFingerDown, scaleFingerUp};
        animatorSet = executeAnimations(-1, null, togetherAnimators, sequenceAnimators,
                START_DELAY,
                new AnimatorEndListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        ripple.hide();
                        animatorSet.start();
                    }
                });
    }

    public void hide() {
        setVisibility(INVISIBLE);
        ripple.hide();
        finger.setVisibility(INVISIBLE);
        if (animatorSet != null) animatorSet.cancel();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getRawX();
        float y = event.getRawY();
        if(onAnchorClickListener != null && isTouchInBounds(x,y))
            onAnchorClickListener.onAnchorClicked();
        return false;
    }

    boolean isTouchInBounds(float x, float y) {
        if(anchorBounds == null) return false;
        return x >= anchorBounds.left && x <= anchorBounds.right
                && y >= anchorBounds.top && y <= anchorBounds.bottom;
    }

    public void setOnAnchorClickListener(OnAnchorClickListener onAnchorClickListener) {
        this.onAnchorClickListener = onAnchorClickListener;
    }

    public void setAnchorBounds(Rect rect) {
        this.anchorBounds = rect;
    }

}
