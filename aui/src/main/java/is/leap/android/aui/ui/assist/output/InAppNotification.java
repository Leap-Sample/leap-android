package is.leap.android.aui.ui.assist.output;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;

import is.leap.android.aui.AUIConstants;
import is.leap.android.aui.R;
import is.leap.android.aui.LeapAUIInternal;
import is.leap.android.aui.ui.LeapCustomViewGroup;
import is.leap.android.aui.ui.LeapIcon;
import is.leap.android.aui.ui.assist.listener.AnimatorEndListener;
import is.leap.android.aui.ui.assist.view.DraggableLayout;
import is.leap.android.aui.ui.view.LeapWebView;
import is.leap.android.aui.ui.view.shapes.RoundedCornerView;
import is.leap.android.aui.util.AnimUtils;
import is.leap.android.aui.util.AppUtils;
import is.leap.android.core.Constants;
import is.leap.android.core.EventConstants;
import is.leap.android.core.data.model.DismissAction;
import is.leap.android.core.data.model.IconSetting;
import is.leap.android.core.data.model.Style;
import is.leap.android.core.util.StringUtils;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class InAppNotification extends SameWindowWebContentAssist
        implements DraggableLayout.CompletionListener, DraggableLayout.SwipeActionListener,
        LeapCustomViewGroup {

    public static final String TOP_NOTIFICATION = "top";
    public static final String BOTTOM_NOTIFICATION = "bottom";
    public static final int DURATION_ENTER_ANIM = 200;
    public static final int DURATION_ENTER_ICON_ALPHA = 40;
    private final int PX_MARGIN;
    private DraggableLayout swipeLayout;
    private RoundedCornerView roundedCornerWrapper;
    private LeapIcon leapIcon;
    private String alignment;
    private FrameLayout layoutNotificationWrapper;

    public InAppNotification(Activity activity, View rootView, String accessibilityText) {
        super(activity, rootView);
        PX_MARGIN = AppUtils.dpToPxInt(getContext(), AUIConstants.DEFAULT_MARGIN_16);
        init(activity, accessibilityText);
    }

    @Override
    public void init(Activity activity, String accessibilityText) {
        swipeLayout = (DraggableLayout) LeapAUIInternal.getInstance().inflate(R.layout.leap_layout_in_app_notification);
        swipeLayout.setCompletionListener(this);
        swipeLayout.setSwipeActionListener(this);
        AppUtils.setContentDescription(swipeLayout, accessibilityText);

        layoutNotificationWrapper = swipeLayout.findViewById(R.id.layout_notification_wrapper);
        setupView(getContext());
        hideIcon();
        initLeapRootView();
        hide(false);
        addToRoot();
    }

    private void hideIcon() {
        leapIcon.hide();
    }

    private void showIcon() {
        leapIcon.show();
    }

    @Override
    public void setIconSetting(IconSetting iconSetting) {
        super.setIconSetting(iconSetting);
        leapIcon.setProps(iconSetting.bgColor, iconSetting.htmlUrl, iconSetting.contentUrls);
    }

    @Override
    public void show() {
        super.show();
        if (!isWebRendered) return;
        swipeLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void hide() {
        super.hide();
        swipeLayout.setVisibility(View.GONE);
    }

    @Override
    public View getAssistView() {
        return swipeLayout;
    }

    @Override
    public void performEnterAnimation(final AnimatorEndListener animatorEndListener) {
        swipeLayout.setVisibility(View.INVISIBLE);
        LinearInterpolator interpolator = new LinearInterpolator();
        boolean isTop = TOP_NOTIFICATION.equals(alignment);
        int fromYValue = isTop ? -PX_MARGIN : PX_MARGIN;
        int toYValue = 0;
        ObjectAnimator yAnimator = AnimUtils.getTranslationYAnimator(swipeLayout, DURATION_ENTER_ANIM,
                interpolator, 0, new AnimatorEndListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        swipeLayout.setVisibility(View.VISIBLE);
                        if (!isIconEnabled()) {
                            if (animatorEndListener != null)
                                animatorEndListener.onAnimationEnd(animation);
                        }
                    }
                }, fromYValue, toYValue);
        if (!isIconEnabled()) {
            yAnimator.start();
            return;
        }
        ObjectAnimator alphaAnimator = AnimUtils.getAlphaAnimator(leapIcon, 0, 1,
                DURATION_ENTER_ICON_ALPHA, interpolator, 0, new AnimatorEndListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        showIcon();
                    }
                });
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(yAnimator).after(alphaAnimator);
        animatorSet.start();
    }

    @Override
    public void performExitAnimation(final AnimatorEndListener animatorEndListener) {
        ObjectAnimator overlayAlphaAnimator = AnimUtils.getAlphaAnimator(
                swipeLayout, 1f, 0f, 0, null, 0,
                new AnimatorEndListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (assistAnimationListener != null)
                            assistAnimationListener.onIndependentIconAnimationCanStart(Constants.Visual.VISUAL_TYPE_NOTIFICATION);
                        if (animatorEndListener != null)
                            animatorEndListener.onAnimationEnd(animation);
                    }
                }
        );
        overlayAlphaAnimator.start();
    }

    @Override
    void applyAlignment(String alignment) {
        this.alignment = alignment == null ? TOP_NOTIFICATION : alignment;
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) roundedCornerWrapper.getLayoutParams();
        FrameLayout.LayoutParams swipeLayoutParams = (FrameLayout.LayoutParams) swipeLayout.getLayoutParams();
        if (TOP_NOTIFICATION.equals(alignment)) {
            layoutParams.gravity = Gravity.TOP;
            swipeLayoutParams.gravity = Gravity.TOP;
        } else {
            layoutParams.gravity = Gravity.BOTTOM;
            swipeLayoutParams.gravity = Gravity.BOTTOM;
        }
        roundedCornerWrapper.setLayoutParams(layoutParams);
        swipeLayout.setLayoutParams(swipeLayoutParams);
    }

    @Override
    void updateContentLayout(int pageWidth, int pageHeight) {
        //WebView Container Alignment i.e. RoundedCornerView
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) roundedCornerWrapper.getLayoutParams();
        layoutParams.height = pageHeight;

        //Web Container Alignment
        FrameLayout.LayoutParams swipeLayoutParams = (FrameLayout.LayoutParams) swipeLayout.getLayoutParams();
        swipeLayoutParams.height = WRAP_CONTENT;

        roundedCornerWrapper.setLayoutParams(layoutParams);
        swipeLayout.setLayoutParams(swipeLayoutParams);

        //Root layout alignment
        DraggableLayout.Params params = createSwipeLayoutParams(alignment);
        swipeLayout.setParams(params);
        swipeLayout.setPadding(PX_MARGIN, PX_MARGIN, PX_MARGIN, PX_MARGIN);

        //Show and align icon
        showIcon(pageHeight);
    }

    @Override
    public void applyStyle(Style style) {
        if (style == null) return;
        super.applyStyle(style);

        // Set Elevation
        float elevation = getElevation(style, AUIConstants.DEFAULT_MARGIN_8);
        roundedCornerWrapper.setElevation(elevation);

        // Set Corner Radius
        float cornerRadius = getCornerRadius(style, AUIConstants.DEFAULT_MARGIN_8);
        roundedCornerWrapper.setCornerRadius(cornerRadius);
    }

    @Override
    public void setUpLayoutAction(DismissAction action) {

    }

    private DraggableLayout.Params createSwipeLayoutParams(String alignment) {
        DraggableLayout.Params swipeParams = new DraggableLayout.Params();

        swipeParams.maxYPos = PX_MARGIN;
        swipeParams.maxXPos = PX_MARGIN;

        if (StringUtils.isNotNullAndNotEmpty(alignment)){
            switch (alignment) {
                case TOP_NOTIFICATION:
                    swipeParams.swipeDirection = DraggableLayout.Params.SWIPE_DIRECTION_UP;
                    break;
                case BOTTOM_NOTIFICATION:
                    swipeParams.swipeDirection = DraggableLayout.Params.SWIPE_DIRECTION_DOWN;
                    break;
            }
        }

        return swipeParams;
    }

    private void showIcon(int contentHeight) {
        if (!isIconEnabled()) return;
        showIcon();
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) leapIcon.getLayoutParams();
        layoutParams.gravity = isIconLeftAligned() ? Gravity.START : Gravity.END;
        int defIconMargin = AppUtils.dpToPxInt(getContext(), AUIConstants.AUI_ASSOCIATE_ICON_MARGIN);

        if (TOP_NOTIFICATION.equals(alignment)) {
            layoutParams.gravity |= Gravity.TOP;
            layoutParams.topMargin = contentHeight + defIconMargin;
            leapIcon.setLayoutParams(layoutParams);
            return;
        }
        layoutParams.gravity |= Gravity.BOTTOM;
        layoutParams.bottomMargin = contentHeight + defIconMargin;
        leapIcon.setLayoutParams(layoutParams);
    }

    @Override
    public void onCompletion() {
        hide();
        if (this.assistActionListener != null)
            assistActionListener.onAssistActionPerformed(EventConstants.ON_SWIPE_TO_DISMISS);
        if (assistAnimationListener != null)
            assistAnimationListener.onIndependentIconAnimationCanStart(Constants.Visual.VISUAL_TYPE_NOTIFICATION);
    }

    @Override
    public void onSwipeComplete(int alignment) {
        this.swipeLayout.swipeCompletion(alignment);
    }

    @Override
    public boolean isNonAnchorAssist() {
        return true;
    }

    @Override
    public void setupView(Context context) {
        roundedCornerWrapper = new RoundedCornerView(context);

        layoutNotificationWrapper.addView(roundedCornerWrapper);

        LeapWebView leapWebView = new LeapWebView(context);
        setLeapWebView(leapWebView);

        leapIcon = LeapIcon.getAssociatedLeapIcon(context);
        layoutNotificationWrapper.addView(leapIcon);

    }
}
