package org.codefromheaven.controller;

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
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.codefromheaven.dto.Link;
import org.codefromheaven.dto.data.ButtonDTO;
import org.codefromheaven.dto.data.SectionDTO;
import org.codefromheaven.dto.Setting;
import org.codefromheaven.dto.data.SubSectionDTO;
import org.codefromheaven.dto.settings.KeyValueDTO;
import org.codefromheaven.dto.settings.SettingsDTO;
import org.codefromheaven.dto.settings.VisibilitySettingKey;
import org.codefromheaven.helpers.ImageLoader;
import org.codefromheaven.helpers.LinkUtils;
import org.codefromheaven.service.LoadFromJsonService;
import org.codefromheaven.service.animal.AnimalService;
import org.codefromheaven.service.command.GitBashService;
import org.codefromheaven.service.command.PowerShellService;
import org.codefromheaven.service.settings.FilesToLoadSettingsService;
import org.codefromheaven.service.settings.HiddenElementSettingsService;
import org.codefromheaven.service.settings.SettingsService;
import org.codefromheaven.service.version.AppVersionService;

public class MainWindowController implements Initializable {

    private static final String DOWNLOAD_UPDATE_BUTTON_BASE_TEXT = "Download and install";

    @FXML
    public VBox primaryPage;
    @FXML
    private ScrollPane mainScrollPane;

    @FXML
    private MenuItem changeVisibleElements;
    @FXML
    private MenuItem changeSettings;
    @FXML
    private MenuItem pinJarFileToTaskBar;

    @FXML
    private MenuItem news;
    @FXML
    private MenuItem reportIssues;
    @FXML
    private MenuItem githubProject;
    @FXML
    private MenuItem githubDocumentation;
    @FXML
    private MenuItem githubAboutAuthor;

