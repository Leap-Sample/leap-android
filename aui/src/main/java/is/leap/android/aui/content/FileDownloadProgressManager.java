package is.leap.android.aui.content;

import is.leap.android.aui.LeapAUILogger;

import java.util.Map;

public class FileDownloadProgressManager implements FileRepository.ResponseListener {
    private final Object lock = new Object();
    private int totalCount;
    private int failedCount;
    private int finishCount;
    private final FileRepository fileRepository;
    private final ProgressListener progressListener;

    FileDownloadProgressManager(FileRepository fileRepository, ProgressListener progressListener) {
        this.fileRepository = fileRepository;
        this.progressListener = progressListener;
    }

    public void download(String directory, String fileName, String url, int priority) {
        totalCount++;
        fileRepository.download(directory, fileName, url, this, priority);
    }

    public void download(Map<String, String> filePathToUrlMap, int priority) {
        download(null, filePathToUrlMap, priority);
    }

    public void download(String directory, Map<String, String> fileNameToUrlMap, int priority) {
        for (Map.Entry<String, String> entry : fileNameToUrlMap.entrySet()) {
            String url = entry.getKey();
            String fileName = entry.getValue();
            totalCount++;
            fileRepository.download(directory, fileName, url, this, priority);
        }
    }

    private void reportProgress() {
        synchronized (lock) {
            int percent = totalCount == 0 ? 100
                    : (int) Math.floor(finishCount * 100f / totalCount);

            progressListener.onProgress(percent);

            if ((failedCount + finishCount) == totalCount) {
                progressListener.onFinish(failedCount);
                LeapAUILogger.debugDownload("reportProgress() : onFinish() : " + totalCount);
                reset();
            }
        }
    }

    private void reset() {
        totalCount = 0;
        failedCount = 0;
    }

    @Override
    public void onSuccess() {
        synchronized (lock) {
            finishCount++;
            reportProgress();
            LeapAUILogger.debugDownload("onSuccess() : finishCount : " + finishCount);
        }
    }

    @Override
    public void onFailure() {
        synchronized (lock) {
            failedCount++;
            reportProgress();
        }
    }

    public interface ProgressListener {
        void onProgress(int percent);

        void onFinish(int failedCount);
    }

}