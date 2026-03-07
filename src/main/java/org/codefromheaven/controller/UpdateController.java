package org.codefromheaven.controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.codefromheaven.service.animal.AnimalService;
import org.codefromheaven.service.style.StyleService;
import org.codefromheaven.service.update.DownloadLatestVersionService;
import org.codefromheaven.service.version.AppVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UpdateController {

    private final AnimalService animalService;
    private final DownloadLatestVersionService downloadLatestVersionService;
    private final StyleService styleService;
    private final AppVersionService appVersionService;

    @Autowired
    public UpdateController(
            AnimalService animalService,
            DownloadLatestVersionService downloadLatestVersionService,
            StyleService styleService,
            AppVersionService appVersionService) {
        this.animalService = animalService;
        this.downloadLatestVersionService = downloadLatestVersionService;
        this.styleService = styleService;
        this.appVersionService = appVersionService;
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

        Label label = new Label("Downloading update " + appVersionService.getLatestVersion() + "...");
        HBox labelContainer = new HBox(label);
        labelContainer.setAlignment(Pos.CENTER_LEFT);

        ProgressBar progressBar = new ProgressBar();
        progressBar.setMaxWidth(Double.MAX_VALUE);

        VBox vbox = new VBox(labelContainer, progressBar);
        vbox.setAlignment(Pos.CENTER);
        vbox.setSpacing(20);
        Insets margin = new Insets(0, 20, 0, 20);
        VBox.setMargin(labelContainer, margin);
        VBox.setMargin(progressBar, margin);

        vbox.getStyleClass().add("background-primary");
        label.getStyleClass().add("label-on-dark-background");

        Scene scene = new Scene(vbox, 300, 150);
        scene.getStylesheets().add(styleService.getCurrentStyleUrl());
        popupStage.setScene(scene);

        Task<Void> downloadTask = downloadLatestVersionService.createDownloadTask();

        downloadTask.setOnSucceeded(event -> {
            try {
                // Wait few seconds to ensure that downloading was completed
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            Platform.runLater(() -> {
                label.setText("Download completed.");

                PopupController.showPopup(
                        "Update is ready. After closing this window, please wait a moment while the application automatically restarts.",
                        javafx.scene.control.Alert.AlertType.INFORMATION);
                popupStage.close();
                runReplaceApplicationScriptInNewThread();
                closeApplication();
            });
        });

        progressBar.progressProperty().bind(downloadTask.progressProperty());

        Thread downloadThread = new Thread(downloadTask);
        downloadThread.setDaemon(true);
        downloadThread.start();

        popupStage.showAndWait();
    }

    public void runReplaceApplicationScriptInNewThread() {
        new Thread(() -> {
            try {
                String tmpDirPath = System.getProperty("user.dir") + java.io.File.separator + "tmp";
                java.io.File updateBat = new java.io.File(tmpDirPath, "update.bat");
                String batContent = "@echo off\r\n" +
                        "set TMP_DIR=%~dp0\r\n" +
                        "set APP_DIR=%TMP_DIR%..\r\n" +
                        "ping 127.0.0.1 -n 4 > nul\r\n" +
                        "cd /d \"%APP_DIR%\"\r\n" +
                        "powershell -NoProfile -Command \"Expand-Archive -Force -Path '%TMP_DIR%"
                        + AppVersionService.ZIP_NAME + "' -DestinationPath '.'\"\r\n" +
                        "start \"\" \"ScriptControlPanel.exe\"\r\n" +
                        "rmdir /s /q \"%TMP_DIR%\"\r\n" +
                        "exit\r\n";
                java.nio.file.Files.writeString(updateBat.toPath(), batContent);

                ProcessBuilder processBuilder = new ProcessBuilder("cmd", "/c", "update.bat");
                processBuilder.directory(new java.io.File(tmpDirPath));
                processBuilder.start();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    public void closeApplication() {
        Platform.exit();
    }

}