    @FXML
    private ImageView updateNotification;
    @FXML
    private MenuItem checkForUpdates;
    @FXML
    private MenuItem downloadAndInstall;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        updateNotification.setImage(ImageLoader.getImage("/update/notification.png"));
        boolean noSectionPresent = HiddenElementSettingsController.loadAllElements().isEmpty();
        changeVisibleElements.setDisable(!noSectionPresent);
        this.loadContent();
    }

    @FunctionalInterface
    public interface ContentLoader {
        void loadContent();
    }

    private void loadContent() {
        checkForUpdates();
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
        mainScrollPane.setMaxHeight(getMaxHeight());
        mainScrollPane.setFitToHeight(true);
        mainScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    }

    private double getMaxHeight() {
        Optional<String> maxWindowHeightString = SettingsService.loadValue(Setting.MAX_WINDOW_HEIGHT);
        if (maxWindowHeightString.isPresent() && !maxWindowHeightString.get().isEmpty()) {
            return Integer.parseInt(maxWindowHeightString.get());
        }
        // Calculate size based on screen height
        double screenHeight = Screen.getPrimary().getVisualBounds().getHeight();
        return screenHeight - 65;
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
        List<SectionDTO> loadedElements = LoadFromJsonService.load(fileToLoad);
        addSectionHeader(loadedElements.stream().findFirst().get().sectionName());
        addElementsToScene(loadedElements, fileToLoad, visibilitySettings);
    }

    private void addElementsToScene(
            List<SectionDTO> sections, String fileToLoad, SettingsDTO visibilitySettings
    ) {
        for (SectionDTO section : sections) {
            for (SubSectionDTO subSection : section.subSections()) {
                if (!isAnyElementInSubSectionEnabled(fileToLoad, section.sectionName(), subSection.subSectionName(), visibilitySettings)) {
                    continue;
                }
                primaryPage.getChildren().add(createHeaderForSection(subSection.subSectionName()));
                primaryPage.getStyleClass().add("background-primary");

                HBox rows = new HBox();
                rows.getStyleClass().add("hbox-spacing");

                for (ButtonDTO command : subSection.commands()) {
                    VisibilitySettingKey key = new VisibilitySettingKey(section.sectionName(), subSection.subSectionName(), command.buttonName());
                    if (!HiddenElementSettingsController.isElementVisible(key, visibilitySettings)) {
                        continue;
                    }
                    rows.getChildren().add(createButton(command));
                }
                primaryPage.getChildren().add(rows);
            }
        }
    }

    private VBox createHeaderForSection(String sectionName) {
        VBox section = new VBox();
        Text text = new Text(sectionName);
        text.getStyleClass().add("text-header");
        section.getChildren().add(text);
        return section;
    }

    private Button createButton(ButtonDTO buttonDTO) {
        Button button = new Button(buttonDTO.buttonName());
        button.getStyleClass().add("button-default");
        button.setOnMouseEntered(e -> button.getStyleClass().add("button-selected"));
        button.setOnMouseExited(e -> button.getStyleClass().remove("button-selected"));
        button.setTooltip(createTooltip(buttonDTO.description()));
        switch (buttonDTO.elementType()) {
            case BASH:
            case POWERSHELL:
                addButtonListenerForServiceCommands(button, buttonDTO);
                break;
            case LINK:
                button.setOnMouseClicked(event -> LinkUtils.openPageInBrowser(buttonDTO.command()));
                break;
            default:
                throw new RuntimeException("Unrecognised element type provided: " + buttonDTO.elementType());
        }
        return button;
    }

    private void addButtonListenerForServiceCommands(Button button, ButtonDTO buttonDTO) {
        switch (buttonDTO.elementType()) {
            case BASH:
                addButtonListenerForBashCommand(button, buttonDTO);
                break;
            case POWERSHELL:
                addButtonListenerForPowerShellCommand(button, buttonDTO);
                break;
            default:
                throw new RuntimeException("Not recognised console: " + buttonDTO.elementType());
        }
    }

    private void addButtonListenerForBashCommand(Button button, ButtonDTO buttonDTO) {
        button.setOnMouseClicked(event -> {
            if (buttonDTO.popupInputDisplayed()) {
                Optional<String> result = createTextInputDialog(buttonDTO.popupInputMessage());
                result.ifPresent(name -> {
                    GitBashService.runCommand(buttonDTO.scriptLocationParamName(), buttonDTO.autoCloseConsole(), buttonDTO.command() + " " + name);
                });
            } else {
                GitBashService.runCommand(buttonDTO.scriptLocationParamName(), buttonDTO.autoCloseConsole(), buttonDTO.command());
            }
        });
    }

    private void addButtonListenerForPowerShellCommand(Button button, ButtonDTO buttonDTO) {
        button.setOnMouseClicked(event -> {
            if (buttonDTO.popupInputDisplayed()) {
                Optional<String> result = createTextInputDialog(buttonDTO.popupInputMessage());
                result.ifPresent(name -> {
                    PowerShellService.runCommand(buttonDTO.scriptLocationParamName(), buttonDTO.autoCloseConsole(), buttonDTO.command() + " " + name);
                });
            } else {
                PowerShellService.runCommand(buttonDTO.scriptLocationParamName(), buttonDTO.autoCloseConsole(), buttonDTO.command());
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

    private Tooltip createTooltip(String tooltipText) {
        Tooltip tt = new Tooltip(tooltipText);
        tt.getStyleClass().add("tooltip-custom");
        tt.setShowDelay(Duration.ONE);
        tt.setShowDuration(Duration.INDEFINITE);
        return tt;
    }

    private boolean isAnyElementInSectionEnabled(String fileToLoad, SettingsDTO visibilitySettings) {
        List<SectionDTO> sections = LoadFromJsonService.load(fileToLoad);
        for (SectionDTO section : sections) {
            for (SubSectionDTO subSection : section.subSections()) {
                for (ButtonDTO command : subSection.commands()) {
                    VisibilitySettingKey key = new VisibilitySettingKey(section.sectionName(), subSection.subSectionName(), command.buttonName());
                    boolean visible = HiddenElementSettingsController.isElementVisible(key, visibilitySettings);
                    if (visible) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isAnyElementInSubSectionEnabled(String fileName, String sectionString, String subSectionString, SettingsDTO visibilitySettings) {
        Optional<SectionDTO> sectionOptional = LoadFromJsonService
                .load(fileName).stream().filter(sectionTmp -> sectionTmp.sectionName().equals(sectionString)).findFirst();
        if (sectionOptional.isEmpty()) {
            return false;
        }
        Optional<SubSectionDTO> subSectionOptional = sectionOptional
                .get().subSections().stream().filter(subSection -> subSection.subSectionName().equals(subSectionString)).findFirst();
        if (subSectionOptional.isEmpty()) {
            return false;
        }
        for (ButtonDTO command : subSectionOptional.get().commands()) {
            VisibilitySettingKey key = new VisibilitySettingKey(sectionString, subSectionString, command.buttonName());
            boolean visible = HiddenElementSettingsController.isElementVisible(key, visibilitySettings);
            if (visible) {
                return true;
            }
        }
        return false;
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
        SettingsController controller = new SettingsController(this::loadContent, this::resizeMainWindow, this::checkForUpdates);
        controller.setupPage();
    }

    @FXML
    private void handlePinJarFileToTaskBar() {
        LinkUtils.openPageInBrowser(Link.PIN_JAR_TO_TASKBAR.getUrl());
    }

    @FXML
    private void handleNews() {
        LinkUtils.openPageInBrowser(Link.GH_RELEASES.getUrl());
    }

    @FXML
    private void handleReportIssues() {
        LinkUtils.openPageInBrowser(Link.ISSUES.getUrl());
    }

    @FXML
    private void handleGithubProject() {
        LinkUtils.openPageInBrowser(Link.GH_PROJECT.getUrl());
    }

    @FXML
    private void handleGithubDocumentation() {
        LinkUtils.openPageInBrowser(Link.DOCUMENTATION.getUrl());
    }

    @FXML
    private void handleGithubAboutAuthor() {
        LinkUtils.openPageInBrowser(Link.ABOUT_AUTHOR.getUrl());
    }

    @FXML
    private void handleCheckForUpdates() {
        checkForUpdates();
        if (AppVersionService.isNewVersionAvailable()) {
            handleDownloadAndInstall();
//            ConfirmationPopupController confirmationPopupController =
//                    new ConfirmationPopupController("Everything up to date!");
//            confirmationPopupController.setupPage();
        } else {
            ConfirmationPopupController confirmationPopupController =
                    new ConfirmationPopupController("Everything up to date!");
            confirmationPopupController.setupPage();
        }
    }

    @FXML
    private void handleDownloadAndInstall() {
        UpdateController controller = new UpdateController();
        controller.setupPage();
    }

    @FunctionalInterface
    public interface CheckForUpdates {
        void checkForUpdates();
    }

    private void checkForUpdates() {
        AppVersionService.checkForUpdates();

        boolean newerVersionPresent = AppVersionService.isNewVersionAvailable();
        downloadAndInstall.setDisable(!newerVersionPresent);
        updateNotification.setVisible(newerVersionPresent);
        String updateButtonText = DOWNLOAD_UPDATE_BUTTON_BASE_TEXT;
        if (newerVersionPresent) {
            updateButtonText += " " + AppVersionService.getLatestVersion();
        }
        downloadAndInstall.setText(updateButtonText);
    }

}
