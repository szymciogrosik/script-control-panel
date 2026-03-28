package org.codefromheaven.controller;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.codefromheaven.context.SpringContext;
import org.codefromheaven.dto.FileType;
import org.codefromheaven.dto.Setting;
import org.codefromheaven.dto.Style;
import org.codefromheaven.dto.settings.FieldOnPageDTO;
import org.codefromheaven.dto.settings.SettingDTO;
import org.codefromheaven.dto.settings.SettingType;
import org.codefromheaven.dto.settings.SettingsDTO;
import org.codefromheaven.resources.AnimalProvider;
import org.codefromheaven.service.animal.AnimalService;
import org.codefromheaven.service.settings.SettingsService;
import org.codefromheaven.service.style.StyleService;
import org.codefromheaven.service.settings.SettingsServiceBase;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static javafx.stage.Modality.APPLICATION_MODAL;

public class SettingsController {

    private final MainWindowController.ContentLoader loader;
    private final MainWindowController.ResizeWindow resizeMainWindow;
    private final MainWindowController.CheckForUpdates checkForUpdates;
    private final MainWindowController.StyleReloader styleReloader;

    public SettingsController(
            MainWindowController.ContentLoader loader, MainWindowController.ResizeWindow resizeMainWindow,
            MainWindowController.CheckForUpdates checkForUpdates, MainWindowController.StyleReloader styleReloader) {
        this.loader = loader;
        this.resizeMainWindow = resizeMainWindow;
        this.checkForUpdates = checkForUpdates;
        this.styleReloader = styleReloader;
    }

