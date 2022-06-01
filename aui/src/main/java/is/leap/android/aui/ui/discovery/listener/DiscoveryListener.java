package is.leap.android.aui.ui.discovery.listener;

import is.leap.android.core.data.model.WebContentAction;

/**
 * Events that we get from particular discovery output
 */
public interface DiscoveryListener {
    void onDiscoveryOptIn(int flowSelected);

    //In case of Multi flow
    void onDiscoveryOptInFromFlowMenu(WebContentAction contentAction);
}