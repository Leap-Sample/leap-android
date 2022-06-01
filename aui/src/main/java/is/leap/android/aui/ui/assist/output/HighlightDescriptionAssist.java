package is.leap.android.aui.ui.assist.output;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;

import is.leap.android.aui.AUIConstants;
import is.leap.android.aui.ui.LeapIcon;
import is.leap.android.aui.ui.assist.AssistUtils;
import is.leap.android.aui.ui.assist.listener.AnimatorEndListener;
import is.leap.android.aui.ui.assist.view.HighlightDescView;
import is.leap.android.aui.ui.assist.view.HighlightDescViewPositioner;
import is.leap.android.aui.ui.assist.view.HighlightView;
import is.leap.android.aui.ui.assist.view.TooltipHighlightBg;
import is.leap.android.aui.ui.view.LeapWebView;
import is.leap.android.aui.util.AnimUtils;
import is.leap.android.aui.util.AppUtils;
import is.leap.android.core.EventConstants;
import is.leap.android.core.data.model.AssistInfo;
import is.leap.android.core.data.model.DismissAction;
import is.leap.android.core.data.model.ExtraProps;
import is.leap.android.core.data.model.IconSetting;
import is.leap.android.core.data.model.Style;

import static is.leap.android.aui.util.AnimUtils.executeAnimations;
import static is.leap.android.core.Constants.ExtraProps.ANIMATE_HIGHLIGHT;
import static is.leap.android.core.Constants.ExtraProps.HIGHLIGHT_BG_SHAPE;
import static is.leap.android.core.Constants.ExtraProps.HIGHLIGHT_CIRCLE_TYPE;
import static is.leap.android.core.Constants.ExtraProps.HIGHLIGHT_CONNECTOR_COLOR;
import static is.leap.android.core.Constants.ExtraProps.HIGHLIGHT_CONNECTOR_LENGTH;
import static is.leap.android.core.Constants.ExtraProps.HIGHLIGHT_CONNECTOR_TYPE;
import static is.leap.android.core.Constants.ExtraProps.HIGHLIGHT_CORNER_RADIUS;
import static is.leap.android.core.Constants.ExtraProps.HIGHLIGHT_TYPE;
import static is.leap.android.core.Constants.ExtraProps.SOLID_WITH_CIRCLE;
import static is.leap.android.core.Constants.Visual.VISUAL_TYPE_HIGHLIGHT_WITH_DESC;

public class HighlightDescriptionAssist extends SameWindowWebContentAssist implements HighlightView.HighlightActionListener {

    protected static final int DEFAULT_CONTENT_MARGIN = AUIConstants.DEFAULT_MARGIN_5;
    protected HighlightDescView highlightDescView;
    protected Rect descViewBounds;
    protected LeapIcon iconView;
    protected Rect descViewPosition;
    protected int connectorAlignment;
    private final Rect rootViewBounds;

    public HighlightDescriptionAssist(Activity activity, View rootView, String accessibilityText) {
        super(activity, rootView);
        rootViewBounds = AssistUtils.getRootRect(rootView);
        init(activity, accessibilityText);
    }

    @Override
    public void init(Activity activity, String accessibilityText) {
        highlightDescView = new HighlightDescView(activity);
        highlightDescView.setHighlightActionListener(this);
        AppUtils.setContentDescription(highlightDescView, accessibilityText);

        iconView = LeapIcon.getAssociatedLeapIcon(activity);
        highlightDescView.addView(iconView);
        hideIcon();

        setLeapWebView(highlightDescView.getLeapWebView());
        initLeapRootView();
        hide(false);
        addToRoot();
        makeIconUnclickable();
    }

    private void makeIconUnclickable() {
        iconView.setOnClickListener(null);
    }

    private void hideIcon() {
        iconView.hide();
    }

    void showIcon() {
        iconView.show();
    }

    @Override
    public void setIconSetting(IconSetting iconSetting) {
        super.setIconSetting(iconSetting);
        iconView.setProps(iconSetting.bgColor, iconSetting.htmlUrl, iconSetting.contentUrls);
    }

    @Override
    public void setAssistInfo(AssistInfo assistInfo) {
        super.setAssistInfo(assistInfo);
        highlightDescView.setHighlightAreaClickable(assistInfo.anchorClickable);
    }

    @Override
    public View getAssistView() {
        return highlightDescView;
    }

    @Override
    public void applyStyle(Style style) {
        if (style == null) return;
        super.applyStyle(style);
        if (style.maxWidth > 0) {
            int screenWidth = rootViewBounds.width();
            int maxWidth = (int) (style.maxWidth * screenWidth);
            highlightDescView.updateContentLayout(maxWidth);
        }

        // Set Bg Color
        TooltipHighlightBg.setBgColor(highlightDescView, style.bgColor);
    }

    @Override
    public void setUpLayoutAction(DismissAction dismissAction) {
        if (dismissAction == null) return;
        highlightDescView.setDismissOnOutsideClick(dismissAction.outsideClick);
    }

