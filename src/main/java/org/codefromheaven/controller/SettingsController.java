package org.codefromheaven.controller;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import org.codefromheaven.context.SpringContext;
import org.codefromheaven.dto.Setting;
import org.codefromheaven.dto.Style;
import org.codefromheaven.dto.settings.FieldOnPageDTO;
import org.codefromheaven.dto.settings.SettingDTO;
import org.codefromheaven.dto.settings.SettingsDTO;
import org.codefromheaven.resources.AnimalProvider;
import org.codefromheaven.service.animal.AnimalService;
import org.codefromheaven.service.settings.SettingsService;
import org.codefromheaven.service.style.StyleService;

import java.util.ArrayList;
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
        List<SettingDTO> comboSettings = getComboSettings(settings);
        List<SettingDTO> checkBoxSettings = getCheckBoxSettings(settings);

        List<SettingDTO> alreadyUsedSettings = new ArrayList<>();
        alreadyUsedSettings.addAll(comboSettings);
        alreadyUsedSettings.addAll(checkBoxSettings);

        List<SettingDTO> textSettings = getTextSettings(settings, alreadyUsedSettings);

        TextField[] textValueFields = new TextField[textSettings.size()];
        CheckBox[] checkBoxFields = new CheckBox[checkBoxSettings.size()];
        ComboBox<String>[] comboValueFields = new ComboBox[comboSettings.size()];

        int globalPosition = 0;
        for (int i = 0; i < textSettings.size(); i++, globalPosition++) {
            SettingDTO setting = textSettings.get(i);
            Label label = new Label(getLabel(setting) + ":");
            label.getStyleClass().add("label-on-dark-background");
            TextField field = createTextField(setting.getValue(), textValueFields, i);
            gridPane.add(label, 0, globalPosition);
            gridPane.add(field, 1, globalPosition);
        }

        for (int i = 0; i < checkBoxSettings.size(); i++, globalPosition++) {
            SettingDTO setting = checkBoxSettings.get(i);
            Label label = new Label(getLabel(setting) + ":");
            label.getStyleClass().add("label-on-dark-background");
            CheckBox field = createCheckBox(setting.getValue(), checkBoxFields, i);
            field.getStyleClass().add("check-box-on-dark-background");
            gridPane.add(label, 0, globalPosition);
            gridPane.add(field, 1, globalPosition);
        }

        for (int i = 0; i < comboSettings.size(); i++, globalPosition++) {
            SettingDTO setting = comboSettings.get(i);
            Label label = new Label(getLabel(setting) + ":");
            label.getStyleClass().add("label-on-dark-background");
            ComboBox<String> field = createComboBox(setting, comboValueFields, i);
            gridPane.add(label, 0, globalPosition);
            gridPane.add(field, 1, globalPosition);
        }

        return new FieldOnPageDTO(textValueFields, comboValueFields, checkBoxFields);
    }

    private List<SettingDTO> getComboSettings(List<SettingDTO> settings) {
        return settings.stream()
                .filter(elem -> org.codefromheaven.dto.settings.SettingType.SELECT.equals(elem.getType()))
                .collect(Collectors.toList());
    }

    private List<SettingDTO> getCheckBoxSettings(List<SettingDTO> settings) {
        return settings.stream()
                .filter(elem -> org.codefromheaven.dto.settings.SettingType.SWITCH.equals(elem.getType()))
                .collect(Collectors.toList());
    }

    private List<SettingDTO> getTextSettings(List<SettingDTO> settings, List<SettingDTO> alreadyUsedSettings) {
        return settings.stream().filter(elem -> !alreadyUsedSettings.contains(elem)).collect(Collectors.toList());
    }

    private TextField createTextField(String value, TextField[] valueFields, int i) {
        TextField textField = new TextField(value);
        textField.setPrefWidth(500);
        valueFields[i] = textField;
        return textField;
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
        List<SettingDTO> comboSettings = getComboSettings(settings);
        List<SettingDTO> checkBoxSettings = getCheckBoxSettings(settings);

        List<SettingDTO> alreadyUsedSettings = new ArrayList<>();
        alreadyUsedSettings.addAll(comboSettings);
        alreadyUsedSettings.addAll(checkBoxSettings);

        List<SettingDTO> textSettings = getTextSettings(settings, alreadyUsedSettings);

        for (int i = 0; i < textSettings.size(); i++) {
            SettingDTO setting = textSettings.get(i);
            String newValue = valueFields.textFields()[i].getText();
            setting.setValue(newValue);
        }

        for (int i = 0; i < comboSettings.size(); i++) {
            SettingDTO setting = comboSettings.get(i);
            String newValue = valueFields.comboBoxes()[i].getValue();
            setting.setValue(newValue);
        }

        for (int i = 0; i < checkBoxSettings.size(); i++) {
            SettingDTO setting = checkBoxSettings.get(i);
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

    private String getLabel(SettingDTO SettingDTO) {
        if (SettingDTO.getDescription() != null && !SettingDTO.getDescription().isBlank()) {
            return SettingDTO.getDescription();
        } else {
            return SettingDTO.getKey();
        }
    }

}
