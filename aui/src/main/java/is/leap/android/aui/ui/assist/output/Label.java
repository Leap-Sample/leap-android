package is.leap.android.aui.ui.assist.output;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import is.leap.android.aui.ui.LeapCustomViewGroup;
import is.leap.android.aui.ui.assist.listener.AnimatorEndListener;
import is.leap.android.aui.ui.view.LeapWebView;
import is.leap.android.aui.ui.view.shapes.RoundedCornerView;
import is.leap.android.aui.util.AnimUtils;
import is.leap.android.aui.util.AppUtils;
import is.leap.android.core.Constants;
import is.leap.android.core.data.model.DismissAction;
import is.leap.android.core.data.model.Style;
import is.leap.android.core.util.StringUtils;

public class Label extends SameWindowWebContentAssist implements LeapCustomViewGroup {

    private static final int DEFAULT_CORNER_RADIUS = 4;
    private static final int DEF_LABEL_HEIGHT = 16;
    private static final String DEF_LABEL_BG = "#109A0D";
    private static final float DEF_LABEL_ALPHA = 1.0f;
    private RoundedCornerView labelView;
    private LeapWebView labelWebView;

    private int labelWidth;
    private int labelHeight;

    public Label(Activity activity, View rootView, String accessibilityText) {
        super(activity, rootView);
        init(activity, accessibilityText);
    }

    @Override
    public void init(Activity activity, String accessibilityText) {
        setupView(getContext());
        AppUtils.setContentDescription(labelView, accessibilityText);
        setLeapWebView(labelWebView);
        initLeapRootView();
        hide(false);
        addToRoot();
        initDefault();
    }

    @Override
    public View getAssistView() {
        return labelView;
    }

    @Override
    public void applyStyle(Style style) {
        super.applyStyle(style);
        if (style == null) return;

        labelView.setStroke(style.strokeWidth, Color.parseColor(style.strokeColor));

        // Update Corner radius
        float cornerRadius = getCornerRadius(style, DEFAULT_CORNER_RADIUS);
        labelView.setCornerRadius(cornerRadius);

        // Set Elevation
        float elevation = getElevation(style, 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            labelView.setElevation(elevation);
        }
    }

    @Override
    public void setUpLayoutAction(DismissAction action) {

    }

    @Override
    public void updateLayoutParams(Rect oldRect, Rect rect, String alignment) {
        super.updateLayoutParams(oldRect, rect, alignment);
        if (!isWebRendered) return;
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) labelView.getLayoutParams();
        layoutParams.width = labelWidth;
        layoutParams.height = labelHeight;
        if (alignment == null)
            alignment = Constants.Alignment.BOTTOM_CENTER;
        if (StringUtils.isNotNullAndNotEmpty(alignment)){
            switch (alignment) {
                case Constants.Alignment.CENTER:
                    layoutParams.topMargin = rect.centerY() - labelHeight / 2;
                    layoutParams.leftMargin = rect.centerX() - labelWidth / 2;
                    break;
                case Constants.Alignment.RIGHT_CENTER:
                    layoutParams.topMargin = rect.centerY() - labelHeight / 2;
                    layoutParams.leftMargin = rect.right - labelWidth / 2;
                    break;

                case Constants.Alignment.LEFT_CENTER:
                    layoutParams.topMargin = rect.centerY() - labelHeight / 2;
                    layoutParams.leftMargin = rect.left - labelWidth / 2;
                    break;

                case Constants.Alignment.BOTTOM_LEFT:
                    layoutParams.topMargin = rect.bottom - labelHeight / 2;
                    layoutParams.leftMargin = rect.left - labelWidth / 2;
                    break;
                case Constants.Alignment.BOTTOM_RIGHT:
                    layoutParams.topMargin = rect.bottom - labelHeight / 2;
                    layoutParams.leftMargin = rect.right - labelWidth / 2;
                    break;
                case Constants.Alignment.BOTTOM:
                case Constants.Alignment.BOTTOM_CENTER:
                    layoutParams.topMargin = rect.bottom - labelHeight / 2;
                    layoutParams.leftMargin = rect.centerX() - labelWidth / 2;
                    break;
                case Constants.Alignment.LEFT:
                case Constants.Alignment.TOP_LEFT:
                    layoutParams.topMargin = rect.top - labelHeight / 2;
                    layoutParams.leftMargin = rect.left - labelWidth / 2;
                    break;
                case Constants.Alignment.RIGHT:
                case Constants.Alignment.TOP_RIGHT:
                    layoutParams.topMargin = rect.top - labelHeight / 2;
                    layoutParams.leftMargin = rect.right - labelWidth / 2;
                    break;
                case Constants.Alignment.TOP:
                case Constants.Alignment.TOP_CENTER:
                    layoutParams.topMargin = rect.top - labelHeight / 2;
                    layoutParams.leftMargin = rect.centerX() - labelWidth / 2;
                    break;
            }
        }
        labelView.setLayoutParams(layoutParams);
    }

    @Override
    public void performEnterAnimation(final AnimatorEndListener animationEndListener) {
        showContent();
        labelView.setScaleX(0f);
        labelView.setScaleY(0f);
        ObjectAnimator scaleContainer = AnimUtils.getScaleAnimator(labelView,
                0f, 1f,
                0f, 1f,
                40, null, 0, new AnimatorEndListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        labelView.setScaleX(1f);
                        labelView.setScaleY(1f);
                        if (animationEndListener != null)
                            animationEndListener.onAnimationEnd(animation);
                    }
                }
        );
        scaleContainer.start();
    }

    @Override
    public void performExitAnimation(final AnimatorEndListener animationEndListener) {
        showContent();
        labelView.setScaleX(1f);
        labelView.setScaleY(1f);
        ObjectAnimator scaleContainer = AnimUtils.getScaleAnimator(labelView,
                1f, 0f,
                1f, 0f,
                40, null, 0, new AnimatorEndListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        labelView.setScaleX(0f);
                        labelView.setScaleY(0f);
                        if (animationEndListener != null)
                            animationEndListener.onAnimationEnd(animation);
                    }
                }
        );
        scaleContainer.start();
    }

    private void showContent() {
        labelView.setVisibility(View.VISIBLE);
        labelWebView.setVisibility(View.VISIBLE);
    }

    @Override
    void updateContentLayout(int pageWidth, int pageHeight) {
        labelWidth = pageWidth;
        labelHeight = pageHeight;
        labelWebView.updateLayout(labelWidth, labelHeight);

        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) labelView.getLayoutParams();
        if (layoutParams == null) {
            layoutParams = new FrameLayout.LayoutParams(labelWidth, labelHeight);
        } else {
            layoutParams.width = labelWidth;
            layoutParams.height = labelHeight;
        }
        labelView.setLayoutParams(layoutParams);
    }

    private void initDefault() {
        labelView.setAlpha(DEF_LABEL_ALPHA);
        labelView.setBackgroundColor(Color.parseColor(DEF_LABEL_BG));
        labelHeight = AppUtils.dpToPxInt(getContext(), DEF_LABEL_HEIGHT);
    }

    @Override
    public void setupView(Context context) {
        labelView = new RoundedCornerView(context);

        labelWebView = new LeapWebView(context);
        labelView.addView(labelWebView);

        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) labelWebView.getLayoutParams();
        layoutParams.gravity = Gravity.CENTER;
        labelWebView.setLayoutParams(layoutParams);
    }
}

