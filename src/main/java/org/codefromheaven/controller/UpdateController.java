package org.codefromheaven.controller;

import javafx.application.Platform;
import javafx.scene.control.ProgressBar;
import org.codefromheaven.dto.Setting;
import org.codefromheaven.service.command.GitBashService;
import org.codefromheaven.service.update.DownloadLatestVersionService;

public class UpdateController {

    private ProgressBar progressBar;

    public UpdateController() {
    }

    public void setupPage() {
        DownloadLatestVersionService.download();
        runReplaceApplicationBashScriptInNewThread();
        closeApplication();
    }

    public void runReplaceApplicationBashScriptInNewThread() {
        new Thread(() -> {
            try {
                // Wait for 5 seconds to ensure the download is complete
                Thread.sleep(5000);
                // Run the bash script to replace the JAR and restart the application
                GitBashService.runCommand(Setting.TMP_DIRECTORY.getName(), true, "./update_and_restart.sh");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void closeApplication() {
        Platform.exit();
        // Optionally, add a delay to ensure the application exits before the bash script runs
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
