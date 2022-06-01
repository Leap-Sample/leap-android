package is.leap.android.aui.content;

import android.content.res.Resources;

import is.leap.android.aui.LeapAUILogger;
import is.leap.android.core.data.LeapCoreCache;
import is.leap.android.core.networking.Http;
import is.leap.android.core.networking.ThreadExecutor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileRepositoryImpl implements FileRepository {

    private final Object lock = new Object();
    private final Resources resources;
    private final ThreadExecutor threadExecutor;

    public FileRepositoryImpl(Resources resources, ThreadExecutor threadExecutor) {
        this.resources = resources;
        this.threadExecutor = threadExecutor;
    }

    @Override
    public void download(String directory, String fileName, String url, int priority) {
        download(directory, fileName, url, null, priority);
    }

    @Override
    public void download(final String directory, final String fileName, final String url,
                         final ResponseListener listener, int priority) {
        Http.Request httpReq = new Http.Request(Http.Method.GET)
                .withSSLPinning(resources)
                .enableLog(LeapCoreCache.isLogEnabled)
                .url(url);

        LeapAUILogger.debugDownload("download() fire Priority: " + priority + " : for " + url);

        httpReq.execute(threadExecutor.setPriority(priority), new Http.HttpCb() {
            @Override
            public void onResponse(Http.Request req, Http.Response res) {
                synchronized (lock) {
                    File file = new File(directory, fileName);
                    if (file.exists()) {
                        if (listener != null) listener.onSuccess();
                        return;
                    }
                    try {
                        file.createNewFile();
                        FileOutputStream fos = new FileOutputStream(file);
                        fos.write(res.data);
                        fos.close();
                        LeapAUILogger.debugDownload("download() done for file : " + file.getAbsolutePath());
                    } catch (FileNotFoundException e) {
                        LeapAUILogger.errorDownload(e.getMessage() + " : fileName : " + fileName);
                    } catch (IOException e) {
                        LeapAUILogger.errorDownload(e.getMessage() + " : fileName : " + fileName);
                    }
                    if (listener != null) listener.onSuccess();
                }
            }

            @Override
            public void onFailure(Http.Request req, Http.Response res, Exception e) {
                synchronized (lock) {
                    if (listener != null) listener.onFailure();
                }
            }
        });
    }

    @Override
    public void download(final File file, String url, int priority) {
        Http.Request httpReq = new Http.Request(Http.Method.GET)
                .withSSLPinning(resources)
                .enableLog(LeapCoreCache.isLogEnabled)
                .url(url);

        LeapAUILogger.debugDownload("download() fire Priority: " + priority + " : for " + url);

        httpReq.execute(threadExecutor.setPriority(priority), new Http.HttpCb() {
            @Override
            public void onResponse(Http.Request req, Http.Response res) {
                synchronized (lock) {
                    if (file.exists()) return;
                    try {
                        file.createNewFile();
                        FileOutputStream fos = new FileOutputStream(file);
                        fos.write(res.data);
                        fos.close();
                        LeapAUILogger.debugDownload("download() done for file : " + file.getAbsolutePath());
                    } catch (FileNotFoundException e) {
                        LeapAUILogger.errorDownload(e.getMessage() + " : file : " + file.getAbsolutePath());
                    } catch (IOException e) {
                        LeapAUILogger.errorDownload(e.getMessage() + " : file : " + file.getAbsolutePath());
                    }
                }
            }

            @Override
            public void onFailure(Http.Request req, Http.Response res, Exception e) {
                synchronized (lock) {
                    LeapAUILogger.debugDownload("download() : onFailure() : " + e.getMessage());
                }
            }
        });
    }
}
