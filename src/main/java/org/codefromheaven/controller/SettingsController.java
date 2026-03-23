package org.codefromheaven.controller;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.codefromheaven.context.SpringContext;
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

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

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
        settingsStage.setTitle("Additional settings");
        settingsStage.getIcons().add(SpringContext.getBean(AnimalService.class).getRandomAnimalImage());
        settingsStage.setResizable(false);

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(20));
        gridPane.setVgap(10);
        gridPane.setHgap(10);
        gridPane.getStyleClass().add("background-primary");

        SettingsDTO configSettings = SpringContext.getBean(SettingsService.class).load();
        List<SettingDTO> settings = configSettings.getSettings().stream().filter(SettingDTO::isEditable).toList();

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

        Scene scene = new Scene(scrollPane);
        scene.getStylesheets().add(SpringContext.getBean(StyleService.class).getCurrentStyleUrl());
        settingsStage.setScene(scene);
        settingsStage.showAndWait();
    }

    private FieldOnPageDTO loadElementsToPage(List<SettingDTO> settings, GridPane gridPane) {
        TextField[] textValueFields = new TextField[settings.size()];
        CheckBox[] checkBoxFields = new CheckBox[settings.size()];
        ComboBox<String>[] comboValueFields = new ComboBox[settings.size()];

        for (int i = 0; i < settings.size(); i++) {
            SettingDTO setting = settings.get(i);
            Label label = new Label(getLabel(setting) + ":");
            label.getStyleClass().add("label-on-dark-background");

            gridPane.add(label, 0, i);

            if (SettingType.SELECT.equals(setting.getType())) {
                ComboBox<String> field = createComboBox(setting, comboValueFields, i);
                gridPane.add(field, 1, i);
            } else if (SettingType.SWITCH.equals(setting.getType())) {
                CheckBox field = createCheckBox(setting.getValue(), checkBoxFields, i);
                field.getStyleClass().add("check-box-on-dark-background");
                gridPane.add(field, 1, i);
            } else if (SettingType.NUMBER.equals(setting.getType())) {
                TextField field = createNumberField(setting.getValue(), textValueFields, i);
                gridPane.add(field, 1, i);
            } else if (SettingType.PATH.equals(setting.getType())) {
                HBox field = createPathSelector(setting, textValueFields, i);
                gridPane.add(field, 1, i);
            } else {
                TextField field = createTextField(setting.getValue(), textValueFields, i);
                gridPane.add(field, 1, i);
            }
        }

        return new FieldOnPageDTO(textValueFields, comboValueFields, checkBoxFields);
    }

    private TextField createTextField(String value, TextField[] valueFields, int i) {
        TextField textField = new TextField(value);
        textField.setPrefWidth(500);
        valueFields[i] = textField;
        return textField;
    }

    private TextField createNumberField(String value, TextField[] valueFields, int i) {
        TextField textField = new TextField(value);
        textField.setPrefWidth(500);
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
        textField.setPrefWidth(430);
        Button browseButton = new Button("Browse");
        browseButton.getStyleClass().add("button-default");
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

    private void doActionOnSave(List<SettingDTO> settings, FieldOnPageDTO valueFields, Stage settingsStage) {
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
        }

        SpringContext.getBean(SettingsService.class).saveSettings(new SettingsDTO(settings));
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

}
