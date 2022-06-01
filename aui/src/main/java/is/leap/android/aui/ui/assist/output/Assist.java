package is.leap.android.aui.ui.assist.output;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;

import java.lang.ref.WeakReference;
import java.util.Map;

import is.leap.android.aui.ui.assist.listener.AnimatorEndListener;
import is.leap.android.aui.ui.assist.listener.AssistActionListener;
import is.leap.android.aui.ui.listener.AssistDisplayListener;
import is.leap.android.aui.ui.listener.AssistAnimationListener;
import is.leap.android.aui.util.AppUtils;
import is.leap.android.core.AppExecutors;
import is.leap.android.core.Constants;
import is.leap.android.core.data.model.AssistInfo;
import is.leap.android.core.data.model.DismissAction;
import is.leap.android.core.data.model.ExtraProps;
import is.leap.android.core.data.model.IconSetting;
import is.leap.android.core.data.model.Style;

public abstract class Assist {

    public static final String OPT_OUT_CLICK = "optOutClick";

    WeakReference<Activity> activityRef;
    protected AssistInfo assistInfo;
    AssistActionListener assistActionListener;
    IconSetting iconSetting;
    protected AppExecutors appExecutors;
    Runnable delayRunnable;
    private String audioLocale;
    AssistAnimationListener assistAnimationListener;
    AssistDisplayListener assistDisplayListener;
    protected boolean shouldTrackAnchorTouch;
    protected AppScrollListener appScrollListener;

    Assist(Activity activity) {
        this.activityRef = new WeakReference<>(activity);
    }

    public void setAssistInfo(AssistInfo assistInfo) {
        this.assistInfo = assistInfo;
        if (assistInfo == null || assistInfo.layoutInfo == null) return;
        if (isNonAnchorAssist()) applyAlignment(getAlignment());
        applyStyle(getLayoutStyle());
        setUpLayoutAction(assistInfo.layoutInfo.dismissAction);
    }

    void applyAlignment(String alignment) {
    }

    private String getAlignment() {
        if (assistInfo == null || assistInfo.layoutInfo == null) return null;
        return assistInfo.layoutInfo.alignment;
    }

    public void setIconSetting(IconSetting iconSetting) {
        this.iconSetting = iconSetting;
    }

    abstract public void init(Activity activity, String accessibilityText);

    abstract public void applyStyle(Style style);

    abstract public void setUpLayoutAction(DismissAction dismissAction);

    public abstract void remove();

    abstract public void updateLayoutParams(Rect oldRect, Rect rect, String alignment);

    abstract public void setContent(String htmlUrl, Map<String, String> contentFileUriMap);

    abstract public void hide(boolean withAnim);

    abstract public void show();

    abstract protected void clearAnimation();

    abstract protected void alignIcon(Rect contentBounds);

    public void hide() {
        hide(true);
    }

    public void performEnterAnimation(AnimatorEndListener animatorEndListener) {

    }

    public void performExitAnimation(AnimatorEndListener animatorEndListener) {

    }

    public void setAudioLocale(String audioLocale) {
        this.audioLocale = audioLocale;
    }

    public String getAudioLocale() {
        return audioLocale;
    }

    boolean isIconEnabled() {
        return iconSetting != null && iconSetting.isEnable();
    }

    boolean isIconLeftAligned() {
        return iconSetting != null && iconSetting.leftAlign;
    }

    Style getLayoutStyle() {
        if (assistInfo == null || assistInfo.layoutInfo == null) return null;
        return assistInfo.layoutInfo.style;
    }

    boolean highlightAnchor() {
        return assistInfo != null && assistInfo.highlightAnchor;
    }

    public Context getContext() {
        return activityRef.get();
    }

    public Activity getCurrentActivity() {
        return activityRef.get();
    }

    public void setAnchorPosition(Rect oldRect, Rect rect, String alignment) {
        if (alignment == null && assistInfo != null && assistInfo.layoutInfo != null)
            alignment = assistInfo.layoutInfo.alignment;
        updateLayoutParams(oldRect, rect, alignment);
    }

    public void setAnchorPosition(Rect oldRect, Rect rect) {
        setAnchorPosition(oldRect, rect, null);
    }

    public void setAssistAnimationListener(AssistAnimationListener assistLifeCycleCB) {
        this.assistAnimationListener = assistLifeCycleCB;
    }

    public ExtraProps getAssistExtraProps() {
        return assistInfo == null ? null : assistInfo.extraProps;
    }

    public void setAppExecutor(AppExecutors appExecutors) {
        this.appExecutors = appExecutors;
    }

    public abstract boolean isNonAnchorAssist();

    public void onActivityPause() {
        if (delayRunnable != null && appExecutors != null) {
            appExecutors
                    .mainThread()
                    .removeCallbacks(delayRunnable);
            delayRunnable = null;
        }
    }

    public boolean shouldAutoScroll() {
        if (isNonAnchorAssist()) return false;
        if (assistInfo == null) return false;
        String assistType = assistInfo.type;
        return !Constants.Visual.VISUAL_TYPE_BEACON.equals(assistType)
                && !Constants.Visual.VISUAL_TYPE_LABEL.equals(assistType);
    }

    public void setAssistActionListener(AssistActionListener assistActionListener) {
        this.assistActionListener = assistActionListener;
    }

    public void setAssistDisplayListener(AssistDisplayListener assistDisplayListener) {
        this.assistDisplayListener = assistDisplayListener;
    }

    public float getCornerRadius(Style style, int defaultCornerRadius) {
        float cornerRadius = style == null ? -1 : style.cornerRadius;
        if (cornerRadius == -1) cornerRadius = defaultCornerRadius;
        cornerRadius = AppUtils.dpToPx(getContext(), cornerRadius);
        return cornerRadius;
    }

    public float getElevation(Style style, int defaultElevation) {
        float elevation = style == null ? -1 : style.elevation;
        if (elevation == -1) elevation = defaultElevation;
        elevation = AppUtils.dpToPx(getContext(), elevation);
        return elevation;
    }

    public void setShouldTrackTouch(boolean shouldTrackAnchorTouch) {
        this.shouldTrackAnchorTouch = shouldTrackAnchorTouch;
    }

    public void setOnScrollListener(AppScrollListener appScrollListener) {
        this.appScrollListener = appScrollListener;
    }

    public interface AppScrollListener {
        void canStartScroll(Rect assistLocation, int scrollTo, boolean forceScroll);
    }
}