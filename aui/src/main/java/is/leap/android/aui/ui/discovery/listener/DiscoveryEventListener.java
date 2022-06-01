package is.leap.android.aui.ui.discovery.listener;

import is.leap.android.core.data.model.LeapFlowDiscovery;

/**
 * Discovery events that will be send to Core SDK, these are send from DiscoveryManager
 * to UIManager
 */
public interface DiscoveryEventListener {
    void onDiscoveryOptIn(int flowSelected);

    void onDiscoveryOptInFromFlowMenu(String projectId, String redirect, String flowTitle);

    void onDiscoveryOptInFromFlowMenu(String subFlowProjectId);

    void onDiscoveryOptOut();

    void onDiscoveryLangClicked(LeapFlowDiscovery currentFlowDiscovery);

    void onAutoFlowStart();
}
