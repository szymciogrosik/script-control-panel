package org.codefromheaven.controller;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import org.codefromheaven.context.SpringContext;
import org.codefromheaven.dto.Setting;
import org.codefromheaven.dto.Style;
import org.codefromheaven.dto.settings.FieldOnPageDTO;
import org.codefromheaven.dto.settings.KeyValueDTO;
import org.codefromheaven.dto.settings.SettingsDTO;
import org.codefromheaven.resources.AnimalProvider;
import org.codefromheaven.service.animal.AnimalService;
import org.codefromheaven.service.settings.SettingsService;
import org.codefromheaven.service.style.StyleService;
import org.codefromheaven.service.settings.FilesToLoadSettingsService;
import org.codefromheaven.service.LoadFromJsonService;
import org.codefromheaven.dto.data.SectionDTO;
import org.codefromheaven.dto.data.SubSectionDTO;
import org.codefromheaven.dto.data.ButtonDTO;
import org.codefromheaven.dto.ElementType;
import org.codefromheaven.service.settings.SettingsServiceBase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Collections;
import java.util.Optional;
import java.io.File;
import java.io.IOException;

import static javafx.stage.Modality.APPLICATION_MODAL;

public class SettingsController {

    private final static Set<Setting> COMBO_FIELD_SETTINGS = Set.of(Setting.IMAGE_NAME, Setting.APP_STYLE);
    private final static Set<Setting> CHECKBOX_FIELD_SETTINGS = Set.of(Setting.ALLOW_FOR_UPGRADES, Setting.ALLOW_PRE_RELEASES);

    private final MainWindowController.ContentLoader loader;
    private final MainWindowController.ResizeWindow resizeMainWindow;
    private final MainWindowController.CheckForUpdates checkForUpdates;
    private final MainWindowController.StyleReloader styleReloader;

    public SettingsController(
            MainWindowController.ContentLoader loader, MainWindowController.ResizeWindow resizeMainWindow,
            MainWindowController.CheckForUpdates checkForUpdates, MainWindowController.StyleReloader styleReloader
    ) {
        this.loader = loader;
        this.resizeMainWindow = resizeMainWindow;
        this.checkForUpdates = checkForUpdates;
        this.styleReloader = styleReloader;
    }

    public void setupPage() {
        Stage settingsStage = new Stage();
        settingsStage.initModality(APPLICATION_MODAL);
        settingsStage.getIcons().add(SpringContext.getBean(AnimalService.class).getRandomAnimalImage());
        settingsStage.setTitle("Additional settings");
        settingsStage.setResizable(true); // Allow resizing for split pane

        TabPane tabPane = new TabPane();
        tabPane.getStyleClass().add("background-primary");

        Tab generalSettingsTab = createGeneralSettingsTab(settingsStage);
        Tab layoutEditorTab = createLayoutEditorTab();

        tabPane.getTabs().addAll(generalSettingsTab, layoutEditorTab);

        // Ensure tabs are not closable
        generalSettingsTab.setClosable(false);
        layoutEditorTab.setClosable(false);

        Scene scene = new Scene(tabPane, 800, 600); // Increased size for better layout editing
        scene.getStylesheets().add(SpringContext.getBean(StyleService.class).getCurrentStyleUrl());
        settingsStage.setScene(scene);
        settingsStage.showAndWait();
    }

    private Tab createGeneralSettingsTab(Stage settingsStage) {
        Tab tab = new Tab("General Settings");

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(20));
        gridPane.setVgap(10);
        gridPane.setHgap(10);
        gridPane.getStyleClass().add("background-primary");

        SettingsDTO configSettings = SpringContext.getBean(SettingsService.class).load();
        List<KeyValueDTO> settings = configSettings.getSettings().stream().filter(KeyValueDTO::isEditable).toList();

        FieldOnPageDTO valueFields = loadElementsToPage(settings, gridPane);

