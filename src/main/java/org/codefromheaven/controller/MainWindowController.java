package org.codefromheaven.controller;

import java.net.URL;
import java.util.*;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.commons.lang3.StringUtils;
import org.codefromheaven.App;
import org.codefromheaven.dto.ElementType;
import org.codefromheaven.dto.Link;
import org.codefromheaven.dto.data.ButtonDTO;
import org.codefromheaven.dto.data.SectionDTO;
import org.codefromheaven.dto.data.SubSectionDTO;
import org.codefromheaven.dto.data.LayoutAndButtonsDTO;
import org.codefromheaven.service.settings.VisibilitySettings;
import org.codefromheaven.helpers.ImageLoader;
import org.codefromheaven.helpers.LinkUtils;
import org.codefromheaven.helpers.MaxHighUtils;
import org.codefromheaven.service.animal.AnimalService;
import org.codefromheaven.service.command.GitBashService;
import org.codefromheaven.service.command.PowerShellService;
import org.codefromheaven.service.network.NetworkService;
import org.codefromheaven.service.settings.LayoutService;
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
    public MainWindowController(AnimalService animalService, SettingsService settingsService,
            AppVersionService appVersionService, NetworkService networkService, StyleService styleService) {
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
    private ImageView updateNotification;
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
        LayoutAndButtonsDTO layoutAndButtons = SpringContext.getBean(LayoutService.class).load();

        boolean anyElementEnabled = false;
        if (layoutAndButtons != null && layoutAndButtons.layout() != null) {
            if (isAnyElementInSectionsEnabled(layoutAndButtons.layout(), visibilitySettings)) {
                addElementsToScene(layoutAndButtons.layout(), visibilitySettings);
                anyElementEnabled = true;
            }
        }

        if (!anyElementEnabled) {
            addInformationAboutBuildingConfiguration(visibilitySettings);
        }

        addAuthorNote();

        double requiredWidth = calculateWidthWithOverhead(calculateMaxRowWidth());
        double screenWidth = Screen.getPrimary().getVisualBounds().getWidth();
        double finalWidth = Math.min(requiredWidth, screenWidth - 100);
        mainScrollPane.setPrefWidth(finalWidth);
    }

    private double calculateMaxRowWidth() {
        double maxWidth = 0;
        for (Node sectionNode : primaryPage.getChildren()) {
            if (sectionNode.getStyleClass().contains("author-note")) {
                continue;
            }

            if (sectionNode instanceof FlowPane flowPane) {
                double rowWidth = 0;
                for (Node btnNode : flowPane.getChildren()) {
                    if (btnNode instanceof Button btn) {
                        Text textNode = new Text(btn.getText());
                        textNode.setFont(Font.font("Roboto", FontWeight.BOLD, 12));
                        rowWidth += calculateWidthWithOverhead(textNode.getLayoutBounds().getWidth());
                    }
                }
                rowWidth += Math.max(0, flowPane.getChildren().size() - 1) * flowPane.getHgap();
                maxWidth = Math.max(maxWidth, rowWidth);
            }
            else if (sectionNode instanceof VBox) {
                for (Node innerNode : ((VBox) sectionNode).getChildren()) {
                    if (innerNode instanceof Text) {
                        Text textNode = new Text(((Text) innerNode).getText());
                        textNode.setFont(Font.font("Roboto", FontWeight.BOLD, 18));
                        maxWidth = Math.max(maxWidth, textNode.getLayoutBounds().getWidth());
                    }
                }
            }
        }
        return Math.max(maxWidth, App.MIN_WIDTH);
    }

    private double calculateWidthWithOverhead(double screenWidth) {
        return screenWidth + 20;
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

    private void addAuthorNote() {
        VBox section = new VBox();
        section.getStyleClass().add("author-note");
        ImageView authorImageView = new ImageView(animalService.getCurrentAnimalImage());
        authorImageView.getStyleClass().add("author-image");
        Tooltip.install(authorImageView, createTooltip("Made with love by SJG"));
        authorImageView.setOnMouseClicked(event -> {
            animalService.replaceCurrentAnimalToNextAnimal();
            authorImageView.setImage(animalService.getCurrentAnimalImage());
        });

        Group root = new Group(authorImageView);
        section.getChildren().add(root);
        primaryPage.getChildren().add(section);
    }

    private void addElementsToScene(List<SectionDTO> loadedElements, VisibilitySettings visibilitySettings) {
        addElementsToSceneBase(loadedElements, visibilitySettings);
    }

    private void addElementsToSceneBase(
            List<SectionDTO> sections, VisibilitySettings visibilitySettings) {

        double screenWidth = Screen.getPrimary().getVisualBounds().getWidth();

        for (SectionDTO section : sections) {
            primaryPage.getStyleClass().add("primary-page");

            if (!isAnyElementInSectionsEnabled(Collections.singletonList(section), visibilitySettings)) {
                continue;
            }

            addSectionHeader(section.sectionName());

            for (SubSectionDTO subSection : section.subSections()) {
                if (!isAnyElementInSubSectionEnabled(subSection, visibilitySettings)) {
                    continue;
                }
                primaryPage.getChildren().add(createHeaderForSection(subSection.subSectionName()));
                primaryPage.getStyleClass().add("background-primary");

                FlowPane rows = new FlowPane();
                rows.setHgap(5);
                rows.setVgap(5);
                rows.setAlignment(Pos.CENTER_LEFT);
                rows.setPrefWrapLength(screenWidth - 100);

                for (ButtonDTO buttonDTO : subSection.buttons()) {
                    if (!visibilitySettings.isVisible(buttonDTO)) {
                        continue;
                    }
                    Button button = createButton(buttonDTO);
                    button.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
                    rows.getChildren().add(button);
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
        Button button = new Button(buttonDTO.getButtonName());
        button.getStyleClass().add("button-default");
        if (StringUtils.isNotBlank(buttonDTO.getDescription())) {
            button.setTooltip(createTooltip(buttonDTO.getDescription()));
        }
        switch (buttonDTO.getElementType()) {
            case BASH:
            case POWERSHELL:
            case PYTHON:
            case DIRECT_COMMAND_BASH:
            case DIRECT_COMMAND_POWERSHELL:
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
            case DIRECT_COMMAND_BASH:
            case PYTHON:
                addButtonListenerForBashCommand(button, buttonDTO);
                break;
            case POWERSHELL:
            case DIRECT_COMMAND_POWERSHELL:
                addButtonListenerForPowerShellCommand(button, buttonDTO);
                break;
            default:
                throw new RuntimeException("Not recognised console: " + buttonDTO.getElementType());
        }
    }

    private String addPrefixToCommand(String command, ElementType elementType) {
        return switch (elementType) {
            case BASH -> "./" + command;
            case POWERSHELL -> ".\\" + command;
            case PYTHON -> settingsService.getPythonScriptsPrefix() + " " + command;
            default -> command;
        };
    }

    private void addButtonListenerForBashCommand(Button button, ButtonDTO buttonDTO) {
        button.setOnMouseClicked(event -> {
            if (buttonDTO.getSingleInputPopup() != null) {
                Optional<String> result = createTextInputDialog(
                        buttonDTO.getSingleInputPopup().getMessage(),
                        buttonDTO.getSingleInputPopup().getDefaultValue());
                result.ifPresent(name -> {
                    for (String command : buttonDTO.getCommands()) {
                        String prefixedCommand = addPrefixToCommand(command, buttonDTO.getElementType());
                        GitBashService.runCommand(buttonDTO.getScriptLocationParamName(),
                                                  buttonDTO.isAutoCloseConsole(), prefixedCommand + " " + name);
                    }
                });
            } else {
                for (String command : buttonDTO.getCommands()) {
                    String prefixedCommand = addPrefixToCommand(command, buttonDTO.getElementType());
                    GitBashService.runCommand(buttonDTO.getScriptLocationParamName(), buttonDTO.isAutoCloseConsole(),
                                              prefixedCommand);
                }
            }
        });
    }

    private void addButtonListenerForPowerShellCommand(Button button, ButtonDTO buttonDTO) {
        button.setOnMouseClicked(event -> {
            if (buttonDTO.getSingleInputPopup() != null) {
                Optional<String> result = createTextInputDialog(
                        buttonDTO.getSingleInputPopup().getMessage(),
                        buttonDTO.getSingleInputPopup().getDefaultValue());
                result.ifPresent(name -> {
                    for (String command : buttonDTO.getCommands()) {
                        String prefixedCommand = addPrefixToCommand(command, buttonDTO.getElementType());
                        PowerShellService.runCommand(buttonDTO.getScriptLocationParamName(),
                                                     buttonDTO.isAutoCloseConsole(), prefixedCommand + " " + name);
                    }
                });
            } else {
                for (String command : buttonDTO.getCommands()) {
                    String prefixedCommand = addPrefixToCommand(command, buttonDTO.getElementType());
                    PowerShellService.runCommand(buttonDTO.getScriptLocationParamName(), buttonDTO.isAutoCloseConsole(),
                                                 prefixedCommand);
                }
            }
        });
    }

    private Optional<String> createTextInputDialog(String popupInputMessage, String defaultValue) {
        TextInputDialog dialog = new TextInputDialog(defaultValue);

        dialog.setTitle("Information required");
        dialog.setHeaderText(null);
        dialog.setGraphic(null);
        dialog.setContentText(popupInputMessage);
        Stage dialogStage = (Stage) dialog.getDialogPane().getScene().getWindow();
        dialogStage.getIcons().add(animalService.getNextAnimalOrRandomIfNotPresent());
        return dialog.showAndWait();
    }

    private Tooltip createTooltip(String tooltipText) {
        Tooltip tt = new Tooltip(tooltipText);
        tt.getStyleClass().add("tooltip-custom");
        tt.setShowDelay(Duration.ONE);
        tt.setShowDuration(Duration.INDEFINITE);
        return tt;
    }

    private boolean isAnyElementInSectionsEnabled(
            List<SectionDTO> sections, VisibilitySettings visibilitySettings) {
        for (SectionDTO section : sections) {
            for (SubSectionDTO subSection : section.subSections()) {
                boolean anyElementInSubSectionEnabled = isAnyElementInSubSectionEnabled(
                        subSection, visibilitySettings);
                if (anyElementInSubSectionEnabled) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isAnyElementInSubSectionEnabled(
            SubSectionDTO subSection,
            VisibilitySettings visibilitySettings) {
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
        if (primaryPage.getScene() == null || primaryPage.getScene().getWindow() == null) {
            return;
        }

        Stage mainStage = (Stage) primaryPage.getScene().getWindow();
        mainStage.sizeToScene();

        if (mainStage.getHeight() > MaxHighUtils.getMaxHeight()) {
            mainStage.setHeight(MaxHighUtils.getMaxHeight());
        }

        mainStage.centerOnScreen();
    }

    @FXML
    private void handleChangeVisibleElements() {
        HiddenElementSettingsController controller = new HiddenElementSettingsController(
                primaryPage.getScene().getWindow(),
                this::loadContent,
                                                                                         this::resizeMainWindow);
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
        SettingsController controller = new SettingsController(
                primaryPage.getScene().getWindow(),
                this::loadContent, this::resizeMainWindow,
                                                               this::checkForUpdates, this::reloadStyle);
        controller.setupPage();
    }

    @FXML
    private void handleEditLayoutAndButtons() {
        LayoutAndButtonsEditorController controller = new LayoutAndButtonsEditorController(
                primaryPage.getScene().getWindow(),
                this::loadContent,
                                                                                           this::resizeMainWindow);
        controller.setupPage();
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
            triggerDownloadAndInstallPopup();
        } else {
            if (networkService.isNetworkPresent()) {
                PopupController.showPopup(primaryPage.getScene().getWindow(), "Everything up to date!", Alert.AlertType.INFORMATION);
            } else {
                networkService.showPopupNetworkNotPresent(primaryPage.getScene().getWindow());
            }
        }
    }

    @FXML
    private void handleDownloadAndInstall() {
        if (!appVersionService.isNewVersionAvailable()) {
            checkForUpdates();
        }

        if (appVersionService.isNewVersionAvailable()) {
            triggerDownloadAndInstallPopup();
        }
    }

    private void triggerDownloadAndInstallPopup() {
        PopupController.showConfirmationPopup(
                primaryPage.getScene().getWindow(),
                "New version " + appVersionService.getLatestVersion()
                        + " is available.\nDo you want to download and install it now?",
                "Download and install",
                () -> {
                    UpdateController controller = SpringContext.getBean(UpdateController.class);
                    controller.setupPage(primaryPage.getScene().getWindow());
                });
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
        ButtonDTO exampleConfig = new ButtonDTO("Example configuration", "",
                                                Collections.singletonList(Link.WIKI.getUrl()),
                                                ElementType.LINK, true, null,
                                                "Open link in default browser", true, sectionName, subSectionName);
        ButtonDTO buildYourOwnConfig = new ButtonDTO("Build your own configuration", "",
                                                     Collections.singletonList(Link.WIKI_CONFIGURATION.getUrl()),
                                                     ElementType.LINK, true, null,
                                                     "Open link in default browser", true, sectionName, subSectionName);
        SubSectionDTO subSection = new SubSectionDTO(subSectionName, Arrays.asList(exampleConfig, buildYourOwnConfig));
        SectionDTO section = new SectionDTO(sectionName, Collections.singletonList(subSection));
        addElementsToScene(Collections.singletonList(section), visibilitySettings);
    }
}