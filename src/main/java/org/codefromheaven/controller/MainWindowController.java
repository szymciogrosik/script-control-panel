package org.codefromheaven.controller;

import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.codefromheaven.dto.LoadedElementDTO;
import org.codefromheaven.dto.Setting;
import org.codefromheaven.dto.settings.KeyValueDTO;
import org.codefromheaven.dto.settings.SettingsDTO;
import org.codefromheaven.service.LoadFromJsonService;
import org.codefromheaven.service.animal.AnimalService;
import org.codefromheaven.service.command.GitBashService;
import org.codefromheaven.service.command.PowerShellService;
import org.codefromheaven.service.settings.FilesToLoadSettingsService;
import org.codefromheaven.service.settings.HiddenElementSettingsService;
import org.codefromheaven.service.settings.SettingsServiceBase;

public class MainWindowController implements Initializable {

    @FXML
    public VBox primaryPage;
    @FXML
    private ScrollPane mainScrollPane;

    @FXML
    private MenuItem changeVisibleElements;
    @FXML
    private MenuItem changeSettings;

    @FXML
    private MenuItem news;
    @FXML
    private MenuItem githubProject;
    @FXML
    private MenuItem aboutAuthor;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.loadContent();
    }

    @FunctionalInterface
    public interface ContentLoader {
        void loadContent();
    }

    private void loadContent() {
        primaryPage.getChildren().clear();
        setupScrollPane();

        SettingsDTO visibilitySettings = HiddenElementSettingsService.loadVisibilitySettings();
        SettingsDTO filesToLoad = FilesToLoadSettingsService.load();
        for (String fileToLoad : filesToLoad.getSettings().stream().map(KeyValueDTO::getKey).collect(Collectors.toList())) {
            if (isAnyElementInSectionEnabled(fileToLoad, visibilitySettings)) {
                addElementsToScene(fileToLoad, visibilitySettings);
            }
        }
        addAuthorNote("Made with love by Szymon Gross");
    }

    private void setupScrollPane() {
        String maxWindowHeightString = SettingsServiceBase.loadValue(Setting.MAX_WINDOW_HEIGHT).get();
        int maxWindowHeight = Integer.parseInt(maxWindowHeightString);
        mainScrollPane.setMaxHeight(maxWindowHeight);
        mainScrollPane.setFitToHeight(true);
        mainScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    }

    private void addSectionHeader(String headerName) {
        VBox section = new VBox();
        Text text = new Text(headerName);
        text.getStyleClass().add("text-main-header");
        section.getChildren().add(text);
        primaryPage.getChildren().add(section);
    }

    private void addAuthorNote(String authorNote) {
        VBox section = new VBox();
        section.setAlignment(Pos.CENTER);
        ImageView authorImageView = new ImageView(AnimalService.getInstance().getCurrentAnimalImage());
        authorImageView.getStyleClass().add("author-image");
        Tooltip.install(authorImageView, createTooltip(authorNote));
        authorImageView.setOnMouseClicked(event -> {
            AnimalService.getInstance().replaceCurrentAnimalToRandomAnimal();
            authorImageView.setImage(AnimalService.getInstance().getCurrentAnimalImage());
        });
        Group root = new Group(authorImageView);
        section.getChildren().add(root);
        primaryPage.getChildren().add(section);
    }

    private void addElementsToScene(String fileToLoad, SettingsDTO visibilitySettings) {
        List<LoadedElementDTO> loadedElements = LoadFromJsonService.load(fileToLoad);
        addSectionHeader(loadedElements.stream().findAny().get().getSectionName());
        addElementsToScene(loadedElements, fileToLoad, visibilitySettings);
    }

    private void addElementsToScene(
            List<LoadedElementDTO> loadedElements, String fileToLoad, SettingsDTO visibilitySettings
    ) {
        List<String> subSectionsList =
                loadedElements.stream().map(LoadedElementDTO::getSubSectionName).collect(Collectors.toList());
        List<String> uniqueSubSections = new ArrayList<>();
        subSectionsList.forEach(elem -> {
            if (!uniqueSubSections.contains(elem)) {
                uniqueSubSections.add(elem);
            }
        });

        for (String subSectionName : uniqueSubSections) {
            if (!isAnyElementInSubSectionEnabled(fileToLoad, subSectionName, visibilitySettings)) {
                continue;
            }
            primaryPage.getChildren().add(createHeaderForSection(subSectionName));
            primaryPage.getStyleClass().add("background-primary");

            HBox rows = new HBox();
            rows.getStyleClass().add("hbox-spacing");

            List<LoadedElementDTO> sectionElements = loadedElements.stream()
                                                                   .filter(elem -> elem.getSubSectionName().equals(subSectionName))
                                                                   .collect(Collectors.toList());
            for (LoadedElementDTO loadedElement : sectionElements) {
                if (!HiddenElementSettingsController.isElementVisible(loadedElement, visibilitySettings)) {
                    continue;
                }
                rows.getChildren().add(createButton(loadedElement));
            }
            primaryPage.getChildren().add(rows);
        }
    }

    private VBox createHeaderForSection(String sectionName) {
        VBox section = new VBox();
        Text text = new Text(sectionName);
        text.getStyleClass().add("text-header");
        section.getChildren().add(text);
        return section;
    }

    private Button createButton(LoadedElementDTO loadedElement) {
        Button button = new Button(loadedElement.getButtonName());
        button.getStyleClass().add("button-default");
        button.setOnMouseEntered(e -> button.getStyleClass().add("button-selected"));
        button.setOnMouseExited(e -> button.getStyleClass().remove("button-selected"));
        button.setTooltip(createTooltip(loadedElement.getDescription()));
        switch (loadedElement.getElementType()) {
            case BASH:
            case POWERSHELL:
                addButtonListenerForServiceCommands(button, loadedElement);
                break;
            case LINK:
                button.setOnMouseClicked(event -> openPageInBrowser(loadedElement.getCommand()));
                break;
            default:
                throw new RuntimeException("Unrecognised element type provided: " + loadedElement.getElementType());
        }
        return button;
    }

    private void addButtonListenerForServiceCommands(Button button, LoadedElementDTO loadedElement) {
        switch (loadedElement.getElementType()) {
            case BASH:
                addButtonListenerForBashCommand(button, loadedElement);
                break;
            case POWERSHELL:
                addButtonListenerForPowerShellCommand(button, loadedElement);
                break;
            default:
                throw new RuntimeException("Not recognised console: " + loadedElement.getElementType());
        }
    }

    private void addButtonListenerForBashCommand(Button button, LoadedElementDTO loadedElement) {
        button.setOnMouseClicked(event -> {
            if (loadedElement.isPopupInputDisplayed()) {
                Optional<String> result = createTextInputDialog(loadedElement.getPopupInputMessage());
                result.ifPresent(name -> {
                    GitBashService.runCommand(loadedElement.getScriptLocationParamName(), loadedElement.isAutoCloseConsole(), loadedElement.getCommand() + " " + name);
                });
            } else {
                GitBashService.runCommand(loadedElement.getScriptLocationParamName(), loadedElement.isAutoCloseConsole(), loadedElement.getCommand());
            }
        });
    }

    private void addButtonListenerForPowerShellCommand(Button button, LoadedElementDTO loadedElement) {
        button.setOnMouseClicked(event -> {
            if (loadedElement.isPopupInputDisplayed()) {
                Optional<String> result = createTextInputDialog(loadedElement.getPopupInputMessage());
                result.ifPresent(name -> {
                    PowerShellService.runCommand(loadedElement.getScriptLocationParamName(), loadedElement.isAutoCloseConsole(), loadedElement.getCommand() + " " + name);
                });
            } else {
                PowerShellService.runCommand(loadedElement.getScriptLocationParamName(), loadedElement.isAutoCloseConsole(), loadedElement.getCommand());
            }
        });
    }

    private Optional<String> createTextInputDialog(String popupInputMessage) {
        TextInputDialog dialog = new TextInputDialog("default_value");

        dialog.setTitle("Information required");
        dialog.setHeaderText(null);
        dialog.setGraphic(null);
        dialog.setContentText(popupInputMessage);
        Stage dialogStage = (Stage) dialog.getDialogPane().getScene().getWindow();
        dialogStage.getIcons().add(AnimalService.getInstance().getRandomAnimalImage()); // Update the path accordingly
        return dialog.showAndWait();
    }

    private void openPageInBrowser(String url) {
        try {
            URI u = new URI(url);
            java.awt.Desktop.getDesktop().browse(u);
        } catch (Exception e) {
            throw new RuntimeException("Cannot open page in browser", e);
        }
    }

    private Tooltip createTooltip(String tooltipText) {
        Tooltip tt = new Tooltip(tooltipText);
        tt.getStyleClass().add("tooltip-custom");
        tt.setShowDelay(Duration.ONE);
        tt.setShowDuration(Duration.INDEFINITE);
        return tt;
    }

    private boolean isAnyElementInSectionEnabled(String fileToLoad, SettingsDTO visibilitySettings) {
        return LoadFromJsonService.load(fileToLoad)
                                  .stream().anyMatch(elem -> HiddenElementSettingsController.isElementVisible(elem, visibilitySettings));
    }

    private boolean isAnyElementInSubSectionEnabled(String fileName, String subsection, SettingsDTO visibilitySettings) {
        return LoadFromJsonService.load(fileName)
                                  .stream().filter(elem -> Objects.equals(elem.getSubSectionName(), subsection))
                                  .anyMatch(elem -> HiddenElementSettingsController.isElementVisible(elem, visibilitySettings));
    }

    @FunctionalInterface
    public interface ResizeWindow {
        void resizeMainWindow();
    }

    private void resizeMainWindow() {
        Stage mainStage = (Stage) primaryPage.getScene().getWindow();
        mainStage.sizeToScene();
    }

    @FXML
    private void handleChangeVisibleElements() {
        HiddenElementSettingsController controller = new HiddenElementSettingsController(this::loadContent, this::resizeMainWindow);
        controller.setupPage();
    }

    @FXML
    private void handleChangeSettings() {
        SettingsController controller = new SettingsController(this::loadContent, this::resizeMainWindow);
        controller.setupPage();
    }

    @FXML
    private void handleNews() {

    }

    @FXML
    private void handleGithubProject() {

    }

    @FXML
    private void handleAboutAuthor() {

    }

}
