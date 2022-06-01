package is.leap.android.aui.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.FrameLayout;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import is.leap.android.aui.AUIConstants;
import is.leap.android.aui.data.LeapAUICache;
import is.leap.android.aui.ui.view.LeapIconLoader;
import is.leap.android.aui.ui.view.LeapWebView;
import is.leap.android.aui.ui.view.shapes.RoundedCornerView;
import is.leap.android.aui.util.AppUtils;
import is.leap.android.core.data.model.AssistInfo;
import is.leap.android.core.util.StringUtils;

public class LeapIcon extends FrameLayout implements LeapCustomViewGroup {

    private static final int DEF_CORNER_RADIUS = 18;
    public static final int ICON_ELEVATION = 6;
    public static final int INDEPENDENT_ICON_SIZE = 52;
    public static final int ASSOCIATE_ICON_SIZE = 36;
    private RoundedCornerView iconRoot;
    private FrameLayout iconContentWrapper;
    private LeapWebView iconWebview;
    private LeapWebView iconAudioWebview;
    private String htmlUrl;
    private LeapIconLoader iconProgress;
    private OnTouchListener onTouchListener;
    private View delegateOnTouchView;
    private Context context;
    private int iconSize;

    public static LeapIcon getAssociatedLeapIcon(Context context) {
        return getLeapIcon(context, ASSOCIATE_ICON_SIZE);
    }

    public static LeapIcon getIndependentLeapIcon(Context context) {
        return getLeapIcon(context, INDEPENDENT_ICON_SIZE);
    }

    private static LeapIcon getLeapIcon(Context context, int independentIconSize) {
        LeapIcon leapIcon = new LeapIcon(context);
        int cornerRadius = AppUtils.dpToPxInt(context, independentIconSize / 2f);
        leapIcon.setCornerRadius(cornerRadius);
        leapIcon.updateSize(independentIconSize, independentIconSize);
        return leapIcon;
    }

    public LeapIcon(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        setupView(context);
        int cornerRadius = AppUtils.dpToPxInt(context, DEF_CORNER_RADIUS);
        addView(iconRoot);
        handleTouchListener();
        iconRoot.setCornerRadius(cornerRadius);
    }

    @Override
    public void setupView(Context context) {
        iconRoot = new RoundedCornerView(context);

        iconContentWrapper = new FrameLayout(context);
        iconRoot.addView(iconContentWrapper);

        iconWebview = new LeapWebView(context);
        iconAudioWebview = new LeapWebView(context);
        iconProgress = new LeapIconLoader(context);
        iconProgress.setVisibility(GONE);

        iconContentWrapper.addView(iconWebview);
        iconContentWrapper.addView(iconAudioWebview);
        iconContentWrapper.addView(iconProgress);
    }

    public void updateSize(int widthInDp, int heightInDp) {
        int width = AppUtils.dpToPxInt(context, widthInDp);
        int height = AppUtils.dpToPxInt(context, heightInDp);
        this.iconSize = width;
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        if (layoutParams == null) {
            layoutParams = new ViewGroup.LayoutParams(width, height);
        } else {
            layoutParams.width = width;
            layoutParams.height = height;
        }
        setLayoutParams(layoutParams);
    }

    public int getIconSize() {
        return iconSize;
    }

    void addElevation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            iconRoot.setElevation(AppUtils.dpToPxInt(getContext(), ICON_ELEVATION));
        }
    }

    public void setCornerRadius(int radius) {
        iconRoot.setCornerRadius(radius);
    }

    // Delegates the OnTouchListener to the provided view
    public void setDelegateOnTouchListenerView(View delegateOnTouchView) {
        this.delegateOnTouchView = delegateOnTouchView;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void handleTouchListener() {
        OnTouchListener webViewTouchListener = new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (delegateOnTouchView == null)
                    delegateOnTouchView = LeapIcon.this;
                if (onTouchListener != null) onTouchListener.onTouch(delegateOnTouchView, event);
                return true;
            }
        };
        iconWebview.setOnTouchListener(webViewTouchListener);
        iconAudioWebview.setOnTouchListener(webViewTouchListener);
    }

    public void setTouchListener(OnTouchListener onTouchListener) {
        this.onTouchListener = onTouchListener;
    }

    public void setProps(String bgColor, String htmlUrl, List<String> contentUrls) {
        this.htmlUrl = htmlUrl;
        setBgColor(bgColor);
        loadIconContent();
    }

    public void hide() {
        if (getVisibility() != VISIBLE) return;
        setVisibility(INVISIBLE);
    }

    public void show() {
        if (getVisibility() == VISIBLE) return;
        setVisibility(VISIBLE);
    }

    void loadIconContent() {
        final Map<String, String> contentFileUriMap = new HashMap<>();
        contentFileUriMap.put(htmlUrl, null);
        String assistHtmlUrl = AssistInfo.getHtmlUrl(htmlUrl);
        if (LeapAUICache.iconSettings == null || LeapAUICache.iconSettings.contentFileUriMap.isEmpty())
            iconWebview.loadUrl(assistHtmlUrl, contentFileUriMap);
        else
            iconWebview.loadUrl(assistHtmlUrl, LeapAUICache.iconSettings.contentFileUriMap);
    }

    void showIconView() {
        if (iconWebview.getVisibility() == View.VISIBLE) return;
        iconWebview.setVisibility(View.VISIBLE);
    }

    void hideIconView() {
        if (iconWebview.getVisibility() != View.VISIBLE) return;
        iconWebview.setVisibility(View.GONE);
    }

    void loadAudioContent() {
        iconAudioWebview.loadUrl(AUIConstants.audioAnimPath, null);
    }

    void showAudioView() {
        if (iconAudioWebview.getVisibility() == View.VISIBLE) return;
        iconAudioWebview.setVisibility(View.VISIBLE);
    }

    void hideAudioView() {
        if (iconAudioWebview.getVisibility() != View.VISIBLE) return;
        iconAudioWebview.setVisibility(View.GONE);
    }

    private void setBgColor(String bgColor) {
        // make webview transparent for icon
        iconWebview.setBackgroundColor(Color.TRANSPARENT);
        iconWebview.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);

        // make webview transparent for Audio
        iconAudioWebview.setBackgroundColor(Color.TRANSPARENT);
        iconAudioWebview.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);

        if (StringUtils.isNotNullAndNotEmpty(bgColor))
            iconContentWrapper.setBackgroundColor(Color.parseColor(bgColor));
    }

    void hideProgress() {
        if (iconProgress.getVisibility() != View.VISIBLE) return;
        iconProgress.setVisibility(View.GONE);
    }

    void showProgress() {
        if (iconProgress.getVisibility() == View.VISIBLE) return;
        iconProgress.setVisibility(View.VISIBLE);
    }

}