    @Override
    public void hide(boolean withAnim) {
        super.hide(withAnim);
        highlightDescView.hide();
    }

    @Override
    public void show() {
        super.show();
        if (!isWebRendered) return;
        if (assistDisplayListener != null)
            assistDisplayListener.onAnchorAssistDetected(getElevation());
        highlightDescView.show();
    }

    @Override
    public void updateLayoutParams(Rect oldRect, Rect rect, String alignment) {
        super.updateLayoutParams(oldRect, rect, alignment);
        if (!isWebRendered) return;
        Activity activity = getCurrentActivity();
        int margin = AppUtils.dpToPxInt(getContext(), DEFAULT_CONTENT_MARGIN);

        ExtraProps extraProps = getAssistExtraProps();
        String connectorType;
        String connectorColor;
        String highlightType;
        String highlightBgShape = null;
        int connectorLength;
        int cornerRadius;
        boolean animateHighlight = true;
        if (extraProps == null) {
            connectorType = SOLID_WITH_CIRCLE;
            connectorColor = HighlightDescView.DEFAULT_CONNECTOR_COLOR;
            highlightType = HIGHLIGHT_CIRCLE_TYPE;
            connectorLength = HighlightDescView.DEFAULT_CONNECTOR_LENGTH;
            cornerRadius = AUIConstants.DEFAULT_MARGIN_5;
        } else {
            connectorType = extraProps.getStringProp(HIGHLIGHT_CONNECTOR_TYPE);
            connectorColor = extraProps.getStringProp(HIGHLIGHT_CONNECTOR_COLOR);
            highlightType = extraProps.getStringProp(HIGHLIGHT_TYPE);
            highlightBgShape = extraProps.getStringProp(HIGHLIGHT_BG_SHAPE);
            connectorLength = extraProps.getIntProp(HIGHLIGHT_CONNECTOR_LENGTH);
            cornerRadius = extraProps.getIntProp(HIGHLIGHT_CORNER_RADIUS);
            animateHighlight = extraProps.getBooleanProp(ANIMATE_HIGHLIGHT, true);
        }

        highlightDescView.setAnimateHighlight(animateHighlight);
        int shape = highlightDescView.getShapeProps(highlightType);
        highlightDescView.setShape(shape);
        int bgShape = highlightDescView.getBgShapeProps(highlightBgShape);
        highlightDescView.setBgShape(bgShape);
        highlightDescView.setCornerRadius(AppUtils.dpToPx(activity, cornerRadius));
        highlightDescView.setProps(connectorType, connectorLength, connectorColor);
        connectorLength = highlightDescView.getTotalConnectorLength();
        highlightDescView.updateBounds(rect);
        int highlightPadding = highlightDescView.getHighlightPadding();

        Rect rootWindowBounds = AssistUtils.getEffectiveRootWindowBounds(getCurrentActivity(), getTopWindowView());
        if(doesntFitContentInScreen(rect, rootWindowBounds, descViewBounds,
                connectorLength + highlightPadding)) {

            int scrollTo = rootWindowBounds.bottom - descViewBounds.height()
                        - AppUtils.dpToPxInt(getContext(), AUIConstants.DEFAULT_MARGIN_10)
                        - connectorLength - highlightPadding;
            appScrollListener.canStartScroll(rect, scrollTo, this.scrollTo != scrollTo);
            this.scrollTo = scrollTo;
        } else {
            int scrollTo = -1;
            int _70_margin = AppUtils.dpToPxInt(getContext(), AUIConstants.DEFAULT_MARGIN_70);
            if(canBeShadowedByToolbar(rect)) {
                scrollTo = 2 * _70_margin;
            } else if (canBeShadowedByNavBar(rect, rootViewBounds)) {
                scrollTo = rootWindowBounds.bottom - 2 * _70_margin;
            }
            if(scrollTo == -1) return;
            appScrollListener.canStartScroll(rect, scrollTo, this.scrollTo != scrollTo);
            this.scrollTo = scrollTo;
        }

        int associatedIconHeight = isIconEnabled()
                ? AppUtils.dpToPxInt(activity, AUIConstants.ASSOCIATE_ICON_HEIGHT) +
                AppUtils.dpToPxInt(activity, AUIConstants.AUI_ASSOCIATE_ICON_MARGIN)
                : 0;


        int statusBarHeight = AppUtils.getStatusBarHeight(activity);
        HighlightDescViewPositioner positioner = new HighlightDescViewPositioner(descViewBounds, rootViewBounds,
                rect, connectorLength, margin, highlightPadding, statusBarHeight, associatedIconHeight);

        descViewPosition = positioner.getDescViewPosition();
        connectorAlignment = positioner.getConnectorAlignment();
        int connectorStartX = positioner.getConnectorStartX();
        int connectorStartY = positioner.getConnectorStartY(connectorAlignment);

        highlightDescView.setConnectorLineProps(connectorStartX, connectorStartY, connectorAlignment);
        highlightDescView.updateDescLayoutParams(descViewPosition, rootViewBounds);
        alignIcon(descViewPosition);
    }

