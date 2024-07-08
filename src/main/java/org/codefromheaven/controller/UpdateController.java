package org.codefromheaven.controller;

import javafx.scene.control.ProgressBar;
import org.codefromheaven.service.update.DownloadLatestVersionService;

public class UpdateController {

    private ProgressBar progressBar;

    public UpdateController() {
    }

    public void setupPage() {
        DownloadLatestVersionService.downloadAndInstall();
    }

}
