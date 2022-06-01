package is.leap.android.aui.ui;

import is.leap.android.aui.LeapAUILogger;
import is.leap.android.core.AppExecutors;

public class TriggerDelayController {

    private static final String TAG = TriggerDelayController.class.getSimpleName();
    private DelayRunnable delayRunnable;
    private final AppExecutors.ThreadHandler bgThreadHandler;
    private final DelayListener delayListener;
    private long timeTakenToDownload;
    private long delay;

    public TriggerDelayController(DelayListener delayListener, AppExecutors.ThreadHandler bgThreadHandler) {
        this.delayListener = delayListener;
        this.bgThreadHandler = bgThreadHandler;
    }

    public void setTimeTakenToDownload(long timeTakenToDownload) {
        this.timeTakenToDownload = timeTakenToDownload;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public void executeDelay() {
        delayRunnable = new DelayRunnable(delayListener);

        //Calculate effective delay : timeTakenToDownload - delay
        LeapAUILogger.debugAUI(TAG + " timeTakenToDownload : " + timeTakenToDownload);
        LeapAUILogger.debugAUI(TAG + " current delay : " + delay);
        long effectiveDelay = getEffectiveDelay(delay);
        LeapAUILogger.debugAUI(TAG + " effectiveDelay : " + effectiveDelay);
        if (effectiveDelay <= 0 && delayListener != null) {
            delayListener.onDelayComplete();
            return;
        }
        bgThreadHandler.setDelay(effectiveDelay)
                .executeDelayed(delayRunnable);
        resetDelay();
    }

    /**
     * This methods decrements the download time taken from the trigger delay
     *
     * @param delay is the the trigger delay
     */
    private long getEffectiveDelay(long delay) {
        if (timeTakenToDownload == 0) return delay;
        else if (timeTakenToDownload >= delay) return 0;
        return delay - timeTakenToDownload;
    }

    private void removeDelayedThread() {
        if (bgThreadHandler == null || delayRunnable == null) return;
        bgThreadHandler.removeCallbacks(delayRunnable);
    }

    public void reset() {
        resetDelay();
        removeDelayedThread();
    }

    private void resetDelay() {
        timeTakenToDownload = 0;
        delay = 0;
    }

    public long getDelay() {
        return delay;
    }

    public static class DelayRunnable implements Runnable {

        private final DelayListener delayListener;

        public DelayRunnable(DelayListener delayListener) {
            this.delayListener = delayListener;
        }

        @Override
        public void run() {
            if (delayListener != null) {
                delayListener.onDelayComplete();
            }
        }
    }

    public interface DelayListener {

        void onDelayComplete();
    }

}
