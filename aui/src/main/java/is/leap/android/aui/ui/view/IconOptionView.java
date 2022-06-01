package is.leap.android.aui.ui.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import is.leap.android.aui.AUIConstants;
import is.leap.android.aui.LeapAUIInternal;
import is.leap.android.aui.R;
import is.leap.android.aui.data.LeapAUICache;
import is.leap.android.aui.ui.LeapCustomViewGroup;
import is.leap.android.aui.ui.assist.listener.AnimationEndListener;
import is.leap.android.aui.ui.assist.listener.AnimatorEndListener;
import is.leap.android.aui.util.AnimUtils;
import is.leap.android.aui.util.AppUtils;

import static is.leap.android.aui.AUIConstants.DEFAULT_MARGIN_14;
import static is.leap.android.aui.util.AppUtils.getImageView;

public class IconOptionView extends FrameLayout implements View.OnClickListener, LeapCustomViewGroup {

    public static final int CORNER_RADIUS_IN_DP = 26;
    public static final int ICON_WIDTH_IN_DP = 52;
    public static final int ANIMATION_DURATION = 100;
    public static final int OPTIONS_TRANSLATION_VALUE = 10;
    public static final float CROSS_ZOOM_IN_FROM_VALUE = 0.571f;
    public static final float CROSS_ZOOM_OUT_TO_VALUE = 0.571f;
    public static final int LEAP_ICON_CROSS_SIZE = 42;
    public static final int IMG_CROSS_SIZE = DEFAULT_MARGIN_14;
    public static final int IC_STOP_SIZE = DEFAULT_MARGIN_14;
    public static final int MARGIN_START = 14;
    public static float ICON_OPTIONS_CORNER_RADIUS;
    public static final int OPTIONS_CONTAINER_RIGHT_PADDING = 18;
    public static final int OPTIONS_CONTAINER_LEFT_PADDING = 24;
    public static final int ICON_OPTIONS_STOP_MARGIN = 14;
    private TextView txtStop;
    private TextView txtLanguage;
    private OptionActionListener optionActionListener;
    private LinearLayout iconOptionView;
    private LinearLayout iconOptionContainer;
    private LeapBgShapeView icLeapCross;
    private LeapBgShapeView icStop;
    private boolean leftAlign;
    private View iconOptionWrapper;
    private View icLanguage;
    private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private RectF rect;
    private float iconWidth;
    private float optionsTranslateXValue;
    private LinearLayout stopLayout;
    private LinearLayout languageLayout;

    public IconOptionView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        setWillNotDraw(false);
        iconOptionView = (LinearLayout) LeapAUIInternal.getInstance().inflate(R.layout.leap_icon_option_layout);
        stopLayout = iconOptionView.findViewById(R.id.stop_layout);
        setupView(context);
        addView(iconOptionView);
        iconOptionContainer = iconOptionView.findViewById(R.id.icon_option_container);
        iconOptionWrapper = iconOptionView.findViewById(R.id.option_wrapper);
        txtStop = iconOptionView.findViewById(R.id.txt_stop);

