package org.codefromheaven.controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.codefromheaven.dto.Setting;
import org.codefromheaven.helpers.FileUtils;
import org.codefromheaven.service.animal.AnimalService;
import org.codefromheaven.service.command.GitBashService;
import org.codefromheaven.service.update.DownloadLatestVersionService;

public class UpdateController {

    private ProgressBar progressBar;

    public UpdateController() {
    }

    public void setupPage() {
        showUpdatePopup();
    }

    private void showUpdatePopup() {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.getIcons().add(AnimalService.getInstance().getRandomAnimalImage());
        popupStage.setTitle("Downloading Update");

        Label label = new Label("Downloading update...");
        ProgressBar progressBar = new ProgressBar();
        Button installButton = new Button("Install and restart");
        installButton.setVisible(false);

        VBox vbox = new VBox(label, progressBar, installButton);
        vbox.setAlignment(Pos.CENTER);
        vbox.setSpacing(20);

        Scene scene = new Scene(vbox, 300, 150);
        popupStage.setScene(scene);

        Task<Void> downloadTask = DownloadLatestVersionService.createDownloadTask();

        downloadTask.setOnSucceeded(event -> {
            label.setText("Download completed.");
            installButton.setVisible(true);
        });

        progressBar.progressProperty().bind(downloadTask.progressProperty());

        installButton.setOnAction(e -> {
            FileUtils.copyFileFromResourceToTmp("utils", "update_and_restart.sh");
            runReplaceApplicationBashScriptInNewThread();
            closeApplication();
            popupStage.close();
        });

        Thread downloadThread = new Thread(downloadTask);
        downloadThread.setDaemon(true);
        downloadThread.start();

        popupStage.showAndWait();
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
