package is.leap.android.aui.ui;

import is.leap.android.aui.content.ContentDownloadManager;
import is.leap.android.aui.content.FileDownloadProgressManager;
import is.leap.android.aui.content.FileRepository;
import is.leap.android.aui.content.FileRepositoryImpl;
import is.leap.android.core.AppExecutors;
import is.leap.android.core.Constants;
import is.leap.android.core.LeapCoreInternal;
import is.leap.android.aui.LeapAUILogger;
import is.leap.android.core.data.LeapCoreCache;
import is.leap.android.core.data.model.SoundInfo;
import is.leap.android.core.networking.ThreadExecutor;

import java.util.List;
import java.util.Map;
import java.util.Set;


public class DownloadController {

    private final AppExecutors appExecutors;
    private final ContentDownloadManager contentDownloadManager;

    DownloadController() {
        appExecutors = LeapCoreInternal.getAppExecutors();
        ThreadExecutor threadExecutor = new ThreadExecutor();
        FileRepository fileRepository = new FileRepositoryImpl(LeapCoreInternal.getResources(), threadExecutor);
        contentDownloadManager = new ContentDownloadManager(fileRepository);
    }

    void downloadInBulk(final Map<String, List<SoundInfo>> soundsMap,
                        final Map<String, String> auiContentUrlMap, final Set<String> ignoredUrlSet) {
        appExecutors.bgThread().post(new Runnable() {
            @Override
            public void run() {
                if (soundsMap != null && !soundsMap.isEmpty()) {
                    contentDownloadManager.downloadBulkSounds(soundsMap, Constants.Priority.DEFAULT,
                            ignoredUrlSet);
                }
                if (auiContentUrlMap != null && !auiContentUrlMap.isEmpty()) {
                    contentDownloadManager.downloadBulkContents(auiContentUrlMap,
                            Constants.Priority.DEFAULT, ignoredUrlSet);
                }
            }
        });
    }

    void downloadSounds(final Map<String, List<SoundInfo>> soundInfoMap) {
        if (soundInfoMap == null || soundInfoMap.isEmpty()) return;
        appExecutors.bgThread().post(new Runnable() {
            @Override
            public void run() {
                contentDownloadManager.downloadBulkSounds(soundInfoMap, Constants.Priority.DEFAULT, null);
            }
        });
    }

    void checkContextContent(final Map<String, SoundInfo> soundInfoMap, Map<String, String> contentFileUriMap,
                             final String contextDownloadAlias, String audioLocale, final Callback callback) {
        if (isContextContentDownloaded(LeapCoreCache.fileDownloadStatusMap.get(contextDownloadAlias))) {
            LeapAUILogger.debugDownload("Alias " + contextDownloadAlias + " : instruction onAlreadyAvailable()");
            if (callback != null) callback.onContextFilesAvailable();
            return;
        }

        final long timeStampBeforeDownloadStart = System.currentTimeMillis();
        boolean isDownloaded = contentDownloadManager.downloadContextContent(contextDownloadAlias,
                audioLocale,
                soundInfoMap.get(audioLocale),
                contentFileUriMap,
                LeapCoreCache.fileDownloadStatusMap,
                new FileDownloadProgressManager.ProgressListener() {
                    @Override
                    public void onProgress(int percent) {
                        LeapAUILogger.debugDownload("Alias " + contextDownloadAlias + " : content download progress : " + percent);
                        if (callback != null) callback.onContextFilesDownloading();
                    }

                    @Override
                    public void onFinish(int failedCount) {
                        long totalTimeTaken = System.currentTimeMillis() - timeStampBeforeDownloadStart;
                        LeapAUILogger.debugDownload("Alias " + contextDownloadAlias + " : content download finish : timeTaken : "+totalTimeTaken);
                        if (callback != null) callback.onContextFilesDownloaded(totalTimeTaken);
                    }
                });
        if (isDownloaded) {
            LeapAUILogger.debugDownload("Alias : " + contextDownloadAlias + " : isDownloaded");
            if (callback != null) callback.onContextFilesAvailable();
        }
    }

    void checkPageContents(String contextDownloadAlias, Map<String, String> downloadMap) {
        Integer downloadStatus = LeapCoreCache.contextAliasDownloadStatus.get(contextDownloadAlias);
        if (downloadStatus != null && downloadStatus == Constants.DownloadStatus.STATUS_DOWNLOADED) return;
        boolean isDownloaded = contentDownloadManager.downloadBulkContents(
                downloadMap, Constants.Priority.MEDIUM, null);
        if (isDownloaded) {
            LeapCoreCache.contextAliasDownloadStatus.put(contextDownloadAlias, Constants.DownloadStatus.STATUS_DOWNLOADED);
            return;
        }
        LeapCoreCache.contextAliasDownloadStatus.put(contextDownloadAlias, Constants.DownloadStatus.STATUS_NOT_DOWNLOADED);
    }

    /**
     * @param fileToStatusMap fileName to download status for a particular contextDownloadAlias
     * @return whether the context contents are downloaded or not
     */
    boolean isContextContentDownloaded(Map<String, Integer> fileToStatusMap) {
        if (fileToStatusMap == null || fileToStatusMap.isEmpty()) return true;
        return !fileToStatusMap.containsValue(Constants.DownloadStatus.STATUS_NOT_DOWNLOADED);
    }

    public interface Callback {
        void onContextFilesAvailable();

        void onContextFilesDownloading();

        void onContextFilesDownloaded(long timeTaken);
    }
}