        stopLayout.setOnClickListener(this);
        icLanguage = iconOptionView.findViewById(R.id.ic_language);
        txtLanguage = iconOptionView.findViewById(R.id.txt_language);
        languageLayout = iconOptionView.findViewById(R.id.language_layout);
        languageLayout.setOnClickListener(this);
        ICON_OPTIONS_CORNER_RADIUS = AppUtils.dpToPx(getContext(), CORNER_RADIUS_IN_DP);
        float width = 2 * ICON_OPTIONS_CORNER_RADIUS;
        rect = new RectF(0, 0, width, width);
        iconWidth = AppUtils.dpToPx(getContext(), ICON_WIDTH_IN_DP);
        optionsTranslateXValue = AppUtils.dpToPx(getContext(), OPTIONS_TRANSLATION_VALUE);

    }

    @Override
    public void setupView(Context context) {

        Resources resources = LeapAUIInternal.getInstance().getResources();
        icLeapCross = new LeapBgShapeView(context, LEAP_ICON_CROSS_SIZE, LEAP_ICON_CROSS_SIZE,
                LeapShapeDrawable.CIRCLE, resources.getColor(R.color.leap_33_transparent));
        icLeapCross.setId(R.id.ic_leap_cross);
        iconOptionView.addView(icLeapCross, 0);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) icLeapCross.getLayoutParams();

        int margin = AppUtils.dpToPxInt(context, 5);
        layoutParams.setMargins(margin, margin, margin, margin);
        icLeapCross.setLayoutParams(layoutParams);
        icLeapCross.setOnClickListener(this);

        Context leapContext = LeapAUIInternal.getInstance().getContext();
        ImageView imgCross = getImageView(leapContext, R.drawable.ic_leap_cross);
        icLeapCross.addView(imgCross);
        LayoutParams imgCrossLayoutParams = (LayoutParams) imgCross.getLayoutParams();
        imgCrossLayoutParams.width = imgCrossLayoutParams.height = AppUtils.dpToPxInt(context, IMG_CROSS_SIZE);
        imgCrossLayoutParams.gravity = Gravity.CENTER;
        imgCross.setLayoutParams(imgCrossLayoutParams);

        int bgColor = resources.getColor(R.color.leap_33_opaque);
        int borderColor = resources.getColor(android.R.color.white);
        int borderWidth = AppUtils.dpToPxInt(context, 2);
        icStop = new LeapBgShapeView(context, IC_STOP_SIZE, IC_STOP_SIZE, LeapShapeDrawable.CIRCLE,
                bgColor, borderColor, borderWidth);
        stopLayout.addView(icStop, 0);
        LinearLayout.LayoutParams icStopLP = (LinearLayout.LayoutParams) icStop.getLayoutParams();
        icStopLP.gravity = Gravity.CENTER_VERTICAL;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            icStopLP.setMarginStart(AppUtils.dpToPxInt(context, MARGIN_START));
        }
        icStop.setLayoutParams(icStopLP);
    }

    public void setOptionActionListener(OptionActionListener optionActionListener) {
        this.optionActionListener = optionActionListener;
    }

    public void setStopText(String stopText) {
        txtStop.setText(stopText);
    }

    public void setLanguageText(String languageText) {
        txtLanguage.setText(languageText);
    }

    public void showLanguage() {
        txtLanguage.setVisibility(VISIBLE);
        icLanguage.setVisibility(VISIBLE);
    }

    public void hideLanguage() {
        txtLanguage.setVisibility(GONE);
        icLanguage.setVisibility(GONE);
    }

    @Override
    public void onClick(final View v) {

        animateCollapsing(new AnimationEndListener() {

            @Override
            public void onAnimationEnd(Animation animation) {
                int id = v.getId();
                if (id == R.id.ic_leap_cross) {
                    optionActionListener.onOptionCrossClicked();
                } else if (id == R.id.stop_layout) {
                    optionActionListener.onOptionStopClicked();
                } else if (id == R.id.language_layout) {
                    optionActionListener.onOptionLanguageClicked();
                }
            }

            @Override
            public void onAnimationStart(Animation ignored) {
                optionActionListener.onOptionDismissAnimationStarted();
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawRoundRect(rect, ICON_OPTIONS_CORNER_RADIUS, ICON_OPTIONS_CORNER_RADIUS, bgPaint);
    }

    private void animateCollapsing(final AnimationEndListener animatorEndListener) {
        ObjectAnimator crossZoomOut = AnimUtils.getScaleAnimator(icLeapCross, 1f, CROSS_ZOOM_OUT_TO_VALUE,
                1f, CROSS_ZOOM_OUT_TO_VALUE, ANIMATION_DURATION, new AccelerateDecelerateInterpolator(),
                0, null);

        ObjectAnimator crossAlphaAnimator = AnimUtils.getAlphaAnimator(icLeapCross, 1, 0, ANIMATION_DURATION,
                new AccelerateDecelerateInterpolator(), 0, null);
        float[] transLateXValues = leftAlign
                ? new float[]{0, -optionsTranslateXValue}
                : new float[]{0, optionsTranslateXValue};
        ObjectAnimator optionWrapperTranslationX = AnimUtils.getTranslationXAnimator(iconOptionWrapper, ANIMATION_DURATION,
                new AccelerateDecelerateInterpolator(), 0,
                null, transLateXValues);

        ObjectAnimator optionWrapperAlphaAnimator = AnimUtils.getAlphaAnimator(iconOptionWrapper, 1, 0, ANIMATION_DURATION,
                new AccelerateDecelerateInterpolator(), 0, null);

        final AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(crossZoomOut, crossAlphaAnimator,
                optionWrapperTranslationX, optionWrapperAlphaAnimator);
        animatorSet.addListener(new AnimatorEndListener() {
            @Override
            public void onAnimationEnd(Animator animation) {

                animatorEndListener.onAnimationStart(null);

                final int width = iconOptionView.getWidth();
                float endX = iconWidth / width;

                scale(1, endX, width, new AnimatorEndListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        animatorEndListener.onAnimationEnd(null);
                        setVisibility(GONE);
                    }
                });
            }
        });
        animatorSet.start();
    }

    private void scale(float startX, float endX, final int width, final AnimatorEndListener animatorEndListener) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(startX, endX);
        valueAnimator.setDuration(ANIMATION_DURATION);
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                if (leftAlign) {
                    rect.left = 0;
                    rect.right = value * width;
                } else {
                    rect.left = width - (value * width);
                    rect.right = width;
                }
                invalidate();
            }
        });
        valueAnimator.addListener(new AnimatorEndListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                animatorEndListener.onAnimationEnd(animation);
            }
        });
        valueAnimator.start();
    }

    public void show() {
        setVisibility(VISIBLE);
    }

    public void showWithAnim() {
        icLeapCross.setVisibility(INVISIBLE);
        iconOptionWrapper.setVisibility(INVISIBLE);
        show();
        final int width = iconOptionView.getWidth();
        float startX = iconWidth / width;

        scale(startX, 1, width, new AnimatorEndListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                icLeapCross.setVisibility(VISIBLE);
                iconOptionWrapper.setVisibility(VISIBLE);
                ObjectAnimator crossZoomIn = AnimUtils.getScaleAnimator(icLeapCross, CROSS_ZOOM_IN_FROM_VALUE, 1f,
                        CROSS_ZOOM_IN_FROM_VALUE, 1f, ANIMATION_DURATION, new AccelerateDecelerateInterpolator(),
                        0, null);

                ObjectAnimator crossAlphaAnimator = AnimUtils.getAlphaAnimator(icLeapCross, 0, 1, ANIMATION_DURATION,
                        new AccelerateDecelerateInterpolator(), 0, null);

                float[] transLateXValues = leftAlign
                        ? new float[]{-optionsTranslateXValue, 0}
                        : new float[]{optionsTranslateXValue, 0};
                ObjectAnimator optionWrapperTranslationX = AnimUtils.getTranslationXAnimator(iconOptionWrapper, ANIMATION_DURATION,
                        new AccelerateDecelerateInterpolator(), 0,
                        null, transLateXValues);

                ObjectAnimator optionWrapperAlphaAnimator = AnimUtils.getAlphaAnimator(iconOptionWrapper, 0, 1, ANIMATION_DURATION,
                        new AccelerateDecelerateInterpolator(), 0, null);

                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.playTogether(crossZoomIn, crossAlphaAnimator,
                        optionWrapperTranslationX, optionWrapperAlphaAnimator);
                animatorSet.start();
            }
        });
    }

    public void hide() {
        setVisibility(INVISIBLE);
    }

    public void setBgColor(String bgColor) {
        bgPaint.setColor(Color.parseColor(bgColor));
        invalidate();
    }

    public void setAlignment(boolean leftAlign) {
        this.leftAlign = leftAlign;
        int crossIndex = iconOptionContainer.indexOfChild(icLeapCross);
        LinearLayout.LayoutParams icStoplP = (LinearLayout.LayoutParams) icStop.getLayoutParams();
        if (leftAlign) {
            icStoplP.leftMargin = AppUtils.dpToPxInt(getContext(), ICON_OPTIONS_STOP_MARGIN);
            int rightPadding = AppUtils.dpToPxInt(getContext(), OPTIONS_CONTAINER_RIGHT_PADDING);
            iconOptionContainer.setPadding(0, 0, rightPadding, 0);
            if (crossIndex != 0) {
                iconOptionContainer.removeViewAt(crossIndex);
                iconOptionContainer.addView(icLeapCross, 0);
            }
        } else {
            icStoplP.leftMargin = 0;
            int leftPadding = AppUtils.dpToPxInt(getContext(), OPTIONS_CONTAINER_LEFT_PADDING);
            iconOptionContainer.setPadding(leftPadding, 0, 0, 0);
            int childCount = iconOptionContainer.getChildCount();
            int lastChildIndex = childCount - 1;
            if (lastChildIndex != crossIndex) {
                iconOptionContainer.removeViewAt(crossIndex);
                iconOptionContainer.addView(icLeapCross);
            }
        }
        icStop.setLayoutParams(icStoplP);
    }

    public void setContentDescription() {
        AppUtils.setContentDescription(stopLayout,
                LeapAUICache.getAccessibilityText(AUIConstants.AccessibilityText.STOP));
        AppUtils.setContentDescription(languageLayout,
                LeapAUICache.getAccessibilityText(AUIConstants.LANGUAGE));
        AppUtils.setContentDescription(icLeapCross,
                LeapAUICache.getAccessibilityText(AUIConstants.AccessibilityText.CLOSE_ICON_OPTIONS));
    }

    public interface OptionActionListener {

        void onOptionDismissAnimationStarted();

        void onOptionCrossClicked();

        void onOptionStopClicked();

        void onOptionLanguageClicked();
    }


}
