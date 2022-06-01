package is.leap.android.aui.ui;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.Set;

import is.leap.android.aui.AUIConstants;
import is.leap.android.aui.LeapAUIInternal;
import is.leap.android.aui.LeapAUILogger;
import is.leap.android.aui.data.LeapAUICache;
import is.leap.android.aui.manager.KeyboardVisibilityManager;
import is.leap.android.aui.ui.assist.InstructionManager;
import is.leap.android.aui.ui.discovery.DiscoveryManager;
import is.leap.android.aui.ui.discovery.listener.DiscoveryEventListener;
import is.leap.android.aui.ui.listener.AssistDisplayListener;
import is.leap.android.aui.ui.listener.IUIChangeHolder;
import is.leap.android.aui.ui.listener.InstructionListener;
import is.leap.android.aui.ui.listener.LanguageSelectionListener;
import is.leap.android.aui.ui.view.LeapDisableDialog;
import is.leap.android.aui.util.AppUtils;
import is.leap.android.core.AppExecutors;
import is.leap.android.core.Constants;
import is.leap.android.core.EventConstants;
import is.leap.android.core.LeapCoreInternal;
import is.leap.android.core.LeapElementActionCallbacks;
import is.leap.android.core.contract.UIContextContract;
import is.leap.android.core.data.LeapCoreCache;
import is.leap.android.core.data.model.IconSetting;
import is.leap.android.core.data.model.Instruction;
import is.leap.android.core.data.model.LeapFlowDiscovery;
import is.leap.android.core.data.model.LeapLanguage;
import is.leap.android.core.data.model.SoundInfo;
import is.leap.android.core.data.model.WebContentAction;
import is.leap.android.core.util.StringUtils;

import static is.leap.android.aui.util.AppUtils.getView;
import static is.leap.android.core.Constants.LeapContextEvent.CONFIG_UPDATE;
import static is.leap.android.core.Constants.LeapContextEvent.ON_ASSIST_DETECTED;
import static is.leap.android.core.Constants.LeapContextEvent.ON_ASSIST_NOT_DETECTED;
import static is.leap.android.core.Constants.LeapContextEvent.ON_FLOW_MENU_START_SCREEN_DETECTED;
import static is.leap.android.core.Constants.LeapContextEvent.ON_FLOW_MENU_START_SCREEN_NOT_DETECTED;
import static is.leap.android.core.Constants.LeapContextEvent.ON_LEAP_CONTEXT_NOT_DETECTED;
import static is.leap.android.core.Constants.LeapContextEvent.ON_PLATFORM_INJECTION_CHANGED;
import static is.leap.android.core.Constants.LeapContextEvent.RESET;
import static is.leap.android.core.Constants.LeapContextEvent.SUCCESS;

