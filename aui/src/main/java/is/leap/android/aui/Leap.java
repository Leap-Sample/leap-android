package is.leap.android.aui;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.webkit.WebView;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import is.leap.android.LeapEventCallbacks;
import is.leap.android.aui.util.AppUtils;
import is.leap.android.core.LeapElementActionCallbacks;
import is.leap.android.core.data.LeapCoreCache;
import is.leap.android.core.data.model.ProjectProps;

public final class Leap {

    private static LeapAUIInternal leapAUIInternal;

    static void init(Application application) {
        if (isInValidInit(application)) return;
        initialise(application);
    }

    public static void start(String apiKey, Map<String, Object> properties) {
        if (isSDKNotInitialised() || isInvalidAPIKey(apiKey)) return;
        ProjectPropsBuilder projectPropsBuilder = new ProjectPropsBuilder(null)
                .setResetProject(false);
        ProjectProps projectProps = projectPropsBuilder.build();
        leapAUIInternal.start(apiKey, properties,projectProps);
    }

    public static void startProject(String projectID) {
        if (isSDKNotInitialised() || isInvalidAPIKey(LeapCoreCache.apiKey)) return;
        ProjectPropsBuilder projectPropsBuilder = new ProjectPropsBuilder(projectID)
                .setResetProject(false);
        ProjectProps projectProps = projectPropsBuilder.build();
        leapAUIInternal.start(LeapCoreCache.apiKey, null, projectProps);
    }

    public static void embedProject(String projectID) {
        if (isSDKNotInitialised() || isInvalidAPIKey(LeapCoreCache.apiKey)) return;
        ProjectPropsBuilder projectPropsBuilder = new ProjectPropsBuilder(projectID)
                .setResetProject(true)
                .setEmbed(true);
        ProjectProps projectProps = projectPropsBuilder.build();
        leapAUIInternal.start(LeapCoreCache.apiKey, null, projectProps);
    }

    /**
     *
     * @param projectID the Project ID
     * @param resetProject Whether to reset the past user experience cache of this project
     */
    public static void startProject(String projectID, boolean resetProject) {
        if (isSDKNotInitialised() || isInvalidAPIKey(LeapCoreCache.apiKey)) return;
        ProjectPropsBuilder projectPropsBuilder = new ProjectPropsBuilder(projectID)
                .setResetProject(resetProject);
        ProjectProps projectProps = projectPropsBuilder.build();
        leapAUIInternal.start(LeapCoreCache.apiKey, null, projectProps);
    }


    public static void start(String apiKey) {
        if (isSDKNotInitialised() || isInvalidAPIKey(apiKey)) return;
        leapAUIInternal.start(apiKey);
    }

    private static void flush(Map<String, Object> properties) {
        if (isSDKNotInitialised()) return;
        if (properties == null || properties.isEmpty()) {
            LeapAUILogger.debugAUI("Nothing to flush, returning ");
            return;
        }
        leapAUIInternal.flushData(properties);
    }

    private static void initialise(Application application) {
        if (leapAUIInternal == null) {
            leapAUIInternal = new LeapAUIInternal(application, null);
            return;
        }
        LeapAUILogger.debugAUI("LeapAUI SDK already initialised ");
    }

    public static void setLeapEventCallbacks(LeapEventCallbacks eventCallbacks) {
        if (isSDKNotInitialised()) return;
        leapAUIInternal.setLeapEventCallbacks(eventCallbacks);
    }

    public static void enableWeb(WebView webView) {
        if (isSDKNotInitialised() || webView == null) return;
        leapAUIInternal.enableWeb(webView);
    }

    public static void updateWebViewScale(float newScale) {
        if (isSDKNotInitialised() || newScale == 0.0f) return;
        leapAUIInternal.updateWebViewScale(newScale);
    }

    private static void logout() {
        if (isSDKNotInitialised()) return;
        leapAUIInternal.logout();
    }

    /**
     * 1. Stops Context detection and resets all UI
     * 2. Resets cache
     */
    public static void disable() {
        if (isSDKNotInitialised() || !LeapCoreCache.isLeapEnabled) return;
        LeapAUILogger.debugAUI("disable() called by client ");
        leapAUIInternal.disable();
    }

    private static boolean isInvalidAPIKey(String apiKey) {
        if (apiKey == null || apiKey.isEmpty()) {
            Log.d(LeapAUILogger.TAG, "Start() aborted. INVALID API Key. API Key cannot be empty or null");
            return true;
        }
        return false;
    }

