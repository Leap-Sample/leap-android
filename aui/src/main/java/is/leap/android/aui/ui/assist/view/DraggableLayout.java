package is.leap.android.aui.ui.assist.view;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Property;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;

import androidx.customview.widget.ViewDragHelper;

import is.leap.android.aui.data.LeapAUICache;
import is.leap.android.aui.ui.assist.listener.AnimatorEndListener;

import static is.leap.android.aui.ui.assist.view.DraggableLayout.Params.SWIPE_DIRECTION_DOWN;
import static is.leap.android.aui.ui.assist.view.DraggableLayout.Params.SWIPE_DIRECTION_LEFT;
import static is.leap.android.aui.ui.assist.view.DraggableLayout.Params.SWIPE_DIRECTION_RIGHT;
import static is.leap.android.aui.ui.assist.view.DraggableLayout.Params.SWIPE_DIRECTION_UP;

public class DraggableLayout extends FrameLayout {

    public static final int DURATION_SWIPE_Y = 200;
    public static final int DURATION_SWIPE_X = 300;
    private ViewDragHelper dragHelper;
    private int minimumFlingVelocity;
    private Params params;

    private SwipeActionListener swipeActionListener;

    private CompletionListener completionListener;

    public DraggableLayout(Context context) {
        super(context);
        init(context);
    }

    public DraggableLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DraggableLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        dragHelper = ViewDragHelper.create(this, 1f / 8f, new ViewDragCallback());
        minimumFlingVelocity = ViewConfiguration.get(context).getScaledMinimumFlingVelocity();
    }

    public void setSwipeActionListener(SwipeActionListener swipeActionListener) {
        this.swipeActionListener = swipeActionListener;
    }

    public void setCompletionListener(CompletionListener completionListener) {
        this.completionListener = completionListener;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        dragHelper.processTouchEvent(ev);
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        dragHelper.processTouchEvent(event);
        return true;
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (dragHelper.continueSettling(true)) {
            postInvalidateOnAnimation();
        }
    }

    public interface SwipeActionListener {
        void onSwipeComplete(int alignment);
    }

    public interface CompletionListener {
        void onCompletion();
    }

    private class ViewDragCallback extends ViewDragHelper.Callback {

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return true;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            switch (params.swipeDirection) {
                case SWIPE_DIRECTION_LEFT:
                    if (left > 0) return 0;
                    break;
                case SWIPE_DIRECTION_RIGHT:
                    if (left < 0) return 0;
                    break;
                case SWIPE_DIRECTION_DOWN:
                case SWIPE_DIRECTION_UP:
                    return params.maxXPos;
            }
            return left;
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            switch (params.swipeDirection) {
                case SWIPE_DIRECTION_UP:
                    return Math.min(top, params.maxYPos);
                case SWIPE_DIRECTION_DOWN:
                    return Math.max(top, params.maxYPos);
            }
            return 0;
        }

        @Override
        public int getViewHorizontalDragRange( View child) {
            if (params.swipeDirection == SWIPE_DIRECTION_LEFT || params.swipeDirection == SWIPE_DIRECTION_RIGHT)
                return getWidth();
            return params.maxXPos;
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            return child.getHeight();
        }

        @Override
        public void onViewReleased(View releasedChild, float xVel, float yVel) {
            boolean swipeCompleted = false;
            switch (params.swipeDirection) {
                case SWIPE_DIRECTION_DOWN: {
                    int slop = yVel > minimumFlingVelocity ? getHeight() / 3 : getHeight() / 2;
                    if (releasedChild.getTop() > slop)
                        swipeCompleted = true;
                    break;
                }
                case SWIPE_DIRECTION_UP: {
                    int slop = Math.abs(yVel) < minimumFlingVelocity ? getHeight() / 3 : getHeight() / 2;
                    if (releasedChild.getBottom() < slop)
                        swipeCompleted = true;
                    break;
                }
                case SWIPE_DIRECTION_LEFT: {
                    int slop = xVel < minimumFlingVelocity ? getWidth() : getWidth() / 3;
                    if (releasedChild.getRight() < slop)
                        swipeCompleted = true;
                    break;
                }
                case Params.SWIPE_DIRECTION_RIGHT: {
                    int slop = xVel > minimumFlingVelocity ? getWidth() / 6 : getWidth() / 3;
                    if (releasedChild.getLeft() > slop)
                        swipeCompleted = true;
                    break;
                }
            }

            if (swipeCompleted && swipeActionListener != null) {
                swipeActionListener.onSwipeComplete(params.swipeDirection);
                return;
            }

            dragHelper.settleCapturedViewAt(params.maxXPos, params.maxYPos);
            invalidate();
        }

    }

    public void swipeCompletion(int swipeDirection) {
        ObjectAnimator objectAnimator;
        Property<View, Float> property = null;
        float[] values = null;
        long duration = 0;
        if (swipeDirection == SWIPE_DIRECTION_LEFT) {
            property = TRANSLATION_X;
            values = new float[]{0, -getWidth()};
            duration = DURATION_SWIPE_X;
        } else if (swipeDirection == SWIPE_DIRECTION_RIGHT) {
            property = TRANSLATION_X;
            values = new float[]{LeapAUICache.screenWidth - getWidth(), LeapAUICache.screenWidth};
            duration = DURATION_SWIPE_X;
        } else if (swipeDirection == SWIPE_DIRECTION_DOWN) {
            property = TRANSLATION_Y;
            values = new float[]{LeapAUICache.screenHeight * 2};
            duration = DURATION_SWIPE_Y;
        } else if (swipeDirection == SWIPE_DIRECTION_UP) {
            property = TRANSLATION_Y;
            values = new float[]{-LeapAUICache.screenHeight * 2};
            duration = DURATION_SWIPE_Y;
        }

        objectAnimator = ObjectAnimator.ofFloat(this, property, values);
        objectAnimator.setInterpolator(new LinearInterpolator());
        objectAnimator.setDuration(duration);
        objectAnimator.addListener(new AnimatorEndListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (completionListener != null)
                    completionListener.onCompletion();
            }
        });
        objectAnimator.start();
    }

    public static class Params {
        public static final int SWIPE_DIRECTION_UP = 0;
        public static final int SWIPE_DIRECTION_DOWN = 1;
        public static final int SWIPE_DIRECTION_LEFT = 2;
        public static final int SWIPE_DIRECTION_RIGHT = 3;

        public int swipeDirection;
        public int maxYPos = 0;
        public int maxXPos = 0;
    }

    public void setParams(DraggableLayout.Params params) {
        this.params = params;
    }
}
