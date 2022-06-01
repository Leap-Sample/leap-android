package is.leap.android.aui.ui.assist;

import android.graphics.Rect;
import android.view.View;
import android.webkit.WebView;

import is.leap.android.aui.AUIConstants;
import is.leap.android.aui.ui.assist.listener.AssistBoundListener;
import is.leap.android.aui.ui.assist.listener.WebAnchorElementClickListener;
import is.leap.android.aui.ui.listener.IUIChangeHolder;
import is.leap.android.core.AppExecutors;
import is.leap.android.core.Constants;
import is.leap.android.aui.LeapAUILogger;
import is.leap.android.core.contextdetection.detector.JSLoader;
import is.leap.android.core.contextdetection.detector.JSMaker;

import org.json.JSONObject;

public class WebAssistHelper implements JSLoader.JSListener {

    private static final int _800_MS_DELAY = 800;
    private final JSLoader jsLoader;
    private final IUIChangeHolder uiChangeHolder;
    private final AppExecutors appExecutors;
    private final WebAnchorElementClickListener webAnchorElementClickListener;
    private final AssistBoundListener boundListener;
    private Runnable oneSecondRunnable;
    private Runnable focusRunnable;

    public WebAssistHelper(AppExecutors appExecutors, WebAnchorElementClickListener webAnchorElementClickListener,
                           AssistBoundListener boundListener, IUIChangeHolder uiChangeHolder) {
        this.appExecutors = appExecutors;
        this.webAnchorElementClickListener = webAnchorElementClickListener;
        this.boundListener = boundListener;
        this.jsLoader = new JSLoader(this, appExecutors);
        this.uiChangeHolder = uiChangeHolder;
    }

    static String getWebScript(String assistIdentifier, String assistType) {
        if (Constants.Visual.ACTION_TYPE_CLICK.equals(assistType))
            return JSMaker.getScript(assistIdentifier, Constants.JSMaker.JS_FUNCTION_PERFORM_CLICK);
        return JSMaker.getScript(assistIdentifier, Constants.JSMaker.JS_FUNCTION_GET_ASSIST_BOUNDS);
    }

    public void executeFocusScript(final WebView webView, final String script,
                                   final String functionNameToExecute){
        focusRunnable = new Runnable() {
            @Override
            public void run() {
                jsLoader.loadScript(webView, script, functionNameToExecute, false);
            }
        };
        appExecutors.mainThread().post(focusRunnable);
    }

    public void stop() {
        if (oneSecondRunnable != null) {
            appExecutors.mainThread().removeCallbacks(oneSecondRunnable);
            oneSecondRunnable = null;
        }
        if(focusRunnable != null){
            appExecutors.mainThread().removeCallbacks(focusRunnable);
            focusRunnable = null;
        }
    }

    static String getFunctionNameToExecute(String assistType) {
        if (Constants.Visual.ACTION_TYPE_CLICK.equals(assistType)) {
            return Constants.JSMaker.JS_FUNCTION_PERFORM_CLICK;
        }
        return Constants.JSMaker.JS_FUNCTION_GET_ASSIST_BOUNDS;
    }

    public void executeScript(final WebView webView, final String script,
                              final String functionNameToExecute) {
        stop();
        oneSecondRunnable = new Runnable() {
            @Override
            public void run() {
                LeapAUILogger.debugAUI(" Injecting JS "+functionNameToExecute);
                jsLoader.loadScript(webView, script, functionNameToExecute, false);
                if (!Constants.JSMaker.JS_FUNCTION_GET_ASSIST_BOUNDS.equals(functionNameToExecute)) return;
                LeapAUILogger.debugAUI(" Injecting JS getAssistBounds() ");
                appExecutors.mainThread()
                        .setDelay(_800_MS_DELAY)
                        .executeDelayed(oneSecondRunnable);
            }
        };
        appExecutors.mainThread().post(oneSecondRunnable);
    }

    public void checkUpdatedBounds(View rootView, View assistAnchorView, Rect assistLocation) {
        if(rootView == null) return;
        AssistBoundsHelper.checkBounds(uiChangeHolder.getCurrentActivity(), rootView, assistAnchorView, assistLocation, boundListener);
    }

    @Override
    public void onValueReceived(JSONObject valueObj) {
        if (isClickPerFormed(valueObj)) {
            webAnchorElementClickListener.onWebAnchorElementClick();
            return;
        }

        Rect assistLocation = getWebAssistBounds(uiChangeHolder.getWebView(), valueObj);
        boundListener.onBoundsCalculated(assistLocation);
    }

    private boolean isClickPerFormed(JSONObject valueObj) {
        if (valueObj == null) return false;
        return valueObj.optBoolean(AUIConstants.CLICK_PERFORMED, false);
    }

    private Rect getWebAssistBounds(WebView webView, JSONObject jSResponse) {
        int[] webViewBounds = new int[2];
        if (webView != null) webView.getLocationOnScreen(webViewBounds);
        return AssistBoundsHelper.getAssistLocation(jSResponse, webViewBounds);
    }

}
