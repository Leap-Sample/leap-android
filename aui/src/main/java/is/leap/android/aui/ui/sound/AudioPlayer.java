package is.leap.android.aui.ui.sound;

import android.annotation.SuppressLint;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;

import is.leap.android.aui.LeapAUIInternal;
import is.leap.android.aui.LeapAUILogger;
import is.leap.android.core.util.StringUtils;

import java.io.File;

@SuppressLint("NewApi")
public class AudioPlayer extends Sound implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

    private MediaPlayer mediaPlayer;

    /**
     * Pass the file path url to play the corresponding sound
     *
     * @param soundPath is the path of sound file on the device
     */
    @Override
    public void play(String soundPath) {
        stopPrevious();

        if (StringUtils.isNullOrEmpty(soundPath)) return;
        File file = new File(soundPath);
        if (!file.exists()) return;

        addAudioFocus();
        mediaPlayer = MediaPlayer.create(LeapAUIInternal.getInstance().getContext(), Uri.fromFile(file));
        AudioAttributes audioAttributes = getAudioAttributes();
        if (mediaPlayer == null) {
            LeapAUILogger.errorSound("AudioPlayer : MediaPlayer cannot be initialized, sound might be corrupted, check file : " + soundPath);
            return;
        }
        mediaPlayer.setAudioAttributes(audioAttributes);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        if (soundListener != null) soundListener.onAudioStarted();
        LeapAUILogger.debugSound("AudioPlayer : play() executed");
    }

    private void stopPrevious() {
        if (isPlaying()) {
            stop();
            try {
                if (mediaPlayer != null) mediaPlayer.prepareAsync();
            } catch (IllegalStateException e) {
                //'e' can be null, that's why a hardcoded message is being print...
                LeapAUILogger.errorSound("AudioPlayer : stopPrevious() : IllegalStateException");
            }
        }
    }

    @Override
    public void stop() {
        if (mediaPlayer == null) return;
        removeAudioFocus();
        try {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.reset();
            }
        } catch (IllegalStateException e) {
            //'e' can be null, that's why a hardcoded message is being print...
            LeapAUILogger.errorSound("AudioPlayer : stop() : IllegalStateException");
        }
    }

    @Override
    public void release() {
        if (mediaPlayer != null) {
            removeAudioFocus();
            try {
                mediaPlayer.release();
            } catch (IllegalStateException e) {
                LeapAUILogger.errorSound("AudioPlayer : release() : IllegalStateException : " + e.getMessage());
            }
        }
    }

    @Override
    boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        removeAudioFocus();
        if (soundListener != null) {
            soundListener.onAudioFinished();
            soundListener = null;
        }
    }
}