    public void setupPage() {
        Stage settingsStage = new Stage();
        settingsStage.initModality(APPLICATION_MODAL);
        settingsStage.setTitle("Change application settings");
        settingsStage.getIcons().add(SpringContext.getBean(AnimalService.class).getRandomAnimalImage());
        settingsStage.setResizable(false);

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // --- Load Data ---
        // 1. Default layer overrides
        SettingsDTO defaultSettingsDto = SettingsServiceBase.loadDefaultSettings(FileType.SETTINGS);
        List<SettingDTO> defaultSettings = defaultSettingsDto.getSettings().stream()
                .map(SettingDTO::new) // Deep copy
                .collect(Collectors.toList());

        // 2. My Own layer overrides
        SettingsDTO myOwnSettingsDto = SpringContext.getBean(SettingsService.class).load();
        List<SettingDTO> myOwnSettings = myOwnSettingsDto.getSettings().stream()
                .map(SettingDTO::new) // Deep copy
                .collect(Collectors.toList());

        // --- Create Grids ---
        GridPane defaultGrid = createStandardGridPane();
        FieldOnPageDTO defaultFields = loadElementsToPage(defaultSettings, defaultGrid, true);
        ScrollPane defaultScroll = createScrollPane(defaultGrid);
        
        GridPane myOwnGrid = createStandardGridPane();
        FieldOnPageDTO myOwnFields = loadElementsToPage(myOwnSettings, myOwnGrid, false);
        ScrollPane myOwnScroll = createScrollPane(myOwnGrid);

        // --- Cross-tab bindings ---
        for (int i = 0; i < defaultSettings.size(); i++) {
            CheckBox editableCb = defaultFields.editableCheckboxes()[i];
            boolean isPersonalOnly = Setting.IMAGE_NAME.getName().equals(defaultSettings.get(i).getKey());

            if (isPersonalOnly) {
                // Personal-only: always locked in Default (both checkbox and input)
                editableCb.setDisable(true);
                defaultFields.comboBoxes()[i].setDisable(true);
                // My Own field stays enabled — skip binding
                continue;
            }

            SettingType type = myOwnSettings.get(i).getType();
            if (SettingType.SELECT.equals(type)) {
                myOwnFields.comboBoxes()[i].disableProperty().bind(editableCb.selectedProperty().not());
            } else if (SettingType.SWITCH.equals(type)) {
                myOwnFields.checkBoxes()[i].disableProperty().bind(editableCb.selectedProperty().not());
            } else {
                myOwnFields.textFields()[i].disableProperty().bind(editableCb.selectedProperty().not());
            }
        }

        // --- Toggle Buttons (Mock Tabs) ---
        ToggleButton defaultLayoutTabBtn = new ToggleButton("Default settings");
        defaultLayoutTabBtn.getStyleClass().add("button-default");

        ToggleButton myOwnLayoutTabBtn = new ToggleButton("My own settings");
        myOwnLayoutTabBtn.getStyleClass().add("button-default");

        ToggleGroup tabGroup = new ToggleGroup();
        defaultLayoutTabBtn.setToggleGroup(tabGroup);
        myOwnLayoutTabBtn.setToggleGroup(tabGroup);

        defaultLayoutTabBtn.opacityProperty().bind(javafx.beans.binding.Bindings.when(defaultLayoutTabBtn.selectedProperty()).then(1.0).otherwise(0.6));
        myOwnLayoutTabBtn.opacityProperty().bind(javafx.beans.binding.Bindings.when(myOwnLayoutTabBtn.selectedProperty()).then(1.0).otherwise(0.6));

        HBox tabBar = new HBox(10, defaultLayoutTabBtn, myOwnLayoutTabBtn);
        tabBar.setPadding(new Insets(10, 10, 0, 10));

        // --- Description labels (from text resources, same as Layout Editor) ---
        Label defaultDesc = new Label(loadResourceText("/editor/tab_settings_default_desc.txt"));
        defaultDesc.setWrapText(true);
        defaultDesc.getStyleClass().add("tab-desc-label");

        Label myOwnDesc = new Label(loadResourceText("/editor/tab_settings_my_own_desc.txt"));
        myOwnDesc.setWrapText(true);
        myOwnDesc.getStyleClass().add("tab-desc-label");

        VBox defaultContent = new VBox(5, defaultDesc, defaultScroll);
        VBox.setVgrow(defaultScroll, Priority.ALWAYS);
        defaultContent.setPadding(new Insets(5, 10, 0, 10));

        VBox myOwnContent = new VBox(5, myOwnDesc, myOwnScroll);
        VBox.setVgrow(myOwnScroll, Priority.ALWAYS);
        myOwnContent.setPadding(new Insets(5, 10, 0, 10));

        // --- Content Area ---
        StackPane contentPane = new StackPane(defaultContent);

        defaultLayoutTabBtn.setSelected(true);

        defaultLayoutTabBtn.setOnAction(e -> {
            if (!defaultLayoutTabBtn.isSelected()) defaultLayoutTabBtn.setSelected(true);
            contentPane.getChildren().setAll(defaultContent);
        });

        myOwnLayoutTabBtn.setOnAction(e -> {
            if (!myOwnLayoutTabBtn.isSelected()) myOwnLayoutTabBtn.setSelected(true);
            contentPane.getChildren().setAll(myOwnContent);
        });

        // --- Bottom action area ---
        Button saveButton = new Button("Save");
        saveButton.getStyleClass().add("button-default");
        saveButton.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(saveButton, Priority.ALWAYS);

        HBox buttonBox = new HBox(saveButton);
        buttonBox.setSpacing(10);
        buttonBox.setFillHeight(true);
        buttonBox.setPadding(new Insets(10));

        VBox root = new VBox(tabBar, contentPane, buttonBox);
        root.getStyleClass().add("background-primary");
        VBox.setVgrow(contentPane, Priority.ALWAYS);

        saveButton.setOnAction(event -> {
            doActionOnSave(defaultSettings, defaultFields, myOwnSettings, myOwnFields, settingsStage);
        });

        Scene scene = new Scene(root);
        scene.getStylesheets().add(SpringContext.getBean(StyleService.class).getCurrentStyleUrl());
        settingsStage.setScene(scene);
        settingsStage.sizeToScene();
        settingsStage.showAndWait();
    }

