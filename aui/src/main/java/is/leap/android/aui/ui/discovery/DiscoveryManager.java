package is.leap.android.aui.ui.discovery;

import android.util.SparseBooleanArray;

import is.leap.android.aui.data.LeapAUICache;
import is.leap.android.aui.ui.assist.InstructionManager;
import is.leap.android.aui.ui.assist.output.Assist;
import is.leap.android.aui.ui.discovery.listener.DiscoveryEventListener;
import is.leap.android.aui.ui.discovery.listener.DiscoveryListener;
import is.leap.android.core.AppExecutors;
import is.leap.android.core.Constants;
import is.leap.android.core.EventConstants;
import is.leap.android.core.data.LeapCoreCache;
import is.leap.android.core.data.model.IconSetting;
import is.leap.android.core.data.model.Instruction;
import is.leap.android.core.data.model.LeapFlowDiscovery;
import is.leap.android.core.data.model.WebContentAction;

public class DiscoveryManager implements DiscoveryListener {

    private final DiscoveryEventListener discoveryListener;
    private final InstructionManager instructionManager;
    private LeapFlowDiscovery currentFlowDiscovery;
    private final AppExecutors.ThreadHandler mainThread;
    private final SparseBooleanArray discoveryVisitedMap = new SparseBooleanArray();

    public DiscoveryManager(DiscoveryEventListener discoveryListener,
                            InstructionManager instructionManager,
                            AppExecutors appExecutors) {
        this.discoveryListener = discoveryListener;
        this.mainThread = appExecutors.mainThread();
        this.instructionManager = instructionManager;
    }

    /**
     * Prepares for discovery
     *
     * @param leapFlowDiscovery is the discovery to be shown
     */
    public void setDiscovery(LeapFlowDiscovery leapFlowDiscovery) {
        if (leapFlowDiscovery == null) return;
        this.currentFlowDiscovery = leapFlowDiscovery;
    }

    public LeapFlowDiscovery getLeapFlowDiscovery() {
        return currentFlowDiscovery;
    }

    public boolean isCurrentDiscoveryFlowMenu() {
        return currentFlowDiscovery != null && currentFlowDiscovery.isFlowMenu();
    }

    /**
     * Shows required discovery UI:
     * 1.Sets the {@link LeapFlowDiscovery}
     * 2.Checks whether it should show discovery UI or not
     * 3.Checks whether auto flow start is required refer {@link LeapFlowDiscovery#autoStart}
     */
    public void showDiscovery() {
        if (currentFlowDiscovery == null) return;
        if(currentFlowDiscovery.isStartSubFlowWithoutDiscovery()) {
            setupFlowStartedActions(false);
            discoveryListener.onDiscoveryOptInFromFlowMenu(currentFlowDiscovery.getSubFlowProjectId());
            return;
        }
        if (!discoveryVisitedMap.get(currentFlowDiscovery.id))
            discoveryVisitedMap.put(currentFlowDiscovery.id, true);

        if (autoFlowStart(currentFlowDiscovery)) {
            discoveryListener.onAutoFlowStart();
            return;
        }
        Instruction instruction = currentFlowDiscovery.instruction;
        if (instruction == null) return;

        instructionManager.setInstruction(instruction);
        IconSetting iconSetting = new IconSetting(LeapAUICache.iconSettings);
        iconSetting.setEnable(currentFlowDiscovery.enableIcon);
        instructionManager.show(Constants.AUIExperienceType.DISCOVERY, iconSetting,
                LeapCoreCache.audioLocale, currentFlowDiscovery.flowProjectIds);
    }

    /**
     * Enables Leap to auto start the flow(auto flow selection):
     *
     * @param leapFlowDiscovery current discovery
     * @return whether trigger should auto start or not
     */
    private boolean autoFlowStart(LeapFlowDiscovery leapFlowDiscovery) {
        if (shouldAutoStart(leapFlowDiscovery)) {
            startFlow(leapFlowDiscovery.getSingleFlowId());
            return true;
        }
        return false;
    }

    public boolean shouldAutoStart(LeapFlowDiscovery leapFlowDiscovery) {
        return leapFlowDiscovery != null && leapFlowDiscovery.autoStart && !leapFlowDiscovery.isFlowMenu();
    }

    @Override
    public void onDiscoveryOptIn(int flowSelected) {
        if (discoveryListener != null) discoveryListener.onDiscoveryOptIn(flowSelected);
    }

