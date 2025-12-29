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
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.codefromheaven.context.SpringContext;
import org.codefromheaven.dto.data.SectionDTO;
import org.codefromheaven.dto.settings.KeyValueDTO;
import org.codefromheaven.dto.settings.SettingsDTO;
import org.codefromheaven.service.settings.VisibilitySettings;
import org.codefromheaven.helpers.MaxHighUtils;
import org.codefromheaven.service.LoadFromJsonService;
import org.codefromheaven.service.animal.AnimalService;
import org.codefromheaven.service.settings.FilesToLoadSettingsService;
import org.codefromheaven.service.style.StyleService;

import java.util.ArrayList;
import java.util.List;

public class HiddenElementSettingsController {

    private final MainWindowController.ContentLoader loader;
    private final MainWindowController.ResizeWindow resizeMainWindow;
    private final VisibilitySettings visibilitySettings = new VisibilitySettings();

    public HiddenElementSettingsController(
            MainWindowController.ContentLoader loader, MainWindowController.ResizeWindow resizeMainWindow
    ) {
        this.loader = loader;
        this.resizeMainWindow = resizeMainWindow;
    }

    public void setupPage() {
        Stage settingsStage = new Stage();
        settingsStage.initModality(Modality.APPLICATION_MODAL);
        settingsStage.setTitle("Settings - Change visibility of elements");
        settingsStage.getIcons().add(SpringContext.getBean(AnimalService.class).getRandomAnimalImage());
        settingsStage.setResizable(false);

        VBox settingsRoot = new VBox(10);
        settingsRoot.getStyleClass().add("background-primary");
        ScrollPane scrollPane = new ScrollPane();
        VBox contentBox = new VBox(10);
        contentBox.setPadding(new Insets(10));

        loadPageContent(contentBox);

        scrollPane.setContent(contentBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setMaxHeight(MaxHighUtils.getMaxHeight());

        settingsRoot.getChildren().add(scrollPane);

        Button btnSave = new Button("Save");
        btnSave.setMaxWidth(Double.MAX_VALUE);
        HBox.setMargin(btnSave, new Insets(0, 10, 10, 10));
        btnSave.setPadding(new Insets(5));

        HBox buttonContainer = new HBox(btnSave);
        buttonContainer.setAlignment(Pos.CENTER);
        HBox.setHgrow(btnSave, Priority.ALWAYS);

        btnSave.setOnAction(event -> {
            visibilitySettings.saveSettings();
            loader.loadContent();
            resizeMainWindow.resizeMainWindow();
            settingsStage.close();
        });

        settingsRoot.getChildren().add(buttonContainer);

        // Scene height will be adjusted dynamically
        Scene scene = new Scene(settingsRoot, 400, 100);
        scene.getStylesheets().add(SpringContext.getBean(StyleService.class).getCurrentStyleUrl());
        settingsStage.setScene(scene);

        // Add a listener to adjust the height dynamically as elements are added
        contentBox.heightProperty().addListener((observable, oldValue, newValue) -> {
            double newHeight = Math.min(newValue.doubleValue() + 90, MaxHighUtils.getMaxHeight());
            settingsStage.setHeight(newHeight);
        });

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
                subSection.buttons().forEach(button -> {
                    CheckBox checkBox = new CheckBox(button.getName());
                    boolean isChecked = visibilitySettings.isVisible(button);
                    checkBox.setSelected(isChecked);
                    checkBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                        visibilitySettings.updateVisibilitySetting(button, newVal);
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

}
