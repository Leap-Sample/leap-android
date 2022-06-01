package is.leap.android.aui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import is.leap.android.core.contract.UIContextContract;
import is.leap.android.core.data.LeapCoreCache;

public class AUIControlReceiver extends BroadcastReceiver {

    public static final String ACTION_AUI_CONTROL_RECEIVER = "is.leap.android.aui.AUIControlReceiver";
    public static final String ACTION_KEY = "ACTION";
    public static final String VALUE_KEY = "VALUE";
    public static final String ACTION_STOP = "STOP";
    public static final String ACTION_PREVIEW = "START_PREVIEW";
    public static final String PREVIEW_CONFIGS_KEY = "configs";

    @Override
    public void onReceive(Context context, Intent intent) {
        String message = "AUIControlReceiver : onReceive() : ";
        String intentAction = intent.getAction();
        if (ACTION_AUI_CONTROL_RECEIVER.equals(intentAction)) {
            Bundle extras = intent.getExtras();
            if (extras == null) return;
            String action = extras.getString(ACTION_KEY);
            LeapAUILogger.debugAUI(message + action);
            if ((ACTION_STOP.equals(action))) {
                LeapAUIInternal.getInstance().disable();
                return;
            }
            if ((ACTION_PREVIEW.equals(action))) {
                LeapCoreCache.isPreviewModeON = true;
                String value = extras.getString(VALUE_KEY);
                JSONArray previewConfigArray = getPreviewJSON(value);
                if (previewConfigArray == null || previewConfigArray.length() == 0) {
                    LeapAUILogger.debugAUI("preview config json array empty or null");
                    return;
                }
                LeapAUIInternal.getInstance().reset();
                UIContextContract.UIListener uiListener = LeapAUIInternal.getInstance().getUIListener();
                if (uiListener != null) uiListener.onPreviewConfigReceived(previewConfigArray);
            }
        }
    }

    private JSONArray getPreviewJSON(String value) {
        try {
            JSONObject jsonObject = new JSONObject(value);
            return jsonObject.getJSONArray(PREVIEW_CONFIGS_KEY);
        } catch (JSONException e) {
            LeapAUILogger.debugAUI("Not able to build preview config json array: " + e);
            return null;
        }
    }
}