package org.codefromheaven.controller;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.codefromheaven.dto.LoadedElementDTO;
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

    public HiddenElementSettingsController(
            MainWindowController.ContentLoader loader, MainWindowController.ResizeWindow resizeMainWindow
    ) {
        this.loader = loader;
        this.resizeMainWindow = resizeMainWindow;
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

        Scene scene = new Scene(settingsRoot, 400, 600);
        settingsStage.setScene(scene);

        settingsStage.setOnHidden(event -> {
            loader.loadContent();
            resizeMainWindow.resizeMainWindow();
        });
        settingsStage.showAndWait();
    }

    private void loadPageContent(VBox contentBox) {
        List<LoadedElementDTO> allElements = loadAllElements();
        SettingsDTO visibilitySettings = HiddenElementSettingsService.loadVisibilitySettings();

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
        HiddenElementSettingsService.updateVisibilitySetting(
                element.getSubSectionName(), element.getButtonName(), newVal);
    }

    public static boolean isElementVisible(LoadedElementDTO element, SettingsDTO visibilitySettings) {
        return visibilitySettings.getSettings().stream().noneMatch(
                elem -> HiddenElementSettingsService.isMatchingSetting(
                        elem, element.getSubSectionName(), element.getButtonName()));
    }

}
