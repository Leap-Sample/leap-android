package is.leap.android.aui.ui.assist.output;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.webkit.WebSettings;
import android.webkit.WebView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import is.leap.android.aui.AUIConstants;
import is.leap.android.aui.LeapAUILogger;
import is.leap.android.aui.ui.assist.listener.AnimatorEndListener;
import is.leap.android.aui.ui.view.LeapHtmlContentInteractionListener;
import is.leap.android.aui.ui.view.LeapJavaScriptInterface;
import is.leap.android.aui.ui.view.LeapWebView;
import is.leap.android.aui.util.AppUtils;
import is.leap.android.core.data.LeapCoreCache;
import is.leap.android.core.data.model.Style;
import is.leap.android.core.data.model.WebContentAction;

import static is.leap.android.aui.AUIConstants.LeapJavaScriptInterface.JS_OBJ_NAME;

public abstract class WebContentAssist extends Assist implements LeapHtmlContentInteractionListener {

    boolean isWebRendered;
    boolean isShowCalled;
    private LeapWebView leapWebView;
    private int pageWidth;
    private int pageHeight;

    /**
     * Problem: In discovery, after exit animation(when view goes out of bound), the user didn't perform exit here.
     * But the view goes out of bound and exit animation was called. After exit animation icon animation starts(for non-associated icon)
     * So, now two icons will be visible(associated and non-associated)
     * <p>
     * Solution: {@param actionTakenForExit} was added and toggled to true when user performed exit.
     * For eg. user clicked outside, clicked cross etc.
     * So, now this boolean will be checked when after exit animation icon animation starts(for non-associated icon)
     * And only one icon will be shown
     */
    private boolean actionTakenForExit;

    private Set<String> flowProjectIds;

    public boolean isActionTakenForExit() {
        return actionTakenForExit;
    }

    public void setActionTakenForExit(boolean actionTakenForExit) {
        this.actionTakenForExit = actionTakenForExit;
    }

    WebContentAssist(Activity activity) {
        super(activity);
    }

    @Override
    public void show() {
        isShowCalled = true;
    }

    @Override
    public void hide(boolean withAnim) {
        isShowCalled = false;
    }

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    void setLeapWebView(LeapWebView leapWebView) {
        this.leapWebView = leapWebView;
        WebSettings settings = leapWebView.getSettings();
        settings.setDomStorageEnabled(true);
        settings.setJavaScriptEnabled(true);
        this.leapWebView.addJavascriptInterface(new LeapJavaScriptInterface(this), JS_OBJ_NAME);
    }

    @Override
    public void setContent(String htmlUrl, final Map<String, String> contentFileUriMap) {
        leapWebView.loadUrl(htmlUrl, contentFileUriMap);
    }

