package is.leap.android.sample.listeners;

import org.json.JSONObject;

public interface ValidationListener {
    void onSuccessfulValidation(JSONObject config);
    void onFailedValidation();
}
