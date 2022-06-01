package is.leap.android.aui.data;

import android.content.Context;
import android.util.Log;
import android.util.SparseBooleanArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import is.leap.android.LeapSharedPref;
import is.leap.android.aui.BuildConfig;
import is.leap.android.aui.util.AppUtils;
import is.leap.android.core.data.LeapCoreCache;
import is.leap.android.core.data.model.IconSetting;
import is.leap.android.core.data.model.LeapLanguage;
import is.leap.android.core.data.model.SoundInfo;

public class LeapAUICache {

    public static SparseBooleanArray isMutedMap = new SparseBooleanArray();
    public static SparseBooleanArray flowDiscoveredAtLeastOnceMap = new SparseBooleanArray();
    public static SparseBooleanArray flowDiscoveredMapInSession = new SparseBooleanArray();
    public static SparseBooleanArray discoveryInteractedMap = new SparseBooleanArray();
    private static LeapAUICache instance = new LeapAUICache();
    public static List<LeapLanguage> leapLanguageList = new ArrayList<>();
    public static IconSetting iconSettings;
    public static boolean isInDiscovery = true;


    //Window dimensions
    public static int statusBarHeight;
    public static int softButtonBarHeight;
    public static int screenWidth;
    public static int screenHeight;
    private LeapSharedPref leapSharedPref;
    private static Map<String, String> defaultAccessibilityTextMap;

    private LeapAUICache() {
        if (instance != null) {
            Log.d("LeapAUI", "Already Initialised");
            return;
        }
        instance = this;
        initLeapAUICache();
    }

    public static void setLanguageSelected() {
        LeapCoreCache.isLanguageNotSelected = false;
        instance.leapSharedPref.setLanguageSelected();
    }

    public static List<LeapLanguage> getLanguagesByLocale(List<String> localeCodes) {
        List<LeapLanguage> languageList = leapLanguageList;
        if (languageList == null) return null;
        if (localeCodes == null) return languageList;
        List<LeapLanguage> languages = new ArrayList<>();
        for (String locale : localeCodes) {
            for (LeapLanguage language : languageList) {
                if (locale.equals(language.locale))
                    languages.add(language);
            }
        }
        return languages;
    }

    public static boolean isDiscoveryInteracted(int id) {
        return discoveryInteractedMap.get(id);
    }

    public static void setIsInDiscovery(boolean isInDiscovery) {
        LeapAUICache.isInDiscovery = isInDiscovery;
    }

    private void initLeapAUICache() {
        LeapCoreCache.auiSdkVersion = BuildConfig.VERSION_NAME;
        leapSharedPref = LeapSharedPref.getInstance();
        LeapCoreCache.isLanguageNotSelected = leapSharedPref.isLanguageNotSelected();
        isMutedMap = leapSharedPref.getMutedMap();
        flowDiscoveredAtLeastOnceMap = leapSharedPref.getFlowDiscoveredMap();
    }

    public static LeapAUICache getInstance() {
        return instance;
    }

    /**
     * Scenarios considered:
     * 1.Is disabled i.e. marked as true when feature is disabled
     * 2.Is disabled i.e. marked as true when disable is called via Panel
     * 3.It is toggled when the client want to show/hide the Leap Asst.
     */
    public static void setInDiscovery() {
        isInDiscovery = true;
    }

    public static void onInit(List<LeapLanguage> leapLanguageList,
                              Map<String, String> accessibilityTextMap) {
        defaultAccessibilityTextMap = accessibilityTextMap;
        LeapAUICache.leapLanguageList = leapLanguageList;
    }

    public static String getAccessibilityText(String key) {
        if (defaultAccessibilityTextMap == null || defaultAccessibilityTextMap.isEmpty())
            return null;
        return defaultAccessibilityTextMap.get(key);
    }

    public static LeapLanguage getLanguage(String audioLocale) {
        if (audioLocale == null) return null;
        if (leapLanguageList == null || leapLanguageList.isEmpty()) return null;
        for (LeapLanguage leapLanguage : leapLanguageList) {
            if (audioLocale.equals(leapLanguage.locale)) return leapLanguage;
        }
        return null;
    }

    public static class TriggerHistory {

