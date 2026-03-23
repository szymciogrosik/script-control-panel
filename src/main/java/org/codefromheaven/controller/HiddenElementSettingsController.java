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
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.codefromheaven.context.SpringContext;
import org.codefromheaven.dto.data.SectionDTO;
import org.codefromheaven.dto.data.LayoutAndButtonsDTO;
import org.codefromheaven.service.settings.VisibilitySettings;
import org.codefromheaven.helpers.MaxHighUtils;
import org.codefromheaven.service.animal.AnimalService;
import org.codefromheaven.service.settings.LayoutService;
import org.codefromheaven.service.style.StyleService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class HiddenElementSettingsController {

    private final MainWindowController.ContentLoader loader;
    private final MainWindowController.ResizeWindow resizeMainWindow;
    private final VisibilitySettings visibilitySettings = new VisibilitySettings();

    public HiddenElementSettingsController(
            MainWindowController.ContentLoader loader, MainWindowController.ResizeWindow resizeMainWindow) {
        this.loader = loader;
        this.resizeMainWindow = resizeMainWindow;
    }

    public void setupPage() {
        Stage settingsStage = new Stage();
        settingsStage.initModality(Modality.APPLICATION_MODAL);
        settingsStage.setTitle("Change elements visibility");
        settingsStage.getIcons().add(SpringContext.getBean(AnimalService.class).getRandomAnimalImage());
        settingsStage.setResizable(false);

        VBox settingsRoot = new VBox(5);
        settingsRoot.getStyleClass().add("background-primary");
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.getStyleClass().add("scroll-pane-transparent");
        VBox contentBox = new VBox(10);
        contentBox.setPadding(new Insets(10));

        loadPageContent(contentBox);

        scrollPane.setContent(contentBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setMaxHeight(MaxHighUtils.getMaxHeight());

        // --- Info description box ---
        Label descLabel = new Label(loadResourceText("/editor/tab_visibility_desc.txt"));
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(Double.MAX_VALUE);
        descLabel.setPrefWidth(460);
        descLabel.getStyleClass().add("tab-desc-label");
        descLabel.setStyle("-fx-padding: 5 10 0 10;");
        settingsRoot.getChildren().add(descLabel);

        settingsRoot.getChildren().add(scrollPane);

        Button btnSave = new Button("Save");
        btnSave.getStyleClass().add("button-default");
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
        Scene scene = new Scene(settingsRoot, 480, 100);
        scene.getStylesheets().add(SpringContext.getBean(StyleService.class).getCurrentStyleUrl());
        settingsStage.setScene(scene);

        // Resize stage based on actual rendered heights of both the desc label and the content
        Runnable recalcHeight = () -> {
            double needed = contentBox.getHeight() + descLabel.getHeight() + 90;
            settingsStage.setHeight(Math.min(needed, MaxHighUtils.getMaxHeight()));
        };
        contentBox.heightProperty().addListener((obs, o, n) -> recalcHeight.run());
        descLabel.heightProperty().addListener((obs, o, n) -> recalcHeight.run());

        settingsStage.showAndWait();
    }

    private void loadPageContent(VBox contentBox) {
        List<SectionDTO> allElements = loadAllElements();

        allElements.forEach(section -> {
            VBox sectionBox = new VBox(5);
            Text sectionHeader = new Text(section.sectionName());
            sectionHeader.getStyleClass().add("text-main-header");
            sectionBox.getChildren().add(sectionHeader);
            section.subSections().forEach(subSection -> {
                Text subSectionHeader = new Text(subSection.subSectionName());
                subSectionHeader.getStyleClass().add("text-header");
                sectionBox.getChildren().add(subSectionHeader);
                subSection.buttons().forEach(button -> {
                    CheckBox checkBox = new CheckBox(button.getButtonName());
                    checkBox.getStyleClass().add("check-box-on-dark-background");
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
        LayoutAndButtonsDTO layoutAndButtons = SpringContext.getBean(LayoutService.class).load();
        if (layoutAndButtons != null && layoutAndButtons.layout() != null) {
            return layoutAndButtons.layout();
        }
        return new ArrayList<>();
    }

    private String loadResourceText(String resourcePath) {
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is != null) {
                return new String(is.readAllBytes(), StandardCharsets.UTF_8).trim();
            }
        } catch (IOException ignored) {
            ignored.printStackTrace();
        }
        return "";
    }

}
