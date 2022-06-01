package is.leap.android.aui.ui.sound;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Handler;

import is.leap.android.aui.LeapAUIInternal;
import is.leap.android.aui.LeapAUILogger;

@SuppressLint("NewApi")
public abstract class Sound implements AudioManager.OnAudioFocusChangeListener {

    private final AudioManager audioManager;
    protected SoundListener soundListener;

    Sound() {
        audioManager = (AudioManager) LeapAUIInternal.getInstance().getContext().getSystemService(Context.AUDIO_SERVICE);
    }

    public void setSoundListener(SoundListener soundListener) {
        this.soundListener = soundListener;
    }

    AudioAttributes getAudioAttributes() {
        return new AudioAttributes.Builder()
                .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build();
    }

    public void addAudioFocus() {
        if (audioManager == null) return;

        int audioFocusResult;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            AudioAttributes playbackAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();

            AudioFocusRequest focusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                    .setAudioAttributes(playbackAttributes)
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener(this, new Handler())
                    .build();
            audioFocusResult = audioManager.requestAudioFocus(focusRequest);
        } else {
            audioFocusResult = audioManager.requestAudioFocus(this,
                    // Use the music stream.
                    AudioManager.STREAM_MUSIC,
                    // Request temporary focus.
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
        }

        if (audioFocusResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            LeapAUILogger.debugSound("addAudioFocus() AUDIO_FOCUS_REQUEST_GRANTED");
        } else if (audioFocusResult == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
            LeapAUILogger.debugSound("addAudioFocus() AUDIO_FOCUS_REQUEST_FAILED");
        } else if (audioFocusResult == AudioManager.AUDIOFOCUS_REQUEST_DELAYED) {
            LeapAUILogger.debugSound("addAudioFocus() AUDIO_FOCUS_REQUEST_DELAYED");
        }
    }

    public void removeAudioFocus() {
        if (audioManager == null) return;
        audioManager.abandonAudioFocus(this);
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                LeapAUILogger.debugSound("onAudioFocusChange() : AUDIO_FOCUS_GAIN");
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                LeapAUILogger.debugSound("onAudioFocusChange() : AUDIO_FOCUS_LOSS_TRANSIENT_CAN_DUCK");
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                LeapAUILogger.debugSound("onAudioFocusChange() : AUDIO_FOCUS_LOSS_TRANSIENT");
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                LeapAUILogger.debugSound("onAudioFocusChange() : AUDIO_FOCUS_LOSS");
                break;
        }
    }

    abstract public void stop();

    abstract public void play(String soundName);

    abstract public void release();

    abstract boolean isPlaying();
}