        Button saveButton = new Button("Save");
        saveButton.getStyleClass().add("button-default");
        saveButton.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(saveButton, Priority.ALWAYS);

        HBox buttonBox = new HBox(saveButton);
        buttonBox.setSpacing(10);
        buttonBox.setFillHeight(true);

        gridPane.add(buttonBox, 0, settings.size(), 2, 1);
        GridPane.setMargin(buttonBox, new Insets(10, 0, 0, 0));

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(gridPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setMaxHeight(700);
        scrollPane.getStyleClass().add("scroll-pane-transparent");

        saveButton.setOnAction(event -> {
            doActionOnSave(settings, valueFields, settingsStage);
        });

        tab.setContent(scrollPane);
        return tab;
    }

    private Tab createLayoutEditorTab() {
        Tab tab = new Tab("Layout Editor");

        SplitPane splitPane = new SplitPane();
        splitPane.getStyleClass().add("background-primary");

        // Left Pane: File Selector and TreeView
        VBox leftPane = new VBox(10);
        leftPane.setPadding(new Insets(10));
        leftPane.getStyleClass().add("background-primary");

        // File Selector
        ComboBox<String> fileSelector = new ComboBox<>();
        fileSelector.setMaxWidth(Double.MAX_VALUE);
        fileSelector.setPromptText("Select config file");

        // Populate file selector
        SettingsDTO filesToLoad = FilesToLoadSettingsService.load();
        List<String> fileKeys = filesToLoad.getSettings().stream()
                .map(KeyValueDTO::getKey)
                .collect(Collectors.toList());
        fileSelector.getItems().addAll(fileKeys);

        // TreeView
        TreeView<Object> treeView = new TreeView<>();
        treeView.getStyleClass().add("background-primary");
        VBox.setVgrow(treeView, Priority.ALWAYS);

        fileSelector.setOnAction(event -> {
            String selectedFile = fileSelector.getValue();
            if (selectedFile != null) {
                populateTreeView(treeView, selectedFile);
            }
        });

        // Toolbar
        HBox toolbar = new HBox(5);
        toolbar.getStyleClass().add("hbox-spacing");
        Button addSectionBtn = new Button("Add Section");
        Button addSubSectionBtn = new Button("Add SubSection");
        Button addButtonBtn = new Button("Add Button");
        Button deleteBtn = new Button("Delete");
        Button newFileBtn = new Button("New File");

        List<Button> buttons = Arrays.asList(addSectionBtn, addSubSectionBtn, addButtonBtn, deleteBtn, newFileBtn);
        buttons.forEach(b -> {
            b.getStyleClass().add("button-default");
            // Reduce font size for toolbar buttons to fit better
            b.setStyle("-fx-font-size: 10px;");
        });

        // Initial state
        addSectionBtn.setDisable(true);
        addSubSectionBtn.setDisable(true);
        addButtonBtn.setDisable(true);
        deleteBtn.setDisable(true);

        // Selection Listener for Toolbar
        treeView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean isFile = newVal != null && newVal.getValue() instanceof String;
            boolean isSection = newVal != null && newVal.getValue() instanceof SectionDTO;
            boolean isSubSection = newVal != null && newVal.getValue() instanceof SubSectionDTO;
            boolean isButton = newVal != null && newVal.getValue() instanceof ButtonDTO;

            addSectionBtn.setDisable(!isFile);
            addSubSectionBtn.setDisable(!isSection);
            addButtonBtn.setDisable(!isSubSection);
            deleteBtn.setDisable(newVal == null || isFile); // Cannot delete file from here
        });

        // Action Handlers
        newFileBtn.setOnAction(e -> handleNewFile(fileSelector));

        addSectionBtn.setOnAction(e -> {
            TreeItem<Object> selectedItem = treeView.getSelectionModel().getSelectedItem();
            if (selectedItem != null && selectedItem.getValue() instanceof String) {
                SectionDTO newSection = new SectionDTO("New Section", new ArrayList<>());
                TreeItem<Object> newItem = new TreeItem<>(newSection);
                selectedItem.getChildren().add(newItem);
                selectedItem.setExpanded(true);
            }
        });

        addSubSectionBtn.setOnAction(e -> {
            TreeItem<Object> selectedItem = treeView.getSelectionModel().getSelectedItem();
            if (selectedItem != null && selectedItem.getValue() instanceof SectionDTO) {
                SubSectionDTO newSub = new SubSectionDTO("New SubSection", new ArrayList<>());
                TreeItem<Object> newItem = new TreeItem<>(newSub);
                selectedItem.getChildren().add(newItem);
                selectedItem.setExpanded(true);
            }
        });

        addButtonBtn.setOnAction(e -> {
            TreeItem<Object> selectedItem = treeView.getSelectionModel().getSelectedItem();
            if (selectedItem != null && selectedItem.getValue() instanceof SubSectionDTO) {
                ButtonDTO newButton = new ButtonDTO("New Button", "ALL_EXAMPLE_SCRIPTS_PATH", new ArrayList<>(), ElementType.BASH, false, false, "", "Description", true, "", "");
                TreeItem<Object> newItem = new TreeItem<>(newButton);
                selectedItem.getChildren().add(newItem);
                selectedItem.setExpanded(true);
            }
        });

        deleteBtn.setOnAction(e -> {
            TreeItem<Object> selectedItem = treeView.getSelectionModel().getSelectedItem();
            if (selectedItem != null && selectedItem.getParent() != null) {
                selectedItem.getParent().getChildren().remove(selectedItem);
            }
        });

        leftPane.getChildren().addAll(new Label("Configuration File:"), fileSelector, toolbar, treeView);

        // Right Pane: Details Editor
        VBox rightPane = new VBox(10);
        rightPane.setPadding(new Insets(10));
        rightPane.getStyleClass().add("background-primary");
        Label placeholder = new Label("Select an item to edit details");
        placeholder.getStyleClass().add("label-on-dark-background");
        rightPane.getChildren().add(placeholder);

        // Bind selection listener to update right pane
        treeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            rightPane.getChildren().clear();
            if (newValue == null) {
                rightPane.getChildren().add(placeholder);
            } else {
                buildDetailsPane(rightPane, newValue.getValue(), treeView);
            }
        });

        // Save Button for Layout Editor
        Button saveLayoutButton = new Button("Save Layout");
        saveLayoutButton.getStyleClass().add("button-default");
        saveLayoutButton.setMaxWidth(Double.MAX_VALUE);
        saveLayoutButton.setOnAction(e -> {
            String selectedFile = fileSelector.getValue();
            if (selectedFile != null) {
                saveLayout(selectedFile, treeView);
                loader.loadContent();
                resizeMainWindow.resizeMainWindow();
            }
        });

        VBox rightContainer = new VBox(10);
        rightContainer.getStyleClass().add("background-primary");
        VBox.setVgrow(rightPane, Priority.ALWAYS);
        rightContainer.getChildren().addAll(rightPane, saveLayoutButton);
        rightContainer.setPadding(new Insets(10));

        splitPane.getItems().addAll(leftPane, rightContainer);
        splitPane.setDividerPositions(0.4);

        tab.setContent(splitPane);
        return tab;
    }

    private void saveLayout(String fileName, TreeView<Object> treeView) {
        // Reconstruct List<SectionDTO> from TreeView
        List<SectionDTO> sections = new ArrayList<>();
        TreeItem<Object> root = treeView.getRoot();

        if (root != null) {
            for (TreeItem<Object> sectionItem : root.getChildren()) {
                if (sectionItem.getValue() instanceof SectionDTO) {
                    SectionDTO oldSection = (SectionDTO) sectionItem.getValue();
                    String sectionName = oldSection.sectionName();
                    List<SubSectionDTO> subSections = new ArrayList<>();

                    for (TreeItem<Object> subSectionItem : sectionItem.getChildren()) {
                        if (subSectionItem.getValue() instanceof SubSectionDTO) {
                            SubSectionDTO oldSubSection = (SubSectionDTO) subSectionItem.getValue();
                            String subSectionName = oldSubSection.subSectionName();
                            List<ButtonDTO> buttons = new ArrayList<>();

                            for (TreeItem<Object> buttonItem : subSectionItem.getChildren()) {
                                if (buttonItem.getValue() instanceof ButtonDTO) {
                                    ButtonDTO oldButton = (ButtonDTO) buttonItem.getValue();
                                    // Create new ButtonDTO with correct parent names to ensure consistency
                                    buttons.add(new ButtonDTO(
                                        oldButton.getName(),
                                        oldButton.getScriptLocationParamName(),
                                        oldButton.getCommands(),
                                        oldButton.getElementType(),
                                        oldButton.isAutoCloseConsole(),
                                        oldButton.isPopupInputDisplayed(),
                                        oldButton.getPopupInputMessage(),
                                        oldButton.getDescription(),
                                        oldButton.isVisibleAsDefault(),
                                        sectionName,
                                        subSectionName
                                    ));
                                }
                            }
                            subSections.add(new SubSectionDTO(subSectionName, buttons));
                        }
                    }
                    sections.add(new SectionDTO(sectionName, subSections));
                }
            }
        }

        org.codefromheaven.service.SaveJsonService.save(fileName, sections);
    }

    private FieldOnPageDTO loadElementsToPage(List<KeyValueDTO> settings, GridPane gridPane) {
        List<KeyValueDTO> comboSettings = getComboSettings(settings);
        List<KeyValueDTO> checkBoxSettings = getCheckBoxSettings(settings);

        List<KeyValueDTO> alreadyUsedSettings = new ArrayList<>();
        alreadyUsedSettings.addAll(comboSettings);
        alreadyUsedSettings.addAll(checkBoxSettings);

        List<KeyValueDTO> textSettings = getTextSettings(settings, alreadyUsedSettings);

        TextField[] textValueFields = new TextField[textSettings.size()];
        CheckBox[] checkBoxFields = new CheckBox[checkBoxSettings.size()];
        ComboBox<String>[] comboValueFields = new ComboBox[comboSettings.size()];

        int globalPosition = 0;
        for (int i = 0; i < textSettings.size(); i++, globalPosition++) {
            KeyValueDTO setting = textSettings.get(i);
            Label label = new Label(getLabel(setting) + ":");
            label.getStyleClass().add("label-on-dark-background");
            TextField field = createTextField(setting.getValue(), textValueFields, i);
            gridPane.add(label, 0, globalPosition);
            gridPane.add(field, 1, globalPosition);
        }

        for (int i = 0; i < checkBoxSettings.size(); i++, globalPosition++) {
            KeyValueDTO setting = checkBoxSettings.get(i);
            Label label = new Label(getLabel(setting) + ":");
            label.getStyleClass().add("label-on-dark-background");
            CheckBox field = createCheckBox(setting.getValue(), checkBoxFields, i);
            field.getStyleClass().add("check-box-on-dark-background");
            gridPane.add(label, 0, globalPosition);
            gridPane.add(field, 1, globalPosition);
        }

        for (int i = 0; i < comboSettings.size(); i++, globalPosition++) {
            KeyValueDTO setting = comboSettings.get(i);
            Label label = new Label(getLabel(setting) + ":");
            label.getStyleClass().add("label-on-dark-background");
            ComboBox<String> field = createComboBox(setting, comboValueFields, i);
            gridPane.add(label, 0, globalPosition);
            gridPane.add(field, 1, globalPosition);
        }

        return new FieldOnPageDTO(textValueFields, comboValueFields, checkBoxFields);
    }

    private List<KeyValueDTO> getComboSettings(List<KeyValueDTO> settings) {
        return settings.stream().filter(elem -> COMBO_FIELD_SETTINGS.stream().anyMatch(key -> key.getName().equals(elem.getKey())))
                .collect(Collectors.toList());
    }

    private List<KeyValueDTO> getCheckBoxSettings(List<KeyValueDTO> settings) {
        return settings.stream().filter(elem -> CHECKBOX_FIELD_SETTINGS.stream().anyMatch(key -> key.getName().equals(elem.getKey())))
                       .collect(Collectors.toList());
    }

    private List<KeyValueDTO> getTextSettings(List<KeyValueDTO> settings, List<KeyValueDTO> alreadyUsedSettings) {
        return settings.stream().filter(elem -> !alreadyUsedSettings.contains(elem)).collect(Collectors.toList());
    }

    private TextField createTextField(String value, TextField[] valueFields, int i) {
        TextField textField = new TextField(value);
        textField.setPrefWidth(500);
        valueFields[i] = textField;
        return textField;
    }

    private ComboBox<String> createComboBox(KeyValueDTO setting, ComboBox<String>[] valueFields, int i) {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setPrefWidth(500);
        String[] options = getComboBoxOptions(setting.getKey());

        comboBox.getItems().addAll(options);

        if (options.length > 0) {
            comboBox.setValue(setting.getValue());
        }

        valueFields[i] = comboBox;
        return comboBox;
    }

    private String[] getComboBoxOptions(String settingKey) {
        if (settingKey.equals(Setting.IMAGE_NAME.getName())) {
            return AnimalProvider.getDeterminateAnimals().stream().sorted().toArray(String[]::new);
        }
        if (settingKey.equals(Setting.APP_STYLE.getName())) {
            return Arrays.stream(Style.values()).map(Style::name).sorted().toArray(String[]::new);
        }
        throw new IllegalArgumentException("Key " + settingKey + " is not supported.");
    }

    private CheckBox createCheckBox(String value, CheckBox[] valueFields, int i) {
        CheckBox checkBox = new CheckBox();
        checkBox.setSelected(Boolean.parseBoolean(value));
        valueFields[i] = checkBox;
        return checkBox;
    }

    private void doActionOnSave(List<KeyValueDTO> settings, FieldOnPageDTO valueFields, Stage settingsStage) {
        List<KeyValueDTO> comboSettings = getComboSettings(settings);
        List<KeyValueDTO> checkBoxSettings = getCheckBoxSettings(settings);

        List<KeyValueDTO> alreadyUsedSettings = new ArrayList<>();
        alreadyUsedSettings.addAll(comboSettings);
        alreadyUsedSettings.addAll(checkBoxSettings);

        List<KeyValueDTO> textSettings = getTextSettings(settings, alreadyUsedSettings);

        for (int i = 0; i < textSettings.size(); i++) {
            KeyValueDTO setting = textSettings.get(i);
            String newValue = valueFields.textFields()[i].getText();
            setting.setValue(newValue);
        }

        for (int i = 0; i < comboSettings.size(); i++) {
            KeyValueDTO setting = comboSettings.get(i);
            String newValue = valueFields.comboBoxes()[i].getValue();
            setting.setValue(newValue);
        }

        for (int i = 0; i < checkBoxSettings.size(); i++) {
            KeyValueDTO setting = checkBoxSettings.get(i);
            String newValue = String.valueOf(valueFields.checkBoxes()[i].isSelected());
            setting.setValue(newValue);
        }

        SpringContext.getBean(SettingsService.class).saveSettings(new SettingsDTO(settings));
        styleReloader.reloadStyle();
        loader.loadContent();
        resizeMainWindow.resizeMainWindow();
        checkForUpdates.checkForUpdates();
        settingsStage.close();
    }

    private String getLabel(KeyValueDTO keyValueDTO) {
        if (keyValueDTO.getDescription() != null && !keyValueDTO.getDescription().isBlank()) {
            return keyValueDTO.getDescription();
        } else {
            return keyValueDTO.getKey();
        }
    }

    private void handleNewFile(ComboBox<String> fileSelector) {
        TextInputDialog dialog = new TextInputDialog("new_commands");
        dialog.setTitle("New Configuration File");
        dialog.setHeaderText("Create a new JSON configuration file");
        dialog.setContentText("Please enter file name (without extension):");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            // 1. Create empty file
            // Use SettingsServiceBase logic to determine the path for the new custom file
            String configPath = SettingsServiceBase.getMyOwnFileDir(name);
            File configFile = new File(configPath);

            if (!configFile.exists()) {
                try {
                    // Create basic structure
                    // Using SaveJsonService to write empty list
                    org.codefromheaven.service.SaveJsonService.saveBase(configPath, new ArrayList<>());

                    // 2. Add to FilesToLoadSettings
                    SettingsDTO filesToLoad = FilesToLoadSettingsService.load();
                    filesToLoad.getSettings().add(new KeyValueDTO(name, "", true, ""));
                    SpringContext.getBean(SettingsService.class).saveSettings(filesToLoad);

                    // 3. Refresh Selector
                    fileSelector.getItems().add(name);
                    fileSelector.setValue(name);
                } catch (Exception e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Could not create file");
                    alert.setContentText(e.getMessage());
                    alert.showAndWait();
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Warning");
                alert.setHeaderText("File already exists");
                alert.showAndWait();
            }
        });
    }

    private void buildDetailsPane(VBox rightPane, Object item, TreeView<Object> treeView) {
        if (item instanceof String) {
            // File selected
            Label label = new Label("File: " + item);
            label.getStyleClass().add("text-header");
            rightPane.getChildren().add(label);
        } else if (item instanceof SectionDTO) {
            SectionDTO section = (SectionDTO) item;
            Label title = new Label("Edit Section");
            title.getStyleClass().add("text-header");

            TextField nameField = new TextField(section.sectionName());
            Button applyButton = new Button("Apply Change");
            applyButton.getStyleClass().add("button-default");

            applyButton.setOnAction(e -> {
                // Since SectionDTO is a record, we need to replace it in the tree item
                SectionDTO newSection = new SectionDTO(nameField.getText(), section.subSections());
                updateTreeItem(treeView, item, newSection);
            });

            rightPane.getChildren().addAll(title, new Label("Name:"), nameField, applyButton);

        } else if (item instanceof SubSectionDTO) {
            SubSectionDTO subSection = (SubSectionDTO) item;
            Label title = new Label("Edit Sub-Section");
            title.getStyleClass().add("text-header");

            TextField nameField = new TextField(subSection.subSectionName());
            Button applyButton = new Button("Apply Change");
            applyButton.getStyleClass().add("button-default");

            applyButton.setOnAction(e -> {
                SubSectionDTO newSubSection = new SubSectionDTO(nameField.getText(), subSection.buttons());
                updateTreeItem(treeView, item, newSubSection);
            });

            rightPane.getChildren().addAll(title, new Label("Name:"), nameField, applyButton);

        } else if (item instanceof ButtonDTO) {
            ButtonDTO button = (ButtonDTO) item;
            Label title = new Label("Edit Button");
            title.getStyleClass().add("text-header");

            ScrollPane scrollPane = new ScrollPane();
            scrollPane.getStyleClass().add("scroll-pane-transparent");
            VBox content = new VBox(10);
            content.setPadding(new Insets(0, 10, 0, 0));

            // Fields
            TextField nameField = new TextField(button.getName());
            TextField descriptionField = new TextField(button.getDescription());
            ComboBox<ElementType> typeCombo = new ComboBox<>();
            typeCombo.getItems().addAll(ElementType.values());
            typeCombo.setValue(button.getElementType());
            TextField paramNameField = new TextField(button.getScriptLocationParamName());
            CheckBox autoCloseCheck = new CheckBox("Auto Close Console");
            autoCloseCheck.setSelected(button.isAutoCloseConsole());
            CheckBox popupCheck = new CheckBox("Show Popup Input");
            popupCheck.setSelected(button.isPopupInputDisplayed());
            TextField popupMsgField = new TextField(button.getPopupInputMessage());
            TextArea commandsArea = new TextArea(String.join("\n", button.getCommands()));
            commandsArea.setPrefRowCount(3);
            CheckBox visibleDefaultCheck = new CheckBox("Visible as Default");
            visibleDefaultCheck.setSelected(button.isVisibleAsDefault());

            Button applyButton = new Button("Apply Change");
            applyButton.getStyleClass().add("button-default");

            applyButton.setOnAction(e -> {
                List<String> commands = Arrays.asList(commandsArea.getText().split("\\n"));
                ButtonDTO newButton = new ButtonDTO(
                        nameField.getText(),
                        paramNameField.getText(),
                        commands,
                        typeCombo.getValue(),
                        autoCloseCheck.isSelected(),
                        popupCheck.isSelected(),
                        popupMsgField.getText(),
                        descriptionField.getText(),
                        visibleDefaultCheck.isSelected(),
                        button.getKey().sectionName(),
                        button.getKey().subSectionName()
                );
                updateTreeItem(treeView, item, newButton);
            });

            addFormRow(content, "Name:", nameField);
            addFormRow(content, "Description:", descriptionField);
            addFormRow(content, "Type:", typeCombo);
            addFormRow(content, "Param Name:", paramNameField);
            addFormRow(content, "Commands (one per line):", commandsArea);
            addFormRow(content, "Popup Message:", popupMsgField);
            content.getChildren().addAll(autoCloseCheck, popupCheck, visibleDefaultCheck, applyButton);

            scrollPane.setContent(content);
            rightPane.getChildren().addAll(title, scrollPane);
        }
    }

    private void addFormRow(VBox container, String labelText, javafx.scene.Node field) {
        Label label = new Label(labelText);
        label.getStyleClass().add("label-on-dark-background");
        container.getChildren().addAll(label, field);
    }

    private void updateTreeItem(TreeView<Object> treeView, Object oldItem, Object newItem) {
        TreeItem<Object> item = findTreeItem(treeView.getRoot(), oldItem);
        if (item != null) {
            item.setValue(newItem);
        }
    }

    private TreeItem<Object> findTreeItem(TreeItem<Object> root, Object value) {
        if (root.getValue() == value) return root;
        for (TreeItem<Object> child : root.getChildren()) {
            TreeItem<Object> result = findTreeItem(child, value);
            if (result != null) return result;
        }
        return null;
    }

    private void populateTreeView(TreeView<Object> treeView, String selectedFile) {
        List<SectionDTO> sections = LoadFromJsonService.load(selectedFile);
        TreeItem<Object> root = new TreeItem<>(selectedFile);
        root.setExpanded(true);

        for (SectionDTO section : sections) {
            TreeItem<Object> sectionItem = new TreeItem<>(section);
            sectionItem.setExpanded(true);

            for (SubSectionDTO subSection : section.subSections()) {
                TreeItem<Object> subSectionItem = new TreeItem<>(subSection);

                for (ButtonDTO button : subSection.buttons()) {
                    TreeItem<Object> buttonItem = new TreeItem<>(button);
                    subSectionItem.getChildren().add(buttonItem);
                }
                sectionItem.getChildren().add(subSectionItem);
            }
            root.getChildren().add(sectionItem);
        }

        treeView.setRoot(root);

        // Custom Cell Factory to display names instead of toString()
        treeView.setCellFactory(tv -> new TreeCell<Object>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    if (item instanceof String) {
                        setText((String) item);
                    } else if (item instanceof SectionDTO) {
                        setText(((SectionDTO) item).sectionName());
                    } else if (item instanceof SubSectionDTO) {
                        setText(((SubSectionDTO) item).subSectionName());
                    } else if (item instanceof ButtonDTO) {
                        setText(((ButtonDTO) item).getName());
                    } else {
                        setText(item.toString());
                    }
                }
            }
        });
    }

}
