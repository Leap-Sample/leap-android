package is.leap.android.aui.ui;

import android.app.Activity;

import java.util.List;

import is.leap.android.aui.data.LeapAUICache;
import is.leap.android.aui.ui.listener.LanguageSelectionListener;
import is.leap.android.aui.ui.view.LanguageOptionSheet;
import is.leap.android.core.AppExecutors;
import is.leap.android.core.data.model.AssistInfo;
import is.leap.android.core.data.model.IconSetting;
import is.leap.android.core.data.model.LanguageOption;
import is.leap.android.core.data.model.LeapLanguage;

public class LanguageOptionManager {

    private LanguageOptionSheet languageOptionSheet;
    LanguageSelectionListener languageSelectionListener;
    private final AppExecutors appExecutors;

    public LanguageOptionManager(LanguageSelectionListener languageSelectionListener,
                                 AppExecutors appExecutors) {
        this.languageSelectionListener = languageSelectionListener;
        this.appExecutors = appExecutors;
    }

    public void show(Activity activity, boolean enableIcon, LanguageOption languageOption,
                     List<LeapLanguage> discoveryLangList, String dismissReason) {
        IconSetting iconSetting = new IconSetting(LeapAUICache.iconSettings);
        iconSetting.setEnable(enableIcon);
        languageOptionSheet = new LanguageOptionSheet(activity, discoveryLangList,
                appExecutors.mainThread(), iconSetting, languageSelectionListener,
                languageOption.accessibilityText, dismissReason);
        languageOptionSheet.setAppExecutor(appExecutors);
        AssistInfo assistInfo = AssistInfo.getLanguageOptionAssistInfo(languageOption.htmlUrl, languageOption.accessibilityText);
        languageOptionSheet.setAssistInfo(assistInfo);
        String assistHtmlUrl = AssistInfo.getHtmlUrl(assistInfo.htmlUrl);
        languageOptionSheet.setContent(assistHtmlUrl, languageOption.contentFileUriMap);
        languageOptionSheet.show();
    }

    public void hide() {
        if (languageOptionSheet == null) return;
        languageOptionSheet.hide(false);
    }


}
