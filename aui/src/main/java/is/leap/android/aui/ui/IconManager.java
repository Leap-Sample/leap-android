package is.leap.android.aui.ui;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;

import java.lang.ref.WeakReference;

import is.leap.android.aui.LeapAUIInternal;
import is.leap.android.aui.LeapAUILogger;
import is.leap.android.aui.R;
import is.leap.android.aui.data.LeapAUICache;
import is.leap.android.aui.ui.assist.listener.AnimatorEndListener;
import is.leap.android.aui.ui.assist.view.TrashState;
import is.leap.android.aui.ui.assist.view.TrashView;
import is.leap.android.aui.ui.listener.IUIChangeHolder;
import is.leap.android.aui.ui.view.IconOptionView;
import is.leap.android.aui.util.AnimUtils;
import is.leap.android.aui.util.AppUtils;
import is.leap.android.core.AppExecutors;
import is.leap.android.core.data.LeapCoreCache;
import is.leap.android.core.data.model.IconSetting;
import is.leap.android.core.data.model.LeapLanguage;

import static is.leap.android.aui.AUIConstants.DEFAULT_MARGIN_20;
import static is.leap.android.aui.util.AnimUtils.executeAnimations;
import static is.leap.android.core.Constants.Visual.VISUAL_TYPE_PING;

public class IconManager implements IconOptionView.OptionActionListener, LeapCustomViewGroup {

    private static final String LEAP_BUTTON = "leap_icon";
    public static final float ICON_SCALE_X = 0.6923f;
    public static final float ICON_SCALE_Y = 0.6923f;
    public static final int ICON_SCALE_DURATION = 80;
    public static final int ICON_FADE_DURATION = 80;
    public static final int ICON_ANIM_DURATION = 80;
    public static final int ANIMATION_DURATION = 100;
    static final int REST = 1;
    static final int PROGRESS = 2;
    static final int AUDIO = 3;
    public static final float SCALE_VALUE = 0.667f;
    public static final int SCALE_FROM_VALUE = 1;
    public static final int TRASH_LAYOUT_HEIGHT = 200;
    private IconOptionView iconOptionView;
    private final FrameLayout leapIconWrapper;

    private LeapIcon leapIcon;
    private TrashView trashView;
    private WeakReference<View> rootViewRef;
    private final FrameLayout iconRootView;
    IconState iconState = IconState.NORMAL;

    private View getRootView() {
        return AppUtils.getView(rootViewRef);
    }

    private final IconDragTouch.DragListener dragListener = new IconDragTouch.DragListener() {
        @Override
        public void onDragged() {
            setIconState(IconState.DRAGGED);
        }

        @Override
        public boolean isTrashAndLeapIconIntersecting() {
            return Rect.intersects(trashView.getTrashIconBound(), getAbsoluteBounds(leapIcon));
        }

        @Override
        public float getDefaultIconXValue() {
            return leapIconWrapper.getX() - leapIconWrapper.getTranslationX();
        }

        @Override
        public float getDefaultIconYValue() {
            return leapIconWrapper.getY() - leapIconWrapper.getTranslationY();
        }

        @Override
        public void onTrash(int trashState) {
            switch (trashState) {
                case TrashState.NORMAL:
                    resetViewSize();
                    trashView.show();
                    break;
                case TrashState.NONE:
                    hideTrash();
                    break;
                case TrashState.TRASH_ICON_INTERSECTED:
                    trashView.show();
                    // expand trash icon
                    trashView.scale();
                    // shrink discovery icon
                    shrink(leapIcon);
                    break;
                case TrashState.TRASH_COLLECTED:
                    // Show "Are you sure" dialog.
                    hideTrash();
                    iconActionListener.onIconTrashCollected();
                    break;

            }
        }

        private void hideTrash() {
            resetViewSize();
            trashView.hide();
        }

        private void resetViewSize() {
            trashView.scaleToNormal();
            scaleToNormal(leapIcon);
        }
    };
    private int iconBottomMargin;
    private boolean isOptionShowing;

    public void scaleToNormal(View view) {
        scale(view, 1);
    }

    private void scale(View view, float scaleFactor) {
        view.setScaleX(scaleFactor);
        view.setScaleY(scaleFactor);
    }

    public void shrink(View view) {
        scale(view, 0.7f);
    }

