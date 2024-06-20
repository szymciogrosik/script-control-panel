package org.codefromheaven.controller;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import org.codefromheaven.dto.settings.KeyValueDTO;
import org.codefromheaven.dto.settings.SettingsDTO;
import org.codefromheaven.service.animal.AnimalService;
import org.codefromheaven.service.settings.SettingsService;

import java.util.List;

import static javafx.stage.Modality.APPLICATION_MODAL;

public class AdditionalSettingsController {

    private final MainWindowController.ContentLoader loader;
    private final MainWindowController.ResizeWindow resizeMainWindow;

    public AdditionalSettingsController(MainWindowController.ContentLoader loader, MainWindowController.ResizeWindow resizeMainWindow) {
        this.loader = loader;
        this.resizeMainWindow = resizeMainWindow;
    }

    public void setupPage() {
        Stage settingsStage = new Stage();
        settingsStage.initModality(APPLICATION_MODAL);
        settingsStage.getIcons().add(AnimalService.getInstance().getRandomAnimalImage());
        settingsStage.setTitle("Additional settings");

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(20));
        gridPane.setVgap(10);
        gridPane.setHgap(10);

        SettingsDTO configSettings = SettingsService.load();
        List<KeyValueDTO> settings = configSettings.getSettings();
        TextField[] valueFields = new TextField[settings.size()];

        for (int i = 0; i < settings.size(); i++) {
            KeyValueDTO setting = settings.get(i);
            Label label = new Label(setting.getKey() + ":");
            TextField textField = new TextField(setting.getValue());
            textField.setPrefWidth(500);
            valueFields[i] = textField;
            gridPane.add(label, 0, i);
            gridPane.add(textField, 1, i);
        }

        Button saveButton = new Button("Save");
        saveButton.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(saveButton, Priority.ALWAYS);

        HBox buttonBox = new HBox(saveButton);
        buttonBox.setSpacing(10);
        buttonBox.setFillHeight(true);

        gridPane.add(buttonBox, 0, settings.size(), 2, 1);
        GridPane.setMargin(buttonBox, new Insets(10, 0, 0, 0));

        // Create ScrollPane and set its properties
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(gridPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setMaxHeight(700);

        saveButton.setOnAction(event -> {
            for (int i = 0; i < settings.size(); i++) {
                KeyValueDTO setting = settings.get(i);
                String newValue = valueFields[i].getText();
                setting.setValue(newValue);
            }
            SettingsService.saveSettings(configSettings);
            loader.loadContent();
            resizeMainWindow.resizeMainWindow();
            settingsStage.close();
        });

        Scene scene = new Scene(scrollPane);  // Set ScrollPane as the root of the scene
        settingsStage.setScene(scene);
        settingsStage.showAndWait();
    }

}
