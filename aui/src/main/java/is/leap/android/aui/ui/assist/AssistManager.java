package is.leap.android.aui.ui.assist;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Set;

import is.leap.android.aui.LeapAUIInternal;
import is.leap.android.aui.LeapAUILogger;
import is.leap.android.aui.ui.assist.annotations.ArrowAction;
import is.leap.android.aui.ui.assist.listener.AssistActionListener;
import is.leap.android.aui.ui.assist.listener.AssistBoundListener;
import is.leap.android.aui.ui.assist.listener.WebAnchorElementClickListener;
import is.leap.android.aui.ui.assist.output.Assist;
import is.leap.android.aui.ui.assist.output.DialogAssist;
import is.leap.android.aui.ui.assist.view.Arrow;
import is.leap.android.aui.ui.listener.AssistAnimationListener;
import is.leap.android.aui.ui.listener.AssistDisplayListener;
import is.leap.android.aui.ui.listener.IUIChangeHolder;
import is.leap.android.aui.util.AppUtils;
import is.leap.android.core.AppExecutors;
import is.leap.android.core.Constants;
import is.leap.android.core.contextdetection.detector.JSMaker;
import is.leap.android.core.data.model.AssistInfo;
import is.leap.android.core.data.model.IconSetting;

import static is.leap.android.aui.util.AppUtils.getView;

