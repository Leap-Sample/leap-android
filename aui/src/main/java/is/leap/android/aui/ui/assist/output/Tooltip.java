package is.leap.android.aui.ui.assist.output;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;

import is.leap.android.aui.AUIConstants;
import is.leap.android.aui.data.LeapAUICache;
import is.leap.android.aui.ui.LeapCustomViewGroup;
import is.leap.android.aui.ui.LeapIcon;
import is.leap.android.aui.ui.assist.AssistUtils;
import is.leap.android.aui.ui.assist.listener.AnimatorEndListener;
import is.leap.android.aui.ui.assist.view.HighlightView;
import is.leap.android.aui.ui.assist.view.TooltipHighlightBg;
import is.leap.android.aui.ui.view.LeapWebView;
import is.leap.android.aui.ui.view.shapes.TooltipMaskView;
import is.leap.android.aui.ui.view.shapes.TooltipPositioner;
import is.leap.android.aui.util.AnimUtils;
import is.leap.android.aui.util.AppUtils;
import is.leap.android.core.Constants;
import is.leap.android.core.EventConstants;
import is.leap.android.core.data.model.AssistInfo;
import is.leap.android.core.data.model.DismissAction;
import is.leap.android.core.data.model.ExtraProps;
import is.leap.android.core.data.model.IconSetting;
import is.leap.android.core.data.model.Style;

import static is.leap.android.aui.ui.view.shapes.TooltipMaskView.TOOLTIP_TIP_HEIGHT;
import static is.leap.android.aui.util.AnimUtils.executeAnimations;
import static is.leap.android.core.Constants.ExtraProps.HIGHLIGHT_CIRCLE_TYPE;

