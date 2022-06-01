package is.leap.android.aui.ui.view;

public interface LeapHtmlContentInteractionListener {

    void renderComplete(int pageWidth, int pageHeight);

    void onResize(int pageWidth, int pageHeight);

    void onWebActionPerformed(String action_type, Object value);

    String getLanguages();

    String getLanguageOptionThemeColor();

    String getAudioLocale();

    /**
     * To check whether to ceil the width and height callback value.
     * Required only for tooltip type wrap.
     * If ceil is not applied, tooltip content comes down in a new line which is not desired.
     * If ceil is applied to all AUI, AUI content resizes which cause performance issue in animation
     * @return boolean
     */
    boolean shouldCeilWidthAndHeightValue();

    String getPersonalizedTags();

    String getFlowList();
}
