package org.codefromheaven.controller;

import java.net.URL;
import java.util.*;

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
import org.codefromheaven.App;
import org.codefromheaven.dto.ElementType;
import org.codefromheaven.dto.Link;
import org.codefromheaven.dto.data.ButtonDTO;
import org.codefromheaven.dto.data.SectionDTO;
import org.codefromheaven.dto.data.SubSectionDTO;
import org.codefromheaven.dto.settings.KeyValueDTO;
import org.codefromheaven.dto.settings.SettingsDTO;
import org.codefromheaven.service.settings.VisibilitySettings;
import org.codefromheaven.helpers.ImageLoader;
import org.codefromheaven.helpers.LinkUtils;
import org.codefromheaven.helpers.MaxHighUtils;
import org.codefromheaven.service.LoadFromJsonService;
import org.codefromheaven.service.animal.AnimalService;
import org.codefromheaven.service.command.GitBashService;
import org.codefromheaven.service.command.PowerShellService;
import org.codefromheaven.service.network.NetworkService;
import org.codefromheaven.service.settings.FilesToLoadSettingsService;
import org.codefromheaven.service.settings.SettingsService;
import org.codefromheaven.context.SpringContext;
import org.codefromheaven.service.style.StyleService;
import org.codefromheaven.service.version.AppVersionService;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Autowired;

@Component
public class MainWindowController implements Initializable {

    private static final String DOWNLOAD_UPDATE_BUTTON_BASE_TEXT = "Download and install";
    private final AnimalService animalService;
    private final SettingsService settingsService;
    private final AppVersionService appVersionService;
    private final NetworkService networkService;
    private final StyleService styleService;

    @Autowired
    public MainWindowController(AnimalService animalService, SettingsService settingsService, AppVersionService appVersionService, NetworkService networkService, StyleService styleService) {
        this.animalService = animalService;
        this.settingsService = settingsService;
        this.appVersionService = appVersionService;
        this.networkService = networkService;
        this.styleService = styleService;
    }

    @FXML
    public VBox primaryPage;
    @FXML
    private ScrollPane mainScrollPane;

    @FXML
    private Menu menuUpdateButton;

    @FXML
    private MenuItem changeVisibleElements;
    @FXML
    private MenuItem changeSettings;
    @FXML
    private MenuItem pinBashFileToTaskBar;
    @FXML
    private MenuItem runAppWithWindowsStartup;

