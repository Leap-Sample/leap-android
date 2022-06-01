package is.leap.android.aui;

import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import java.util.Map;

import is.leap.android.LeapEventCallbacks;
import is.leap.android.LeapSharedPref;
import is.leap.android.aui.data.LeapAUICache;
import is.leap.android.aui.ui.UIManager;
import is.leap.android.core.LeapCore;
import is.leap.android.core.LeapCoreInternal;
import is.leap.android.core.LeapElementActionCallbacks;
import is.leap.android.core.contract.UIContextContract;
import is.leap.android.core.data.LeapCoreCache;
import is.leap.android.core.data.model.ProjectProps;
import is.leap.android.core.util.StringUtils;

public class LeapAUIInternal {

    private static LeapAUIInternal instance;
    private Application application;
    private final Context context;
    private UIManager uiManager;
    private boolean isLeapAUIStarted = false;

    public LeapAUIInternal(Application application, Context context) {
        this.context = context;
        if (instance != null && LeapCoreCache.isLeapEnabled) {
            LeapAUILogger.debugAUI("Already Initialised");
            return;
        }
        LeapCore.init(application);
        init(application);
    }

    public static LeapAUIInternal getInstance() {
        return instance;
    }

    private void init(Application application) {
        LeapAUILogger.debugInit("Leap AUI SDK Version : " + BuildConfig.VERSION_NAME);
        LeapAUILogger.debugInit("Leap AUI SDK Build Type : " + BuildConfig.BUILD_TYPE);
        instance = this;
        this.application = application;
        initialiseReceiver(application);
        // Initialise UIManager
        uiManager = new UIManager();
        UIContextContract.UIListener uiListener = LeapCoreInternal.Injector.getUIListener(uiManager);
        uiManager.setUIListener(uiListener);
        // Init SharedPref
        LeapSharedPref.init(application);
    }

    private void initialiseReceiver(Application application) {
        AUIControlReceiver controlReceiver = new AUIControlReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("is.leap.android.aui.AUIControlReceiver");
        application.registerReceiver(controlReceiver, filter);
    }

    public Context getApp() {
        return this.application;
    }

    public Context getContext() {
        if (context == null) return application;
        return context;
    }

    public UIContextContract.UIListener getUIListener() {
        if(uiManager == null) return null;
        return uiManager.getUiListener();
    }

    public Resources getResources() {
        return getContext().getResources();
    }

    public View inflate(int layoutResId, ViewGroup parent, boolean attachToParent) {
        LayoutInflater inflater = LayoutInflater.from(application).cloneInContext(getContext());
        return inflater.inflate(layoutResId, parent, attachToParent);
    }

    public View inflate(int layoutResId) {
        return inflate(layoutResId, null, false);
    }

    public void enableWeb(WebView webView) {
        LeapCore.enableWeb(webView);
    }

    void updateWebViewScale(float newScale) {
        LeapCore.updateWebViewScale(newScale);
    }

    public void reset() {
        if (uiManager != null) uiManager.disable();
        LeapAUICache.reset();
        LeapAUICache.clearCachedMaps();
        LeapAUICache.setInDiscovery();
    }

    void addElementActionCallbacks(LeapElementActionCallbacks leapElementActionCallbacks) {
        LeapCoreCache.setLeapElementActionCallbacks(leapElementActionCallbacks);
    }

    void setLeapEventCallbacks(LeapEventCallbacks leapEventCallbacks) {
        LeapCoreInternal.setLeapEventCallbacks(leapEventCallbacks);
    }

    public void start(String apiKey, Map<String, Object> properties,
                      ProjectProps projectProps) {
        if (isWebViewNotEnabled()) return;
        if (LeapCoreCache.isPreviewModeON) return;
        if (!isLeapAUIStarted) {
            LeapCore.start(apiKey, properties, projectProps);
            isLeapAUIStarted = true;
            return;
        }

        if(StringUtils.isNotNullAndNotEmpty(projectProps.projectID)) {
            // Reset only UI if project ID is valid
            if (uiManager != null) uiManager.disable();
            LeapAUICache.setInDiscovery();
        } else {
            // Reset all if start is called without projectID
            reset();
        }
        LeapCore.start(apiKey, properties, projectProps);

    }

    public void start(String apiKey) {
        if (isWebViewNotEnabled()) return;
        if (!isLeapAUIStarted) {
            LeapCore.start(apiKey);
            isLeapAUIStarted = true;
            return;
        }
        reset();
        LeapCore.start(apiKey);
    }

    private boolean isWebViewNotEnabled() {
        try {
            new WebView(application);
        } catch (Exception e) {
            // Catch android.webkit.WebViewFactory$MissingWebViewPackageException:
            // Failed to load WebView provider: No WebView installed
            Log.d("isWebViewNotEnabled",  " : " + e.getMessage());
            return true;
        }
        return false;
    }

    public void flushData(Map<String, Object> properties) {
        if (properties == null || properties.isEmpty()) return;
        LeapCore.flushData(properties);
    }

    public void logout() {
        LeapCore.logout();
    }

    public void disable() {
        LeapCore.disable();
        reset();
    }
}
