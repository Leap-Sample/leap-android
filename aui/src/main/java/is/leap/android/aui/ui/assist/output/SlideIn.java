package is.leap.android.aui.ui.assist.output;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;

import is.leap.android.aui.AUIConstants;
import is.leap.android.aui.LeapAUIInternal;
import is.leap.android.aui.R;
import is.leap.android.aui.data.LeapAUICache;
import is.leap.android.aui.ui.LeapCustomViewGroup;
import is.leap.android.aui.ui.assist.listener.AnimatorEndListener;
import is.leap.android.aui.ui.assist.view.DraggableLayout;
import is.leap.android.aui.ui.view.LeapWebView;
import is.leap.android.aui.ui.view.shapes.RoundedCornerView;
import is.leap.android.aui.util.AppUtils;
import is.leap.android.core.Constants;
import is.leap.android.core.EventConstants;
import is.leap.android.core.data.model.DismissAction;
import is.leap.android.core.data.model.Style;

import is.leap.android.aui.util.AnimUtils;
import is.leap.android.core.util.StringUtils;

public class SlideIn extends SameWindowWebContentAssist
        implements DraggableLayout.SwipeActionListener,
        DraggableLayout.CompletionListener, LeapCustomViewGroup {

    private static final int INITIAL_MARGIN = 8;
    private static final double BOTTOM_MARGIN_FACTOR = 0.8;
    private static final double TOP_MARGIN_FACTOR = 0.2;
    private static final double MAX_WIDTH = LeapAUICache.screenWidth * BOTTOM_MARGIN_FACTOR;

    private DraggableLayout swipeToDismissLayout;
    private RoundedCornerView slideInBorderView;
    private LeapWebView slideInWebView;

    private int slideInWidth;
    private int slideInHeight;
    private String slideInAlignment;

    public SlideIn(Activity activity, View rootView, String accessibilityText) {
        super(activity, rootView);
        init(activity, accessibilityText);
    }

    @Override
    void applyAlignment(String alignment) {
        slideInAlignment = alignment;

        if (slideInAlignment == null)
            slideInAlignment = Constants.Alignment.BOTTOM_RIGHT;

        //Root layout alignment
        DraggableLayout.Params params = createSwipeLayoutParams(slideInAlignment);
        swipeToDismissLayout.setParams(params);
    }

    private void updateMargin() {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) swipeToDismissLayout.getLayoutParams();

        if (StringUtils.isNotNullAndNotEmpty(slideInAlignment)) {
            switch (slideInAlignment) {
                case Constants.Alignment.LEFT:
                case Constants.Alignment.BOTTOM_LEFT: {
                    layoutParams.topMargin = (int) (LeapAUICache.screenHeight * BOTTOM_MARGIN_FACTOR);
                    layoutParams.leftMargin = 0;
                }
                break;
                case Constants.Alignment.RIGHT:
                case Constants.Alignment.BOTTOM_RIGHT: {
                    layoutParams.topMargin = (int) (LeapAUICache.screenHeight * BOTTOM_MARGIN_FACTOR);
                    layoutParams.leftMargin = LeapAUICache.screenWidth - slideInWidth;
                }
                break;
                case Constants.Alignment.TOP_LEFT: {
                    layoutParams.topMargin = (int) (LeapAUICache.screenHeight * TOP_MARGIN_FACTOR);
                    layoutParams.leftMargin = 0;
                }
                break;
                case Constants.Alignment.TOP_RIGHT: {
                    layoutParams.topMargin = (int) (LeapAUICache.screenHeight * TOP_MARGIN_FACTOR);
                    layoutParams.leftMargin = LeapAUICache.screenWidth - slideInWidth;
                }
                break;
            }
        }
        swipeToDismissLayout.setLayoutParams(layoutParams);
    }

    @Override
    public View getAssistView() {
        return swipeToDismissLayout;
    }

    @Override
    void updateContentLayout(int pageWidth, int pageHeight) {
        if (pageWidth <= MAX_WIDTH)
            slideInWidth = pageWidth;
        else
            slideInWidth = (int) (MAX_WIDTH);
        updateMargin();
        slideInHeight = pageHeight;
        slideInWebView.updateLayout(slideInWidth, slideInHeight);
        updateRadiusByAlignment(slideInHeight / 2, slideInAlignment);

        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) swipeToDismissLayout.getLayoutParams();
        if (layoutParams == null) {
            layoutParams = new FrameLayout.LayoutParams(slideInWidth, slideInHeight);
        } else {
            layoutParams.width = slideInWidth;
            layoutParams.height = slideInHeight;
        }
        swipeToDismissLayout.setLayoutParams(layoutParams);
    }

    @Override
    public void init(Activity activity, String accessibilityText) {
        LeapAUIInternal instance = LeapAUIInternal.getInstance();
        swipeToDismissLayout = (DraggableLayout) instance.inflate(R.layout.leap_slide_in_dismiss_layout);
        swipeToDismissLayout.setCompletionListener(this);
        swipeToDismissLayout.setSwipeActionListener(this);
        AppUtils.setContentDescription(slideInBorderView, accessibilityText);
        setLeapWebView(slideInWebView);
        initLeapRootView();
        hide(false);
        addToRoot();
    }

    @Override
    public void setUpLayoutAction(DismissAction action) {

    }

    @Override
    public void applyStyle(Style style) {
        super.applyStyle(style);
        if (style == null) return;

        slideInBorderView.setStroke(style.strokeWidth, Color.parseColor(style.strokeColor));

        // Update Corner radius
        float cornerRadius = getCornerRadius(style, AUIConstants.DEFAULT_MARGIN_8);
        slideInBorderView.setCornerRadius(cornerRadius);

        // Set Elevation
        float elevation = getElevation(style, 0);
        slideInBorderView.setElevation(elevation);
    }

    private DraggableLayout.Params createSwipeLayoutParams(String alignment) {
        DraggableLayout.Params swipeParams = new DraggableLayout.Params();

        if (isLeftAligned(alignment)) {
            swipeParams.swipeDirection = DraggableLayout.Params.SWIPE_DIRECTION_LEFT;
        } else {
            swipeParams.swipeDirection = DraggableLayout.Params.SWIPE_DIRECTION_RIGHT;
        }

        return swipeParams;
    }

    @Override
    public void performEnterAnimation(AnimatorEndListener animationEndListener) {
        animateWebViewWrapper(true, animationEndListener);
    }

    @Override
    public void performExitAnimation(AnimatorEndListener animationEndListener) {
        animateWebViewWrapper(false, animationEndListener);
    }

    private void animateWebViewWrapper(boolean entry, final AnimatorEndListener animationListener) {
        showContent();
        int duration = entry ? 200 : 150;
        if (isLeftAligned(assistInfo.layoutInfo.alignment)) {
            if (entry)
                // Slide Left to right
                slide(duration, -slideInWidth, INITIAL_MARGIN);
            else
                // Slide right to left
                slide(duration, 0, -slideInWidth);
            return;
        }
        if (entry)
            // Slide right to left
            slide(duration, LeapAUICache.screenWidth, LeapAUICache.screenWidth - slideInWidth + INITIAL_MARGIN);
        else
            // Slide Left to right
            slide(duration, 0, slideInWidth);
    }

    private void slide(int duration, int from, int to) {
        AnimUtils.animateViewByTranslationX(swipeToDismissLayout,
                from,
                to,
                duration,
                new LinearInterpolator(),
                null);
    }

    private void showContent() {
        slideInBorderView.setVisibility(View.VISIBLE);
        slideInWebView.setVisibility(View.VISIBLE);
    }

    private void hideContent() {
        slideInBorderView.setVisibility(View.INVISIBLE);
        slideInWebView.setVisibility(View.INVISIBLE);
    }

    private boolean isLeftAligned(String alignment) {
        switch (alignment) {
            case Constants.Alignment.BOTTOM_LEFT:
            case Constants.Alignment.LEFT:
            case Constants.Alignment.TOP_LEFT:
                return true;
        }
        return false;
    }

    private void updateRadiusByAlignment(int webContentHeight, String alignment) {
        slideInBorderView.setCornerRadius(webContentHeight);
        if (isLeftAligned(alignment)) {
            slideInBorderView.setEnableTopLeftCorner(false);
            slideInBorderView.setEnableBottomLeftCorner(false);
            return;
        }
        slideInBorderView.setEnableTopRightCorner(false);
        slideInBorderView.setEnableBottomRightCorner(false);
    }

    @Override
    public boolean isNonAnchorAssist() {
        return true;
    }

    @Override
    public void onSwipeComplete(int alignment) {
        this.swipeToDismissLayout.swipeCompletion(alignment);
    }

    @Override
    public void onCompletion() {
        if (this.assistActionListener != null)
            assistActionListener.onAssistActionPerformed(EventConstants.ON_SWIPE_TO_DISMISS);
    }

    @Override
    public void setupView(Context context) {
        slideInBorderView = new RoundedCornerView(context);

        slideInWebView = new LeapWebView(context);
        slideInBorderView.addView(slideInWebView);

        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) slideInWebView.getLayoutParams();
        layoutParams.gravity = Gravity.CENTER;
        slideInWebView.setLayoutParams(layoutParams);
    }
}
