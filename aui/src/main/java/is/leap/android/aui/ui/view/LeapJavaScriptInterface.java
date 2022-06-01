package is.leap.android.aui.ui.view;

import android.webkit.JavascriptInterface;

import org.json.JSONException;
import org.json.JSONObject;

import is.leap.android.aui.LeapAUILogger;

import static is.leap.android.aui.AUIConstants.LeapJavaScriptInterface.EVENT_TYPE_ACTION_TAKEN;
import static is.leap.android.aui.AUIConstants.LeapJavaScriptInterface.EVENT_TYPE_KEY;
import static is.leap.android.aui.AUIConstants.LeapJavaScriptInterface.EVENT_TYPE_RENDERING_COMPLETE;
import static is.leap.android.aui.AUIConstants.LeapJavaScriptInterface.EVENT_TYPE_RESIZE;
import static is.leap.android.aui.AUIConstants.LeapJavaScriptInterface.HEIGHT;
import static is.leap.android.aui.AUIConstants.LeapJavaScriptInterface.PAGE_META_DATA;
import static is.leap.android.aui.AUIConstants.LeapJavaScriptInterface.RECT;
import static is.leap.android.aui.AUIConstants.LeapJavaScriptInterface.WIDTH;

public class LeapJavaScriptInterface {
    private final LeapHtmlContentInteractionListener interactionListener;

    public LeapJavaScriptInterface(LeapHtmlContentInteractionListener interactionListener) {
        this.interactionListener = interactionListener;
    }

    @JavascriptInterface
    public String getFlowMenuData() {
        return interactionListener.getFlowList();
    }

    @JavascriptInterface
    public String getPersonalizedTags() {
        return interactionListener.getPersonalizedTags();
    }

    @JavascriptInterface
    public String getLanguages() {
        return interactionListener.getLanguages();
    }

    @JavascriptInterface
    public String getThemeColor() {
        return interactionListener.getLanguageOptionThemeColor();
    }

    @JavascriptInterface
    public String getLocale() {
        return interactionListener.getAudioLocale();
    }

    private static int pageRectToViewHeight(boolean ceilWidthAndHeight, JSONObject jsonObject) {
        try {
            double height = jsonObject.getJSONObject(PAGE_META_DATA).getJSONObject(RECT).optDouble(HEIGHT, -1d);
            return (int) (ceilWidthAndHeight ? Math.ceil(height) : height);
        } catch (JSONException e) {
            LeapAUILogger.errorAUI("pageRectToViewHeight could not get page height", e);
            return -1;
        }
    }

    private static int pageRectToViewWidth(boolean ceilWidthAndHeight, JSONObject jsonObject) {
        try {
            //Direct conversion to int was just taking the integer part and was causing an unnecessary resize (one issue seen in the tooltip)
            //However, we needed the 'ceil' value in case the fractional part was >= .5
            double width = jsonObject.getJSONObject(PAGE_META_DATA).getJSONObject(RECT).optDouble(WIDTH, -1d);
            return (int) (ceilWidthAndHeight ? Math.ceil(width) : width);
        } catch (JSONException e) {
            LeapAUILogger.errorAUI("pageRectToViewWidth could not get page height", e);
            return -1;
        }
    }

    private void handleActionTaken(JSONObject jsonObject) {
        LeapAUILogger.debugAUI("handleActionTaken: " + jsonObject);
        //listen to the action_event
        if (jsonObject == null) return;
        interactionListener.onWebActionPerformed(EVENT_TYPE_ACTION_TAKEN, jsonObject);
    }

    @JavascriptInterface
    public void postMessage(String message) {
        LeapAUILogger.debugAUI("postMessage called: " + message);
        try {
            JSONObject jsonObject = new JSONObject(message);
            String messageType = jsonObject.getString(EVENT_TYPE_KEY);
            switch (messageType) {
                case EVENT_TYPE_RENDERING_COMPLETE:
                    handleRenderComplete(jsonObject);
                    break;
                case EVENT_TYPE_RESIZE:
                    handleOnResize(jsonObject);
                    break;
                case EVENT_TYPE_ACTION_TAKEN:
                    handleActionTaken(jsonObject);
                    break;
            }
        } catch (JSONException ignored) {

        }
    }

    private void handleRenderComplete(final JSONObject jsonObject) {
        boolean ceilWidthAndHeight = interactionListener.shouldCeilWidthAndHeightValue();
        int pageWidth = pageRectToViewWidth(ceilWidthAndHeight, jsonObject);
        int pageHeight = pageRectToViewHeight(ceilWidthAndHeight, jsonObject);
        interactionListener.renderComplete(pageWidth, pageHeight);
    }

    private void handleOnResize(final JSONObject jsonObject) {
        boolean ceilWidthAndHeight = interactionListener.shouldCeilWidthAndHeightValue();
        int pageWidth = pageRectToViewWidth(ceilWidthAndHeight, jsonObject);
        int pageHeight = pageRectToViewHeight(ceilWidthAndHeight, jsonObject);
        interactionListener.onResize(pageWidth, pageHeight);
    }

}