    @Override
    public void applyStyle(Style style) {
        // Set Transparent content
        leapWebView.setBackgroundColor(Color.TRANSPARENT);
        leapWebView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    public void renderComplete(final int pageWidth, final int pageHeight) {
        appExecutors.mainThread().post(new Runnable() {
            @Override
            public void run() {
                Activity currentActivity = getCurrentActivity();
                int pageWidthInDP = inPx(currentActivity, pageWidth, AppUtils.getScreenWidth(currentActivity));
                int pageHeightInDP = inPx(currentActivity, pageHeight, AppUtils.getScreenHeight(currentActivity));
                WebContentAssist.this.pageWidth = pageWidthInDP;
                WebContentAssist.this.pageHeight = pageHeightInDP;
                isWebRendered = true;
                leapWebView.updateLayout(pageWidthInDP, pageHeightInDP);
                updateContentLayout(pageWidthInDP, pageHeightInDP);
                if (isShowCalled) show();
            }
        });

    }

    private static int inPx(Activity activity, int dp, int maxPx) {
        int px = AppUtils.dpToPxInt(activity, dp);
        if (px > maxPx) px = maxPx;
        return px;
    }

    abstract void updateContentLayout(int pageWidth, int pageHeight);

    @Override
    public void onResize(final int pageWidth, final int pageHeight) {
        appExecutors.mainThread().post(new Runnable() {
            @Override
            public void run() {
                Activity currentActivity = getCurrentActivity();
                int pageWidthInDP = inPx(currentActivity, pageWidth, AppUtils.getScreenWidth(currentActivity));
                int pageHeightInDP = inPx(currentActivity, pageHeight, AppUtils.getScreenHeight(currentActivity));
                //return if the bounds are same
                if (WebContentAssist.this.pageWidth == pageWidthInDP && WebContentAssist.this.pageHeight == pageHeightInDP)
                    return;
                WebContentAssist.this.pageWidth = pageWidthInDP;
                WebContentAssist.this.pageHeight = pageHeightInDP;
                leapWebView.updateLayout(pageWidthInDP, pageHeightInDP);
                updateContentLayout(pageWidthInDP, pageHeightInDP);
            }
        });

    }

    @Override
    public void onWebActionPerformed(final String actionType, final Object value) {
        setActionTakenForExit(true);
        appExecutors.mainThread().post(new Runnable() {
            @Override
            public void run() {
                performExitAnimation(new AnimatorEndListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        WebContentAction contentAction = null;
                        if (value != null) {
                            contentAction = WebContentAction.build((JSONObject) value);
                            if (contentAction != null && contentAction.isDismissed) hide(false);
                        }

                        if (assistActionListener != null) {
                            assistActionListener.onAssistActionPerformed(contentAction);
                        }

                        if (assistAnimationListener != null)
                            assistAnimationListener.onExitAnimationFinished();
                    }
                });
            }
        });
    }

    @Override
    public void onActivityPause() {
        leapWebView.onPause();
    }

    // To be overridden in Language popup
    @Override
    public String getLanguages() {
        return null;
    }

    // To be overridden in Language popup
    @Override
    public String getLanguageOptionThemeColor() {
        return null;
    }

    @Override
    public String getAudioLocale() {
        return super.getAudioLocale();
    }

    // To be overridden in Tooltip Output
    @Override
    public boolean shouldCeilWidthAndHeightValue() {
        return false;
    }

    @Override
    public String getPersonalizedTags() {
        JSONObject jsonObject = new JSONObject();
        try {
            Map<String, String> userPropsMap = getUserPropsMap(LeapCoreCache.customUsrStrProperties,
                    LeapCoreCache.customUsrLongProperties);
            jsonObject = new JSONObject(userPropsMap);
        } catch (Exception ignored) {
        }
        return jsonObject.toString();
    }

    @Override
    public String getFlowList() {
        if (flowProjectIds == null || flowProjectIds.isEmpty()) return null;
        JSONObject projectCompletedJSON = getProjectCompletedJSON(LeapCoreCache.projectCompletedMap,
                LeapCoreCache.audioLocale, flowProjectIds);
        LeapAUILogger.debugAUI("getFlowList(): " + projectCompletedJSON);
        return projectCompletedJSON == null ? null : projectCompletedJSON.toString();
    }

    public static JSONObject getProjectCompletedJSON(Map<String, Boolean> projectCompletedMap,
                                                     String currentLocale, Set<String> discoveryFlowIdSet) {
        if (projectCompletedMap == null || projectCompletedMap.isEmpty()) return null;

        JSONObject projCompletedJSON = new JSONObject();
        try {
            projCompletedJSON.put(AUIConstants.LANGUAGE, currentLocale);
            JSONArray jsonArray = new JSONArray();
            for (Map.Entry<String, Boolean> entry : projectCompletedMap.entrySet()) {
                String projectId = entry.getKey();
                if (!discoveryFlowIdSet.contains(projectId)) continue;
                JSONObject projJSONObj = new JSONObject();
                projJSONObj.put(AUIConstants.ID, projectId);
                projJSONObj.put(AUIConstants.COMPLETED, entry.getValue());
                jsonArray.put(projJSONObj);
            }
            projCompletedJSON.put(AUIConstants.PROJECTS, jsonArray);
            return projCompletedJSON;
        } catch (JSONException e) {
            LeapAUILogger.debugAUI("getProjectCompletedJSON(): JSONException: " + e.getMessage());
            return null;
        }
    }

    /**
     * Merge both the maps and return a single map
     *
     * @param strUserPropsMap  User properties map, type: Map<String, String>
     * @param longUserPropsMap User properties map, type:
     * @return a merged single Map<String, String>
     */
    private Map<String, String> getUserPropsMap(Map<String, String> strUserPropsMap,
                                                Map<String, Long> longUserPropsMap) {
        //Make sure Map<String, String> of properties is valid
        if (strUserPropsMap == null) {
            LeapAUILogger.debugAUI("strUserPropsMap is null, creating a new map");
            strUserPropsMap = new HashMap<>();
        }

        //Make sure the Map<String, Long> of properties is valid
        //Also convert it to Map<String, String>
        Map<String, String> convertedMap = convertMapLongValueToString(longUserPropsMap);

        if (convertedMap == null || convertedMap.isEmpty()) {
            LeapAUILogger.debugAUI("longUserPropsMap is null or empty");
            return strUserPropsMap;
        }

        //Now Add all the values to a single/common map
        strUserPropsMap.putAll(convertedMap);

        return strUserPropsMap;
    }

    /**
     * Convert the Map<String, Long> to Map<String, String>
     *
     * @param strToLongMap is a map of type Map<String, Long>
     * @return a Map<String, String>
     */
    private Map<String, String> convertMapLongValueToString(Map<String, Long> strToLongMap) {
        if (strToLongMap == null || strToLongMap.isEmpty()) return null;
        Map<String, String> convertedMap = new HashMap<>();
        for (Map.Entry<String, Long> entry : strToLongMap.entrySet()) {
            Long longValue = entry.getValue();
            if (longValue == null) continue;
            convertedMap.put(entry.getKey(), String.valueOf(longValue));
        }
        return convertedMap;
    }

    public void setFlowProjectIds(Set<String> flowProjectIds) {
        this.flowProjectIds = flowProjectIds;
    }
}
