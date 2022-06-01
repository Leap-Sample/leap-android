package is.leap.android.aui.ui.sound;

import androidx.core.util.Pair;

import is.leap.android.aui.LeapAUILogger;
import is.leap.android.core.data.model.SoundInfo;
import is.leap.android.core.util.StringUtils;

public class SoundManager {

    private final AudioPlayer audioPlayer;
    private final TTSPlayer ttsPlayer;
    private final SoundListener listener;
    private String previousSound;
    private Pair<String, Boolean> soundCompletedPair;

    public SoundManager(SoundListener listener) {
        audioPlayer = new AudioPlayer();
        ttsPlayer = new TTSPlayer();
        this.listener = listener;
    }

    private void initListeners() {
        audioPlayer.setSoundListener(listener);
        ttsPlayer.setSoundListener(listener);
    }

    private boolean isSoundCompleted(String soundName) {
        return soundCompletedPair != null && (soundName != null && soundName.equals(soundCompletedPair.first)
                && soundCompletedPair.second != null && soundCompletedPair.second);
    }

    public void play(SoundInfo soundInfo, String audioLocale) {
        if (soundInfo == null) return;
        LeapAUILogger.debugSound("play() executed for : " + soundInfo.toString());
        String soundName = soundInfo.name;
        if (isPlaying(soundInfo)) {
            if (soundName != null && soundName.equals(previousSound)) return;
            stop();
        }
        initListeners();
        if (shouldUseTTS(soundInfo, audioLocale)) {
            playTTS(soundName, soundInfo.text);
            return;
        }
        playAudio(soundName, soundInfo.fileUriPath);
    }

    public void stop() {
        audioPlayer.stop();
        ttsPlayer.stop();
    }

    private void playAudio(String soundName, String fileUri) {
        if (StringUtils.isNullOrEmpty(fileUri)) return;
        if (previousSound != null && previousSound.equals(soundName)) {
            if (isSoundCompleted(soundName)) return;
        } else {
            resetSoundCompletedPair();
        }
        previousSound = soundName;
        audioPlayer.play(fileUri);
    }

    private void resetSoundCompletedPair() {
        soundCompletedPair = null;
    }

    private void playTTS(String soundName, String text) {
        if (StringUtils.isNullOrEmpty(text)) return;
        if (previousSound != null && previousSound.equals(soundName)) {
            if (isSoundCompleted(soundName)) return;
        } else {
            resetSoundCompletedPair();
        }
        previousSound = soundName;
        ttsPlayer.play(text);
    }

    private boolean shouldUseTTS(SoundInfo soundInfo, String audioLocale) {
        return soundInfo.isTTSEnabled && !StringUtils.isNullOrEmpty(soundInfo.text)
                && ttsPlayer.checkLanguageDownloaded(audioLocale);
    }

    private boolean isPlaying(SoundInfo soundInfo) {
        if (soundInfo.isTTSEnabled) return ttsPlayer.isPlaying();
        return audioPlayer.isPlaying();
    }

    /**
     * Required in case of opt out and when there is new instruction
     */
    public void resetPreviousSound() {
        previousSound = null;
        resetSoundCompletedPair();
    }

    public void setSoundCompleted() {
        soundCompletedPair = new Pair<>(previousSound, true);
    }
}
