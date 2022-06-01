package is.leap.android.aui.ui.assist.output;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import is.leap.android.aui.AUIConstants;
import is.leap.android.aui.LeapAUIInternal;
import is.leap.android.aui.LeapAUILogger;
import is.leap.android.aui.R;
import is.leap.android.aui.data.LeapAUICache;
import is.leap.android.aui.ui.LeapCustomViewGroup;
import is.leap.android.aui.ui.LeapIcon;
import is.leap.android.aui.ui.assist.listener.AnimatorEndListener;
import is.leap.android.aui.ui.view.LeapWebView;
import is.leap.android.aui.ui.view.shapes.RoundedCornerView;
import is.leap.android.aui.util.AnimUtils;
import is.leap.android.aui.util.AppUtils;
import is.leap.android.core.Constants;
import is.leap.android.core.EventConstants;
import is.leap.android.core.data.model.AssistInfo;
import is.leap.android.core.data.model.DismissAction;
import is.leap.android.core.data.model.IconSetting;
import is.leap.android.core.data.model.Style;
import is.leap.android.core.util.StringUtils;

import static is.leap.android.aui.AUIConstants.AccessibilityText.PING_CROSS;

public class Ping extends SameWindowWebContentAssist implements View.OnClickListener,
        LeapCustomViewGroup {

    private RelativeLayout pingRootView;
    private FrameLayout pingOverlay;
    private LinearLayout pingContentLayout;
    private RoundedCornerView roundedCornerWrapper;
    private LeapIcon leapIcon;
    private LeapWebView leapWebView;
    private ImageView crossView;

    private final int SIDE_MARGIN;
    private final int CROSS_BOTTOM_MARGIN;
    private final int ICON_INITIAL_POS;
    private final int BUBBLE_INITIAL_POS;
    private static final int ENTER_ANIM_ICON_DURATION = 100;
    private static final int EXIT_ANIM_ICON_DURATION = 100;
    private static final int ENTER_ANIM_BUBBLE_DURATION = 100;
    private static final int EXIT_ANIM_BUBBLE_DURATION = 100;
    private static final int ENTER_ALPHA_DURATION = 100;
    private static final int EXIT_ALPHA_DURATION = 100;

    public Ping(Activity activity, View rootView, String accessibilityText) {
        super(activity, rootView);
        SIDE_MARGIN = AppUtils.dpToPxInt(getContext(), AUIConstants.DEFAULT_MARGIN_20);
        CROSS_BOTTOM_MARGIN = AppUtils.dpToPxInt(getContext(), AUIConstants.DEFAULT_MARGIN_8);
        ICON_INITIAL_POS = AppUtils.dpToPxInt(getContext(), AUIConstants.DEFAULT_MARGIN_26);
        BUBBLE_INITIAL_POS = AppUtils.dpToPxInt(getContext(), AUIConstants.DEFAULT_MARGIN_34);
        init(activity, accessibilityText);
    }

    @Override
    public void init(Activity activity, String accessibilityText) {
        pingRootView = (RelativeLayout) LeapAUIInternal.getInstance().inflate(R.layout.leap_layout_ping);
        pingOverlay = pingRootView.findViewById(R.id.ping_overlay);
        pingOverlay.setVisibility(View.GONE);
        pingContentLayout = pingRootView.findViewById(R.id.ping_content_layout);
        crossView = pingRootView.findViewById(R.id.cross_image);
        crossView.setVisibility(View.INVISIBLE);
        crossView.setOnClickListener(this);
        setupView(getContext());
        AppUtils.setContentDescription(roundedCornerWrapper, accessibilityText);
        AppUtils.setContentDescription(crossView, LeapAUICache.getAccessibilityText(PING_CROSS));

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) pingContentLayout.getLayoutParams();
        layoutParams.addRule(RelativeLayout.ABOVE, R.id.leap_associate_icon);
        pingContentLayout.setLayoutParams(layoutParams);
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
    public View getAssistView() {
        return pingRootView;
    }

    @Override
    public void setIconSetting(IconSetting iconSetting) {
        super.setIconSetting(iconSetting);
        leapIcon.setProps(iconSetting.bgColor, iconSetting.htmlUrl, iconSetting.contentUrls);
    }

    @Override
    void updateContentLayout(int pageWidth, int pageHeight) {
        leapWebView.updateLayout(pageWidth, pageHeight);

        //WebView Container Alignment i.e. RoundedCornerView
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) roundedCornerWrapper.getLayoutParams();
        layoutParams.height = pageHeight;
        layoutParams.width = pageWidth;
        layoutParams.gravity = isIconLeftAligned() ? Gravity.START : Gravity.END;
        roundedCornerWrapper.setLayoutParams(layoutParams);

        //Container Alignment
        RelativeLayout.LayoutParams contentParam = (RelativeLayout.LayoutParams) pingContentLayout.getLayoutParams();
        if (isIconLeftAligned()) {
            contentParam.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            contentParam.leftMargin = SIDE_MARGIN;
        } else {
            contentParam.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            contentParam.rightMargin = SIDE_MARGIN;
        }
        pingContentLayout.setLayoutParams(contentParam);

        //Cross Icon Alignment
        LinearLayout.LayoutParams crossViewParam = new LinearLayout.LayoutParams(crossView.getLayoutParams());
        crossViewParam.gravity = isIconLeftAligned() ? Gravity.START : Gravity.END;
        crossViewParam.bottomMargin = CROSS_BOTTOM_MARGIN;
        crossView.setLayoutParams(crossViewParam);

        //Leap Icon Alignment
        RelativeLayout.LayoutParams iconParam = new RelativeLayout.LayoutParams(leapIcon.getLayoutParams());
        iconParam.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        if (isIconLeftAligned()) {
            iconParam.leftMargin = SIDE_MARGIN;
            iconParam.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        } else {
            iconParam.rightMargin = SIDE_MARGIN;
            iconParam.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        }
        int insetBottom = AppUtils.getInsetBottom(getCurrentActivity());
        iconParam.bottomMargin = AppUtils.dpToPxInt(getContext(), iconSetting.iconBottomMargin)
                + insetBottom + ICON_INITIAL_POS;
        leapIcon.setLayoutParams(iconParam);
    }

    @Override
    public void setUpLayoutAction(DismissAction dismissAction) {
        if (dismissAction == null) return;
        boolean outsideDismiss = dismissAction.outsideClick;
        if (outsideDismiss) {
            pingOverlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hide();
                    if (assistActionListener != null)
                        assistActionListener.onAssistActionPerformed(OPT_OUT_CLICK, EventConstants.OUTSIDE_CLICK);
                }
            });
        }
    }

    @Override
    public void setAssistInfo(AssistInfo assistInfo) {
        super.setAssistInfo(assistInfo);
    }

    @Override
    public void performEnterAnimation(final AnimatorEndListener animatorEndListener) {
        pingRootView.setVisibility(View.VISIBLE);
        showIcon();
        hidePingLayout();
        crossView.setVisibility(View.INVISIBLE);

        final LinearInterpolator interpolator = new LinearInterpolator();

        leapIcon.animate()
                .setDuration(ENTER_ANIM_ICON_DURATION)
                .setInterpolator(interpolator)
                .setListener(new AnimatorEndListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        pingContentLayout.setAlpha(0);
                        pingContentLayout.setVisibility(View.VISIBLE);

                        pingContentLayout.animate()
                                .setDuration(ENTER_ANIM_BUBBLE_DURATION)
                                .setInterpolator(interpolator)
                                .setListener(new AnimatorEndListener() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        crossView.setVisibility(View.VISIBLE);
                                        if (animatorEndListener != null)
                                            animatorEndListener.onAnimationEnd(animation);
                                    }
                                })
                                .translationY(-BUBBLE_INITIAL_POS)
                                .start();
                    }
                })
                .translationY(-ICON_INITIAL_POS)
                .start();

        pingContentLayout.setAlpha(0);
        ObjectAnimator alphaAnimator = AnimUtils.getAlphaAnimator(pingContentLayout, 0, 1,
                ENTER_ALPHA_DURATION, interpolator, ENTER_ANIM_ICON_DURATION, new AnimatorEndListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        pingContentLayout.setAlpha(1);
                    }
                });
        alphaAnimator.start();

        ObjectAnimator overlayAlphaAnimator = AnimUtils.getAlphaAnimator(pingOverlay, 0, 1,
                ENTER_ALPHA_DURATION, interpolator, 0, new AnimatorEndListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        pingOverlay.setVisibility(View.VISIBLE);
                    }
                });
        overlayAlphaAnimator.start();
    }

    @Override
    public void performExitAnimation(final AnimatorEndListener animatorEndListener) {
        final LinearInterpolator interpolator = new LinearInterpolator();

        pingContentLayout.animate()
                .setDuration(EXIT_ANIM_BUBBLE_DURATION)
                .setInterpolator(interpolator)
                .setListener(new AnimatorEndListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        hidePingLayout();
                        leapIcon.animate()
                                .setDuration(EXIT_ANIM_ICON_DURATION)
                                .setInterpolator(interpolator)
                                .setListener(new AnimatorEndListener() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        hideIcon();
                                        if (animatorEndListener != null)
                                            animatorEndListener.onAnimationEnd(animation);
                                        if (assistAnimationListener != null)
                                            assistAnimationListener.onIndependentIconAnimationCanStart(Constants.Visual.VISUAL_TYPE_PING);
                                    }
                                })
                                .translationY(0)
                                .start();
                    }
                })
                .translationY(0)
                .start();

        ObjectAnimator alphaAnimator = AnimUtils.getAlphaAnimator(pingContentLayout, 1, 0,
                EXIT_ALPHA_DURATION, interpolator, 0, null);
        alphaAnimator.start();

        ObjectAnimator overlayAlphaAnimator = AnimUtils.getAlphaAnimator(pingOverlay, 1, 0,
                EXIT_ALPHA_DURATION, interpolator, 0, new AnimatorEndListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        pingOverlay.setVisibility(View.GONE);
                    }
                });
        overlayAlphaAnimator.start();
    }

    @Override
    public boolean isNonAnchorAssist() {
        return true;
    }

    private void hidePingLayout() {
        pingContentLayout.setVisibility(View.INVISIBLE);
    }

    @Override
    public void show() {
        super.show();
        if (!isWebRendered) return;
        updateRootLayout(leapIcon);
    }

    @Override
    public void applyStyle(Style style) {
        if (style == null) return;
        super.applyStyle(style);
        if (style.maxWidth > 0) {
            int screenWidth = LeapAUICache.screenWidth;
            int maxWidth = (int) (style.maxWidth * screenWidth);
            ViewGroup.LayoutParams layoutParams = roundedCornerWrapper.getLayoutParams();
            layoutParams.width = maxWidth;
            roundedCornerWrapper.setLayoutParams(layoutParams);
        }

        // Set Elevation
        float elevation = getElevation(style, AUIConstants.DEFAULT_MARGIN_8);
        roundedCornerWrapper.setElevation(elevation);

        // Set Corner Radius
        float cornerRadius = getCornerRadius(style, AUIConstants.DEFAULT_MARGIN_8);
        roundedCornerWrapper.setCornerRadius(cornerRadius);

        //Set bg for overlay
        String bg = style.bgColor;
        if (StringUtils.isNotNullAndNotEmpty(bg)) {
            try {
                int bgColor = Color.parseColor(bg);
                pingOverlay.setBackgroundColor(bgColor);
            } catch (IllegalArgumentException e) {
                LeapAUILogger.debugAUI("Ping bgColor incorrect in config");
            }
        }
    }

    @Override
    public void onClick(View v) {
        crossView.setVisibility(View.INVISIBLE);
        performExitAnimation(new AnimatorEndListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (assistActionListener != null)
                    assistActionListener.onAssistActionPerformed(OPT_OUT_CLICK, EventConstants.CROSS_CLICK);
            }
        });
    }

    @Override
    public void setupView(Context context) {
        roundedCornerWrapper = new RoundedCornerView(context);

        pingContentLayout.addView(roundedCornerWrapper);

        leapWebView = new LeapWebView(context);
        roundedCornerWrapper.addView(leapWebView);

        setLeapWebView(leapWebView);

        leapIcon = LeapIcon.getIndependentLeapIcon(context);
        leapIcon.setId(R.id.leap_associate_icon);
        pingRootView.addView(leapIcon);

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) leapIcon.getLayoutParams();
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        leapIcon.setLayoutParams(layoutParams);
    }
}
