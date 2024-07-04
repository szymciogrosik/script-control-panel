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
import javafx.stage.Stage;
import org.codefromheaven.dto.data.LoadedElementDTO;
import org.codefromheaven.dto.settings.KeyValueDTO;
import org.codefromheaven.dto.settings.SettingsDTO;
import org.codefromheaven.service.LoadFromJsonService;
import org.codefromheaven.service.animal.AnimalService;
import org.codefromheaven.service.settings.FilesToLoadSettingsService;
import org.codefromheaven.service.settings.HiddenElementSettingsService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

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
        List<LoadedElementDTO> allElements = loadAllElements();

        allElements.stream()
                   .collect(Collectors.groupingBy(LoadedElementDTO::getSubSectionName, LinkedHashMap::new, Collectors.toList()))
                   .forEach((sectionName, elements) -> {
                       VBox sectionBox = new VBox(5);
                       sectionBox.getChildren().add(new Label(sectionName));
                       for (LoadedElementDTO element : elements) {
                           CheckBox checkBox = new CheckBox(element.getButtonName());
                           boolean isChecked = isElementVisible(element, visibilitySettings);
                           checkBox.setSelected(isChecked);
                           checkBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                               updateVisibilitySetting(element, newVal);
                           });
                           sectionBox.getChildren().add(checkBox);
                       }
                       contentBox.getChildren().add(sectionBox);
                   });
    }

    private List<LoadedElementDTO> loadAllElements() {
        List<LoadedElementDTO> allElements = new ArrayList<>();
        SettingsDTO filesToLoad = FilesToLoadSettingsService.load();
        for (String fileToLoad : filesToLoad.getSettings().stream().map(KeyValueDTO::getKey).collect(Collectors.toList())) {
            allElements.addAll(LoadFromJsonService.load(fileToLoad));
        }
        return allElements;
    }

    private void updateVisibilitySetting(LoadedElementDTO element, boolean newVal) {
        HiddenElementSettingsService.updateVisibilitySetting(visibilitySettings, element, newVal);
    }

    public static boolean isElementVisible(LoadedElementDTO element, SettingsDTO visibilitySettings) {
        return visibilitySettings.getSettings().stream().noneMatch(
                elem -> HiddenElementSettingsService.isMatchingSetting(elem, element));
    }

}
