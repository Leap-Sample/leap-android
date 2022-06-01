package is.leap.android.aui.ui.assist.output;

import android.animation.Animator;
import android.app.Activity;
import android.graphics.Rect;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import androidx.annotation.RequiresApi;

import java.lang.ref.WeakReference;
import java.util.Map;

import is.leap.android.aui.ui.assist.listener.AnimatorEndListener;
import is.leap.android.aui.util.AppUtils;
import is.leap.android.core.data.model.Style;

public abstract class SameWindowAssist extends Assist {

    private FrameLayout leapRootView;
    private final WeakReference<View> topWindowViewRef;
    private Rect oldAnchorRect;
    private Rect anchorRect;

    SameWindowAssist(Activity activity, View rootView) {
        super(activity);
        this.topWindowViewRef = new WeakReference<>(rootView);
        resetAnchorRect();
    }

    private void resetAnchorRect() {
        this.oldAnchorRect = null;
        this.anchorRect = null;
    }

    private void initLeapRootView() {
        leapRootView = getRootView();
    }

    protected View getTopWindowView() {
        return AppUtils.getView(topWindowViewRef);
    }

    private FrameLayout getRootView() {
        Activity currentActivity = activityRef.get();
        if (currentActivity == null) return null;
        View topWindowView = getTopWindowView();
        if (topWindowView instanceof FrameLayout) {
            return (FrameLayout) topWindowView;
        }
        return null;
    }

    protected void addToRoot() {
        View assistView = getAssistView();
        initLeapRootView();
        removeFromParent(assistView);
        if (leapRootView != null) {
            leapRootView.addView(assistView);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                assistView.setElevation(getElevation());
            }
        }
    }

    // @param rect is relative so Incase of Popup window,
    // the absolute bounds is needed
    protected Rect getAbsoluteBounds(Rect rect, View topWindowView) {
        Rect abSoluteBounds = AppUtils.getBounds(topWindowView);
        int anchorWidth = rect.width();
        int anchorHeight = rect.height();

        // For dialog there is some padding.
        int paddingLeft = topWindowView.getPaddingLeft();
        int paddingTop = topWindowView.getPaddingTop();

        Rect newAnchorBounds = new Rect();
        newAnchorBounds.left = abSoluteBounds.left + rect.left + paddingLeft;
        newAnchorBounds.top = abSoluteBounds.top + rect.top + paddingTop;
        newAnchorBounds.right = newAnchorBounds.left + anchorWidth;
        newAnchorBounds.bottom = newAnchorBounds.top + anchorHeight;
        return newAnchorBounds;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private float getElevation() {
        if (leapRootView == null) return 0.0f;
        float maxElevation = leapRootView.getElevation();
        int children = leapRootView.getChildCount();
        for (int childIterator = 0; childIterator < children; childIterator++) {
            float elevation = leapRootView.getChildAt(childIterator).getElevation();
            maxElevation = Math.max(elevation, maxElevation);
        }
        return maxElevation;
    }

    @Override
    public void remove() {
        View assistView = getAssistView();
        removeFromParent(assistView);
        resetAnchorRect();
    }

    private void removeFromParent(View view) {
        if (view != null && view.getParent() != null) {
            ViewParent parent = view.getParent();
            if (parent instanceof ViewGroup) {
                ViewGroup group = (ViewGroup) parent;
                group.removeView(view);
            }
        }
    }

    @Override
    public void applyStyle(Style style) {

    }

    @Override
    protected void alignIcon(Rect contentBounds) {

    }

    @Override
    protected void clearAnimation() {
        View assistView = getAssistView();
        if (assistView == null) return;
        assistView.clearAnimation();
    }

    abstract public View getAssistView();

    @Override
    public void hide(boolean withAnim) {
        final View assistView = getAssistView();
        if (assistView.getVisibility() != View.VISIBLE) return;
        clearAnimation();
        if (withAnim) {
            if (assistAnimationListener != null)
                assistAnimationListener.onExitAnimationStarted();
            performExitAnimation(new AnimatorEndListener() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    assistView.setVisibility(View.GONE);
                    if (assistAnimationListener != null)
                        assistAnimationListener.onExitAnimationFinished();
                }
            });
            return;
        }
        assistView.setVisibility(View.GONE);
    }

    @Override
    public void show() {
        final View assistView = getAssistView();
        if (assistView.getVisibility() == View.VISIBLE) return;
        clearAnimation();
        if (anchorRect != null) setAnchorPosition(oldAnchorRect, anchorRect);
        executeEnterAnimation(assistView);
    }

    private void executeEnterAnimation(View assistView) {
        assistView.setVisibility(View.VISIBLE);
        if (assistAnimationListener != null)
            assistAnimationListener.onEntryAnimationStarted();
        performEnterAnimation(new AnimatorEndListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (assistAnimationListener != null)
                    assistAnimationListener.onEntryAnimationFinished();
            }
        });
    }

    @Override
    public void updateLayoutParams(Rect oldRect, Rect rect, String alignment) {
        this.oldAnchorRect = oldRect;
        this.anchorRect = rect;
    }

    @Override
    public void setContent(String htmlUrl, Map<String, String> contentFileUriMap) {
        // DO nothing
    }

    @Override
    public boolean isNonAnchorAssist() {
        return false;
    }
}