    @FXML
    private MenuItem news;
    @FXML
    private MenuItem reportIssues;
    @FXML
    private MenuItem githubProject;
    @FXML
    private MenuItem githubDocumentation;

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
        changeVisibleElements.setDisable(noSectionPresent);
        this.loadContent();
    }

    @FunctionalInterface
    public interface ContentLoader {
        void loadContent();
    }

    private void loadContent() {
        refreshHeader();
        checkForUpdates();
        primaryPage.getChildren().clear();
        setupScrollPane();

        VisibilitySettings visibilitySettings = new VisibilitySettings();
        SettingsDTO filesToLoad = FilesToLoadSettingsService.load();

        boolean anyElementEnabled = false;
        for (String fileToLoad : filesToLoad.getSettings().stream().map(KeyValueDTO::getKey).toList()) {
            if (isAnyElementInSectionEnabled(fileToLoad, visibilitySettings)) {
                addElementsToScene(fileToLoad, visibilitySettings);
                anyElementEnabled = true;
            }
        }
        if (!anyElementEnabled) {
            addInformationAboutBuildingConfiguration(visibilitySettings);
        }

        addAuthorNote("Made with love by SJG");
    }

    private void refreshHeader() {
        boolean allowedToUpdate = settingsService.isAllowedToUpdate();
        menuUpdateButton.setVisible(allowedToUpdate);
    }

    private void setupScrollPane() {
        mainScrollPane.setMaxHeight(MaxHighUtils.getMaxHeight());
        mainScrollPane.setFitToHeight(true);
        mainScrollPane.setMinWidth(App.MIN_WIDTH);
        mainScrollPane.setFitToWidth(true);
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
        section.getStyleClass().add("author-note");
        ImageView authorImageView = new ImageView(animalService.getCurrentAnimalImage());
        authorImageView.getStyleClass().add("author-image");
        Tooltip.install(authorImageView, createTooltip(authorNote));
        authorImageView.setOnMouseClicked(event -> {
            animalService.replaceCurrentAnimalToNextAnimal();
            authorImageView.setImage(animalService.getCurrentAnimalImage());
        });
        Group root = new Group(authorImageView);
        section.getChildren().add(root);
        primaryPage.getChildren().add(section);
    }

    private void addElementsToScene(String fileToLoad, VisibilitySettings visibilitySettings) {
        List<SectionDTO> loadedElements = LoadFromJsonService.load(fileToLoad);
        addSectionHeader(loadedElements.stream().findFirst().get().sectionName());
        addElementsToSceneBase(loadedElements, visibilitySettings);
    }

    private void addElementsToScene(List<SectionDTO> loadedElements, VisibilitySettings visibilitySettings) {
        addSectionHeader(loadedElements.stream().findFirst().get().sectionName());
        addElementsToSceneBase(loadedElements, visibilitySettings);
    }

    private void addElementsToSceneBase(
            List<SectionDTO> sections, VisibilitySettings visibilitySettings
    ) {
        for (SectionDTO section : sections) {
            primaryPage.getStyleClass().add("primary-page");
            for (SubSectionDTO subSection : section.subSections()) {
                if (!isAnyElementInSubSectionEnabled(subSection, section.sectionName(), visibilitySettings)) {
                    continue;
                }
                primaryPage.getChildren().add(createHeaderForSection(subSection.subSectionName()));
                primaryPage.getStyleClass().add("background-primary");

                HBox rows = new HBox();
                rows.getStyleClass().add("hbox-spacing");

                for (ButtonDTO button : subSection.buttons()) {
                    if (!visibilitySettings.isVisible(button)) {
                        continue;
                    }
                    rows.getChildren().add(createButton(button));
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
        Button button = new Button(buttonDTO.getName());
        button.getStyleClass().add("button-default");
        button.setTooltip(createTooltip(buttonDTO.getDescription()));
        switch (buttonDTO.getElementType()) {
            case BASH:
            case POWERSHELL:
            case PYTHON:
            case CUSTOM_COMMAND_BASH:
            case CUSTOM_COMMAND_POWERSHELL:
                addButtonListenerForServiceCommands(button, buttonDTO);
                break;
            case LINK:
                button.setOnMouseClicked(event -> {
                    for (String link : buttonDTO.getCommands()) {
                        LinkUtils.openPageInBrowser(link);
                    }
                });
                break;
            default:
                throw new RuntimeException("Unrecognised element type provided: " + buttonDTO.getElementType());
        }
        return button;
    }

    private void addButtonListenerForServiceCommands(Button button, ButtonDTO buttonDTO) {
        switch (buttonDTO.getElementType()) {
            case BASH:
            case CUSTOM_COMMAND_BASH:
            case PYTHON:
                addButtonListenerForBashCommand(button, buttonDTO);
                break;
            case POWERSHELL:
            case CUSTOM_COMMAND_POWERSHELL:
                addButtonListenerForPowerShellCommand(button, buttonDTO);
                break;
            default:
                throw new RuntimeException("Not recognised console: " + buttonDTO.getElementType());
        }
    }

    private void addButtonListenerForBashCommand(Button button, ButtonDTO buttonDTO) {
        button.setOnMouseClicked(event -> {
            if (buttonDTO.isPopupInputDisplayed()) {
                Optional<String> result = createTextInputDialog(buttonDTO.getPopupInputMessage());
                result.ifPresent(name -> {
                    for (String command : buttonDTO.getCommands()) {
                        GitBashService.runCommand(buttonDTO.getScriptLocationParamName(), buttonDTO.isAutoCloseConsole(), command + " " + name);
                    }
                });
            } else {
                for (String command : buttonDTO.getCommands()) {
                    GitBashService.runCommand(buttonDTO.getScriptLocationParamName(), buttonDTO.isAutoCloseConsole(), command);
                }
            }
        });
    }

    private void addButtonListenerForPowerShellCommand(Button button, ButtonDTO buttonDTO) {
        button.setOnMouseClicked(event -> {
            if (buttonDTO.isPopupInputDisplayed()) {
                Optional<String> result = createTextInputDialog(buttonDTO.getPopupInputMessage());
                result.ifPresent(name -> {
                    for (String command : buttonDTO.getCommands()) {
                        PowerShellService.runCommand(buttonDTO.getScriptLocationParamName(), buttonDTO.isAutoCloseConsole(), command + " " + name);
                    }
                });
            } else {
                for (String command : buttonDTO.getCommands()) {
                    PowerShellService.runCommand(buttonDTO.getScriptLocationParamName(), buttonDTO.isAutoCloseConsole(), command);
                }
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
        dialogStage.getIcons().add(animalService.getRandomAnimalImage()); // Update the path accordingly
        return dialog.showAndWait();
    }

    private Tooltip createTooltip(String tooltipText) {
        Tooltip tt = new Tooltip(tooltipText);
        tt.getStyleClass().add("tooltip-custom");
        tt.setShowDelay(Duration.ONE);
        tt.setShowDuration(Duration.INDEFINITE);
        return tt;
    }

    private boolean isAnyElementInSectionEnabled(
            String fileToLoad, VisibilitySettings visibilitySettings
    ) {
        List<SectionDTO> sections = LoadFromJsonService.load(fileToLoad);
        for (SectionDTO section : sections) {
            for (SubSectionDTO subSection : section.subSections()) {
                boolean anyElementInSubSectionEnabled = isAnyElementInSubSectionEnabled(
                        subSection, section.sectionName(), visibilitySettings);
                if (anyElementInSubSectionEnabled) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isAnyElementInSubSectionEnabled(
            SubSectionDTO subSection, String sectionName,
            VisibilitySettings visibilitySettings
    ) {
        for (ButtonDTO button : subSection.buttons()) {
            boolean visible = visibilitySettings.isVisible(button);
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

    @FunctionalInterface
    public interface StyleReloader {
        void reloadStyle();
    }

    private void reloadStyle() {
        primaryPage.getScene().getStylesheets().clear();
        primaryPage.getScene().getStylesheets().add(styleService.getCurrentStyleUrl());
    }

    @FXML
    private void handleChangeSettings() {
        SettingsController controller = new SettingsController(this::loadContent, this::resizeMainWindow, this::checkForUpdates, this::reloadStyle);
        controller.setupPage();
    }

    @FXML
    private void handlePinBashFileToTaskBar() {
        LinkUtils.openPageInBrowser(Link.PIN_BASH_TO_TASKBAR.getUrl());
    }

    @FXML
    private void handleRunAppWithWindowsStartup() {
        LinkUtils.openPageInBrowser(Link.RUN_BASH_ON_WINDOWS_STARTUP.getUrl());
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
        LinkUtils.openPageInBrowser(Link.WIKI.getUrl());
    }

    @FXML
    private void handleCheckForUpdates() {
        checkForUpdates();
        if (appVersionService.isNewVersionAvailable()) {
            handleDownloadAndInstall();
        } else {
            if (networkService.isNetworkPresent()) {
                PopupController.showPopup("Everything up to date!", Alert.AlertType.INFORMATION);
            } else {
                networkService.showPopupNetworkNotPresent();
            }
        }
    }

    @FXML
    private void handleDownloadAndInstall() {
        UpdateController controller = SpringContext.getBean(UpdateController.class);
        controller.setupPage();
    }

    @FunctionalInterface
    public interface CheckForUpdates {
        void checkForUpdates();
    }

    private void checkForUpdates() {
        appVersionService.checkForUpdates();

        boolean newerVersionPresent = appVersionService.isNewVersionAvailable();
        downloadAndInstall.setDisable(!newerVersionPresent);
        updateNotification.setVisible(newerVersionPresent);
        String updateButtonText = DOWNLOAD_UPDATE_BUTTON_BASE_TEXT;
        if (newerVersionPresent) {
            updateButtonText += " " + appVersionService.getLatestVersion();
        }
        downloadAndInstall.setText(updateButtonText);
    }

    private void addInformationAboutBuildingConfiguration(VisibilitySettings visibilitySettings) {
        String sectionName = "Looks like there is nothing to show";
        String subSectionName = "Read about configuration";
        ButtonDTO exampleConfig = new ButtonDTO("Example configuration", "", Collections.singletonList(Link.WIKI.getUrl()),
                                                ElementType.LINK, true, false, "",
                                                "Open link in default browser", true, sectionName, subSectionName);
        ButtonDTO buildYourOwnConfig = new ButtonDTO("Build your own configuration", "", Collections.singletonList(Link.WIKI_CONFIGURATION.getUrl()),
                                                     ElementType.LINK, true, false, "",
                                                     "Open link in default browser", true, sectionName, subSectionName);
        SubSectionDTO subSection = new SubSectionDTO(subSectionName, Arrays.asList(exampleConfig, buildYourOwnConfig));
        SectionDTO section = new SectionDTO(sectionName, Collections.singletonList(subSection));
        addElementsToScene(Collections.singletonList(section), visibilitySettings);
    }

}
