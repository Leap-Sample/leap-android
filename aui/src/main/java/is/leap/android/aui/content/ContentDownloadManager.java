package is.leap.android.aui.content;

import is.leap.android.aui.AUIConstants;
import is.leap.android.aui.LeapAUILogger;
import is.leap.android.core.Constants;
import is.leap.android.core.data.model.SoundInfo;
import is.leap.android.core.util.FileUtils;
import is.leap.android.core.util.StringUtils;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static is.leap.android.core.Constants.DownloadStatus.STATUS_DOWNLOADED;

import androidx.annotation.Nullable;

public class ContentDownloadManager {

    private final FileRepository fileRepository;

    public ContentDownloadManager(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    /**
     * Download & check contents on the basis of context(single download) via contextDownloadAlias
     *
     * @param contextDownloadAlias                   is the contextDownloadAlias of this context
     * @param locale                is the current selected audio locale
     * @param soundInfo             is the sound info for this context
     * @param downloadMap           is the content for this map to be download
     * @param fileDownloadStatusMap is the global contextDownloadAlias status map
     * @param listener              is the download listener
     * @return whether it is downloaded or not
     */
    public boolean downloadContextContent(String contextDownloadAlias,
                                          String locale,
                                          SoundInfo soundInfo,
                                          Map<String, String> downloadMap,
                                          Map<String, Map<String, Integer>> fileDownloadStatusMap,
                                          FileDownloadProgressManager.ProgressListener listener) {
        if (isDownloaded(contextDownloadAlias, locale, fileDownloadStatusMap)) return true;
        Map<String, String> soundMap = getSoundMap(locale, soundInfo);
        downloadMap.putAll(soundMap);
        if (downloadMap.isEmpty()) return true;
        downloadContents(downloadMap, listener);
        return false;
    }

    /**
     * Downloads a {@link Map} of file name to url with progress
     *
     * @param downloadMap is the map of file name to sound,html & content url
     * @param listener    is the download listener
     */
    private void downloadContents(Map<String, String> downloadMap,
                                  FileDownloadProgressManager.ProgressListener listener) {
        if (listener == null) return;
        FileDownloadProgressManager fileDownloadProgressManager = new FileDownloadProgressManager(
                fileRepository, listener);

        String auiContentFolder = FileUtils.getLeapFolder();
        if (auiContentFolder == null) return;
        File fileDirectory = new File(auiContentFolder);
        if (!fileDirectory.exists() || downloadMap.isEmpty()) return;
        fileDownloadProgressManager.download(downloadMap, Constants.Priority.HIGH);
    }

    /**
     * Downloads a {@link Map} of file name to url
     *
     * @param downloadMap is the map of file name to sound,html & content url
     * @param priority    is the {@link Constants.Priority}
     * @return whether it is downloaded or not
     */
    public boolean downloadBulkContents(Map<String, String> downloadMap,
                                        int priority, @Nullable Set<String> ignoredUrlSet) {
        boolean needsDownload = false;
        String auiContentFolder = FileUtils.getLeapFolder();
        if (auiContentFolder == null) return false;
        File fileDirectory = new File(auiContentFolder);
        if (!fileDirectory.exists() || downloadMap.isEmpty()) return false;

        for (Map.Entry<String, String> entry : downloadMap.entrySet()) {
            String url = entry.getKey();
            if (ignoredUrlSet != null && ignoredUrlSet.contains(url)) continue;
            String fileName = entry.getValue();
            File file = new File(fileName);
            if (file.exists()) continue;

            needsDownload = true;

            LeapAUILogger.debugDownload("downloadBulkContents() : URL : " + url);
            LeapAUILogger.debugDownload("downloadBulkContents() : FilePath : " + file.getAbsolutePath());
            LeapAUILogger.debugDownload("downloadBulkContents() : needsDownload : true");
            if (fileRepository != null) fileRepository.download(file, url, priority);
        }
        return !needsDownload;
    }

    public static Map<String, String> getSoundMap(String locale, SoundInfo soundInfo) {
        Map<String, String> downloadMap = new HashMap<>();
        if (soundInfo != null) {
            String postFixUrl = soundInfo.postFixUrl;
            if (StringUtils.isNullOrEmpty(postFixUrl)) return downloadMap;
            String soundKey = FileUtils.getSoundKeyName(locale, postFixUrl);
            String fileName = FileUtils.getFileName(soundKey);
            if (!FileUtils.checkIfFileExists(fileName)) {
                String url = soundInfo.fullUrl;
                downloadMap.put(url, FileUtils.getFileUriPath(fileName));
            }
        }
        return downloadMap;
    }

    /**
     * @param contextDownloadAlias                   is the download contextDownloadAlias
     * @param locale                is the current locale
     * @param fileDownloadStatusMap is the global contextDownloadAlias status map
     * @return checks whether the contextDownloadAlias is downloaded or not
     */
    private boolean isDownloaded(String contextDownloadAlias, String locale,
                                 Map<String, Map<String, Integer>> fileDownloadStatusMap) {
        Map<String, Integer> contentStatusMap = fileDownloadStatusMap.get(contextDownloadAlias);
        if (contentStatusMap == null || contentStatusMap.isEmpty()) return false;
        for (Map.Entry<String, Integer> entry : contentStatusMap.entrySet()) {
            String key = entry.getKey();
            Integer status = entry.getValue();
            if (status == STATUS_DOWNLOADED) continue;

            //handle whether to check current locale(in case of sound)
            if (key.contains(AUIConstants.SOUND_KEY_DELIMITER)) {
                String[] split = key.split(AUIConstants.SOUND_KEY_DELIMITER);
                String localeFromKey = split[0];
                if (locale != null && !locale.equals(localeFromKey)) continue;  // locale cant be null
            }

            //Check if the file exist, then update the status map
            String fileName = FileUtils.getFileName(key);
            LeapAUILogger.debugDownload("isDownloaded() : file exists : " + fileName);
            boolean fileExists = FileUtils.checkIfFileExists(fileName);
            if (fileExists) {
                contentStatusMap.put(key, STATUS_DOWNLOADED);
                continue;
            }

            return false;
        }
        return true;
    }

    /**
     * Downloads list of sounds mapped with locale
     *  @param soundMap is the map of locale to list of sound
     * @param priority is the {@link Constants.Priority}
     */
    public void downloadBulkSounds(Map<String, List<SoundInfo>> soundMap,
                                   int priority, @Nullable Set<String> ignoredUrlSet) {
        if (soundMap == null || soundMap.isEmpty()) return;

        String leapFolder = FileUtils.getLeapFolder();
        if (leapFolder == null) return;
        File fileDirectory = new File(leapFolder);
        if (!fileDirectory.exists()) return;

        for (Map.Entry<String, List<SoundInfo>> entry : soundMap.entrySet()) {
            List<SoundInfo> soundInfoList = entry.getValue();
            if (soundInfoList == null || soundInfoList.isEmpty()) continue;
            for (SoundInfo soundInfo : soundInfoList) {
                String postFixUrl = soundInfo.postFixUrl;
                if (StringUtils.isNullOrEmpty(postFixUrl)) continue;
                String soundKey = FileUtils.getSoundKeyName(entry.getKey(), postFixUrl);
                String fileName = FileUtils.getFileName(soundKey);
                File file = new File(fileDirectory, fileName);
                if (file.exists()) continue;

                String url = soundInfo.fullUrl;
                if (ignoredUrlSet != null && ignoredUrlSet.contains(url)) continue;
                LeapAUILogger.debugDownload("downloadBulkSounds() : URL : " + url);
                LeapAUILogger.debugDownload("downloadBulkSounds() : FilePath : " + file.getAbsolutePath());
                if (fileRepository != null) fileRepository.download(file, url, priority);
            }
        }
    }
}
