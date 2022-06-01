package is.leap.android.aui;

public class AUIConstants {

    public static final String audioAnimPath = "file:///android_asset/leap_audio_anim.html";

    public static final int DEFAULT_MARGIN_5 = 5;
    public static final int DEFAULT_MARGIN_8 = 8;
    public static final int DEFAULT_MARGIN_10 = 10;
    public static final int DEFAULT_MARGIN_14 = 14;
    public static final int DEFAULT_MARGIN_16 = 16;
    public static final int DEFAULT_MARGIN_20 = 20;
    public static final int DEFAULT_MARGIN_26 = 26;
    public static final int DEFAULT_MARGIN_34 = 34;
    public static final int DP_96 = 96;
    public static final int AUI_ASSOCIATE_ICON_MARGIN = 12;
    public static final int ASSOCIATE_ICON_HEIGHT = 36;

    public static final String SOUND_KEY_DELIMITER = "_SOUND_FILE_";

    public static final String LEFT = "left";
    public static final String TOP = "top";
    public static final String RIGHT = "right";
    public static final String BOTTOM = "bottom";
    public static final String CLICK_PERFORMED = "clickPerformed";
    public static final String ALLOW_CORS_HEADER_KEY = "Access-Control-Allow-Origin";
    public static final String ALLOW_CORS_HEADER_VALUE = "*";
    public static final String LANGUAGE = "language";
    public static final String ID = "id";
    public static final String PROJECTS = "projects";
    public static final String COMPLETED = "completed";
    public static final float DEFAULT_MARGIN_70 = 70;

    public static class LeapJavaScriptInterface {
        //Below is kept 'jiny' as it needs to be changed in backend
        public static final String JS_OBJ_NAME = "JinyAndroid";
        public static final String EVENT_TYPE_KEY = "type";
        public static final String EVENT_TYPE_RESIZE = "resize";
        public static final String EVENT_TYPE_RENDERING_COMPLETE = "rendering_complete";
        public static final String EVENT_TYPE_ACTION_TAKEN = "action_taken";
        public static final String PAGE_META_DATA = "pageMetaData";
        public static final String RECT = "rect";
        public static final String WIDTH = "width";
        public static final String HEIGHT = "height";
    }

    public static class WebContentAssist {
        public static final String FILE_TYPE_HTML = "html";
        public static final String FILE_TYPE_GZ = "gz";
        public static final String FILE_TYPE_CSS = "css";
        public static final String FILE_TYPE_JS = "js";
        public static final String FILE_TYPE_PNG = "png";
        public static final String FILE_TYPE_JPG = "jpg";
        public static final String FILE_TYPE_ICO = "ico";
        public static final String FILE_TYPE_SVG = "svg";
        public static final String FILE_TYPE_WEBP = "webp";
        public static final String FILE_TYPE_WOFF = "woff";
        public static final String FILE_TYPE_TTF = "ttf";
        public static final String FILE_TYPE_EOT = "eot";

        public static final String MIME_TYPE_HTML = "text/html";
        public static final String MIME_TYPE_CSS = "text/css";
        public static final String MIME_TYPE_JS = "text/javascript";
        public static final String MIME_TYPE_PNG = "image/png";
        public static final String MIME_TYPE_JPG = "image/jpeg";
        public static final String MIME_TYPE_ICO = "image/x-icon";
        public static final String MIME_TYPE_SVG = "image/svg+xml";
        public static final String MIME_TYPE_WEBP = "image/webp";
        public static final String MIME_TYPE_FONT = "application/x-font-opentype";

        public static final String HTML_ENCODING_GZIP = "gzip";
        public static final String HTML_ENCODING_UTF8 = "UTF-8";

        public static final String ASSET_FILE_PREFIX = "file:///android_asset";
        public static final String BASE_64 = "base64";
        public static final String BASE64_PREFIX = "base64:";
        public static final String BASE64_TEXT_HTML_MIME_TYPE = "text/html; charset=utf-8";
        public static final String HEADER_FIELD_CONTENT_DISPOSITION = "content-disposition";
        public static final String ATTACHMENT = "attachment";

        public static final String DOT = ".";
    }

    public static class AUIFrequency {
        public static final String PLAY_ONCE = "PLAY_ONCE";
        public static final String MANUAL_TRIGGER = "MANUAL_TRIGGER";
        public static final String EVERY_SESSION_UNTIL_DISMISSED = "EVERY_SESSION_UNTIL_DISMISSED";
        public static final String EVERY_SESSION_UNTIL_FLOW_COMPLETE = "EVERY_SESSION_UNTIL_FLOW_COMPLETE";
        public static final String EVERY_SESSION_UNTIL_ALL_FLOWS_ARE_COMPLETED = "EVERY_SESSION_UNTIL_ALL_FLOWS_ARE_COMPLETED";
    }

    public static class AccessibilityText {
        public static final String STOP = "stop";
        public static final String CLOSE_ICON_OPTIONS = "closeIconOptions";
        public static final String ARROW_UP = "arrowUp";
        public static final String ARROW_DOWN = "arrowDown";
        public static final String PING_CROSS = "pingCross";
    }
}