    private IconDragTouch iconDragTouch;
    private View.OnClickListener leapIconClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            iconActionListener.onIconClicked();
        }
    };
    private final IconActionListener iconActionListener;
    private IUIChangeHolder uiChangeHolder;
    private Context context;
    private boolean shouldEnableIcon;

    IconManager(Context context, IconActionListener iconActionListener, AppExecutors appExecutors,
                IUIChangeHolder uiChangeHolder) {
        this.context = context;
        this.iconActionListener = iconActionListener;
        this.uiChangeHolder = uiChangeHolder;
        iconRootView = (FrameLayout) LeapAUIInternal.getInstance().inflate(R.layout.leap_icon_layout);
        iconRootView.setTag(LEAP_BUTTON);
        leapIconWrapper = iconRootView.findViewById(R.id.leap_icon_wrapper);

        setupView(context);

        iconOptionView.setOptionActionListener(this);

        // Delegate OnTouchListener to Leap Icon wrapper View so that the wrapper is dragged
        leapIcon.setDelegateOnTouchListenerView(leapIconWrapper);
        iconDragTouch = new IconDragTouch(dragListener, appExecutors, leapIcon.getIconSize());
        // Since the LeapIcon drag is delegated to its wrapper, make sure to
        // delegate the click from its wrapper back to LeapIcon.
        iconDragTouch.setDelegateOnClickListenerView(leapIcon);
        leapIcon.setOnClickListener(leapIconClickListener);
        leapIcon.setOnTouchListener(iconDragTouch);
        leapIcon.setTouchListener(iconDragTouch);
    }

    @Override
    public void setupView(Context context) {
        iconOptionView = new IconOptionView(context);
        iconOptionView.hide();
        leapIconWrapper.addView(iconOptionView);

        leapIcon = LeapIcon.getIndependentLeapIcon(context);
        leapIcon.hide();
        leapIconWrapper.addView(leapIcon);

        trashView = new TrashView(context);
        iconRootView.addView(trashView);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) trashView.getLayoutParams();
        layoutParams.height = AppUtils.dpToPxInt(context, TRASH_LAYOUT_HEIGHT);
        layoutParams.gravity = Gravity.BOTTOM;
        trashView.setVisibility(View.INVISIBLE);
        trashView.setLayoutParams(layoutParams);
    }

    public void setIconSetting(IconSetting iconSetting) {
        if (iconSetting == null) return;
        iconBottomMargin = AppUtils.dpToPxInt(context, iconSetting.iconBottomMargin);
        align(iconSetting.leftAlign);
        iconDragTouch.setIsLeftAligned(iconSetting.leftAlign);
        iconDragTouch.setDismissible(iconSetting.dismissible);
        leapIcon.addElevation();
        leapIcon.setProps(iconSetting.bgColor, iconSetting.htmlUrl, iconSetting.contentUrls);
        leapIcon.loadAudioContent();
        iconOptionView.setBgColor(iconSetting.bgColor);
        iconOptionView.setContentDescription();
        setState(REST);

        setIconOptionText(LeapCoreCache.audioLocale);
        if (iconSetting.isShowLanguageOption())
            showLanguage();
        else
            hideLanguage();
    }

    public void loadIconContent() {
        leapIcon.loadIconContent();
    }

    void setState(final int state) {
        LeapAUILogger.debugAUI(":Icon state: " + state);
        switch (state) {
            case AUDIO:
                leapIcon.hideProgress();
                leapIcon.hideIconView();
                leapIcon.showAudioView();
                break;
            case PROGRESS:
                leapIcon.hideAudioView();
                leapIcon.showIconView();
                leapIcon.showProgress();
                break;
            case REST:
            default:
                leapIcon.hideAudioView();
                leapIcon.hideProgress();
                leapIcon.showIconView();
        }
    }

    public void showIconOptions() {
        isOptionShowing = true;
        iconOptionView.showWithAnim();
        ObjectAnimator scaleAnimator = AnimUtils.getScaleAnimator(leapIcon, SCALE_FROM_VALUE, SCALE_VALUE,
                SCALE_FROM_VALUE, SCALE_VALUE, ANIMATION_DURATION,
                new AccelerateDecelerateInterpolator(), 0, null);
        ObjectAnimator alphaAnimator = AnimUtils.getAlphaAnimator(leapIcon, 1, 0,
                ANIMATION_DURATION, new AccelerateDecelerateInterpolator(),
                0, new AnimatorEndListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        leapIcon.hide();
                    }
                });
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleAnimator, alphaAnimator);
        animatorSet.start();
    }

    private Rect getAbsoluteBounds(View view) {
        Rect rect = new Rect();
        view.getGlobalVisibleRect(rect);
        return rect;
    }

    private void updateNewBounds(int width, int height) {
        iconDragTouch.setUpdatedWidth(width);
        iconDragTouch.setUpdatedHeight(height);
    }

    private void align(final boolean leftAlign) {
        iconOptionView.setAlignment(leftAlign);
        final FrameLayout.LayoutParams leapIconlP = (FrameLayout.LayoutParams) leapIcon.getLayoutParams();
        final FrameLayout.LayoutParams leapIconWrapperlP = (FrameLayout.LayoutParams) leapIconWrapper.getLayoutParams();
        if (leftAlign) {
            leapIconWrapperlP.leftMargin = AppUtils.dpToPxInt(context, DEFAULT_MARGIN_20);
            leapIconlP.gravity = leapIconWrapperlP.gravity = Gravity.START | Gravity.BOTTOM;
        } else {
            leapIconWrapperlP.rightMargin = AppUtils.dpToPxInt(context, DEFAULT_MARGIN_20);
            leapIconlP.gravity = leapIconWrapperlP.gravity = Gravity.END | Gravity.BOTTOM;
        }
        Activity currentActivity = uiChangeHolder.getCurrentActivity();
        int insetBottom = AppUtils.getInsetBottom(currentActivity);
        leapIconWrapperlP.bottomMargin = iconBottomMargin + insetBottom;
        leapIconWrapper.setLayoutParams(leapIconWrapperlP);
        leapIcon.setLayoutParams(leapIconlP);
    }

    void setRootView(View rootView) {
        if (getRootView() != rootView) removeView();
        if (isNotValidRoot(rootView)) return;
        this.rootViewRef = new WeakReference<>(rootView);
        if (this.iconRootView.getParent() == rootView) return;
        addView();
        updateLayoutParams();
        hide();
    }

    private boolean isNotValidRoot(View rootView) {
        WindowManager.LayoutParams layoutParams;
        try {
            layoutParams = (WindowManager.LayoutParams) rootView.getLayoutParams();
        } catch (Exception ignored) {
            return false;
        }

        if (layoutParams != null && AppUtils.isDialogType(layoutParams.type)) {
            return !AppUtils.isLeapDialog(rootView);
        }
        return false;
    }


    private void updateLayoutParams() {
        Activity currentActivity = uiChangeHolder.getCurrentActivity();
        if (currentActivity == null) return;
        if (!AppUtils.isKeyboardOpen(currentActivity)) {
            updateLayout(false);
            return;
        }
        updateLayout(true);
    }

    public void updateLayout(boolean keyboardOpen) {
        View rootView = getRootView();
        if (rootView == null) return;
        Activity currentActivity = uiChangeHolder.getCurrentActivity();
        if (currentActivity == null) return;
        AppUtils.updateRootLayout(currentActivity, keyboardOpen, rootView, iconRootView, leapIcon);
        Rect rootViewBounds = AppUtils.getRootViewBounds(currentActivity, keyboardOpen, rootView);
        int width = rootViewBounds.width();
        int height = rootViewBounds.height();
        updateNewBounds(width, height);
    }

    public void removeView() {
        ViewParent parent = iconRootView.getParent();
        if (parent == null) return;
        if (parent instanceof ViewGroup) {
            ViewGroup iconRootParent = (ViewGroup) parent;
            iconRootParent.removeView(iconRootView);
        }
    }

    /**
     * This sets the icon state from particular discovery
     * Every discovery will need the {@link IconState}
     *
     * @param iconState is the current icon state
     */
    public void setIconState(IconState iconState) {
        this.iconState = iconState;
    }

    private void addView() {
        View rootView = getRootView();
        if (rootView == null) return;
        ((ViewGroup) rootView).addView(iconRootView);
    }

    public void animateIcon(String assistType) {
        if (!shouldEnableIcon) return;
        show();
        if (VISUAL_TYPE_PING.equals(assistType)) {
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) leapIconWrapper.getLayoutParams();
            Activity currentActivity = uiChangeHolder.getCurrentActivity();
            int insetBottom = AppUtils.getInsetBottom(currentActivity);
            layoutParams.bottomMargin = iconBottomMargin + insetBottom;
            leapIconWrapper.setLayoutParams(layoutParams);
            resetIconPosToDefault();
            return;
        }
        // Scale from 35dp to 52dp
        // Ratio is 36 / 52 = 0.6923
        leapIcon.setScaleX(ICON_SCALE_X);
        leapIcon.setScaleY(ICON_SCALE_Y);
        ObjectAnimator scaleIcon = AnimUtils.getScaleAnimator(leapIcon,
                ICON_SCALE_X, 1f,
                ICON_SCALE_Y, 1f,
                ICON_SCALE_DURATION, null, 0, null);

        leapIcon.setAlpha(0);
        ObjectAnimator iconAlphaAnimator = AnimUtils.getAlphaAnimator(leapIcon,
                0, 1, ICON_FADE_DURATION, null, 0, null);

        Animator[] togetherAnimators = {scaleIcon, iconAlphaAnimator};
        executeAnimations(ICON_ANIM_DURATION, togetherAnimators, null,
                new AnimatorEndListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        leapIcon.setScaleX(1f);
                        leapIcon.setScaleY(1f);
                        leapIcon.setAlpha(1f);
                    }
                });
    }

    void hide() {
        LeapAUILogger.debugAUI("Icon hide called");
        if (iconRootView.getVisibility() != View.VISIBLE) return;
        iconRootView.setVisibility(View.GONE);
        leapIcon.hide();
        iconOptionView.hide();
    }

    void show() {
        if (!shouldEnableIcon) return;
        LeapAUILogger.debugAUI("Icon show called");
        if (iconRootView.getVisibility() == View.VISIBLE) return;
        iconRootView.setVisibility(View.VISIBLE);
        if (isOptionShowing) {
            iconOptionView.show();
            leapIcon.hide();
        } else {
            leapIcon.show();
            iconOptionView.hide();
        }
    }

    void enableIcon(boolean shouldEnableIcon) {
        this.shouldEnableIcon = shouldEnableIcon;
    }

    void bringIconToFront(float elevation) {
        iconRootView.bringToFront();
        iconRootView.setElevation(elevation);
    }

    void onKeyboardToggled(boolean keyboardDetected) {
        updateLayout(keyboardDetected);
    }

    void resetIconPosToDefault() {
        iconDragTouch.resetToDefaultPos(leapIconWrapper);
    }

    public void onActivityPause() {
        if (isOptionShowing)
            collapseOptionView();
        removeView();
    }

    private void collapseOptionView() {
        leapIcon.setScaleX(1);
        leapIcon.setScaleY(1);
        leapIcon.setAlpha(1);
        leapIcon.show();
        iconOptionView.hide();
    }

    @Override
    public void onOptionDismissAnimationStarted() {
        leapIcon.show();
        ObjectAnimator scaleAnimator = AnimUtils.getScaleAnimator(leapIcon, SCALE_VALUE, 1f,
                SCALE_VALUE, 1f, ANIMATION_DURATION,
                new AccelerateDecelerateInterpolator(), 0, null);
        ObjectAnimator alphaAnimator = AnimUtils.getAlphaAnimator(leapIcon, 0, 1,
                100, new AccelerateDecelerateInterpolator(),
                0, null);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleAnimator, alphaAnimator);
        animatorSet.start();
    }

    @Override
    public void onOptionCrossClicked() {
        isOptionShowing = false;
        leapIcon.show();
        iconOptionView.hide();
    }

    @Override
    public void onOptionStopClicked() {
        isOptionShowing = false;
        iconActionListener.onStopClicked();
        leapIcon.show();
        iconOptionView.hide();
    }

    @Override
    public void onOptionLanguageClicked() {
        isOptionShowing = false;
        iconActionListener.onShowLanguageOptionClicked();
    }

    public void setIconOptionText(String locale) {
        final LeapLanguage language = LeapAUICache.getLanguage(locale);
        if (language == null) return;
        iconOptionView.setStopText(language.muteText);
        iconOptionView.setLanguageText(language.changeLanguageText);
        iconOptionView.setVisibility(View.INVISIBLE);
    }

    public void showLanguage() {
        iconOptionView.showLanguage();
    }

    public void hideLanguage() {
        iconOptionView.hideLanguage();
    }

    public void reset() {
        if (!isOptionShowing) return;
        isOptionShowing = false;
        collapseOptionView();
    }

    public interface IconActionListener {
        void onIconTrashCollected();

        void onIconClicked();

        void onShowLanguageOptionClicked();

        void onStopClicked();
    }


}
