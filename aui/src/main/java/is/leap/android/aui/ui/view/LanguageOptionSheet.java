package is.leap.android.aui.ui.view;

import android.animation.Animator;
import android.app.Activity;

import is.leap.android.aui.ui.assist.listener.AnimatorEndListener;
import is.leap.android.aui.ui.assist.output.BottomUp;
import is.leap.android.aui.ui.listener.LanguageSelectionListener;
import is.leap.android.aui.util.AppUtils;
import is.leap.android.core.AppExecutors;
import is.leap.android.core.data.LeapCoreCache;
import is.leap.android.core.data.model.DismissAction;
import is.leap.android.core.data.model.IconSetting;
import is.leap.android.core.data.model.LeapLanguage;
import is.leap.android.core.data.model.WebContentAction;
import is.leap.android.core.util.StringUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import static is.leap.android.core.data.model.LeapLanguage.getLanguageJSONArray;

public class LanguageOptionSheet extends BottomUp {

    private static final int LANGUAGE_POPUP_SIZE = 52;
    private static final String JS_SET_SELECTED_LOCALE = "javascript:setSelectedLocale(\"";
    private static final String JS_ENDING_BRACES = "\");";
    private final List<LeapLanguage> languagesByLocale;
    private final IconSetting iconSetting;
    private final LanguageSelectionListener languageSelectionListener;
    private final AppExecutors.ThreadHandler mainThread;
    private final String dismissReason;

    public LanguageOptionSheet(Activity activity, List<LeapLanguage> languagesByLocale,
                               AppExecutors.ThreadHandler mainThread, IconSetting iconSetting,
                               LanguageSelectionListener languageSelectionListener,
                               String accessibilityText, String dismissReason) {
        super(activity, accessibilityText);
        this.dismissReason = dismissReason;
        this.languagesByLocale = languagesByLocale;
        this.iconSetting = iconSetting;
        this.languageSelectionListener = languageSelectionListener;
        this.mainThread = mainThread;
        setIconSetting(iconSetting);
        int languagePopupIconSize = AppUtils.dpToPxInt(activity, LANGUAGE_POPUP_SIZE);
        updateIconSize(languagePopupIconSize, languagePopupIconSize);
    }

    @Override
    public void setUpLayoutAction(DismissAction dismissAction) {
        // Do nothing so that outside dismiss is disabled
    }

    @Override
    public String getLanguages() {
        JSONArray array = getLanguageJSONArray(languagesByLocale);
        if (array == null) return null;
        return array.toString();
    }

    @Override
    public String getLanguageOptionThemeColor() {
        return iconSetting.bgColor;
    }

    @Override
    public void onWebActionPerformed(final String actionType, final Object value) {
        mainThread.post(new Runnable() {
            @Override
            public void run() {
                performExitAnimation(new AnimatorEndListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        WebContentAction contentAction = null;
                        if (value != null) {
                            contentAction = WebContentAction.build((JSONObject) value);
                            if (contentAction != null && contentAction.isDismissed) hide(false);
                        }

                        if (contentAction != null && contentAction.isLanguageSelected() && contentAction.localeCode != null) {
                            languageSelectionListener.onLanguageSelected(contentAction.localeCode);
                            return;
                        }

                        if (contentAction != null && !contentAction.isOptIn)
                            languageSelectionListener.onLanguageOptedOut(dismissReason);
                    }
                });
            }
        });
    }

    @Override
    public void renderComplete(int pageWidth, int pageHeight) {
        super.renderComplete(pageWidth, pageHeight);
        setSelectedLocale(LeapCoreCache.audioLocale);
    }

    public void setSelectedLocale(final String audioLocale) {
        if (StringUtils.isNullOrEmpty(audioLocale)) return;
        appExecutors.mainThread().post(new Runnable() {
            @Override
            public void run() {
                leapWebView.evaluateJavascript(JS_SET_SELECTED_LOCALE + audioLocale + JS_ENDING_BRACES, null);
            }
        });
    }
}
