package is.leap.android.aui.ui.listener;

import android.app.Activity;
import android.view.View;
import android.webkit.WebView;

/**
 * An Interface for fetching the rootView, webView and current Activity.
 * We can maintain these views at only at one place to make sure there is no memory leak
 */
public interface IUIChangeHolder {

    View getRootView();

    WebView getWebView();

    Activity getCurrentActivity();

}
