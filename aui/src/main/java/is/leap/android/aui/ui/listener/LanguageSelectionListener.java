package is.leap.android.aui.ui.listener;

public interface LanguageSelectionListener {

    void onLanguageSelected(String locale);

    void onLanguageOptedOut(String dismissReason);
    
}
