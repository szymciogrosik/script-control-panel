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

    @Autowired
    public UpdateController(
            AnimalService animalService,
            DownloadLatestVersionService downloadLatestVersionService,
            StyleService styleService
    ) {
        this.animalService = animalService;
        this.downloadLatestVersionService = downloadLatestVersionService;
        this.styleService = styleService;
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

        vbox.getStyleClass().add("background-primary");
        label.getStyleClass().add("label-on-dark-background");
        installButton.getStyleClass().add("button-default");

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
            label.setText("Download completed.");
            installButton.setVisible(true);
        });

        progressBar.progressProperty().bind(downloadTask.progressProperty());

        installButton.setOnAction(e -> {
            runReplaceApplicationScriptInNewThread();
            closeApplication();
            popupStage.close();
        });

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
                        "echo Waiting for application to close...\r\n" +
                        "ping 127.0.0.1 -n 4 > nul\r\n" +
                        "echo Extracting update...\r\n" +
                        "cd ..\r\n" +
                        "powershell -NoProfile -Command \"Expand-Archive -Force -Path 'tmp/"
                        + AppVersionService.ZIP_NAME + "' -DestinationPath '.'\"\r\n" +
                        "echo Starting application...\r\n" +
                        "start \"\" \"ScriptControlPanel.exe\"\r\n";
                java.nio.file.Files.writeString(updateBat.toPath(), batContent);

                ProcessBuilder processBuilder = new ProcessBuilder("cmd", "/c", "start", "\"\"", "update.bat");
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
