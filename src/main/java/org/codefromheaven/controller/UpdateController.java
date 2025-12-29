package org.codefromheaven.controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.codefromheaven.dto.Setting;
import org.codefromheaven.helpers.FileUtils;
import org.codefromheaven.service.animal.AnimalService;
import org.codefromheaven.service.command.GitBashService;
import org.codefromheaven.service.update.DownloadLatestVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UpdateController {

    private final AnimalService animalService;
    private final DownloadLatestVersionService downloadLatestVersionService;

    @Autowired
    public UpdateController(AnimalService animalService, DownloadLatestVersionService downloadLatestVersionService) {
        this.animalService = animalService;
        this.downloadLatestVersionService = downloadLatestVersionService;
    }

    public void setupPage() {
        showUpdatePopup();
    }

    private void showUpdatePopup() {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.getIcons().add(animalService.getRandomAnimalImage());
        popupStage.setTitle("Downloading update");
        popupStage.setResizable(false);

        Label label = new Label("Downloading update...");
        HBox labelContainer = new HBox(label);
        labelContainer.setAlignment(Pos.CENTER_LEFT);

        ProgressBar progressBar = new ProgressBar();
        progressBar.setMaxWidth(Double.MAX_VALUE);

        Button installButton = new Button("Install and restart");
        installButton.setMaxWidth(Double.MAX_VALUE);
        installButton.setVisible(false);

        VBox vbox = new VBox(labelContainer, progressBar, installButton);
        vbox.setAlignment(Pos.CENTER);
        vbox.setSpacing(20);
        Insets margin = new Insets(0, 20, 0, 20);
        VBox.setMargin(labelContainer, margin);
        VBox.setMargin(progressBar, margin);
        VBox.setMargin(installButton, margin);

        Scene scene = new Scene(vbox, 300, 150);
        popupStage.setScene(scene);

        Task<Void> downloadTask = downloadLatestVersionService.createDownloadTask();

        downloadTask.setOnSucceeded(event -> {
            try {
                // Wait few seconds to ensure that downloading was completed
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
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
            GitBashService.runCommand(Setting.TMP_DIRECTORY.getName(), true, "./update_and_restart.sh");
        }).start();
    }

    public void closeApplication() {
        Platform.exit();
    }

}