    private GridPane createStandardGridPane() {
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10));
        gridPane.setVgap(8);
        gridPane.setHgap(10);
        gridPane.getStyleClass().add("background-primary");
        return gridPane;
    }

    private ScrollPane createScrollPane(GridPane gridPane) {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(gridPane);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("scroll-pane-transparent");
        return scrollPane;
    }

    private FieldOnPageDTO loadElementsToPage(List<SettingDTO> settings, GridPane gridPane, boolean showEditableColumn) {
        TextField[] textValueFields = new TextField[settings.size()];
        CheckBox[] checkBoxFields = new CheckBox[settings.size()];
        ComboBox<String>[] comboValueFields = new ComboBox[settings.size()];
        CheckBox[] editableCheckFields = new CheckBox[settings.size()]; // Only used if showEditableColumn

        int rowIndex = 0;
        if (showEditableColumn) {
            Label header = new Label("Editable");
            header.getStyleClass().add("label-on-dark-background");
            gridPane.add(header, 2, rowIndex);
            GridPane.setHalignment(header, HPos.CENTER);
        } else {
            // Reserve header row so data rows align with the Default tab
            Label spacerHeader = new Label("");
            gridPane.add(spacerHeader, 2, rowIndex);
        }
        rowIndex++;

        for (int i = 0; i < settings.size(); i++) {
            SettingDTO setting = settings.get(i);
            Label label = new Label(getLabel(setting) + ":");
            label.getStyleClass().add("label-on-dark-background");

            gridPane.add(label, 0, rowIndex);

            if (SettingType.SELECT.equals(setting.getType())) {
                ComboBox<String> field = createComboBox(setting, comboValueFields, i);
                gridPane.add(field, 1, rowIndex);
            } else if (SettingType.SWITCH.equals(setting.getType())) {
                CheckBox field = createCheckBox(setting.getValue(), checkBoxFields, i);
                field.getStyleClass().add("check-box-on-dark-background");
                gridPane.add(field, 1, rowIndex);
            } else if (SettingType.NUMBER.equals(setting.getType())) {
                TextField field = createNumberField(setting.getValue(), textValueFields, i);
                gridPane.add(field, 1, rowIndex);
            } else if (SettingType.PATH.equals(setting.getType())) {
                HBox field = createPathSelector(setting, textValueFields, i);
                gridPane.add(field, 1, rowIndex);
            } else {
                TextField field = createTextField(setting.getValue(), textValueFields, i);
                gridPane.add(field, 1, rowIndex);
            }

            if (showEditableColumn) {
                CheckBox editableCheckbox = new CheckBox(); // Header describes this now
                editableCheckbox.setSelected(setting.isEditable());
                editableCheckbox.getStyleClass().add("check-box-on-dark-background");
                editableCheckFields[i] = editableCheckbox;
                gridPane.add(editableCheckbox, 2, rowIndex);
                GridPane.setHalignment(editableCheckbox, HPos.CENTER);
            } else {
                // Placeholder to keep row height consistent with the Default tab
                Label spacer = new Label("");
                spacer.setMinWidth(24);
                gridPane.add(spacer, 2, rowIndex);
            }
            
            rowIndex++;
        }

        return new FieldOnPageDTO(textValueFields, comboValueFields, checkBoxFields, editableCheckFields);
    }

    private TextField createTextField(String value, TextField[] valueFields, int i) {
        TextField textField = new TextField(value);
        textField.setPrefWidth(400); // reduced slightly to fit 3rd column if needed
        valueFields[i] = textField;
        return textField;
    }

    private TextField createNumberField(String value, TextField[] valueFields, int i) {
        TextField textField = new TextField(value);
        textField.setPrefWidth(400);
        textField.setTextFormatter(new javafx.scene.control.TextFormatter<>(change -> {
            if (change.getControlNewText().matches("\\d*")) {
                return change;
            }
            return null;
        }));
        valueFields[i] = textField;
        return textField;
    }

    private HBox createPathSelector(SettingDTO setting, TextField[] valueFields, int i) {
        TextField textField = new TextField(setting.getValue());
        textField.setPrefWidth(330);
        Button browseButton = new Button("Browse");
        browseButton.getStyleClass().add("button-default");
        browseButton.disableProperty().bind(textField.disableProperty());
        browseButton.setOnAction(e -> {
            File initialDir = new File(System.getProperty("user.dir"));
            
            if (setting.getKey().equals(Setting.BASH_PATH.getName())) {
                FileChooser fileChooser = new FileChooser();
                if (initialDir.exists() && initialDir.isDirectory()) {
                    fileChooser.setInitialDirectory(initialDir);
                }
                File file = fileChooser.showOpenDialog(textField.getScene().getWindow());
                makeRelativeAndSetText(textField, file);
            } else {
                DirectoryChooser dirChooser = new DirectoryChooser();
                if (initialDir.exists() && initialDir.isDirectory()) {
                    dirChooser.setInitialDirectory(initialDir);
                }
                File dir = dirChooser.showDialog(textField.getScene().getWindow());
                makeRelativeAndSetText(textField, dir);
            }
        });
        valueFields[i] = textField;
        return new HBox(10, textField, browseButton);
    }

    private void makeRelativeAndSetText(TextField textField, File file) {
        if (file != null) {
            Path basePath = Paths.get("").toAbsolutePath();
            Path targetPath = file.toPath().toAbsolutePath();
            try {
                String relativePath = basePath.relativize(targetPath).toString().replace("\\", "/");
                textField.setText(relativePath.isEmpty() ? "." : relativePath);
            } catch (IllegalArgumentException ex) {
                textField.setText(file.getAbsolutePath().replace("\\", "/"));
            }
        }
    }

    private ComboBox<String> createComboBox(SettingDTO setting, ComboBox<String>[] valueFields, int i) {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setPrefWidth(400);
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

    private void readValuesFromFields(List<SettingDTO> settings, FieldOnPageDTO valueFields, boolean isDefaultContext) {
        for (int i = 0; i < settings.size(); i++) {
            SettingDTO setting = settings.get(i);
            String newValue;
            if (SettingType.SELECT.equals(setting.getType())) {
                newValue = valueFields.comboBoxes()[i].getValue();
            } else if (SettingType.SWITCH.equals(setting.getType())) {
                newValue = String.valueOf(valueFields.checkBoxes()[i].isSelected());
            } else {
                newValue = valueFields.textFields()[i].getText();
            }
            setting.setValue(newValue);

            if (isDefaultContext && valueFields.editableCheckboxes() != null) {
                setting.setEditable(valueFields.editableCheckboxes()[i].isSelected());
            }
        }
    }

    private void doActionOnSave(List<SettingDTO> defaultSettings, FieldOnPageDTO defaultFields,
                                List<SettingDTO> myOwnSettings, FieldOnPageDTO myOwnFields,
                                Stage settingsStage) {
        
        // 1. Grab values from UI
        readValuesFromFields(defaultSettings, defaultFields, true);
        readValuesFromFields(myOwnSettings, myOwnFields, false);

        // 2. Force non-editable custom settings to match their default values so they aren't saved
        for (int i = 0; i < defaultSettings.size(); i++) {
            if (!defaultSettings.get(i).isEditable()) {
                myOwnSettings.get(i).setValue(defaultSettings.get(i).getValue());
            }
        }

        // 3. Save configurations mapping exactly to their respective layers
        SettingsServiceBase.saveDefaultSettings(FileType.SETTINGS, new SettingsDTO(defaultSettings));
        SettingsServiceBase.saveMyOwnSettings(FileType.SETTINGS, new SettingsDTO(myOwnSettings));

        // 3. Trigger app refresh
        styleReloader.reloadStyle();
        loader.loadContent();
        resizeMainWindow.resizeMainWindow();
        checkForUpdates.checkForUpdates();
        settingsStage.close();
    }

    private String getLabel(SettingDTO SettingDTO) {
        if (SettingDTO.getDescription() != null && !SettingDTO.getDescription().isBlank()) {
            return SettingDTO.getDescription();
        } else {
            return SettingDTO.getKey();
        }
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