        public static boolean isDismissedByUser(int discoveryID) {
            return LeapCoreCache.isDiscoveryDismissedByUserOnce(discoveryID);
        }

        public static boolean isFlowCompletedOnce(int discoveryID) {
            return LeapCoreCache.isFlowCompletedOnce(discoveryID);
        }
    }

    public static boolean isFlowMenuCompleted(Set<String> flowProjectIds) {
        return LeapCoreCache.isFlowMenuCompleted(flowProjectIds);
    }

    public static boolean isMuted(Integer discoveryId) {
        return isMutedMap.get(discoveryId);
    }

    public static void setMutedLocally(Integer discoveryId, boolean isMuted) {
        isMutedMap.put(discoveryId, isMuted);
    }

    public static void setMuted(Integer discoveryId, boolean isMuted) {
        setMutedLocally(discoveryId, isMuted);
        instance.leapSharedPref.setMuted(discoveryId, isMuted);
    }

    public static void setFlowDiscovered(Integer discoveryId) {
        flowDiscoveredMapInSession.put(discoveryId, true);
        flowDiscoveredAtLeastOnceMap.put(discoveryId, true);
        instance.leapSharedPref.setFlowDiscovered(discoveryId);
    }

    public static boolean isFlowDiscoveredInSession(Integer discoveryId) {
        return flowDiscoveredMapInSession.get(discoveryId);
    }

    public static boolean isFlowDiscovered(Integer discoveryId) {
        return flowDiscoveredAtLeastOnceMap.get(discoveryId);
    }

    /**
     * Map is required whenever we want to download bulk sounds
     *
     * @param locale is the required locale
     */
    public static Map<String, List<SoundInfo>> getSoundMapByLocale(String locale) {
        if (locale == null || locale.isEmpty()) return null;
        Map<String, List<SoundInfo>> soundsMap = LeapCoreCache.soundsMap;
        if (soundsMap == null || soundsMap.isEmpty()) return null;
        List<SoundInfo> soundInfoList = soundsMap.get(locale);
        Map<String, List<SoundInfo>> contextSoundMapByLocale = new HashMap<>();
        contextSoundMapByLocale.put(locale, soundInfoList);
        return contextSoundMapByLocale;
    }

    // Recalculation of statusBarHeight happens in onActivityResume but sometimes the value is 0.
    // To make sure the right value is updated recalculation is needed
    public static int getStatusBarHeight(Context context) {
        int statusBarHeight = LeapAUICache.statusBarHeight;
        if (statusBarHeight <= 0) {
            statusBarHeight = AppUtils.getStatusBarHeight(context);
            LeapAUICache.statusBarHeight = statusBarHeight;
        }
        return statusBarHeight;
    }

    // Recalculation of softButtonBarHeight happens in onActivityResume but sometimes the value is 0.
    // To make sure the right value is updated recalculation is needed
    public static int getSoftButtonBarHeight(Context context) {
        int softButtonBarHeight = LeapAUICache.softButtonBarHeight;
        if (softButtonBarHeight <= 0) {
            softButtonBarHeight = AppUtils.getSoftButtonsBarHeight(context);
            LeapAUICache.softButtonBarHeight = softButtonBarHeight;
        }
        return softButtonBarHeight;
    }

    public static void reset() {
        clear();

        //Reset Window dimensions
        statusBarHeight = -1;
        softButtonBarHeight = -1;
        screenWidth = -1;
        screenHeight = -1;
    }

    public static void setDiscoveryToNotInteracted(int id) {
        discoveryInteractedMap.put(id, false);
    }

    private static void clear() {
        if (leapLanguageList != null) leapLanguageList.clear();
        iconSettings = null;
        isInDiscovery = true;
    }

    public static void clearCachedMaps() {
        isMutedMap.clear();
        flowDiscoveredAtLeastOnceMap.clear();
        flowDiscoveredMapInSession.clear();
    }

    public static void resetPastUserExpForProject(int discoveryID) {
        if (discoveryID == -1) return;
        isMutedMap.delete(discoveryID);
        flowDiscoveredAtLeastOnceMap.delete(discoveryID);
        flowDiscoveredMapInSession.delete(discoveryID);
    }
}