    public static void addLeapElementActionCallbacks(LeapElementActionCallbacks leapElementActionCallbacks) {
        if (isSDKNotInitialised()) return;
        leapAUIInternal.addElementActionCallbacks(leapElementActionCallbacks);
    }

    private static boolean isSDKNotInitialised() {
        if (leapAUIInternal == null) {
            Log.d(LeapAUILogger.TAG, "LeapAUI is not initialised");
            return true;
        }
        return false;
    }

    private static boolean isInValidInit(Context context) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
            Log.d(LeapAUILogger.TAG, "Initialisation aborted. LeapAUI doesn't work on api below 21");
            return true;
        }
        if (AppUtils.isTablet(context)) {
            Log.d(LeapAUILogger.TAG, "Initialisation aborted. LeapAUI doesn't work on Tablets");
            return true;
        }
        return false;
    }

    public static Builder withBuilder(String apiKey) {
        return new Builder(apiKey);
    }

    public static PropertyBuilder withPropertyBuilder() {
        return new PropertyBuilder();
    }

    public static class Builder {
        private final String apiKey;
        private final Map<String, Object> properties = new HashMap<>();

        public Builder(String apiKey) {
            this.apiKey = apiKey;
        }

        /**
         * This builder method is explicitly to be used for passing String values
         * e.g. LeapAUI.addProperty("user_name", "Baba").start()
         *
         * @param key   can be a {@link String}
         * @param value can be a {@link String}
         * @return {@link Builder}
         */
        public Builder addProperty(String key, String value) {
            properties.put(key, value);
            return this;
        }

        /**
         * This builder method is explicitly used for passing Integral values
         * e.g. LeapAUI.addProperty("user_age", 21).start()
         *
         * @param key   can be a {@link String}
         * @param value can be a {@link Long}
         * @return {@link Builder}
         */
        public Builder addProperty(String key, long value) {
            properties.put(key, value);
            return this;
        }

        /**
         * This builder method is explicitly used for passing TIMESTAMP values in DATE format
         * e.g. LeapAUI.addProperty("cart_element_added", new Date()) .start()
         *
         * @param key  can be a {@link String}
         * @param date can be a {@link Date}, new Date() -> current date
         * @return {@link Builder}
         */
        public Builder addProperty(String key, Date date) {
            properties.put(key, date);
            return this;
        }

        public void start() {
            if (isSDKNotInitialised()) return;
            Leap.start(apiKey, properties);
        }
    }

    public static class PropertyBuilder {
        Map<String, Object> props = new HashMap<>();

        /**
         * This builder method is explicitly to be used for passing String values
         * e.g. LeapAUI.addProperty("user_name", "Baba").flush()
         *
         * @param key   can be a {@link String}
         * @param value can be a {@link String}
         * @return {@link Builder}
         */
        public PropertyBuilder addProperty(String key, String value) {
            props.put(key, value);
            return this;
        }

        /**
         * This builder method is explicitly used for passing Integral and long values
         * e.g. LeapAUI.addProperty("user_age", 21).flush()
         *
         * @param key   can be a {@link String}
         * @param value can be a {@link Long}
         * @return {@link Builder}
         */
        public PropertyBuilder addProperty(String key, long value) {
            props.put(key, value);
            return this;
        }

        /**
         * This builder method is explicitly used for passing TIMESTAMP values in DATE format
         * e.g. LeapAUI.addProperty("cart_element_added", new Date()).flush()
         *
         * @param key  can be a {@link String}
         * @param date can be a {@link Date}, new Date() -> current date
         * @return {@link Builder}
         */
        public PropertyBuilder addProperty(String key, Date date) {
            props.put(key, date);
            return this;
        }

        public void flush() {
            Leap.flush(props);
        }
    }

    private static class ProjectPropsBuilder {

        private final ProjectProps projectProps;

        public ProjectPropsBuilder(String projectID) {
            projectProps = new ProjectProps(projectID);
        }

        public ProjectPropsBuilder setResetProject(boolean reset) {
            projectProps.setResetProject(reset);
            return this;
        }

        public ProjectPropsBuilder setEmbed(boolean embed) {
            projectProps.setEmbed(embed);
            return this;
        }

        public ProjectProps build() {
            return projectProps;
        }
    }
}