    @Override
    protected void alignIcon(Rect contentBounds) {
        if (contentBounds == null) return;
        if (!isIconEnabled()) return;
        // Tooltip at top
        showIcon();
        updateIconPosition(contentBounds);
    }

    private void updateIconPosition(Rect contentBounds) {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) iconView.getLayoutParams();
        float defIconMargin = AppUtils.dpToPx(getContext(), AUIConstants.AUI_ASSOCIATE_ICON_MARGIN);
        int iconHeight = AppUtils.dpToPxInt(getContext(), AUIConstants.ASSOCIATE_ICON_HEIGHT);
        layoutParams.width = layoutParams.height = iconHeight;
        float associateIconSize = AppUtils.dpToPx(getContext(), AUIConstants.ASSOCIATE_ICON_HEIGHT);

        if (isIconLeftAligned())
            layoutParams.leftMargin = contentBounds.left;
        else
            layoutParams.leftMargin = (int) (contentBounds.right - associateIconSize);

        if (HighlightDescView.ConnectorAlignment.BOTTOM == connectorAlignment) {
            layoutParams.topMargin = (int) (contentBounds.bottom + defIconMargin);
        } else {
            layoutParams.topMargin = (int) (contentBounds.top - iconHeight - defIconMargin);
        }
        layoutParams.gravity = Gravity.START | Gravity.TOP;
        iconView.setLayoutParams(layoutParams);
    }

    /**
     * ### **Overlay**
     * <p>
     * **Opacity**
     * Beginning time: 0ms, Opacity is 0%
     * End time: 120ms, Opacity is XX% (as selected by the user)
     * Easing: standard
     * <p>
     * ### **Content**
     * <p>
     * **Opacity**
     * Beginning time: 120ms, Opacity is 0%
     * End time: 200ms, Opacity is 100%
     * Easing: standard
     * <p>
     * ### Icon 1
     * <p>
     * **Opacity**
     * Beginning time: 160ms, Opacity is 0%
     * End time: 200ms, Opacity is 100%
     * Easing: standard
     *
     * @param animatorEndListener
     */
    @Override
    public void performEnterAnimation(final AnimatorEndListener animatorEndListener) {
        if (isIconEnabled()) showIcon();
        highlightDescView.setAlpha(0);
        final float bgAlpha = highlightDescView.getBgAlpha();
        ObjectAnimator overlayAlphaAnimator = AnimUtils.getAlphaAnimator(highlightDescView,
                0f, bgAlpha, 120, new LinearInterpolator(),
                0, new AnimatorEndListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        highlightDescView.setAlpha(bgAlpha);
                    }
                });

        ObjectAnimator leapWebViewAlphaAnimator = null;
        final LeapWebView leapWebView = highlightDescView.getLeapWebView();
        leapWebView.setAlpha(0);
        leapWebViewAlphaAnimator = AnimUtils.getAlphaAnimator(leapWebView,
                0f, 1f, 80, new LinearInterpolator(),
                120, new AnimatorEndListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        leapWebView.setAlpha(1);
                    }
                });

        iconView.setAlpha(0);
        ObjectAnimator iconAlphaAnimator = AnimUtils.getAlphaAnimator(iconView,
                0f, 1f, 40, new LinearInterpolator(),
                160, new AnimatorEndListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        iconView.setAlpha(1);
                    }
                });

        Animator[] togetherAnimators = {overlayAlphaAnimator, iconAlphaAnimator, leapWebViewAlphaAnimator};
        executeAnimations(200, togetherAnimators, null, animatorEndListener);
    }

    /**
     * ### **Overlay**
     * <p>
     * **Opacity**
     * Beginning time: 0ms, Opacity is XX%
     * End time: 80ms, Opacity is 0% (as selected by the user)
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
     * ### Icon 2
     * <p>
     * **Opacity**
     * Beginning time: 0ms, Opacity is 0%
     * End time: 80ms, Opacity is 100%
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
        if (assistAnimationListener != null && isActionTakenForExit())
            assistAnimationListener.onIndependentIconAnimationCanStart(VISUAL_TYPE_HIGHLIGHT_WITH_DESC);

        final float bgAlpha = highlightDescView.getBgAlpha();
        ObjectAnimator overlayAlphaAnimator = AnimUtils.getAlphaAnimator(highlightDescView,
                bgAlpha, 0f, 120, new LinearInterpolator(),
                0, new AnimatorEndListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        highlightDescView.hide();
                        if (animationEndListener != null)
                            animationEndListener.onAnimationEnd(animation);
                    }
                });
        overlayAlphaAnimator.start();
    }

    @Override
    void updateContentLayout(int pageWidth, int pageHeight) {
        LeapWebView leapWebView = highlightDescView.getLeapWebView();
        leapWebView.updateLayout(pageWidth, pageHeight);
        highlightDescView.updateContentLayout(pageWidth, pageHeight);
        descViewBounds = new Rect(0, 0, pageWidth, pageHeight);
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
