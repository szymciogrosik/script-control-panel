package org.codefromheaven.controller;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.codefromheaven.dto.data.SectionDTO;
import org.codefromheaven.dto.settings.KeyValueDTO;
import org.codefromheaven.dto.settings.SettingsDTO;
import org.codefromheaven.dto.settings.VisibilitySettingKey;
import org.codefromheaven.service.LoadFromJsonService;
import org.codefromheaven.service.animal.AnimalService;
import org.codefromheaven.service.settings.FilesToLoadSettingsService;
import org.codefromheaven.service.settings.HiddenElementSettingsService;

import java.util.ArrayList;
import java.util.List;

import static javafx.stage.Modality.APPLICATION_MODAL;

public class HiddenElementSettingsController {

    private final MainWindowController.ContentLoader loader;
    private final MainWindowController.ResizeWindow resizeMainWindow;
    private final SettingsDTO visibilitySettings;

    public HiddenElementSettingsController(
            MainWindowController.ContentLoader loader, MainWindowController.ResizeWindow resizeMainWindow
    ) {
        this.loader = loader;
        this.resizeMainWindow = resizeMainWindow;
        this.visibilitySettings = HiddenElementSettingsService.loadVisibilitySettings();
    }

    public void setupPage() {
        Stage settingsStage = new Stage();
        settingsStage.initModality(APPLICATION_MODAL);
        settingsStage.setTitle("Settings - Change visibility of elements");
        settingsStage.getIcons().add(AnimalService.getInstance().getRandomAnimalImage());
        settingsStage.setResizable(false);

        VBox settingsRoot = new VBox(10);
        ScrollPane scrollPane = new ScrollPane();
        VBox contentBox = new VBox(10);
        contentBox.setPadding(new Insets(10));

        loadPageContent(contentBox);

        scrollPane.setContent(contentBox);
        settingsRoot.getChildren().add(scrollPane);

        Button btnSave = new Button("Save");
        btnSave.setMaxWidth(Double.MAX_VALUE);
        HBox.setMargin(btnSave, new Insets(0, 10, 10, 10));
        btnSave.setPadding(new Insets(5));

        HBox buttonContainer = new HBox(btnSave);
        buttonContainer.setAlignment(Pos.CENTER);
        HBox.setHgrow(btnSave, Priority.ALWAYS);

        btnSave.setOnAction(event -> {
            HiddenElementSettingsService.saveSettings(visibilitySettings);
            loader.loadContent();
            resizeMainWindow.resizeMainWindow();
            settingsStage.close();
        });

        settingsRoot.getChildren().add(buttonContainer);

        Scene scene = new Scene(settingsRoot, 400, 600);
        settingsStage.setScene(scene);

        settingsStage.showAndWait();
    }

    private void loadPageContent(VBox contentBox) {
        List<SectionDTO> allElements = loadAllElements();

        allElements.forEach(section -> {
            VBox sectionBox = new VBox(5);
            Label sectionLabel = new Label(section.sectionName());
            sectionLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
            sectionBox.getChildren().add(sectionLabel);
            section.subSections().forEach(subSection -> {
                sectionBox.getChildren().add(new Label(subSection.subSectionName()));
                subSection.commands().forEach(command -> {
                    VisibilitySettingKey key = new VisibilitySettingKey(section.sectionName(), subSection.subSectionName(), command.buttonName());
                    CheckBox checkBox = new CheckBox(command.buttonName());
                    boolean isChecked = isElementVisible(key, visibilitySettings);
                    checkBox.setSelected(isChecked);
                    checkBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                        updateVisibilitySetting(key, newVal);
                    });
                    sectionBox.getChildren().add(checkBox);
                });
            });
            contentBox.getChildren().add(sectionBox);
        });
    }

    public static List<SectionDTO> loadAllElements() {
        List<SectionDTO> allElements = new ArrayList<>();
        SettingsDTO filesToLoad = FilesToLoadSettingsService.load();
        for (String fileToLoad : filesToLoad.getSettings().stream().map(KeyValueDTO::getKey).toList()) {
            allElements.addAll(LoadFromJsonService.load(fileToLoad));
        }
        return allElements;
    }

    private void updateVisibilitySetting(VisibilitySettingKey visibilitySettingKey, boolean newVal) {
        HiddenElementSettingsService.updateVisibilitySetting(visibilitySettings, visibilitySettingKey, newVal);
    }

    public static boolean isElementVisible(VisibilitySettingKey visibilitySettingKey, SettingsDTO visibilitySettings) {
        return visibilitySettings.getSettings().stream().noneMatch(
                elem -> HiddenElementSettingsService.isMatchingSetting(elem, visibilitySettingKey));
    }

}
