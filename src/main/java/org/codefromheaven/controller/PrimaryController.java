package org.codefromheaven.controller;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.codefromheaven.dto.LoadedElement;
import org.codefromheaven.dto.ElementType;
import org.codefromheaven.dto.ScriptType;
import org.codefromheaven.dto.Setting;
import org.codefromheaven.service.AnimalService;
import org.codefromheaven.service.LoadFromCsvService;
import org.codefromheaven.service.GitBashService;
import org.codefromheaven.service.PowerShellService;
import org.codefromheaven.service.SettingsService;

import static javafx.stage.Modality.APPLICATION_MODAL;

public class PrimaryController implements Initializable {

    private static final int SPACING_BETWEEN_BUTTONS = 5;
    private static final int SIZE_OF_AUTHOR_IMAGE_IN_PIXELS = 50;

    private static final String COMMON_BUTTON_STYLES = "-fx-text-fill: #000000; -fx-font-size: 12px;";

    private static final String BUTTON_STYLES = "-fx-background-color: #E39100FF; " + COMMON_BUTTON_STYLES;
    private static final String BUTTON_SELECTED_STYLES = "-fx-background-color: #00b6ae; " + COMMON_BUTTON_STYLES;
    private static final String TOOLTIP_STYLES = "-fx-text-fill: red;";
    private static final Color BACKGROUND_COLOR = Color.rgb(33,33,33);

    @FXML
    private MenuItem changeVisibleElements;

    @FXML
    private ScrollPane mainScrollPane;

