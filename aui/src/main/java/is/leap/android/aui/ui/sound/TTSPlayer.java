package is.leap.android.aui.ui.sound;

import android.annotation.SuppressLint;
import android.media.AudioAttributes;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;

import is.leap.android.aui.LeapAUIInternal;
import is.leap.android.aui.data.LeapAUICache;
import is.leap.android.aui.LeapAUILogger;
import is.leap.android.core.data.model.LeapLanguage;
import is.leap.android.core.util.StringUtils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@SuppressLint("NewApi")
public class TTSPlayer extends Sound implements TextToSpeech.OnInitListener {

    private final TextToSpeech textToSpeech;
    private boolean isTTSInit;
    private final Map<String, Boolean> ttsLangDownloadStatusMap = new HashMap<>();

    TTSPlayer() {
        textToSpeech = new TextToSpeech(LeapAUIInternal.getInstance().getContext(), this);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.ERROR || textToSpeech == null) return;
        AudioAttributes audioAttributes = getAudioAttributes();
        textToSpeech.setAudioAttributes(audioAttributes);
        textToSpeech.setOnUtteranceProgressListener(utteranceProgressListener);
        isTTSInit = true;
    }

    @Override
    public void stop() {
        removeAudioFocus();
        if (textToSpeech != null && isPlaying()) {
            textToSpeech.stop();
        }
    }

    @Override
    public void play(String text) {
        if (isPlaying()) {
            stop();
        }
        //Don't omit the utteranceId from the below which is 'text'
        if (textToSpeech != null) {
            addAudioFocus();
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, text);
        }
    }

    @Override
    public void release() {
        if (textToSpeech != null) {
            removeAudioFocus();
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

    @Override
    boolean isPlaying() {
        if (textToSpeech == null) return false;
        try {
            return textToSpeech.isSpeaking();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private boolean isTTSLanguageDownloaded(String ttsLocaleKey, int langAvailableStatus) {
        if (isAvailable(langAvailableStatus) && textToSpeech != null) {
            Voice voice = textToSpeech.getVoice();
            if (voice != null) {
                Set<String> features = voice.getFeatures();
                boolean isInstalled = !features.contains(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED);
                if (isInstalled) ttsLangDownloadStatusMap.put(ttsLocaleKey, true);
                return isInstalled;
            }
            return false;
        }
        ttsLangDownloadStatusMap.put(ttsLocaleKey, false);
        return false;
    }

    private static boolean isAvailable(int status) {
        return status == TextToSpeech.LANG_AVAILABLE || status == TextToSpeech.LANG_COUNTRY_AVAILABLE
                || status == TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE;
    }

    /**
     * Query in the engine's set of languages and checks whether it contains the supplied ttsLocaleId
     */
    private Locale getMatchedLocale(String ttsLocaleId, String ttsLocale) {
        if (StringUtils.isNullOrEmpty(ttsLocale) || textToSpeech == null) return null;
        Set<Locale> localeSet = textToSpeech.getAvailableLanguages();
        if (localeSet == null || localeSet.isEmpty()) return null;

        for (Locale singleLocale : localeSet) {
            String locale = singleLocale.toString();
            if (ttsLocaleId.equals(locale)) {
                String localeId = singleLocale.getLanguage();
                String regionId = singleLocale.getCountry();
                return new Locale(localeId, regionId);
            }
        }
        return new Locale(ttsLocaleId);
    }

    boolean checkLanguageDownloaded(String audioLocale) {
        if (textToSpeech == null || !isTTSInit) return false;

        LeapLanguage language = LeapAUICache.getLanguage(audioLocale);
        if (language == null) return false;

        String ttsLocaleId = language.getTTSLocaleId();

        Boolean isDownloaded = ttsLangDownloadStatusMap.get(ttsLocaleId);
        if (isDownloaded != null && isDownloaded) return true;

        Locale matchedLocale = getMatchedLocale(ttsLocaleId, language.ttsLocale);
        if (matchedLocale == null) {
            ttsLangDownloadStatusMap.put(ttsLocaleId, false);
            return false;
        }

        int langAvailableStatus = textToSpeech.setLanguage(matchedLocale);
        return isTTSLanguageDownloaded(ttsLocaleId, langAvailableStatus);
    }

    private final UtteranceProgressListener utteranceProgressListener = new UtteranceProgressListener() {
        @Override
        public void onStart(String utteranceId) {
            LeapAUILogger.debugSound("TTS started for utteranceId: " + utteranceId);
            if (soundListener != null) soundListener.onAudioStarted();
        }

        @Override
        public void onDone(String utteranceId) {
            LeapAUILogger.debugSound("TTS done for utteranceId : " + utteranceId);
            if (soundListener != null) soundListener.onAudioFinished();
        }

        @Override
        public void onError(String utteranceId) {
            LeapAUILogger.errorSound("TTS Error while playing sound, it's utteranceId : " + utteranceId);
            if (soundListener != null) soundListener.onAudioFinished();
        }

        @Override
        public void onError(String utteranceId, int errorCode) {
            super.onError(utteranceId, errorCode);
            LeapAUILogger.errorSound("TTS Error while playing sound, it's utteranceId : " + utteranceId);
            LeapAUILogger.errorSound("TTS Error code : " + errorCode);
        }
    };
}