package is.leap.android.aui.ui.view;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import is.leap.android.aui.AUIConstants;
import is.leap.android.aui.Leap;
import is.leap.android.aui.ui.assist.output.WebContentAssist;
import is.leap.android.aui.LeapAUILogger;
import is.leap.android.core.Constants;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class LeapWebView extends WebView {

    private boolean shouldUpdateWidth = true;
    private boolean shouldUpdateHeight = true;

    public LeapWebView(Context context) {
        super(context);
        init();
    }

    private void init() {
        setOverScrollMode(View.OVER_SCROLL_NEVER);
        setVerticalScrollBarEnabled(false);
        setHorizontalScrollBarEnabled(false);
        setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                if (consoleMessage == null) return true;
                LeapAUILogger.debugAUI(consoleMessage.message() + " -- From line " +
                        consoleMessage.lineNumber() + " of " + consoleMessage.sourceId());
                return true;
            }
        });
    }

    // The method overrides below; overScrollBy, scrollTo, and computeScroll prevent page scrolling
    @Override
    public boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY,
                                int scrollRangeX, int scrollRangeY, int maxOverScrollX,
                                int maxOverScrollY, boolean isTouchEvent) {
        return false;
    }

    @Override
    public void scrollTo(int x, int y) {
        // Do nothing
    }

    @Override
    public void computeScroll() {
        // Do nothing
    }

    public void shouldUpdateWidth(boolean shouldUpdateWidth) {
        this.shouldUpdateWidth = shouldUpdateWidth;
    }

    public void shouldUpdateHeight(boolean shouldUpdateHeight) {
        this.shouldUpdateHeight = shouldUpdateHeight;
    }

    public void updateLayout(int pageWidth, int pageHeight) {
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        if (shouldUpdateHeight && pageHeight > 0) layoutParams.height = pageHeight;
        if (shouldUpdateWidth && pageWidth > 0) layoutParams.width = pageWidth;
        setLayoutParams(layoutParams);
    }

    private static WebResourceResponse getWebResponseFromFile(String fileExt, String filePath,
                                                              String mimeType, String encoding) {
        if (filePath == null || filePath.isEmpty()) return null;
        File file = new File(filePath);
        if (!file.exists()) return null;
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            if (AUIConstants.WebContentAssist.FILE_TYPE_GZ.equals(fileExt)) {
                GZIPInputStream gzipInputStream = new GZIPInputStream(fileInputStream);
                return new WebResourceResponse(mimeType, encoding, gzipInputStream);
            }
            return new WebResourceResponse(mimeType, encoding, fileInputStream);
        } catch (IOException e) {
            LeapAUILogger.debugAUI(WebContentAssist.class.getSimpleName() + " IOException : " + e.getMessage());
            return null;
        }
    }

    public void loadUrl(String url, final Map<String, String> contentFileUriMap) {
        if (url == null || url.isEmpty()) return;
        if (url.startsWith(AUIConstants.WebContentAssist.ASSET_FILE_PREFIX)) {
            loadUrl(url);
            return;
        }
        if (url.startsWith(AUIConstants.WebContentAssist.BASE64_PREFIX)) {
            // For testing with base64 html
            loadData(
                    url.substring(AUIConstants.WebContentAssist.BASE64_PREFIX.length()),
                    AUIConstants.WebContentAssist.BASE64_TEXT_HTML_MIME_TYPE,
                    AUIConstants.WebContentAssist.BASE_64);
            return;
        }

        if (contentFileUriMap == null || contentFileUriMap.isEmpty()) return;

        final Map<String, String> mimeTypeMap = getMimeTypeMap();
        setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                String requestUrl = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    requestUrl = request.getUrl().toString();
                }
                if (!contentFileUriMap.containsKey(requestUrl))
                    return super.shouldInterceptRequest(view, request);

                LeapAUILogger.debugAUI(WebContentAssist.class.getSimpleName() + " : intercepting url : " + requestUrl);
                String fileExt = getFileExtFromUrl(requestUrl);
                String mimeType = mimeTypeMap.get(fileExt);
                String encoding = getEncoding(fileExt);
                String filePath = contentFileUriMap.get(requestUrl);
                WebResourceResponse webResponseFromFile = getWebResponseFromFile(fileExt, filePath, mimeType, encoding);
                if (webResponseFromFile != null) {
                    LeapAUILogger.debugAUI(WebContentAssist.class.getSimpleName() + " : intercepting url success : " + filePath);
                    // Whenever, font file .woff is loaded from cache No Access-Control-Allow-Origin
                    // header added error occurs. This is  an error specific to woff files.
                    // To solve this issue, add Access-Control-Allow-Origin header when returning the WebResourceResponse.
                    Map<String, String> responseHeaders = webResponseFromFile.getResponseHeaders();
                    Map<String, String> resHeaders = responseHeaders == null
                            ? new HashMap<String, String>()
                            : responseHeaders;
                    resHeaders.put(AUIConstants.ALLOW_CORS_HEADER_KEY, AUIConstants.ALLOW_CORS_HEADER_VALUE);
                    webResponseFromFile.setResponseHeaders(resHeaders);
                    return webResponseFromFile;
                }

                if (AUIConstants.WebContentAssist.MIME_TYPE_HTML.equals(mimeType)) {
                    return getHtmlMimeTypeWebResourceResponse(requestUrl, mimeType);
                }
                return super.shouldInterceptRequest(view, request);
            }
        });
        super.loadUrl(url);
    }

    public Map<String, String> getMimeTypeMap() {
        Map<String, String> map = new HashMap<>();
        map.put(AUIConstants.WebContentAssist.FILE_TYPE_HTML, AUIConstants.WebContentAssist.MIME_TYPE_HTML);
        map.put(AUIConstants.WebContentAssist.FILE_TYPE_GZ, AUIConstants.WebContentAssist.MIME_TYPE_HTML);
        map.put(AUIConstants.WebContentAssist.FILE_TYPE_CSS, AUIConstants.WebContentAssist.MIME_TYPE_CSS);
        map.put(AUIConstants.WebContentAssist.FILE_TYPE_JS, AUIConstants.WebContentAssist.MIME_TYPE_JS);
        map.put(AUIConstants.WebContentAssist.FILE_TYPE_PNG, AUIConstants.WebContentAssist.MIME_TYPE_PNG);
        map.put(AUIConstants.WebContentAssist.FILE_TYPE_JPG, AUIConstants.WebContentAssist.MIME_TYPE_JPG);
        map.put(AUIConstants.WebContentAssist.FILE_TYPE_ICO, AUIConstants.WebContentAssist.MIME_TYPE_ICO);
        map.put(AUIConstants.WebContentAssist.FILE_TYPE_SVG, AUIConstants.WebContentAssist.MIME_TYPE_SVG);
        map.put(AUIConstants.WebContentAssist.FILE_TYPE_WEBP, AUIConstants.WebContentAssist.MIME_TYPE_WEBP);
        map.put(AUIConstants.WebContentAssist.FILE_TYPE_WOFF, AUIConstants.WebContentAssist.MIME_TYPE_FONT);
        map.put(AUIConstants.WebContentAssist.FILE_TYPE_TTF, AUIConstants.WebContentAssist.MIME_TYPE_FONT);
        map.put(AUIConstants.WebContentAssist.FILE_TYPE_EOT, AUIConstants.WebContentAssist.MIME_TYPE_FONT);
        return map;
    }

    // If MimeType is text/html, check if url is downloadable.
    // The url is downloadable if response content-disposition is attachment
    private WebResourceResponse getHtmlMimeTypeWebResourceResponse(String requestUrl, String mimeType) {
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(requestUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            String contentDisposition = urlConnection
                    .getHeaderField(AUIConstants.WebContentAssist.HEADER_FIELD_CONTENT_DISPOSITION);
            if (contentDisposition == null || !contentDisposition.contains(AUIConstants.WebContentAssist.ATTACHMENT))
                return null;
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            return new WebResourceResponse(mimeType, AUIConstants.WebContentAssist.HTML_ENCODING_UTF8, in);
        } catch (MalformedURLException e) {
            LeapAUILogger.debugAUI(" MalformedURLException : " + e.getMessage());
        } catch (IOException e) {
            LeapAUILogger.debugAUI(" IOException : " + e.getMessage());
        } finally {
            if (urlConnection != null) urlConnection.disconnect();
        }
        return null;
    }

    public String getEncoding(String fileExtension) {
        if (AUIConstants.WebContentAssist.FILE_TYPE_GZ.equals(fileExtension))
            return AUIConstants.WebContentAssist.HTML_ENCODING_GZIP;
        else return AUIConstants.WebContentAssist.HTML_ENCODING_UTF8;
    }

    public String getFileExtFromUrl(String url) {
        if (url == null || url.isEmpty()) return null;
        int lastIndexOfDot = url.lastIndexOf(AUIConstants.WebContentAssist.DOT);
        int urlLength = url.length();
        return url.substring(lastIndexOfDot + 1, urlLength);
    }

    public void delegateTouchListener(final OnTouchListener rootTouchListener) {
        setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                View parent = (View) getParent();
                if (parent == null) return false;
                rootTouchListener.onTouch(parent, event);
                return true;
//                ViewGroup vg = (ViewGroup) parent;
//                for (int i = 0; i < vg.getChildCount(); i++) {
//                    View child = vg.getChildAt(i);
//                    if (child == LeapWebView.this) continue;
//                    child.dispatchTouchEvent(event);
//                }
//                return true;
            }
        });
    }
}