    @FXML
    public VBox primaryPage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.loadContent();
    }

    private void loadContent() {
        primaryPage.getChildren().clear();
        setupScrollPane();

        Map<String, Map<String, Boolean>> visibilitySettings = SettingsService.loadVisibilitySettings();

        if (isAnyElementInSectionEnabled(ElementType.SERVICE_COMMANDS, visibilitySettings)) {
            addSectionHeader("Commands to invoke on environment");
            addElementsToScene(ElementType.SERVICE_COMMANDS, visibilitySettings);
        }

        if (isAnyElementInSectionEnabled(ElementType.UPDATE_DAP_FOR_TEST_COMMANDS, visibilitySettings)) {
            addSectionHeader("Commands to invoke for replacing DapKeys for local tests");
            addElementsToScene(ElementType.UPDATE_DAP_FOR_TEST_COMMANDS, visibilitySettings);
        }

        if (isAnyElementInSectionEnabled(ElementType.LINKS, visibilitySettings)) {
            addSectionHeader("Links");
            addElementsToScene(ElementType.LINKS, visibilitySettings);
        }

        if (isAnyElementInSectionEnabled(ElementType.OPEN_REMOTE_APPS, visibilitySettings)) {
            addSectionHeader("Remote apps");
            addElementsToScene(ElementType.OPEN_REMOTE_APPS, visibilitySettings);
        }

        if (isAnyElementInSectionEnabled(ElementType.SKAT_VPN, visibilitySettings)) {
            addSectionHeader("SKAT VPN");
            addElementsToScene(ElementType.SKAT_VPN, visibilitySettings);
        }

        addAuthorNote("Made with love by Szymon Gross");
    }

    private void setupScrollPane() {
        String maxWindowHeightString = SettingsService.getVariable(Setting.MAX_WINDOW_HEIGHT);
        int maxWindowHeight = Integer.parseInt(maxWindowHeightString);
        mainScrollPane.setMaxHeight(maxWindowHeight);
        mainScrollPane.setFitToHeight(true);
        mainScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        String maxWindowWidthString = SettingsService.getVariable(Setting.MAX_WINDOW_WIDTH);
        int maxWindowWidth = Integer.parseInt(maxWindowWidthString);
        mainScrollPane.setMaxWidth(maxWindowWidth);
        mainScrollPane.setFitToWidth(true);
        mainScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    }

    private void addSectionHeader(String headerName) {
        VBox section = new VBox();
        Text text = new Text(headerName);
        text.getStyleClass().add("text-main-header");
        section.getChildren().add(text);
        primaryPage.getChildren().add(section);
    }

    private void addAuthorNote(String authorName) {
        VBox section = new VBox();
        section.setAlignment(Pos.CENTER);
        ImageView imageView = new ImageView(AnimalService.getInstance().getCurrentAnimalImage());
        imageView.setFitHeight(SIZE_OF_AUTHOR_IMAGE_IN_PIXELS);
        imageView.setFitWidth(SIZE_OF_AUTHOR_IMAGE_IN_PIXELS);
        Tooltip.install(imageView, createTooltip(authorName));
        imageView.setOnMouseClicked(event -> {
            AnimalService.getInstance().drawNewRandomAnimal();
            imageView.setImage(AnimalService.getInstance().getCurrentAnimalImage());
        });
        Group root = new Group(imageView);
        section.getChildren().add(root);
        primaryPage.getChildren().add(section);
    }

    private void addElementsToScene(ElementType type, Map<String, Map<String, Boolean>> visibilitySettings) {
        List<LoadedElement> loadedElements = loadElementsFromCsvFile(type);
        addElementsToScene(loadedElements, type, visibilitySettings);
    }

    private void addElementsToScene(
            List<LoadedElement> loadedElements, ElementType type,
            Map<String, Map<String, Boolean>> visibilitySettings
    ) {
        Set<AbstractMap.SimpleEntry<Integer, String>> uniqueSectionsSet =
                loadedElements.stream()
                              .map(elem -> new AbstractMap.SimpleEntry<>(elem.getSectionDisplayOrder(), elem.getSectionName()))
                              .collect(Collectors.toSet());

        List<AbstractMap.SimpleEntry<Integer, String>> uniqueSections = new ArrayList<>(uniqueSectionsSet);
        uniqueSections.sort(Map.Entry.comparingByKey());

        for (AbstractMap.SimpleEntry<Integer, String> section : uniqueSections) {
            if (!isAnyElementInSubSectionEnabled(type, section.getValue(), visibilitySettings)) {
                continue;
            }
            primaryPage.getChildren().add(createHeaderForSection(section.getValue()));
            primaryPage.setBackground(new Background(new BackgroundFill(BACKGROUND_COLOR, CornerRadii.EMPTY, Insets.EMPTY)));

            HBox rows = new HBox();
            rows.setSpacing(SPACING_BETWEEN_BUTTONS);

            List<LoadedElement> sectionElements = loadedElements.stream()
                                                                .filter(elem -> elem.getSectionName().equals(section.getValue()))
                                                                .sorted(Comparator.comparing(LoadedElement::getCommandOrder))
                                                                .collect(Collectors.toList());
            for (LoadedElement loadedElement : sectionElements) {
                if (!isElementVisible(loadedElement, visibilitySettings)) {
                    continue;
                }
                rows.getChildren().add(createButton(
                        loadedElement.getButtonName(), loadedElement.getCommand(), loadedElement.isPopupInputDisplayed(),
                        loadedElement.getPopupInputMessage(), loadedElement.getDescription(), type));
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

    private Button createButton(String buttonName, String command, boolean popupInputDisplayed,
            String popupInputMessage, String description, ElementType type) {
        Button button = new Button(buttonName);
        button.setStyle(BUTTON_STYLES);
        button.setMinWidth(Region.USE_PREF_SIZE);
        button.setMaxWidth(Region.USE_PREF_SIZE);
        button.setOnMouseEntered(e -> button.setStyle(BUTTON_SELECTED_STYLES));
        button.setOnMouseExited(e -> button.setStyle(BUTTON_STYLES));
        if (ElementType.SERVICE_COMMANDS.equals(type)) {
            addButtonListenerForServiceCommands(button, popupInputDisplayed, popupInputMessage, command, ScriptType.SERVICE_SCRIPT);
        } else if (ElementType.UPDATE_DAP_FOR_TEST_COMMANDS.equals(type)) {
            addButtonListenerForServiceCommands(button, popupInputDisplayed, popupInputMessage, command, ScriptType.UPDATE_DAP_FOR_TESTS_SCRIPT);
        } else if (ElementType.LINKS.equals(type)) {
            button.setOnMouseClicked(event -> openPageInBrowser(command));
        } else if (ElementType.OPEN_REMOTE_APPS.equals(type)) {
            addButtonListenerForServiceCommands(button, popupInputDisplayed, popupInputMessage, command, ScriptType.OPEN_REMOTE_APP_SCRIPT);
        } else if (ElementType.SKAT_VPN.equals(type)) {
            addButtonListenerForServiceCommands(button, popupInputDisplayed, popupInputMessage, command, ScriptType.SKAT_VPN_SCRIPT);
        } else {
            throw new RuntimeException("Unrecognised element type provided: " + type);
        }
        button.setTooltip(createTooltip(description));
        return button;
    }

    private void addButtonListenerForServiceCommands(Button button, boolean popupInputDisplayed, String popupInputMessage, String command, ScriptType scriptType) {
        switch (scriptType.getConsole()) {
            case BASH:
                addButtonListenerForBashCommand(button, popupInputDisplayed, popupInputMessage, command, scriptType);
                break;
            case POWERSHELL:
                addButtonListenerForPowerShellCommand(button, popupInputDisplayed, popupInputMessage, command, scriptType);
                break;
            default:
                throw new RuntimeException("Not recognised console: " + scriptType.getConsole());
        }
    }

    private void addButtonListenerForBashCommand(Button button, boolean popupInputDisplayed, String popupInputMessage, String command, ScriptType scriptType) {
        button.setOnMouseClicked(event -> {
            if (popupInputDisplayed) {
                Optional<String> result = createTextInputDialog(popupInputMessage);
                result.ifPresent(name -> {
                    GitBashService.runCommand(scriptType, command + " " + name);
                });
            } else {
                GitBashService.runCommand(scriptType, command);
            }
        });
    }

    private void addButtonListenerForPowerShellCommand(Button button, boolean popupInputDisplayed, String popupInputMessage, String command, ScriptType scriptType) {
        button.setOnMouseClicked(event -> {
            if (popupInputDisplayed) {
                Optional<String> result = createTextInputDialog(popupInputMessage);
                result.ifPresent(name -> {
                    PowerShellService.runCommand(scriptType, command + " " + name);
                });
            } else {
                PowerShellService.runCommand(scriptType, command);
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
        Tooltip tt = new Tooltip();
        tt.setStyle(TOOLTIP_STYLES);
        tt.setShowDelay(Duration.ONE);
        tt.setShowDuration(Duration.INDEFINITE);
        tt.setText(tooltipText);
        return tt;
    }

    private List<LoadedElement> loadElementsFromCsvFile(ElementType type) {
        try {
            return LoadFromCsvService.load(type);
        } catch (IOException e) {
            throw new RuntimeException("Cannot load files from configuration file", e);
        }
    }

    @FXML
    private void handleChangeVisibleElements() {
        Stage settingsStage = new Stage();
        settingsStage.initModality(APPLICATION_MODAL);
        settingsStage.getIcons().add(AnimalService.getInstance().getRandomAnimalImage());
        VBox settingsRoot = new VBox(10);

        ScrollPane scrollPane = new ScrollPane();
        VBox contentBox = new VBox(10);
        contentBox.setPadding(new Insets(10));

        List<LoadedElement> allElements = loadAllElements();
        Map<String, Map<String, Boolean>> visibilitySettings = SettingsService.loadVisibilitySettings();

        allElements.stream()
                   .collect(Collectors.groupingBy(LoadedElement::getSectionName, LinkedHashMap::new, Collectors.toList()))
                   .forEach((sectionName, elements) -> {
                       VBox sectionBox = new VBox(5);
                       sectionBox.getChildren().add(new Label(sectionName));
                       elements.sort(Comparator.comparingInt(LoadedElement::getSectionDisplayOrder)
                                               .thenComparingInt(LoadedElement::getCommandOrder));
                       for (LoadedElement element : elements) {
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

        scrollPane.setContent(contentBox);
        settingsRoot.getChildren().add(scrollPane);

        Scene scene = new Scene(settingsRoot, 400, 600);
        settingsStage.setTitle("Settings - Change visibility of elements");
        settingsStage.setScene(scene);

        settingsStage.setOnHidden(event -> {
            loadContent();
            resizeMainWindow();
        });
        settingsStage.showAndWait();
    }


    private void resizeMainWindow() {
        Stage mainStage = (Stage) primaryPage.getScene().getWindow();
        mainStage.sizeToScene();
    }

    private List<LoadedElement> loadAllElements() {
        List<LoadedElement> allElements = new ArrayList<>();
        allElements.addAll(loadElementsFromCsvFile(ElementType.SERVICE_COMMANDS));
        allElements.addAll(loadElementsFromCsvFile(ElementType.UPDATE_DAP_FOR_TEST_COMMANDS));
        allElements.addAll(loadElementsFromCsvFile(ElementType.LINKS));
        allElements.addAll(loadElementsFromCsvFile(ElementType.OPEN_REMOTE_APPS));
        allElements.addAll(loadElementsFromCsvFile(ElementType.SKAT_VPN));
        return allElements;
    }

    private void updateVisibilitySetting(LoadedElement element, boolean newVal) {
        SettingsService.updateVisibilitySetting(element.getSectionName(), element.getButtonName(), newVal);
    }

    private boolean isElementVisible(LoadedElement element, Map<String, Map<String, Boolean>> visibilitySettings) {
        return visibilitySettings
                .getOrDefault(element.getSectionName(), new HashMap<>())
                .getOrDefault(element.getButtonName(), true);
    }

    private boolean isAnyElementInSectionEnabled(ElementType section, Map<String, Map<String, Boolean>> visibilitySettings) {
        return loadElementsFromCsvFile(section)
                .stream().anyMatch(elem -> isElementVisible(elem, visibilitySettings));
    }

    private boolean isAnyElementInSubSectionEnabled(ElementType section, String subsection, Map<String, Map<String, Boolean>> visibilitySettings) {
        return loadElementsFromCsvFile(section)
                .stream().filter(elem -> Objects.equals(elem.getSectionName(), subsection))
                .anyMatch(elem -> isElementVisible(elem, visibilitySettings));
    }

}
