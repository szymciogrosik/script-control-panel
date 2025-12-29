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
import org.codefromheaven.dto.settings.KeyValueDTO;
import org.codefromheaven.dto.settings.SettingsDTO;
import org.codefromheaven.resources.AnimalProvider;
import org.codefromheaven.service.animal.AnimalService;
import org.codefromheaven.service.settings.SettingsService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
        settingsStage.setResizable(false);

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(20));
        gridPane.setVgap(10);
        gridPane.setHgap(10);

        SettingsDTO configSettings = SpringContext.getBean(SettingsService.class).load();
        List<KeyValueDTO> settings = configSettings.getSettings().stream().filter(KeyValueDTO::isEditable).toList();

        FieldOnPageDTO valueFields = loadElementsToPage(settings, gridPane);

        Button saveButton = new Button("Save");
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

        saveButton.setOnAction(event -> {
            doActionOnSave(settings, valueFields, settingsStage);
        });

        Scene scene = new Scene(scrollPane);
        settingsStage.setScene(scene);
        settingsStage.showAndWait();
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
            TextField field = createTextField(setting.getValue(), textValueFields, i);
            gridPane.add(label, 0, globalPosition);
            gridPane.add(field, 1, globalPosition);
        }

        for (int i = 0; i < checkBoxSettings.size(); i++, globalPosition++) {
            KeyValueDTO setting = checkBoxSettings.get(i);
            Label label = new Label(getLabel(setting) + ":");
            CheckBox field = createCheckBox(setting.getValue(), checkBoxFields, i);
            gridPane.add(label, 0, globalPosition);
            gridPane.add(field, 1, globalPosition);
        }

        for (int i = 0; i < comboSettings.size(); i++, globalPosition++) {
            KeyValueDTO setting = comboSettings.get(i);
            Label label = new Label(getLabel(setting) + ":");
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

}