public class AssistManager implements AssistBoundListener,
        ScrollHelper.ScrollListener, Arrow.ArrowClickListener, Assist.AppScrollListener {

    private final NativeAssistHelper nativeAssistHelper;
    private final AssistAnimationListener assistAnimationListener;
    private final AssistActionListener assistActionListener;
    private final AssistDisplayListener assistDisplayListener;
    private final IUIChangeHolder uiChangeHolder;
    private final WebAssistHelper webAssistHelper;
    private final AppExecutors appExecutors;
    private final WebAnchorElementClickListener webAnchorElementClickListener;
    private final InstructionChangeListener instructionChangeListener;
    private AssistInfo assistInfo;
    private WeakReference<View> assistAnchorViewRef;
    private Assist currentOutput;
    private Arrow arrowView;
    private Rect assistLocation;
    private IconSetting iconSetting;
    private boolean isAssistScrolledOnce, isScrolling = false;
    private Runnable runnableUpdateBoundsDelayed;

    AssistManager(AppExecutors appExecutors, WebAnchorElementClickListener webAnchorEleClickListnr,
                  AssistAnimationListener assistLifecycleCB, AssistActionListener assistActionListener,
                  AssistDisplayListener assistDisplayListener, IUIChangeHolder uiChangeHolder,
                  InstructionChangeListener instructionChangeListener) {
        this.webAnchorElementClickListener = webAnchorEleClickListnr;
        this.appExecutors = appExecutors;
        nativeAssistHelper = new NativeAssistHelper(appExecutors, this, uiChangeHolder);
        this.assistAnimationListener = assistLifecycleCB;
        this.assistActionListener = assistActionListener;
        this.assistDisplayListener = assistDisplayListener;
        this.uiChangeHolder = uiChangeHolder;
        webAssistHelper = new WebAssistHelper(appExecutors, webAnchorEleClickListnr, this, uiChangeHolder);
        this.instructionChangeListener = instructionChangeListener;
    }

    void initAssist(AssistInfo assistInfo, Map<String, String> assistContentMap, IconSetting iconSetting,
                    String audioLocale, Set<String> flowProjectIds, boolean shouldTrackTouchOnAUI) {
        this.iconSetting = iconSetting;
        View rootView = uiChangeHolder.getRootView();
        this.assistInfo = assistInfo;
        currentOutput = AssistFactory.makeAssist(uiChangeHolder.getCurrentActivity(), assistInfo.type, assistInfo.accessibilityText, rootView);
        if (currentOutput == null) return;
        arrowView = new Arrow(rootView, isIconLeftAligned(), this);
        if (iconSetting != null)
            currentOutput.setIconSetting(iconSetting);
        currentOutput.setAppExecutor(appExecutors);
        currentOutput.setShouldTrackTouch(shouldTrackTouchOnAUI);
        currentOutput.setAssistInfo(assistInfo);
        currentOutput.setAudioLocale(audioLocale);
        String assistHtmlUrl = AssistInfo.getHtmlUrl(assistInfo.htmlUrl);
        currentOutput.setContent(assistHtmlUrl, assistContentMap);
        currentOutput.setAssistAnimationListener(assistAnimationListener);
        currentOutput.setAssistActionListener(assistActionListener);
        currentOutput.setAssistDisplayListener(assistDisplayListener);
        if(shouldAutoScroll())
            currentOutput.setOnScrollListener(this);
        if (isDialog() && flowProjectIds != null) {
            ((DialogAssist) currentOutput).setFlowProjectIds(flowProjectIds);
        }
    }

    private boolean isIconLeftAligned() {
        return iconSetting != null && iconSetting.leftAlign;
    }

    void handleAssist(View assistAnchorView) {
        this.assistAnchorViewRef = new WeakReference<>(assistAnchorView);

        // handle assist action
        if (handleAssistAction(assistAnchorView)) return;
        View rootView = uiChangeHolder.getRootView();
        nativeAssistHelper.detectViewInBounds(rootView, assistAnchorView);
    }

    private boolean handleAssistAction(View assistAnchorView) {
        if (assistInfo == null) return false;
        if (Constants.Visual.ACTION_TYPE_CLICK.equals(assistInfo.type)) {
            assistAnchorView.performClick();
            webAnchorElementClickListener.onWebAnchorElementClick();
            return true;
        }
        return false;
    }

    void handleAssistWebContext() {
        WebView webView = uiChangeHolder.getWebView();
        injectAssistBoundScript(webView);
    }

    private void injectAssistBoundScript(WebView webView) {
        if (assistInfo == null || webView == null) return;
        String script = WebAssistHelper.getWebScript(assistInfo.identifier, assistInfo.type);
        if (script == null || script.isEmpty()) return;
        String functionNameToExecute = WebAssistHelper.getFunctionNameToExecute(assistInfo.type);
        webAssistHelper.executeScript(webView, script, functionNameToExecute);
    }

    @Override
    public void onBoundsCalculated(final Rect assistLocation) {
        appExecutors.mainThread().post(new Runnable() {
            @Override
            public void run() {
                // This to be done just once otherwise everytime we scroll the content, the content will automatically scroll to middle
                final View assistAnchorView = getAssistAnchorView();

                View rootView = uiChangeHolder.getRootView();
                if (assistInfo!=null && assistInfo.isWeb) {
                    webAssistHelper.checkUpdatedBounds(rootView, assistAnchorView, assistLocation);
                    return;
                }

                nativeAssistHelper.checkGlobalUpdateBounds(rootView, assistAnchorView, assistLocation);

                runnableUpdateBoundsDelayed = new Runnable() {
                    @Override
                    public void run() {
                        View assistAnchorView = getAssistAnchorView();
                        View rootView = uiChangeHolder.getRootView();
                        if(rootView == null) return;
                        Rect assistLocation = AssistBoundsHelper.getAssistLocation(rootView, assistAnchorView);
                        nativeAssistHelper.checkGlobalUpdateBounds(rootView, assistAnchorView, assistLocation);
                    }
                };
                appExecutors.mainThread().postDelayed(runnableUpdateBoundsDelayed,1200);
            }
        });
    }

    private boolean shouldAutoScroll() {
        return currentOutput != null && currentOutput.shouldAutoScroll();
    }

    private void injectFocusScript() {
        WebView webView = uiChangeHolder.getWebView();
        if (assistInfo == null || webView == null) return;
        String focusScript = JSMaker.getScript(assistInfo.identifier, Constants.JSMaker.JS_FUNCTION_FOCUS_ELEMENT);
        webAssistHelper.executeFocusScript(webView, focusScript, Constants.JSMaker.JS_FUNCTION_FOCUS_ELEMENT);
    }

    private View getAssistAnchorView() {
        return getView(assistAnchorViewRef);
    }

    @Override
    public void onInBound(final Rect assistLocation) {
        appExecutors.mainThread().post(new Runnable() {
            @Override
            public void run() {
                LeapAUILogger.debugAUI("Bound :: onInBound");
                removeArrowOutput();
                if (assistLocation.equals(AssistManager.this.assistLocation)) return;
                if (currentOutput == null) return;
                currentOutput.setAnchorPosition(AssistManager.this.assistLocation, assistLocation);
                currentOutput.show();
                AssistManager.this.assistLocation = assistLocation;

                Activity currentActivity = uiChangeHolder.getCurrentActivity();
                boolean isKeyboardOpen = AppUtils.isKeyboardOpen(currentActivity);
                if (isKeyboardOpen && shouldAutoScroll() && (!isAssistScrolledOnce)) {
                    scrollAnchor(currentActivity, assistLocation, ScrollHelper.KEYBOARD_SCROLL_TO);
                    setAssistScrolledOnce();
                }
            }
        });
    }

    private boolean scrollAnchor(Activity currentActivity, Rect assistLocation, int scrollTo) {
        return ScrollHelper.autoScroll(currentActivity, assistLocation,
                instructionChangeListener.getCurrentScrollView(),
                instructionChangeListener.getCurrentAppBarLayout(),
                AssistManager.this, scrollTo);
    }

    private void setAssistScrolledOnce() {
        isAssistScrolledOnce = true;
    }

    @Override
    public void onOutBound(final int outBoundSide, final int arrowAction, final Rect assistLocation) {
        appExecutors.mainThread().post(new Runnable() {
            @Override
            public void run() {
                LeapAUILogger.debugAUI("Bound :: onOutBound");
                Activity currentActivity = uiChangeHolder.getCurrentActivity();

                boolean scrollAnchor = false;
                if (shouldAutoScroll() && !isAssistScrolledOnce) {
                    int scrollTo = ArrowAction.KEYBOARD == arrowAction
                            ? ScrollHelper.KEYBOARD_SCROLL_TO
                            : ScrollHelper.NORMAL_SCROLL;
                    scrollAnchor = scrollAnchor(currentActivity, assistLocation, scrollTo);
                    setAssistScrolledOnce();
                    final View assistAnchorView = getAssistAnchorView();
                    handleAutoFocus(assistAnchorView);
                }
                if(scrollAnchor) return;
                hideCurrentOutput();
                AssistManager.this.assistLocation = assistLocation;

                Context context = LeapAUIInternal.getInstance().getContext();
                Rect arrowPointerLocation;
                View rootView = uiChangeHolder.getRootView();
                if (arrowView == null) {
                    arrowView = new Arrow(rootView, isIconLeftAligned(), AssistManager.this);
                }

                int insetBottom = AppUtils.getInsetBottom(currentActivity);
                Rect rootViewBounds = AppUtils.getRootViewBounds(currentActivity, AppUtils.isKeyboardOpen(currentActivity), rootView);
                int width = rootViewBounds.width();
                int height = rootViewBounds.height() - insetBottom;

                int iconBottomMargin;
                if (iconSetting != null) {
                    iconBottomMargin = AppUtils.dpToPxInt(context, iconSetting.iconBottomMargin);
                } else {
                    iconBottomMargin = AppUtils.dpToPxInt(context, IconSetting.DEFAULT_ICON_BOTTOM_MARGIN);
                }
                arrowPointerLocation = AssistBoundsHelper.getArrowPointerLocation(width, height, iconBottomMargin);

                if (showArrow()) {
                    arrowView.rotateArrow(outBoundSide);
                    arrowView.setContentDescription(outBoundSide);
                    arrowView.setArrowAction(arrowAction);
                    arrowView.updateLayoutParams(arrowPointerLocation);
                    if (!isScrolling) {
                        arrowView.show();
                        LeapAUILogger.debugAUI("Bound :: onOutBound: arrow show called");
                    }
                }
            }
        });
    }

    private void handleAutoFocus(final View assistAnchorView) {
        if (assistInfo == null) return;
        if (assistInfo.isWeb) {
            if (assistInfo.autoFocus) {
                injectFocusScript();
            }
            return;
        }

        if (assistInfo.autoFocus) AssistUtils.autoFocus(assistAnchorView);
    }

    public boolean showArrow() {
        return (instructionChangeListener.getCurrentAppBarLayout() != null
                || instructionChangeListener.getCurrentScrollView() != null);
    }

    public void reset() {
        isAssistScrolledOnce = false;
        resetOutput();
    }

    void resetOutput() {
        assistLocation = null;
        stopListeners();
        removePrevOutput();
        appExecutors.stopMainRunnable(runnableUpdateBoundsDelayed);
    }

    void stopListeners() {
        webAssistHelper.stop();
        nativeAssistHelper.stop();
    }

    void hideAssist() {
        hideCurrentOutput();
        hideOutBoundOutput();
    }

    private void removePrevOutput() {
        removeArrowOutput();
        removeCurrentOutput();
    }

    void removeArrowOutput() {
        if (arrowView != null) {
            LeapAUILogger.debugAUI(" Arrow removed ");
            arrowView.hide(); //need to first hide then remove
            arrowView.remove();
            arrowView = null;
        }
    }

    private void hideCurrentOutput() {
        if (currentOutput != null) currentOutput.hide();
    }

    private void removeCurrentOutput() {
        if (currentOutput != null) {
            currentOutput.hide(false);
            currentOutput.remove();
            currentOutput = null;
        }
    }

    private void hideOutBoundOutput() {
        if (arrowView != null) arrowView.hide();
    }

    private void handleArrowClick(@ArrowAction int actionType, Rect viewLocation) {
        Activity currentActivity = uiChangeHolder.getCurrentActivity();
        boolean isKeyboardOpen = AppUtils.isKeyboardOpen(currentActivity);
        int scrollTo = isKeyboardOpen ? ScrollHelper.KEYBOARD_SCROLL_TO : ScrollHelper.NORMAL_SCROLL;
        if (ScrollHelper.autoScroll(currentActivity, viewLocation,
                instructionChangeListener.getCurrentScrollView(),
                instructionChangeListener.getCurrentAppBarLayout(), this, scrollTo)) {
            final View assistAnchorView = getAssistAnchorView();
            handleAutoFocus(assistAnchorView);
            return;
        }
        if (actionType == ArrowAction.KEYBOARD || AppUtils.isKeyboardOpen(currentActivity)) {
            AppUtils.closeKeyboard(currentActivity);
        }
    }

    @Override
    public void onScrollStart() {
        isScrolling = true;
    }

    @Override
    public void onScrollStop() {
        isScrolling = false;
    }

    public void onActivityPause() {
        if (currentOutput == null) return;
        currentOutput.onActivityPause();
        currentOutput.hide(false);
    }

    boolean isNonAnchorAssist() {
        return currentOutput != null && currentOutput.isNonAnchorAssist();
    }

    public void show() {
        if (currentOutput == null) return;
        currentOutput.show();
    }

    @Override
    public void onArrowClicked(int arrowAction) {
        handleArrowClick(arrowAction, assistLocation);
    }

    public void updateAnchorView(View newAnchorView) {
        this.assistAnchorViewRef = new WeakReference<>(newAnchorView);
        View rootView = uiChangeHolder.getRootView();
        nativeAssistHelper.detectViewInBounds(rootView, newAnchorView);
    }

    public boolean isDialog() {
        return currentOutput != null && currentOutput instanceof DialogAssist;
    }

    public ViewGroup getDialogAssistRootView() {
        if (isDialog()) return ((DialogAssist) currentOutput).getDialogAssistRootView();
        return null;
    }

    /**
     *
     * @param assistLocation Rect - The current assist location
     * @param scrollTo int - The y position to scroll to
     * @param forceScroll boolean - required since there is possibility of content resize
     */
    @Override
    public void canStartScroll(Rect assistLocation, int scrollTo, boolean forceScroll) {
        LeapAUILogger.debugAUI("scrollTo: " + scrollTo);
        if (shouldAutoScroll() && (!isAssistScrolledOnce || forceScroll)) {
            Activity currentActivity = uiChangeHolder.getCurrentActivity();
            boolean isKeyboardOpen = AppUtils.isKeyboardOpen(currentActivity);
            scrollTo = isKeyboardOpen ? ScrollHelper.KEYBOARD_SCROLL_TO : scrollTo;
            scrollAnchor(currentActivity, assistLocation, scrollTo);
            setAssistScrolledOnce();
        }
        final View assistAnchorView = getAssistAnchorView();
        handleAutoFocus(assistAnchorView);
    }

    interface InstructionChangeListener {
        View getCurrentScrollView();
        View getCurrentAppBarLayout();
    }
}
