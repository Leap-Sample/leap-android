package is.leap.android.aui.ui.listener;

public interface AssistAnimationListener {

    void onEntryAnimationStarted();

    void onEntryAnimationFinished();

    void onExitAnimationStarted();

    void onExitAnimationFinished();

    void onIndependentIconAnimationCanStart(String assistType);

}