    @Override
    public void onDiscoveryOptInFromFlowMenu(WebContentAction contentAction) {
        if (discoveryListener != null && contentAction != null)
            discoveryListener.onDiscoveryOptInFromFlowMenu(contentAction.projectId, contentAction.deepLink, contentAction.flowTitle);
    }

    public void reset() {
        if (instructionManager != null) instructionManager.reset();
    }

    public void handleAssistActionTaken(Instruction instruction, String actionType) {
        if (EventConstants.ANCHOR_CLICK.equals(actionType)) return;
        if (isOptOutClick(actionType)) {
            onOutOfDiscovery();
            if (discoveryListener != null) discoveryListener.onDiscoveryOptOut();
            return;
        }

        if (instruction != null && currentFlowDiscovery != null) {
            startFlow(currentFlowDiscovery.getSingleFlowId());
        }
    }

    private boolean isOptOutClick(String actionType) {
        return EventConstants.OUTSIDE_ANCHOR_CLICK.equals(actionType) || Assist.OPT_OUT_CLICK.equals(actionType);
    }

    private void hideAssist() {
        mainThread.post(new Runnable() {
            @Override
            public void run() {
                if (instructionManager == null) return;
                // Also reset the instruction manager
                instructionManager.reset();
            }
        });
    }

    public void handleAssistActionTaken(WebContentAction webContentAction) {
        if (webContentAction == null) return;
        //
        hideAssist();

        //handle language click
        if (webContentAction.isLanguageButtonClicked()) {
            discoveryListener.onDiscoveryLangClicked(currentFlowDiscovery);
            return;
        }

        //handle opt in
        if (webContentAction.isOptIn) {
            //below is done here because ui operation needs to be on main thread
            if (currentFlowDiscovery == null) return;
            if (currentFlowDiscovery.isFlowMenu()) {
                startMultiFlow(webContentAction);
                return;
            }
            startFlow(currentFlowDiscovery.getSingleFlowId());
            return;
        }

        //handle opt out
        if (webContentAction.isDismissed) {
            //below is done here because ui operation needs to be on main thread
            onOutOfDiscovery();
            if (discoveryListener != null) discoveryListener.onDiscoveryOptOut();
        }
    }

    public void setupFlowStartedActions(boolean isInteracted) {
        instructionManager.stopSound();
        LeapAUICache.setIsInDiscovery(false);
        if (currentFlowDiscovery != null)
            LeapAUICache.discoveryInteractedMap.put(currentFlowDiscovery.id, isInteracted);
    }

    private void startMultiFlow(WebContentAction webContentAction) {
        setupFlowStartedActions(true);
        onDiscoveryOptInFromFlowMenu(webContentAction);
    }

    private void startFlow(int flowId) {
        setupFlowStartedActions(true);
        onDiscoveryOptIn(flowId);
    }

    public void onOutOfDiscovery() {
        if (currentFlowDiscovery != null)
            LeapAUICache.discoveryInteractedMap.put(currentFlowDiscovery.id, true);
        instructionManager.stopSound();
        instructionManager.resetPreviousSound();
        LeapAUICache.setIsInDiscovery(true);
    }

    public void onActivityPause() {
//        if (LeapAUICache.isInDiscovery && currentFlowDiscovery != null) currentFlowDiscovery = null;

        if (LeapAUICache.isInDiscovery && currentFlowDiscovery != null) {
            if(!currentFlowDiscovery.isStartSubFlowWithoutDiscovery())
                currentFlowDiscovery = null;
        }
        reset();
    }

    public boolean isDiscoverySame(LeapFlowDiscovery leapFlowDiscovery) {
        LeapFlowDiscovery currentDiscovery = getLeapFlowDiscovery();
        return currentDiscovery != null && currentDiscovery.equals(leapFlowDiscovery);
    }

    public void resetCurrentDiscovery() {
        currentFlowDiscovery = null;
    }

    public boolean isDiscoveryVisited(int discoveryID) {
        return discoveryVisitedMap.get(discoveryID);
    }

    public void resetVisitedMap() {
        discoveryVisitedMap.clear();
    }

    public void onStopFlow() {
        if (currentFlowDiscovery != null) {
            if(currentFlowDiscovery.isStartSubFlowWithoutDiscovery())
                currentFlowDiscovery.resetFlowMenuData();
            LeapAUICache.setMuted(currentFlowDiscovery.id, true);
        }
        if (!LeapAUICache.isInDiscovery) LeapAUICache.setIsInDiscovery(true);
    }
}