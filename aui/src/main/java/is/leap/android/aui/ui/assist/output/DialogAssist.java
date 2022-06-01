package is.leap.android.aui.ui.assist.output;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;

import is.leap.android.aui.AUIConstants;
import is.leap.android.aui.LeapAUIInternal;
import is.leap.android.aui.R;
import is.leap.android.aui.ui.LeapCustomViewGroup;
import is.leap.android.aui.ui.LeapIcon;
import is.leap.android.aui.ui.assist.listener.AnimatorEndListener;
import is.leap.android.aui.ui.assist.view.AUIDialog;
import is.leap.android.aui.ui.view.LeapWebView;
import is.leap.android.aui.ui.view.shapes.RoundedCornerView;
import is.leap.android.aui.util.AppUtils;
import is.leap.android.core.EventConstants;
import is.leap.android.core.data.model.DismissAction;
import is.leap.android.core.data.model.IconSetting;
import is.leap.android.core.data.model.LayoutInfo;
import is.leap.android.core.data.model.Style;

public abstract class DialogAssist extends WebContentAssist
        implements View.OnClickListener, LeapCustomViewGroup {

    static final int DEFAULT_DIALOG_CONTENT_MARGIN = 24;
    private static final int DEFAULT_ELEVATION = AUIConstants.DEFAULT_MARGIN_8;
    private static final int DEFAULT_CORNER_RADIUS = AUIConstants.DEFAULT_MARGIN_14;
    private static final int MAX_ALPHA = 255;

    FrameLayout dialogContent;
    protected LeapWebView leapWebView;
    AUIDialog dialog;
    RoundedCornerView roundedCornerWrapper;
    LeapIcon iconView;

    DialogAssist(Activity activity, String accessibilityText) {
        super(activity);
        init(activity, accessibilityText);
    }

    @Override
    public void init(Activity activity, String accessibilityText) {
        setupView(getContext());
        AppUtils.setContentDescription(roundedCornerWrapper, accessibilityText);
        hideIcon();
        dialog = new AUIDialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(dialogContent);
        setLeapWebView(leapWebView);
        dialog.show();
        hideContent();
        makeIconUnclickable();
    }

    @Override
    public void setupView(Context context) {
        dialogContent = new FrameLayout(context);
        dialogContent.setId(R.id.leap_dialog_root);
        dialogContent.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        roundedCornerWrapper = new RoundedCornerView(context);
        leapWebView = new LeapWebView(context);

        roundedCornerWrapper.addView(leapWebView);

        dialogContent.addView(roundedCornerWrapper);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) roundedCornerWrapper.getLayoutParams();
        layoutParams.gravity = Gravity.CENTER;
        roundedCornerWrapper.setLayoutParams(layoutParams);

        iconView = LeapIcon.getAssociatedLeapIcon(context);
        dialogContent.addView(iconView);
    }

    @Override
    void applyAlignment(String alignment) {
        int gravity = LayoutInfo.getAlignment(alignment);
        Window window = dialog.getWindow();
        if (window != null) window.setGravity(gravity);
    }

    @Override
    public void setIconSetting(IconSetting iconSetting) {
        super.setIconSetting(iconSetting);
        if (iconSetting == null) return;
        iconView.setProps(iconSetting.bgColor, iconSetting.htmlUrl, iconSetting.contentUrls);
    }

    protected void updateIconSize(int width, int height) {
        ViewGroup.LayoutParams layoutParams = iconView.getLayoutParams();
        layoutParams.width = width;
        layoutParams.height = height;
        iconView.setLayoutParams(layoutParams);
        iconView.setCornerRadius(width / 2);
    }

    private void makeIconUnclickable() {
        iconView.setOnClickListener(null);
    }

    private void hideIcon() {
        iconView.hide();
    }

    void showIcon() {
        iconView.show();
        iconView.bringToFront();
    }

    @Override
    public void remove() {
        hide(false);
    }

    @Override
    public void applyStyle(Style style) {
        if (style == null) return;
        super.applyStyle(style);

        // Set Elevation
        float elevation = getElevation(style, DEFAULT_ELEVATION);
        roundedCornerWrapper.setElevation(elevation);

        // Set Corner Radius
        float cornerRadius = getCornerRadius(style, DEFAULT_CORNER_RADIUS);
        roundedCornerWrapper.setCornerRadius(cornerRadius);

        // Set Bg Color
        dialogContent.setBackgroundColor(getBgColor(style));

        // Set Alpha
        dialogContent.getBackground().setAlpha((int) (MAX_ALPHA * style.alpha));
    }

    private int getBgColor(Style style) {
        try {
            return Color.parseColor(style.bgColor);
        } catch (Exception ignored) {
            return LeapAUIInternal.getInstance().getResources().getColor(R.color.leap_overlay_bg_extra_light);
        }
    }

    @Override
    public void setUpLayoutAction(DismissAction dismissAction) {
        if (dismissAction == null) return;
        if (dismissAction.outsideClick) dialogContent.setOnClickListener(this);
    }

    private void hideContent() {
        dialogContent.setVisibility(View.INVISIBLE);
        roundedCornerWrapper.setVisibility(View.INVISIBLE);
        leapWebView.setVisibility(View.INVISIBLE);
    }

    void showDialogOverlay() {
        dialogContent.setVisibility(View.VISIBLE);
    }

    void showContent() {
        roundedCornerWrapper.setVisibility(View.VISIBLE);
        leapWebView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void clearAnimation() {
        dialogContent.clearAnimation();
        roundedCornerWrapper.clearAnimation();
        leapWebView.clearAnimation();
    }

    @Override
    public void hide(boolean withAnim) {
        if (dialog == null || !dialog.isShowing()) return;
        if (withAnim) {
            if (assistAnimationListener != null)
                assistAnimationListener.onExitAnimationStarted();
            performExitAnimation(new AnimatorEndListener() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    dialog.dismiss();
                    if (assistAnimationListener != null)
                        assistAnimationListener.onExitAnimationFinished();
                }
            });
            return;
        }
        dialog.dismiss();
    }

    @Override
    public void show() {
        super.show();
        if (!isWebRendered) return;
        if (dialog == null || dialogContent.getVisibility() == View.VISIBLE) return;
        dialog.setStatusBarColor(getBgColor(getLayoutStyle()));
        showAnimation();
    }

    private void showAnimation() {
        if (assistAnimationListener != null)
            assistAnimationListener.onEntryAnimationStarted();
        performEnterAnimation(new AnimatorEndListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (assistAnimationListener != null)
                    assistAnimationListener.onEntryAnimationFinished();
                ViewGroup dialogAssistRootView = getDialogAssistRootView();
                if (dialogAssistRootView == null || assistDisplayListener == null) return;
                assistDisplayListener.onDialogAssistDetected(dialogAssistRootView);
            }
        });
    }

    public ViewGroup getDialogAssistRootView() {
        Window window = dialog.getWindow();
        if (window == null) return null;
        return (ViewGroup) window.getDecorView().getRootView();
    }

    @Override
    public void updateLayoutParams(Rect oldRect, Rect rect, String alignment) {
        // Do Nothing
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.leap_dialog_root) {
            super.hide();
            if (assistActionListener != null)
                assistActionListener.onAssistActionPerformed(OPT_OUT_CLICK, EventConstants.OUTSIDE_CLICK);
        }
    }

    void updateContentLayoutComplete() {
        if (!isIconEnabled()) return;
        roundedCornerWrapper.post(new Runnable() {
            @Override
            public void run() {
                Rect bounds = AppUtils.getBounds(roundedCornerWrapper);
                alignIcon(bounds);
            }
        });
    }

    @Override
    public boolean isNonAnchorAssist() {
        return true;
    }
}