public class UIManager implements UIContextContract.ContextListener, DiscoveryEventListener,
        InstructionListener, KeyboardVisibilityManager.KeyboardVisibilityListener,
        IconManager.IconActionListener, TriggerDelayController.DelayListener, AssistDisplayListener,
        LeapDisableDialog.LeapDisableListener, IUIChangeHolder, DownloadController.Callback, LanguageSelectionListener {

    private final AppExecutors appExecutors;
    private final DiscoveryManager discoveryManager;
    private final InstructionManager instructionManager;
    private final AppExecutors.ThreadHandler mainThread;
    private final DownloadController downloadController;
    private final KeyboardVisibilityManager keyboardVisibilityManager;
    private final LanguageOptionManager languageOptionManager;
    private IconManager iconManager;
    private final TriggerDelayController triggerDelayController;

    private UIContextContract.UIListener uiListener;
    private Runnable instructionRunnable;
    private WeakReference<View> rootRef;
    LeapElementActionCallbacks leapElementActionCallbacks;
    private LeapDisableDialog leapDisableDialog;
    private WeakReference<WebView> webViewRef;
    private WeakReference<Activity> currentActivity;
    private String auiExpType;
    private String audioLocale;

    public UIManager() {
        appExecutors = LeapCoreInternal.getAppExecutors();
        mainThread = appExecutors.mainThread();
        triggerDelayController = new TriggerDelayController(this, appExecutors.bgThread());
        instructionManager = new InstructionManager(this,
                this, appExecutors, this);
        discoveryManager = new DiscoveryManager(this, instructionManager, appExecutors);
        keyboardVisibilityManager = new KeyboardVisibilityManager(this, this);
        downloadController = new DownloadController();
        languageOptionManager = new LanguageOptionManager(this, appExecutors);
    }

    @Override
    public void onConfigFetched(final Map<String, List<SoundInfo>> soundsMap,
                                final Map<String, String> auiContentUrlMap,
                                List<LeapLanguage> leapLanguageList,
                                Map<String, String> defaultAccessibilityTextMap,
                                Set<String> ignoredUrlSet) {
        LeapAUICache.onInit(leapLanguageList, defaultAccessibilityTextMap);
        updateDeviceDimensions();
        mainThread.post(new Runnable() {
            @Override
            public void run() {
                Context context = LeapAUIInternal.getInstance().getApp();
                iconManager = new IconManager(context, UIManager.this, appExecutors, UIManager.this);
            }
        });
        downloadController.downloadInBulk(soundsMap, auiContentUrlMap, ignoredUrlSet);
    }

    /**
     * Called on Bg Thread. Make sure to run any UI operation on main thread
     *
     * @param leapFlowDiscovery is the current leap discovery object
     */
    @Override
    public void onLeapFlowDiscovery(final LeapFlowDiscovery leapFlowDiscovery) {
        discoveryManager.setDiscovery(leapFlowDiscovery);
        //get iconSetting for this discovery
        mainThread.post(new Runnable() {
            @Override
            public void run() {
                setIconSetting(leapFlowDiscovery);
                if (leapFlowDiscovery.hideKeyboard) AppUtils.closeKeyboard(getCurrentActivity());
                if (iconManager != null) {
                    iconManager.setRootView(getRootView());
                    iconManager.enableIcon(leapFlowDiscovery.enableIcon);
                }
            }
        });

        if (leapFlowDiscovery.instruction == null) return;
        instructionManager.setInstruction(leapFlowDiscovery.instruction);
        if (!discoveryManager.isDiscoverySame(leapFlowDiscovery)) {
            triggerDelayController.reset();
        }

        this.auiExpType = Constants.AUIExperienceType.DISCOVERY;
        this.audioLocale = LeapCoreCache.audioLocale;
        triggerDelayController.setDelay(leapFlowDiscovery.getTriggerDelay());
        downloadController.checkContextContent(leapFlowDiscovery.instruction.soundInfoMap,
                leapFlowDiscovery.getContentAUIFileUriMap(), leapFlowDiscovery.getDownloadAlias(),
                LeapCoreCache.audioLocale, this);

    }

    private void onDiscovery(final LeapFlowDiscovery flowDiscovery) {
        if (flowDiscovery == null) return;
        keyboardVisibilityManager.start();
        if (!flowDiscovery.isStartSubFlowWithoutDiscovery() && shouldNotShowDiscovery(flowDiscovery)) {
            if (iconManager != null) iconManager.show();
            return;
        }
        Activity currentActivity = getCurrentActivity();
        if (currentActivity == null) return;
        List<String> localeCodes = flowDiscovery.localeCodes;
        if (shouldShowLanguageOptionBottomSheet(flowDiscovery, localeCodes)) {
            List<LeapLanguage> discoveryLangList = LeapAUICache.getLanguagesByLocale(localeCodes);
            languageOptionManager.show(currentActivity, flowDiscovery.enableIcon,
                    flowDiscovery.languageOption, discoveryLangList, EventConstants.FROM_AUTO_DISCOVERY);
            return;
        }
        LeapAUICache.setFlowDiscovered(flowDiscovery.id);
        if (iconManager != null) iconManager.hide();
        if (AppUtils.isKeyboardOpen(currentActivity))
            AppUtils.closeKeyboard(currentActivity);
        discoveryManager.showDiscovery();
    }

    @Override
    public void onAutoFlowStart() {
        if (uiListener != null) uiListener.onAutoFlowStart();
    }

    private boolean shouldNotShowDiscovery(LeapFlowDiscovery flowDiscovery) {
        return (LeapAUICache.isMuted(flowDiscovery.id))
                || (LeapAUICache.isFlowDiscoveredInSession(flowDiscovery.id)
                && LeapAUICache.isDiscoveryInteracted(flowDiscovery.id))
                || shouldNotAutoTrigger(flowDiscovery);
    }

    private boolean shouldShowLanguageOptionBottomSheet(LeapFlowDiscovery flowDiscovery, List<String> localeCodes) {
        if (LeapCoreCache.isLanguageNotSelected) return flowDiscovery.isMultiLingual();
        return localeCodes != null && !localeCodes.contains(LeapCoreCache.audioLocale);
    }

    private boolean shouldNotAutoTrigger(LeapFlowDiscovery leapFlowDiscovery) {
        String triggerFrequencyType = leapFlowDiscovery.getTriggerFrequencyType();
        if (triggerFrequencyType == null) return false;
        switch (triggerFrequencyType) {
            case AUIConstants.AUIFrequency.MANUAL_TRIGGER:
                return true;
            case AUIConstants.AUIFrequency.EVERY_SESSION_UNTIL_DISMISSED:
                return LeapAUICache.TriggerHistory.isDismissedByUser(leapFlowDiscovery.id);
            case AUIConstants.AUIFrequency.EVERY_SESSION_UNTIL_FLOW_COMPLETE:
                return LeapAUICache.TriggerHistory.isFlowCompletedOnce(leapFlowDiscovery.id);
            case AUIConstants.AUIFrequency.PLAY_ONCE:
                return LeapAUICache.isFlowDiscovered(leapFlowDiscovery.id);
            case AUIConstants.AUIFrequency.EVERY_SESSION_UNTIL_ALL_FLOWS_ARE_COMPLETED:
                return LeapAUICache.isFlowMenuCompleted(leapFlowDiscovery.flowProjectIds);
        }
        return false;
    }

    @Override
    public synchronized void onInstruction(String contextDownloadAlias, final Instruction instruction,
                                           final String auiExpType,
                                           final String audioLocale, final long triggerDelay) {
        final LeapFlowDiscovery currentDiscovery = discoveryManager.getLeapFlowDiscovery();
        if (currentDiscovery != null && LeapAUICache.isMuted(currentDiscovery.id)
                && Constants.AUIExperienceType.FLOW.equals(auiExpType))
            return;
        if (instruction == null) return;
        this.auiExpType = auiExpType;
        if (isExpTypeNotAssist(auiExpType) && LeapAUICache.iconSettings == null) {
            mainThread.post(new Runnable() {
                @Override
                public void run() {
                    setIconSetting(currentDiscovery);
                }
            });
        }
        this.audioLocale = audioLocale;
        triggerDelayController.setDelay(triggerDelay);
        instructionManager.setInstruction(instruction);
        downloadController.checkContextContent(instruction.soundInfoMap,
                instruction.contentFileUriMap, contextDownloadAlias,
                audioLocale, this);
    }

    private void handleInstruction(final Instruction instruction) {
        instructionRunnable = new Runnable() {
            @Override
            public void run() {
                if (getCurrentActivity() == null) return;
                keyboardVisibilityManager.start();
                if (iconManager != null) iconManager.setRootView(getRootView());
                boolean assistNotDialogType = Instruction.isAssistNotDailogType(instruction);
                instructionManager.show(auiExpType, audioLocale);
                handleIconVisibility(assistNotDialogType, auiExpType);
            }
        };
        mainThread.post(instructionRunnable);
    }

    private void handleIconVisibility(boolean assistNotDialogType, String auiExpType) {
        if (Constants.AUIExperienceType.FLOW.equals(auiExpType) && assistNotDialogType
                && !discoveryManager.isCurrentDiscoveryFlowMenu()) {
            if (iconManager != null) iconManager.show();
            return;
        }

        // Hide if AUIExperienceType is ASSIST
        if (Constants.AUIExperienceType.ASSIST.equals(auiExpType)) {
            if (iconManager != null) iconManager.hide();
        }
    }

    @Override
    public void downloadConfigSounds(Map<String, List<SoundInfo>> soundInfoMap) {
        downloadController.downloadSounds(soundInfoMap);
    }

    @Override
    public void onWindowDetected(final View root) {
        this.rootRef = new WeakReference<>(root);
    }

    @Override
    public void onWebViewDetected(WebView webView) {
        this.webViewRef = new WeakReference<>(webView);
    }

    @Override
    public void onLeapContextEvent(String contextEvent) {
        LeapAUILogger.debugAUI("onLeapContextEvent: " + contextEvent);
        switch (contextEvent) {
            case ON_ASSIST_DETECTED:
            case ON_FLOW_MENU_START_SCREEN_NOT_DETECTED:
                hideIcon();
                break;
            case ON_ASSIST_NOT_DETECTED:
                hideInstruction();
                break;
            case ON_PLATFORM_INJECTION_CHANGED:
                instructionManager.stopSound();
                hide();
                break;
            case ON_LEAP_CONTEXT_NOT_DETECTED:
                onLeapContextNotDetected();
                break;
            case RESET:
                reset();
                break;
            case SUCCESS:
                handleSuccessfulCompletionFlow();
                break;
            case CONFIG_UPDATE:
                update();
                break;
            case ON_FLOW_MENU_START_SCREEN_DETECTED:
                onFlowMenuStartScreenDetected();
                break;
        }
    }

    private void onFlowMenuStartScreenDetected() {
        appExecutors.mainThread().post(new Runnable() {
            @Override
            public void run() {
                if (instructionManager.isDialogAssist() && !isInDiscovery()) {
                    ViewGroup dialogRootView = instructionManager.getDialogAssistRootView();
                    if (dialogRootView == null) return;
                    if (iconManager != null) iconManager.setRootView(dialogRootView);
                } else {
                    if (iconManager != null) iconManager.setRootView(getRootView());
                }
                if (iconManager != null) iconManager.show();
            }
        });
    }

    private void onLeapContextNotDetected() {
        instructionManager.stopSound();
        if (!discoveryManager.isCurrentDiscoveryFlowMenu()) {
            hide();
        } else {
            appExecutors.mainThread().post(new Runnable() {
                @Override
                public void run() {
                    hideLanguageOption();
                    removeDiscoveryOnly();
                    removeInstructionOnly();
                    if (isInDiscovery() && iconManager != null) iconManager.removeView();
                    // else condition is handled by ON_FLOW_MENU_START_SCREEN_NOT_DETECTED
                    // because ON_FLOW_MENU_START_SCREEN_NOT_DETECTED gets called only inside the flow
                }
            });
        }
    }

    @Override
    public void onAnchorViewChanged(View newAnchorView) {
        instructionManager.updateAnchorView(newAnchorView);
    }

    private void update() {
        reset();
        LeapAUICache.reset();
    }

    private void handleSuccessfulCompletionFlow() {
        mainThread.post(new Runnable() {
            @Override
            public void run() {
                if (iconManager != null) iconManager.reset();
            }
        });
        reset();
        LeapFlowDiscovery currentDiscovery = discoveryManager.getLeapFlowDiscovery();
        if (currentDiscovery == null) return;
        if (resetSubFlowAutoStart(currentDiscovery)) return;
        LeapAUICache.setMutedLocally(currentDiscovery.id, true);
    }

    private void hideInstruction() {
        mainThread.post(new Runnable() {
            @Override
            public void run() {
                removeInstructionOnly();
            }
        });
    }

    private void hideIcon() {
        mainThread.post(new Runnable() {
            @Override
            public void run() {
                if (iconManager != null) iconManager.removeView();
            }
        });
    }

    private void hide() {
        mainThread.post(new Runnable() {
            @Override
            public void run() {
                removeAUI();
            }
        });
    }

    public void reset() {
        mainThread.post(new Runnable() {
            @Override
            public void run() {
                LeapAUICache.setIsInDiscovery(true);
                removeAUI();
                resetIconState();
                instructionManager.stopSound();
            }
        });
    }

    @Override
    public boolean isInDiscovery() {
        return LeapAUICache.isInDiscovery;
    }

    private void removeAUI() {
        hideLanguageOption();
        removeDiscoveryOnly();
        removeInstructionOnly();
        if (iconManager != null) iconManager.removeView();
    }

    private void removeInstructionOnly() {
        if (instructionManager != null) instructionManager.reset();
    }

    private void removeDiscoveryOnly() {
        if (discoveryManager != null) discoveryManager.reset();
    }

    private void hideLanguageOption() {
        if (languageOptionManager != null) languageOptionManager.hide();
    }

    private void hideLeapDisableDialog() {
        if (leapDisableDialog != null && leapDisableDialog.isShowing()) {
            leapDisableDialog.hideDialog();
            leapDisableDialog = null;
        }
    }

    @Override
    public void onDiscoveryOptIn(int flowSelected) {
        if (uiListener != null) uiListener.onFlowStarted(flowSelected);
    }

    @Override
    public void onDiscoveryOptInFromFlowMenu(String projectId, String redirect, String flowTitle) {
        if (uiListener != null) uiListener.onFlowSelectedFromMenu(projectId, flowTitle);
    }

    @Override
    public void onDiscoveryOptInFromFlowMenu(String subFlowProjectId) {
        if (uiListener != null) uiListener.onFlowSelectedFromMenu(subFlowProjectId);
    }

    private void initiateDeepLinkRedirect(String redirect) {
        if (StringUtils.isNullOrEmpty(redirect)) {
            LeapAUILogger.debugAUI("deep link value null, might not be passed from dashboard");
            return;
        }
        LeapAUILogger.debugAUI("deep link initiated from Leap");
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.parse(redirect);
        intent.setData(uri);
        intent.setPackage(LeapAUIInternal.getInstance().getApp().getPackageName());
        Activity currentActivity = getCurrentActivity();
        if (currentActivity != null) {
            try {
                currentActivity.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                LeapAUILogger.errorAUI("deep link activity not found: " + e.getMessage());
            }
        }
    }

    @Override
    public void onInstructionVisible(String auiExpType, Instruction instruction) {
        if (uiListener != null) uiListener.onInstructionVisible(auiExpType, instruction);
    }

    public void resetIconUI() {
        appExecutors.mainThread().post(new Runnable() {
            @Override
            public void run() {
                if (iconManager != null) iconManager.setState(IconManager.REST);
            }
        });
    }

    @Override
    public void onDiscoveryOptOut() {
        LeapAUILogger.debugAUI("AUI onDiscoveryOptOut()");
        if (uiListener != null) uiListener.onDiscoveryOptOut();
    }

    @Override
    public void onDiscoveryLangClicked(LeapFlowDiscovery flowDiscovery) {
        if (flowDiscovery == null) return;
        List<String> localeCodes = flowDiscovery.localeCodes;
        if (flowDiscovery.isMultiLingual()) {
            List<LeapLanguage> discoveryLangList = LeapAUICache.getLanguagesByLocale(localeCodes);
            languageOptionManager.show(getCurrentActivity(), flowDiscovery.enableIcon,
                    flowDiscovery.languageOption, discoveryLangList, EventConstants.FROM_FLOW_MENU);
        }
    }

    /**
     * It is invoked when:
     * 1. {@link LeapDisableDialog} is show/dismissed
     * 2. {@link LeapDisableDialog} Yes or No is tapped
     *
     * @param action is either {@link EventConstants#DISABLE_PANEL_CLICK_EVENT}
     *               or {@link EventConstants#DISABLE_PANEL_VISIBLE}
     * @param toggle Two Scenario:
     *               1. When action is {@link EventConstants#DISABLE_PANEL_CLICK_EVENT} then
     *               true means {@link LeapDisableDialog} is shown &
     *               false means {@link LeapDisableDialog} is dismissed
     *               2. When action is {@link EventConstants#DISABLE_PANEL_VISIBLE} then
     *               true means {@link LeapDisableDialog} is tapped Yes &
     *               false means {@link LeapDisableDialog} is tapped No
     */
    @Override
    public void onLeapDisableDialogEvent(String action, boolean toggle) {
        if (uiListener != null) {
            uiListener.onAction(action, toggle);
            if (action.equals(EventConstants.DISABLE_PANEL_CLICK_EVENT)) {
                //Here 'toggle' refers to whether Yes or No is tapped
                onLeapDisable(toggle);
            }
            if (action.equals(EventConstants.DISABLE_PANEL_VISIBLE)) {
                //Here 'toggle' refers to panel show/dismissed
                if (toggle) {
                    removeAUI();
                    resetIconState();
                    instructionManager.stopSound();
                }
            }
        }
    }

    private void onLeapDisable(boolean shouldDisable) {
        if (!shouldDisable) return;

        LeapFlowDiscovery currentDiscovery = discoveryManager.getLeapFlowDiscovery();
        if (currentDiscovery != null && uiListener != null) {
            uiListener.onLeapDisabled(currentDiscovery.id);
        }
        LeapAUICache.setInDiscovery();
        removeAUI();
        resetIconState();
        instructionManager.stopSound();
    }

    private void resetIconState() {
        if (iconManager != null) iconManager.reset();
        resetIconUI();
    }

    @Override
    public void onAudioStarted() {
        if (isExpTypeDiscovery(auiExpType)) return;
        appExecutors.mainThread().post(new Runnable() {
            @Override
            public void run() {
                if (iconManager != null) iconManager.setState(IconManager.AUDIO);
            }
        });
    }

    @Override
    public void onAudioFinish() {
        if (isExpTypeDiscovery(auiExpType)) return;
        resetIconUI();
    }

    @Override
    public void onAssistDismissed(String auiExpType, Instruction instruction) {
        LeapAUILogger.debugAUI("onAssistDismissed(): " + auiExpType);
        if (isExpTypeDiscovery(auiExpType)) {
            discoveryManager.onOutOfDiscovery();
            return;
        }
        if (uiListener != null)
            uiListener.onInstructionComplete(auiExpType, instruction);
    }

    @Override
    public void onAssistActionTaken(String auiExpType, Instruction instruction, String actionType) {
        LeapAUILogger.debugAUI("AUI onAssistActionTaken(): Native Action" + auiExpType);
        if (isExpTypeDiscovery(auiExpType)) {
            discoveryManager.handleAssistActionTaken(instruction, actionType);
            return;
        }
        resetIconUI();
        instructionManager.stopSound();
        if (uiListener != null) uiListener.onAssistActionTaken(auiExpType, instruction, actionType);
    }

    @Override
    public void onEndFlowFromAUI(String auiExpType, WebContentAction webContentAction) {
        onStopPerformed();
        if (uiListener != null) uiListener.onEndFlowFromAUI(auiExpType, webContentAction);
    }

    @Override
    public void onAssistActionTaken(String auiExpType, WebContentAction webContentAction) {
        LeapAUILogger.debugAUI("AUI onAssistActionTaken(): WebAction" + auiExpType);

        String actionTag = webContentAction.actionTag;
        if (actionTag != null) {
            if (leapElementActionCallbacks == null)
                leapElementActionCallbacks = LeapCoreCache.getLeapEventActionListener();
            if (leapElementActionCallbacks != null)
                leapElementActionCallbacks.onEventAction(webContentAction.actionName, actionTag);
        }

        handleRedirection(webContentAction);

        if (isExpTypeDiscovery(auiExpType)) {
            discoveryManager.handleAssistActionTaken(webContentAction);
            return;
        }
        resetIconUI();
        instructionManager.stopSound();
        // to counter action taken on web content in instruction
        if (uiListener != null)
            uiListener.onActionTaken(auiExpType, webContentAction.getActionEventAndValuePair());
    }

    /***
     * Used for redirection from webcontent
     * Can be used for implicitly redirecting to an activity using deeplink
     * Can be used for opening an external URL within the browser or outside
     */
    private void handleRedirection(WebContentAction webContentAction) {
        if (webContentAction.isExternalRedirection()) {
            initiateExternalLinkRedirection(webContentAction.externalUrl);
            return;
        }

        if (webContentAction.isDeepLinkRedirection()) {
            initiateDeepLinkRedirect(webContentAction.deepLink);
        }
    }

    private void initiateExternalLinkRedirection(String externalUrl) {
        if (StringUtils.isNullOrEmpty(externalUrl)) {
            LeapAUILogger.debugAUI("external link value null, might not be passed from dashboard");
            return;
        }
        Activity visibleActivity = getCurrentActivity();
        if (visibleActivity == null) return;
        Intent externalRedirection = new Intent(Intent.ACTION_VIEW, Uri.parse(externalUrl));
        try {
            visibleActivity.startActivity(externalRedirection);
        } catch (ActivityNotFoundException e) {
            LeapAUILogger.errorAUI("external link, activity not found: " + e.getMessage());
        }
    }

    @Override
    public void onKeyboardToggled(boolean keyboardDetected) {
        instructionManager.onKeyboardToggled();
        if (iconManager != null) iconManager.onKeyboardToggled(keyboardDetected);
    }

    private void onStopPerformed() {
        discoveryManager.onStopFlow();
        if (iconManager != null) {
            iconManager.reset();
            iconManager.resetIconPosToDefault();
        }
        removeAUI();
        instructionManager.stopSound();
    }

    /***
     * Independent callback listens to only the activity state change to onPause()
     * Stop media playing if ongoing
     *
     */
    @Override
    public void onActivityPause() {
        resetIconUI();
        //stop the music
        triggerDelayController.reset();
        appExecutors.stopMainRunnable(instructionRunnable);
        if (discoveryManager != null) discoveryManager.onActivityPause();
        if (instructionManager != null) {
            instructionManager.onActivityPause();
            instructionManager.stopSound();
            instructionManager.resetPreviousSound();
        }
        if (iconManager != null) iconManager.onActivityPause();
        stopListeningToKeyboardChange();
        hideLanguageOption();
        hideLeapDisableDialog();
        if (currentActivity != null) currentActivity.clear();
    }

    @Override
    public void onPageContext(String contextDownloadAlias, Map<String, String> downloadMapForPage) {
        downloadController.checkPageContents(contextDownloadAlias, downloadMapForPage);
    }

    @Override
    public void onActivityResume(Activity activity) {
        this.currentActivity = new WeakReference<>(activity);
        updateDeviceDimensions();
    }

    @Override
    public void resetPastUserExpForProject(int discoveryID) {
        LeapAUICache.resetPastUserExpForProject(discoveryID);
    }

    @Override
    public void onSubFlowAutoStart(final LeapFlowDiscovery flowMenuDiscovery) {
        if (flowMenuDiscovery == null) return;
        initiateDeepLinkRedirect(flowMenuDiscovery.getSubFlowDeeplinkUrl());
        discoveryManager.setDiscovery(flowMenuDiscovery);
        mainThread.post(new Runnable() {
            @Override
            public void run() {
                if (iconManager != null) iconManager.enableIcon(flowMenuDiscovery.enableIcon);
                setIconSetting(flowMenuDiscovery);
                if (iconManager != null) iconManager.setRootView(getRootView());
            }
        });

        this.auiExpType = Constants.AUIExperienceType.DISCOVERY;
        this.audioLocale = LeapCoreCache.audioLocale;
        downloadController.checkContextContent(flowMenuDiscovery.instruction.soundInfoMap,
                flowMenuDiscovery.getContentAUIFileUriMap(), flowMenuDiscovery.getDownloadAlias(),
                LeapCoreCache.audioLocale, this);
    }

    private void setIconSetting(final LeapFlowDiscovery flowMenuDiscovery) {
        if (flowMenuDiscovery == null) return;
        final IconSetting iconSetting = LeapAUICache.iconSettings =
                LeapCoreCache.iconSettingMap.get(String.valueOf(flowMenuDiscovery.id));
        if (iconManager != null) iconManager.setIconSetting(iconSetting);
    }

    private void updateDeviceDimensions() {
        Activity currentActivity = getCurrentActivity();
        LeapAUICache.screenWidth = AppUtils.getScreenWidth(currentActivity);
        LeapAUICache.screenHeight = AppUtils.getScreenHeight(currentActivity);
        LeapAUICache.statusBarHeight = AppUtils.getStatusBarHeight(LeapAUIInternal.getInstance().getContext());
        LeapAUICache.softButtonBarHeight = AppUtils.getSoftButtonsBarHeight(LeapAUIInternal.getInstance().getContext());
    }

    private void stopListeningToKeyboardChange() {
        if (keyboardVisibilityManager != null) keyboardVisibilityManager.stop();
    }

    public void disable() {
        triggerDelayController.reset();
        appExecutors.stopMainRunnable(instructionRunnable);
        mainThread.post(new Runnable() {
            @Override
            public void run() {
                stopListeningToKeyboardChange();
                hideLeapDisableDialog();
                if (iconManager != null) iconManager.resetIconPosToDefault();
                resetIconState();
                hideIcon();
                discoveryManager.resetVisitedMap();
                discoveryManager.resetCurrentDiscovery();
                instructionManager.reset();
                instructionManager.stopSound();
                removeAUI();
            }
        });
    }

    public void setUIListener(UIContextContract.UIListener uiListener) {
        LeapAUILogger.debugAUI("AUI : setUIListener()");
        this.uiListener = uiListener;
    }

    public UIContextContract.UIListener getUiListener() {
        return uiListener;
    }

    @Override
    public void onIconTrashCollected() {
        if (iconManager != null) iconManager.resetIconPosToDefault();
        leapDisableDialog = new LeapDisableDialog(getCurrentActivity(), this);
        leapDisableDialog.show();
    }

    @Override
    public void onIconClicked() {
        LeapFlowDiscovery flowDiscovery = discoveryManager.getLeapFlowDiscovery();
        if (flowDiscovery == null) return;
        resetSubFlowAutoStart(flowDiscovery);
        if (LeapAUICache.isMuted(flowDiscovery.id)) {
            LeapAUICache.setMuted(flowDiscovery.id, false);
        }
        List<String> localeCodes = flowDiscovery.localeCodes;
        List<LeapLanguage> discoveryLangList = LeapAUICache.getLanguagesByLocale(localeCodes);
        if (isInDiscovery()) {
            if (iconManager != null) iconManager.hide();
            if (shouldShowLanguageOptionBottomSheet(flowDiscovery, localeCodes)) {
                languageOptionManager.show(getCurrentActivity(), flowDiscovery.enableIcon,
                        flowDiscovery.languageOption, discoveryLangList, EventConstants.FROM_ICON_CLICK);
                return;
            }
            LeapAUICache.setDiscoveryToNotInteracted(flowDiscovery.id);
            discoveryManager.showDiscovery();
            return;
        }
        if (flowDiscovery.isFlowMenu()) {
            if (iconManager != null) iconManager.hide();
            discoveryManager.reset();
            LeapAUICache.setIsInDiscovery(true);
            if (iconManager != null) iconManager.setRootView(getRootView());
            discoveryManager.showDiscovery();
            return;
        }
        if (iconManager != null) iconManager.showIconOptions();
    }

    private boolean resetSubFlowAutoStart(LeapFlowDiscovery flowDiscovery) {
        if (!flowDiscovery.isStartSubFlowWithoutDiscovery()) return false;
        flowDiscovery.resetFlowMenuData();
        return true;
    }

    @Override
    public void onShowLanguageOptionClicked() {
        triggerDelayController.reset();
        appExecutors.stopMainRunnable(instructionRunnable);
        instructionManager.onActivityPause();
        instructionManager.stopSound();
        stopListeningToKeyboardChange();

        if (uiListener != null)
            uiListener.onAction(EventConstants.CHANGE_LANG_CLICKED_EVENT, null);
        LeapFlowDiscovery leapFlowDiscovery = discoveryManager.getLeapFlowDiscovery();
        List<LeapLanguage> discoveryLangList = LeapAUICache.getLanguagesByLocale(leapFlowDiscovery.localeCodes);
        languageOptionManager.show(getCurrentActivity(), leapFlowDiscovery.enableIcon,
                leapFlowDiscovery.languageOption, discoveryLangList, EventConstants.FROM_OPTIONS_MENU);
    }

    @Override
    public void onStopClicked() {
        onStopPerformed();
        if (uiListener != null) uiListener.onStopClicked();
    }

    @Override
    public View getRootView() {
        return getView(rootRef);
    }

    @Override
    public WebView getWebView() {
        return AppUtils.getWebView(webViewRef);
    }

    @Override
    public Activity getCurrentActivity() {
        return currentActivity == null ? null : currentActivity.get();
    }

    /***
     * This method is triggered whenever discovery or independent delay's delay has finished
     * This should be used to trigger an action on delay complete
     *
     */
    @Override
    public void onDelayComplete() {
        mainThread.post(new Runnable() {
            @Override
            public void run() {
                triggerDelayController.reset();
                if (isExpTypeDiscovery(auiExpType)) {
                    onDiscovery(discoveryManager.getLeapFlowDiscovery());
                    return;
                }
                if (iconManager != null) iconManager.show();
                handleInstruction(instructionManager.getInstruction());
            }
        });

    }

    @Override
    public void onAnchorAssistDetected(final float elevation) {
        LeapAUILogger.debugAUI("AUI onHighlightDetected()");
        mainThread.post(new Runnable() {
            @Override
            public void run() {
                if (iconManager != null) iconManager.bringIconToFront(elevation);
            }
        });
    }

    @Override
    public void onDialogAssistDetected(final ViewGroup dialogRoot) {
        if (discoveryManager.isCurrentDiscoveryFlowMenu()) return;
        mainThread.post(new Runnable() {
            @Override
            public void run() {
                if (iconManager != null) {
                    iconManager.setRootView(dialogRoot);
                    iconManager.show();
                }
            }
        });
    }

    @Override
    public void onIndependentIconAnimationCanStart(final String assistType) {
        mainThread.post(new Runnable() {
            @Override
            public void run() {
                if (iconManager != null) iconManager.animateIcon(assistType);
            }
        });
    }

    @Override
    public void onContextFilesAvailable() {
        onContextFilesDownloaded(0);
    }

    @Override
    public void onContextFilesDownloading() {
        if (isExpTypeDiscovery(auiExpType)) return;
        mainThread.post(new Runnable() {
            @Override
            public void run() {
                if (iconManager != null) iconManager.setState(IconManager.PROGRESS);
            }
        });
    }

    @Override
    public void onContextFilesDownloaded(final long timeTaken) {
        mainThread.post(new Runnable() {
            @Override
            public void run() {
                if (isExpTypeDiscovery(auiExpType)) {
                    //Only load the content when we have downloaded
                    initDiscovery(timeTaken, true);
                    return;
                }

                if (executeDelay(timeTaken)) return;
                triggerDelayController.reset();
                handleInstruction(instructionManager.getInstruction());
                resetIconUI();
            }
        });
    }

    private void initDiscovery(final long timeTaken, boolean shouldConsiderAUIDelay) {
        if (iconManager != null) iconManager.loadIconContent();
        LeapFlowDiscovery leapFlowDiscovery = discoveryManager.getLeapFlowDiscovery();
        if (leapFlowDiscovery == null) return;
        if (!discoveryManager.isDiscoveryVisited(leapFlowDiscovery.id) && shouldConsiderAUIDelay
                && executeDelay(timeTaken))
            return;
        triggerDelayController.reset();
        onDiscovery(leapFlowDiscovery);
    }

    /**
     * @param timeTaken long
     * @return true if delay is executed
     */
    private boolean executeDelay(long timeTaken) {
        if (triggerDelayController.getDelay() > 0) {
            triggerDelayController.setTimeTakenToDownload(timeTaken);
            triggerDelayController.executeDelay();
            if (iconManager != null) iconManager.hide();
            return true;
        }
        return false;
    }

    private boolean isExpTypeDiscovery(String auiExpType) {
        return isInDiscovery() && Constants.AUIExperienceType.DISCOVERY.equals(auiExpType);
    }

    private boolean isExpTypeNotAssist(String auiExpType) {
        return !Constants.AUIExperienceType.ASSIST.equals(auiExpType);
    }

    @Override
    public void onLanguageSelected(String locale) {
        String previousLocale = LeapCoreCache.audioLocale;
        if (uiListener != null)
            uiListener.onAction(EventConstants.LANG_SELECTED_FROM_SETTING_EVENT, locale);
        keyboardVisibilityManager.start();
        if (iconManager != null) iconManager.setIconOptionText(locale);
        boolean inDiscovery = isInDiscovery();
        LeapCoreCache.setLanguageSelected(locale);
        if (!inDiscovery) {
            if (iconManager != null) iconManager.show();
            if (uiListener != null) uiListener.onLanguageSelected(previousLocale, locale);
            downloadConfigSounds(LeapAUICache.getSoundMapByLocale(locale));
            return;
        }
        LeapFlowDiscovery flowDiscovery = discoveryManager.getLeapFlowDiscovery();
        LeapAUICache.setFlowDiscovered(flowDiscovery.id);
        LeapAUICache.setLanguageSelected();
        if (iconManager != null) iconManager.hide();
        discoveryManager.showDiscovery();
    }

    //Called from Flow Menu, Auto-trigger/Icon Click, from Option Menu
    @Override
    public void onLanguageOptedOut(String dismissReason) {
        if (discoveryManager.isCurrentDiscoveryFlowMenu()) {
            LeapFlowDiscovery leapFlowDiscovery = discoveryManager.getLeapFlowDiscovery();
            if (leapFlowDiscovery != null)
                LeapAUICache.setDiscoveryToNotInteracted(leapFlowDiscovery.id);
        }
        if (uiListener != null)
            uiListener.onAction(EventConstants.LANG_OPTION_DISMISSED, dismissReason);
        if (iconManager != null) iconManager.show();
        keyboardVisibilityManager.start();
    }
}