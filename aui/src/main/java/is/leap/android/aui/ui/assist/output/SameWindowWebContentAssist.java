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

import is.leap.android.aui.AUIConstants;
import is.leap.android.aui.ui.assist.listener.AnimatorEndListener;
import is.leap.android.aui.util.AppUtils;

public abstract class SameWindowWebContentAssist extends WebContentAssist {

    private FrameLayout leapRootView;
    private final WeakReference<View> topWindowViewRef;
    private Rect oldAnchorRect;
    private Rect anchorRect;
    private String alignment;
    protected int scrollTo;

    SameWindowWebContentAssist(Activity activity, View rootView) {
        super(activity);
        this.topWindowViewRef = new WeakReference<>(rootView);
        resetAnchorRect();
    }

    private void resetAnchorRect() {
        this.oldAnchorRect = null;
        this.anchorRect = null;
    }

    void initLeapRootView() {
        leapRootView = getRootView();
    }

    View getTopWindowView() {
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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    protected float getElevation() {
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
    protected void clearAnimation() {
        View assistView = getAssistView();
        if (assistView == null) return;
        assistView.clearAnimation();
    }

    abstract public View getAssistView();

    @Override
    public void hide(boolean withAnim) {
        super.hide(withAnim);
        final View assistView = getAssistView();
        if (assistView.getVisibility() != View.VISIBLE) return;
        clearAnimation();
        if (withAnim) {
            if (assistAnimationListener != null)
                assistAnimationListener.onExitAnimationStarted();
            performExitAnimation(new AnimatorEndListener() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    assistView.setVisibility(View.INVISIBLE);
                    if (assistAnimationListener != null)
                        assistAnimationListener.onExitAnimationFinished();
                }
            });
            return;
        }
        assistView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void show() {
        super.show();
        if (!isWebRendered) return;
        final View assistView = getAssistView();
        if (assistView.getVisibility() == View.VISIBLE) return;
        clearAnimation();
        if (anchorRect != null) {
            setAnchorPosition(oldAnchorRect, anchorRect, alignment);
        }
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
        this.alignment = alignment;
    }

    @Override
    protected void alignIcon(Rect contentBounds) {

    }

    @Override
    public boolean isNonAnchorAssist() {
        return false;
    }

    void updateRootLayout(View mostBottomView) {
        Activity currentActivity = getCurrentActivity();
        View rootView = getRootView();
        View assistRootView = getAssistView();
        AppUtils.updateRootLayout(currentActivity, AppUtils.isKeyboardOpen(currentActivity),
                rootView, assistRootView, mostBottomView);
    }

    protected boolean canBeShadowedByToolbar(Rect anchorRect) {
        // check if shadowed by toolbar
        int _70_margin = AppUtils.dpToPxInt(getContext(), AUIConstants.DEFAULT_MARGIN_70);
        boolean isPartiallyShadowedByToolbarAtTop = anchorRect.top < 0 && anchorRect.bottom <= _70_margin;
        boolean isPartiallyShadowedByToolbarAtBottom = anchorRect.top > 0 && anchorRect.top < _70_margin
                && anchorRect.bottom > _70_margin;
        boolean isCompletelyShadowedByToolbar = anchorRect.top >= 0 && anchorRect.bottom <= _70_margin;

        return isPartiallyShadowedByToolbarAtTop || isPartiallyShadowedByToolbarAtBottom || isCompletelyShadowedByToolbar;
    }

    protected boolean canBeShadowedByNavBar(Rect anchorRect, Rect rootViewBounds) {
        int _70_margin = AppUtils.dpToPxInt(getContext(), AUIConstants.DEFAULT_MARGIN_70);

        // check if shadowed by navigation bar
        int bottomMarginToCheck = rootViewBounds.bottom - _70_margin;
        boolean isPartiallyShadowedByNavBarAtTop = anchorRect.top < bottomMarginToCheck && anchorRect.bottom <= rootViewBounds.bottom;
        boolean isPartiallyShadowedByNavBarAtBottom = anchorRect.top > bottomMarginToCheck && anchorRect.top < rootViewBounds.bottom
                && anchorRect.bottom > rootViewBounds.bottom;
        boolean isCompletelyShadowedByNavBar = anchorRect.top >= bottomMarginToCheck && anchorRect.bottom <= rootViewBounds.bottom;

        return isPartiallyShadowedByNavBarAtTop || isPartiallyShadowedByNavBarAtBottom || isCompletelyShadowedByNavBar;
    }

    protected boolean doesntFitContentInScreen(Rect anchorRect, Rect rootViewBounds,
                                             Rect descViewBounds, int otherSpaceTobeConsidered) {

        int _10_margin = AppUtils.dpToPxInt(getContext(), AUIConstants.DEFAULT_MARGIN_10);

        // Check if it doesnt fit on top and bottom
        boolean doesntFitOnTop = descViewBounds.height() + _10_margin + otherSpaceTobeConsidered > anchorRect.top;
        boolean doesntFitOnBottom = anchorRect.bottom + descViewBounds.height() + _10_margin + otherSpaceTobeConsidered > rootViewBounds.bottom;
        return doesntFitOnTop && doesntFitOnBottom;
    }

}