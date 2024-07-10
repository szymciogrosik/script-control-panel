package org.codefromheaven.service.update;

import javafx.concurrent.Task;
import org.codefromheaven.helpers.FileUtils;
import org.codefromheaven.service.version.AppVersionService;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadLatestVersionService {

    private DownloadLatestVersionService() {
    }

    public static void download() {
        try {
            if (AppVersionService.isNewVersionAvailable()) {
                DownloadLatestVersionService.download(FileUtils.TMP_DIR + "/" + AppVersionService.TMP_NAME);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void download(String savePath) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                URL url = new URL(AppVersionService.getLatestJarDownloadUrl());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

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

        task.setOnSucceeded(event -> System.out.println("Download completed successfully."));
        task.setOnFailed(event -> System.out.println("Download failed: " + task.getException()));

        Thread thread = new Thread(task);
        thread.start();
    }

}