public class Tooltip extends SameWindowWebContentAssist
        implements HighlightView.HighlightActionListener, LeapCustomViewGroup {

    private static final int DEFAULT_CORNER_RADIUS = AUIConstants.DEFAULT_MARGIN_8;
    private static final int DEFAULT_TOOLTIP_MARGIN = AUIConstants.DEFAULT_MARGIN_5;
    public static final int DEFAULT_FULL_WIDTH_TOOLTIP_MARGIN = 48;
    public static final int TOOLTIP_DEFAULT_MARGIN = 7;
    private TooltipHighlightBg toolTipRootView;
    private TooltipMaskView tooltipMaskView;
    private LeapWebView leapWebView;
    private Rect tooltipBounds;
    private Rect toolTipPosition;
    private
    @TooltipMaskView.TipAlignment
    int tipPosition;
    private float tipX;
    private float tipY;
    private LeapIcon iconView;
    private Rect rect;
    private String alignment;
    private final Rect rootViewBounds;

    public Tooltip(Activity activity, View rootView, String accessibilityText) {
        super(activity, rootView);
        rootViewBounds = AssistUtils.getRootRect(rootView);
        init(activity, accessibilityText);
    }

    @Override
    public void init(Activity activity, String accessibilityText) {
        setupView(activity);
        toolTipRootView.setHighlightActionListener(this);
        AppUtils.setContentDescription(tooltipMaskView, accessibilityText);
        hideIcon();

        setLeapWebView(leapWebView);
        initLeapRootView();
        hide(false);
        addToRoot();
        makeIconUnclickable();
    }

    @Override
    public void setupView(Context context) {
        toolTipRootView = new TooltipHighlightBg(context);

        tooltipMaskView = new TooltipMaskView(context);
        tooltipMaskView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT));
        toolTipRootView.addView(tooltipMaskView);

        leapWebView = new LeapWebView(context);
        tooltipMaskView.addView(leapWebView);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        leapWebView.setLayoutParams(params);

        iconView = LeapIcon.getAssociatedLeapIcon(context);
        toolTipRootView.addView(iconView);
    }

    private void makeIconUnclickable() {
        iconView.setOnClickListener(null);
    }

    private void hideIcon() {
        iconView.hide();
    }

    private void showIcon() {
        iconView.show();
    }

    @Override
    public void show() {
        super.show();
        if (!isWebRendered) return;
        if (assistDisplayListener != null)
            assistDisplayListener.onAnchorAssistDetected(getElevation());
    }

    @Override
    public View getAssistView() {
        return toolTipRootView;
    }

    @Override
    public void setIconSetting(IconSetting iconSetting) {
        super.setIconSetting(iconSetting);
        iconView.setProps(iconSetting.bgColor, iconSetting.htmlUrl, iconSetting.contentUrls);
    }

    @Override
    public void setAssistInfo(AssistInfo assistInfo) {
        super.setAssistInfo(assistInfo);
        toolTipRootView.setHighlightAreaClickable(shouldTrackAnchorTouch || assistInfo.anchorClickable);
    }

    @Override
    public void applyStyle(Style style) {
        if (style == null) return;
        super.applyStyle(style);
        if (style.maxWidth > 0) {
            int screenWidth = rootViewBounds == null ? 0 : rootViewBounds.width();
            int maxWidth = (int) (style.maxWidth * screenWidth);
            if (style.maxWidth == 1)
                maxWidth -= AppUtils.dpToPxInt(getContext(), DEFAULT_FULL_WIDTH_TOOLTIP_MARGIN);
            ViewGroup.LayoutParams layoutParams = tooltipMaskView.getLayoutParams();
            layoutParams.width = maxWidth;
            tooltipMaskView.setLayoutParams(layoutParams);
        }

        if (highlightAnchor()) {
            toolTipRootView.highlightAnchor();
            // Set Bg Color
            TooltipHighlightBg.setBgColor(toolTipRootView, style.bgColor);
        } else {
            // reset background
            toolTipRootView.setBgColor(Color.TRANSPARENT);
        }

        // Set Corner Radius
        float cornerRadius = getCornerRadius(style, DEFAULT_CORNER_RADIUS);
        tooltipMaskView.setCornerRadius(cornerRadius);
        tooltipMaskView.setStroke(style.strokeWidth, Color.parseColor(style.strokeColor));

        // Set Elevation
        float elevation = getElevation(style, AUIConstants.DEFAULT_MARGIN_8);
        tooltipMaskView.setElevation(elevation);
    }

    @Override
    public boolean shouldCeilWidthAndHeightValue() {
        ExtraProps extraProps = getAssistExtraProps();
        if (extraProps == null) return false;
        String tooltipType = extraProps.getStringProp(Constants.ExtraProps.TOOLTIP_TYPE);
        return Constants.ExtraProps.WRAP_TOOLTIP_TYPE.equals(tooltipType);
    }

    @Override
    public void setUpLayoutAction(DismissAction dismissAction) {
        if (dismissAction == null) return;
        toolTipRootView.setDismissOnOutsideClick(dismissAction.outsideClick);
    }

    /**
     * ### **Overlay** (If applicable)
     * <p>
     * **Opacity**
     * Beginning time: 0ms, Opacity is 0%
     * End time: 160ms, Opacity is XX% (as selected by the user)
     * Easing: standard
     * <p>
     * ### **Container & content**
     * <p>
     * **Opacity**
     * Beginning time: 0ms, Opacity is 0%
     * End time: 160ms, Opacity is 100%
     * Easing: standard
     * <p>
     * **Position**
     * Beginning time: 0ms, begins at 20 px to the bottom of final position
     * End time: 160ms, Final defined position
     * Easing: standard
     * <p>
     * ### Icon 1
     * <p>
     * **Opacity**
     * Beginning time: 240ms, Opacity is 0%
     * End time: 360ms, Opacity is 100%
     * Easing: standard
     *
     * @param animatorEndListener
     */
    @Override
    public void performEnterAnimation(final AnimatorEndListener animatorEndListener) {

        ObjectAnimator overlayAlphaAnimator = null;
        if (highlightAnchor()) {
            toolTipRootView.setAlpha(0);
            final float bgAlpha = toolTipRootView.getBgAlpha();
            overlayAlphaAnimator = AnimUtils.getAlphaAnimator(toolTipRootView,
                    0f, bgAlpha, 120, new LinearInterpolator(),
                    0, new AnimatorEndListener() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            toolTipRootView.setAlpha(bgAlpha);
                        }
                    });
        }

        tooltipMaskView.setAlpha(0);
        ObjectAnimator containerAlphaAnimator = AnimUtils.getAlphaAnimator(tooltipMaskView,
                0f, 1f, 40, null, 80, new AnimatorEndListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        tooltipMaskView.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        tooltipMaskView.setAlpha(1f);
                    }
                });

        float translationYValue = AppUtils.dpToPx(getContext(), 20);
        float translationYFromValue = TooltipMaskView.TipAlignment.BOTTOM == tipPosition
                ? -translationYValue
                : translationYValue;
        ObjectAnimator contentTranslationYAnimator = AnimUtils.getTranslationYAnimator(tooltipMaskView,
                160, null, 0,
                null, translationYFromValue, 0);

        if (isIconEnabled()) showIcon();
        iconView.setAlpha(0);
        ObjectAnimator iconAlphaAnimator = AnimUtils.getAlphaAnimator(iconView,
                0f, 1f, 80, null, 240, new AnimatorEndListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        iconView.setAlpha(1);
                    }
                });

        Animator[] togetherAnimators;
        if (overlayAlphaAnimator != null)
            togetherAnimators = new Animator[]{overlayAlphaAnimator, containerAlphaAnimator,
                    contentTranslationYAnimator, iconAlphaAnimator};
        else
            togetherAnimators = new Animator[]{containerAlphaAnimator,
                    contentTranslationYAnimator, iconAlphaAnimator};
        executeAnimations(360, togetherAnimators, null, animatorEndListener);

    }

    /**
     * ### Container
     * <p>
     * **Opacity**
     * Beginning time: 0 ms, Opacity is 100%
     * End time: instant , Opacity is 0%
     * Easing: standard
     * <p>
     * ### Content
     * <p>
     * **Opacity**
     * Beginning time: 0 ms, Opacity is 100%
     * End time: instant , Opacity is 0%
     * Easing: standard
     * <p>
     * ### Icon 1
     * <p>
     * **Opacity**
     * Beginning time: 0 ms, Opacity is 100%
     * End time: instant ms, Opacity is 0%
     * Easing: standard
     * <p>
     * ### Overlay
     * <p>
     * **Opacity**
     * Beginning time: 0 ms, Opacity is XX% (as selected by the user)
     * End time: 80 ms, Opacity is 0%
     * Easing: standard
     * <p>
     * ### Icon 2
     * <p>
     * **Opacity**
     * Beginning time: 0 ms, Opacity is 0%
     * End time: 80 ms, Opacity is 100%
     * Easing: standard
     * <p>
     * **Scaling**
     * Beginning time: 0 ms, size equal to icon 1
     * End time: 80 ms, size of icon 2
     * Easing: standard
     *
     * @param animationEndListener
     */
    @Override
    public void performExitAnimation(final AnimatorEndListener animationEndListener) {
        iconView.setVisibility(View.INVISIBLE);
        tooltipMaskView.setVisibility(View.INVISIBLE);

        if (assistAnimationListener != null && isActionTakenForExit())
            assistAnimationListener.onIndependentIconAnimationCanStart(Constants.Visual.VISUAL_TYPE_TOOLTIP);


        final float bgAlpha = toolTipRootView.getBgAlpha();
        ObjectAnimator overlayAlphaAnimator = AnimUtils.getAlphaAnimator(toolTipRootView,
                bgAlpha, 0f,
                highlightAnchor()
                        ? 80
                        : 0,
                new LinearInterpolator(),
                0, new AnimatorEndListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        toolTipRootView.hide();
                        if (animationEndListener != null)
                            animationEndListener.onAnimationEnd(animation);
                    }
                });
        overlayAlphaAnimator.start();
    }

    @Override
    public void updateLayoutParams(Rect oldRect, Rect rect, String alignment) {
        if (rect == null) return;
        this.rect = rect;
        this.alignment = alignment;
        super.updateLayoutParams(oldRect, rect, alignment);
        if (!isWebRendered) return;
        Activity activity = getCurrentActivity();
        int margin = AppUtils.dpToPxInt(getContext(), DEFAULT_TOOLTIP_MARGIN);

        ExtraProps extraProps = getAssistExtraProps();

        int cornerRadius;
        String highlightType;
        boolean animateHighlight = true;
        if (extraProps == null) {
            cornerRadius = AUIConstants.DEFAULT_MARGIN_5;
            highlightType = HIGHLIGHT_CIRCLE_TYPE;
        } else {
            cornerRadius = extraProps.getIntProp(Constants.ExtraProps.HIGHLIGHT_CORNER_RADIUS);
            highlightType = extraProps.getStringProp(Constants.ExtraProps.HIGHLIGHT_TYPE);
            animateHighlight = extraProps.getBooleanProp(Constants.ExtraProps.ANIMATE_HIGHLIGHT, false);
        }

        int shape = toolTipRootView.getShapeProps(highlightType);
        toolTipRootView.setShape(shape);
        toolTipRootView.setCornerRadius(AppUtils.dpToPx(activity, cornerRadius));
        toolTipRootView.setAnimateHighlight(animateHighlight);

        boolean highlightAnchor = highlightAnchor();
        // Set default padding to anchor rect
        int highlightPadding = toolTipRootView.getHighlightPadding();

        int arrowHeightPx = (int) tooltipMaskView.getArrowHeightPx();
        Rect rootWindowBounds = AssistUtils.getEffectiveRootWindowBounds(getCurrentActivity(), getTopWindowView());
        if (doesntFitContentInScreen(rect, rootWindowBounds, tooltipBounds,
                arrowHeightPx + highlightPadding)) {
            int scrollTo = rootWindowBounds.bottom - tooltipBounds.height()
                    - AppUtils.dpToPxInt(getContext(), AUIConstants.DEFAULT_MARGIN_10)
                    - arrowHeightPx - highlightPadding;
            appScrollListener.canStartScroll(rect, scrollTo, this.scrollTo != scrollTo);
            this.scrollTo = scrollTo;
        } else {
            int scrollTo = -1;
            int _70_margin = AppUtils.dpToPxInt(getContext(), AUIConstants.DEFAULT_MARGIN_70);
            if (canBeShadowedByToolbar(rect)) {
                scrollTo = 2 * _70_margin;
            } else if (canBeShadowedByNavBar(rect, rootViewBounds)) {
                scrollTo = rootWindowBounds.bottom - 2 * _70_margin;
            }
            if (scrollTo == -1) return;
            appScrollListener.canStartScroll(rect, scrollTo, this.scrollTo != scrollTo);
            this.scrollTo = scrollTo;
        }

        int associatedIconHeight = isIconEnabled()
                ? AppUtils.dpToPxInt(activity, AUIConstants.ASSOCIATE_ICON_HEIGHT) +
                AppUtils.dpToPxInt(activity, AUIConstants.AUI_ASSOCIATE_ICON_MARGIN)
                : 0;

        int statusBarHeight = AppUtils.getStatusBarHeight(activity);
        TooltipPositioner tooltipPositioner = new TooltipPositioner(tooltipBounds, rootViewBounds,
                rect, margin, highlightPadding, statusBarHeight, associatedIconHeight);
        toolTipPosition = tooltipPositioner.getTooltipPosition();
        toolTipRootView.updateBounds(highlightAnchor, rect);
        tipPosition = tooltipPositioner.getTipPosition();
        tooltipMaskView.setPosition(tipPosition);
        tipX = tooltipPositioner.getTipX();
        tipY = tooltipPositioner.getTipY();

        int marginLeft = adjustTooltipPositions();

        tooltipMaskView.setTipXY(tipX, tipY);
        updateTooltipLayoutParams(marginLeft, rootViewBounds);
        alignIcon(toolTipPosition);
    }

    // The tooltip positions needs to be readjusted if the tip is at left most or right most.
    // Since the html content is loaded and clipped there is always a gap of 12 pixel which is the arrow height
    // To understand more refer this pic
    // https://drive.google.com/file/d/15VqydfUcGdEoZjbBnA8zlNof20abCupg/view?usp=sharing
    private int adjustTooltipPositions() {
        int marginLeft = toolTipPosition.left;
        // Tooltip content right value after the html content is masked
        float tooltipContentRight = toolTipPosition.right - tooltipMaskView.getArrowHeightPx();
        // The starting x value of right tip of tooltip
        float tipRightStartingX = rect.centerX() + tooltipMaskView.getArrowWidthPx() / 2;

        Style style = getLayoutStyle();
        float cornerRadius = getCornerRadius(style, DEFAULT_CORNER_RADIUS);

        float defaultMargin = AppUtils.dpToPx(getContext(), TOOLTIP_DEFAULT_MARGIN);
        // Right most
        if (tipRightStartingX + defaultMargin > tooltipContentRight) {
            float diff = tipRightStartingX + defaultMargin - tooltipContentRight;
            if (diff < cornerRadius) diff += cornerRadius;
            marginLeft += diff;
            // Adjust the tip also
            tipX -= diff;
            return marginLeft;
        }
        // Left most
        // Tooltip content left value after the html content is masked
        float tooltipContentLeft = toolTipPosition.left + tooltipMaskView.getArrowHeightPx();
        // The starting x value of left tip of tooltip
        float tipLeftStartingX = rect.centerX() - tooltipMaskView.getArrowWidthPx() / 2;
        if (tipLeftStartingX - defaultMargin < tooltipContentLeft) {
            float diff = tooltipContentLeft - (tipLeftStartingX - defaultMargin);
            if (diff < cornerRadius) diff += cornerRadius;
            marginLeft -= (diff);
            // Adjust the tip also
            tipX += diff;
        }
        return marginLeft;
    }

    private void updateTooltipLayoutParams(int marginLeft, Rect rootViewBounds) {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) tooltipMaskView.getLayoutParams();
        switch (tipPosition) {
            case TooltipMaskView.TipAlignment.BOTTOM: {
                layoutParams.setMargins(marginLeft, 0, 0, rootViewBounds.bottom - toolTipPosition.bottom);
                layoutParams.gravity = Gravity.START | Gravity.BOTTOM;
                break;
            }
            case TooltipMaskView.TipAlignment.TOP: {
                layoutParams.setMargins(marginLeft, toolTipPosition.top, 0, 0);
                layoutParams.gravity = Gravity.START | Gravity.TOP;
                break;
            }
        }
        tooltipMaskView.setLayoutParams(layoutParams);
    }

    @Override
    public void updateContentLayout(int pageWidth, int pageHeight) {
        pageWidth = getUpdatedPageWidth(pageWidth);
        leapWebView.updateLayout(pageWidth, pageHeight);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) tooltipMaskView.getLayoutParams();
        if (layoutParams == null) {
            layoutParams = new FrameLayout.LayoutParams(pageWidth, pageHeight);
        } else {
            layoutParams.width = pageWidth;
            layoutParams.height = pageHeight;
        }
        tooltipMaskView.setLayoutParams(layoutParams);
        tooltipBounds = new Rect(0, 0, pageWidth, pageHeight);
        updateLayoutParams(null, rect, alignment);
    }

    /**
     * Resolves the issue where tooltip was expanding on phones with bigger width
     * Bug observed on One Plus 7 pro
     *
     * @return updated pageWidth
     */
    private int getUpdatedPageWidth(int pageWidth) {
        if (rootViewBounds == null || getLayoutStyle() == null || getLayoutStyle().maxWidth < 1)
            return pageWidth;
        int updatedWidth = rootViewBounds.width() - AppUtils.dpToPxInt(getContext(), DEFAULT_FULL_WIDTH_TOOLTIP_MARGIN);
        if (getLayoutStyle().maxWidth == 1 && pageWidth > updatedWidth) return updatedWidth;
        return pageWidth;
    }

    @Override
    protected void alignIcon(Rect contentBounds) {
        if (contentBounds == null) return;
        if (!isIconEnabled()) return;
        // Tooltip at top
        updateIconPosition(contentBounds);
    }

    private void updateIconPosition(Rect contentBounds) {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) iconView.getLayoutParams();
        float defIconMargin = AppUtils.dpToPx(getContext(), AUIConstants.AUI_ASSOCIATE_ICON_MARGIN);
        float associateIconSize = AppUtils.dpToPx(getContext(), AUIConstants.ASSOCIATE_ICON_HEIGHT);
        int tooltipTipHeight = AppUtils.dpToPxInt(getContext(), TOOLTIP_TIP_HEIGHT);
        if (isIconLeftAligned())
            layoutParams.leftMargin = (int) (contentBounds.left + tooltipTipHeight);
        else
            layoutParams.leftMargin = (int) (contentBounds.right - associateIconSize - tooltipTipHeight);

        if (TooltipMaskView.TipAlignment.BOTTOM == tipPosition) {
            int softButtonBarHeight = LeapAUICache.getSoftButtonBarHeight(getContext());
            int screenHeight = LeapAUICache.screenHeight + softButtonBarHeight;
            layoutParams.bottomMargin = (int) (screenHeight - contentBounds.top + defIconMargin - tooltipTipHeight);
            layoutParams.gravity = Gravity.START | Gravity.BOTTOM;
            iconView.setLayoutParams(layoutParams);
            return;
        }
        layoutParams.topMargin = (int) (contentBounds.bottom + defIconMargin - tooltipTipHeight);
        layoutParams.gravity = Gravity.START | Gravity.TOP;
        iconView.setLayoutParams(layoutParams);
    }

    @Override
    public void onHighlightClicked() {
        setActionTakenForExit(true);
        if (assistActionListener != null)
            assistActionListener.onAssistActionPerformed(EventConstants.ANCHOR_CLICK);
    }

    @Override
    public void onNonHighlightClicked() {
        if (!assistInfo.isOutsideDismissible()) return;
        setActionTakenForExit(true);
        performExitAnimation(new AnimatorEndListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (assistActionListener != null)
                    assistActionListener.onAssistActionPerformed(EventConstants.OUTSIDE_ANCHOR_CLICK);
            }
        });
    }

}