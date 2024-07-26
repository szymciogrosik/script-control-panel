package org.codefromheaven.service.update;

import javafx.concurrent.Task;
import org.codefromheaven.helpers.FileUtils;
import org.codefromheaven.service.gh.GithubService;
import org.codefromheaven.service.version.AppVersionService;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;

public class DownloadLatestVersionService {

    private static final String TMP_FILE_LOCATION = FileUtils.TMP_DIR + "/" + AppVersionService.TMP_NAME;

    private DownloadLatestVersionService() {
    }

    public static Task<Void> createDownloadTask() {
        try {
            if (AppVersionService.isNewVersionAvailable()) {
                FileUtils.createOrReplaceTmpDirectory();
                return createDownloadTask(TMP_FILE_LOCATION, AppVersionService.getLatestJarDownloadUrl());
            } else {
                throw new RuntimeException("Download update should not be invoked when it is not present");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private static Task<Void> createDownloadTask(String savePath, String downloadUrl) {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                updateProgress(0, 100);

                URL url = new URL(downloadUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                Optional<String> ghDownloadToken = GithubService.getGhDownloadToken();
                if (ghDownloadToken.isPresent()) {
                    connection.setRequestProperty("Authorization", "token " + ghDownloadToken.get());
                }

                int contentLength = connection.getContentLength();
                try (InputStream in = connection.getInputStream(); FileOutputStream out = new FileOutputStream(savePath)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    int totalBytesRead = 0;

                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                        totalBytesRead += bytesRead;
                        updateProgress(totalBytesRead, contentLength);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new IOException("Failed to download the file.");
                }
                return null;
            }
        };
    }

}
