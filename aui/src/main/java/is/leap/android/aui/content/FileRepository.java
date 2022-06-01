package is.leap.android.aui.content;

import java.io.File;

public interface FileRepository {

    void download(String directory, String fileName, String url, int priority);

    void download(String directory, String fileName, String url, ResponseListener listener, int priority);

    void download(File file, String url, int priority);

    interface ResponseListener {
        void onSuccess();

        void onFailure();
    }

}
