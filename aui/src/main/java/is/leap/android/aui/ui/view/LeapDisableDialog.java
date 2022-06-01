package is.leap.android.aui.ui.view;

import android.animation.Animator;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import is.leap.android.aui.LeapAUIInternal;
import is.leap.android.aui.R;
import is.leap.android.aui.data.LeapAUICache;
import is.leap.android.aui.ui.LeapCustomViewGroup;
import is.leap.android.aui.ui.LeapIcon;
import is.leap.android.aui.ui.assist.listener.AnimatorEndListener;
import is.leap.android.aui.ui.assist.view.DraggableLayout;
import is.leap.android.aui.util.AppUtils;
import is.leap.android.core.EventConstants;
import is.leap.android.core.data.model.IconSetting;

public class LeapDisableDialog extends Dialog implements DraggableLayout.SwipeActionListener,
        View.OnClickListener, LeapCustomViewGroup {

    public static final int DURATION_ANIM_DISMISS = 200;
    public static final int TO_ALPHA_VALUE_INVISIBLE = 0;
    private static final float TO_ALPHA_VALUE_VISIBLE = 0.75f;
    private static final int DURATION_FADE = 400;
    private static final int WRAPPER_WIDTH = 100;
    private static final int WRAPPER_HEIGHT = 40;
    private static final int TEXT_SIZE = 16;
    public static final int BORDER_WIDTH_DP = 2;
    public static final int MARGIN_START_DP = 20;
    private View fadeView;
    private LinearLayout frame;
    private final LeapDisableListener leapDisableListener;
    private FrameLayout rootView;

    public LeapDisableDialog(Context context, LeapDisableListener leapDisableListener) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);
        this.leapDisableListener = leapDisableListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        rootView = (FrameLayout) LeapAUIInternal.getInstance().inflate(R.layout.leap_disable_dialog);
        Context context = rootView.getContext();
        setupView(context);
        setContentView(rootView);
        fadeView = findViewById(R.id.fade_view);
        frame = findViewById(R.id.frame);

        IconSetting iconSettings = LeapAUICache.iconSettings;
        if (iconSettings == null) return;
        LeapIcon leapIcon = LeapIcon.getIndependentLeapIcon(context);
        frame.addView(leapIcon, 0);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) leapIcon.getLayoutParams();
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.topMargin = AppUtils.dpToPxInt(context, 28);
        leapIcon.setLayoutParams(layoutParams);

        leapIcon.setProps(iconSettings.bgColor, iconSettings.htmlUrl, iconSettings.contentUrls);
        findViewById(R.id.leap_txt_yes_wrapper).setOnClickListener(this);
        findViewById(R.id.leap_txt_no_wrapper).setOnClickListener(this);

        DraggableLayout draggableLayout = findViewById(R.id.draggableLayout);
        DraggableLayout.Params params = new DraggableLayout.Params();
        params.swipeDirection = DraggableLayout.Params.SWIPE_DIRECTION_DOWN;
        draggableLayout.setParams(params);
        draggableLayout.setSwipeActionListener(this);

        setWindowAttr();
        disableOutsideTouch();
        fade();
        slideUp();
    }

    @Override
    public void show() {
        super.show();
        leapDisableListener.onLeapDisableDialogEvent(EventConstants.DISABLE_PANEL_VISIBLE, true);
    }

    private void disableOutsideTouch() {
        setCanceledOnTouchOutside(false);
    }

    private void setWindowAttr() {
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        getWindow().setGravity(Gravity.BOTTOM);
    }

    private void slideUp() {
        Animation slideUp = AnimationUtils.loadAnimation(getContext(), R.anim.leap_slide_up);
        frame.startAnimation(slideUp);
    }

    private void fade() {
        if (fadeView.getAlpha() != TO_ALPHA_VALUE_VISIBLE) {
            fadeView.animate()
                    .alpha(TO_ALPHA_VALUE_VISIBLE)
                    .setDuration(DURATION_FADE)
                    .start();
        }
    }

    @Override
    public void dismiss() {
        // Slide down the dialog and fadeout the background
        fadeView.animate().alpha(TO_ALPHA_VALUE_INVISIBLE).setDuration(DURATION_ANIM_DISMISS).start();
        frame.animate()
                .translationY(frame.getHeight())
                .setDuration(DURATION_ANIM_DISMISS)
                .alpha(TO_ALPHA_VALUE_INVISIBLE)
                .setInterpolator(new AccelerateInterpolator())
                .setListener(new AnimatorEndListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        LeapDisableDialog.super.dismiss();
                        leapDisableListener.onLeapDisableDialogEvent(EventConstants.DISABLE_PANEL_VISIBLE, false);
                    }
                })
                .start();
    }

    @Override
    public void onSwipeComplete(int alignment) {
        if (alignment == DraggableLayout.Params.SWIPE_DIRECTION_DOWN) dismiss();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.leap_txt_yes_wrapper) {
            leapDisableListener.onLeapDisableDialogEvent(EventConstants.DISABLE_PANEL_CLICK_EVENT, true);
        } else if (id == R.id.leap_txt_no_wrapper) {
            leapDisableListener.onLeapDisableDialogEvent(EventConstants.DISABLE_PANEL_CLICK_EVENT, false);
        }
        dismiss();
    }

    /**
     * Required when we don't want to emit {@value EventConstants#DISABLE_PANEL_VISIBLE} as false, while closing
     * The re-start for context detection happens when the dialog is dismissed, the below is helpful to avoid those scenario
     */
    public void hideDialog() {
        super.dismiss();
    }

    @Override
    public void setupView(Context context) {

        LinearLayout yesNoLayout = rootView.findViewById(R.id.yes_no_layout);

        Context leapContext = LeapAUIInternal.getInstance().getContext();
        Resources resources = LeapAUIInternal.getInstance().getResources();
        int bgColor = resources.getColor(android.R.color.transparent);
        int borderColor = resources.getColor(R.color.leap_disable_assistant_btn_border);
        int borderWidth = AppUtils.dpToPxInt(context, BORDER_WIDTH_DP);
        LeapBgShapeView txtNoWrapper = new LeapBgShapeView(leapContext, WRAPPER_WIDTH, WRAPPER_HEIGHT,
                LeapShapeDrawable.CAPSULE, bgColor, borderColor, borderWidth);

        // Add noTextView to txtNoWrapper
        TextView noTextView = getTextView(context, resources, R.id.leap_txt_no_wrapper, R.string.no);
        txtNoWrapper.addView(noTextView);

        // Add txtNoWrapper to yesNoLayout
        yesNoLayout.addView(txtNoWrapper);

        LeapBgShapeView txtYesWrapper = new LeapBgShapeView(leapContext, WRAPPER_WIDTH, WRAPPER_HEIGHT,
                LeapShapeDrawable.CAPSULE, bgColor, borderColor, borderWidth);

        // Add yesTextView to txtYesWrapper
        TextView yesTextView = getTextView(context, resources, R.id.leap_txt_yes_wrapper, R.string.yes);
        txtYesWrapper.addView(yesTextView);

        // Add txtYesWrapper to yesNoLayout
        yesNoLayout.addView(txtYesWrapper);

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) txtYesWrapper.getLayoutParams();
        layoutParams.setMarginStart(AppUtils.dpToPxInt(context, MARGIN_START_DP));
        txtYesWrapper.setLayoutParams(layoutParams);

    }

    private TextView getTextView(Context context, Resources resources, int viewId, int stringResId) {
        TextView textView = new TextView(context);
        textView.setId(viewId);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);

        layoutParams.gravity = Gravity.CENTER;
        textView.setLayoutParams(layoutParams);
        textView.setText(resources.getText(stringResId));
        textView.setTextColor(resources.getColor(R.color.leap_393939));
        textView.setTextSize(TEXT_SIZE);
        return textView;
    }

    public interface LeapDisableListener {
        void onLeapDisableDialogEvent(String action, boolean value);
    }
}