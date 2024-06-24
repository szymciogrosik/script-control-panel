package org.codefromheaven.controller;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import org.codefromheaven.dto.Setting;
import org.codefromheaven.dto.settings.FieldOnPageDTO;
import org.codefromheaven.dto.settings.KeyValueDTO;
import org.codefromheaven.dto.settings.SettingsDTO;
import org.codefromheaven.resources.AnimalNamesProvider;
import org.codefromheaven.service.animal.AnimalService;
import org.codefromheaven.service.settings.SettingsService;

import java.util.List;
import java.util.stream.Collectors;

import static javafx.stage.Modality.APPLICATION_MODAL;

public class SettingsController {

    private final MainWindowController.ContentLoader loader;
    private final MainWindowController.ResizeWindow resizeMainWindow;

    public SettingsController(
            MainWindowController.ContentLoader loader, MainWindowController.ResizeWindow resizeMainWindow
    ) {
        this.loader = loader;
        this.resizeMainWindow = resizeMainWindow;
    }

    public void setupPage() {
        Stage settingsStage = new Stage();
        settingsStage.initModality(APPLICATION_MODAL);
        settingsStage.getIcons().add(AnimalService.getInstance().getRandomAnimalImage());
        settingsStage.setTitle("Additional settings");

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(20));
        gridPane.setVgap(10);
        gridPane.setHgap(10);

        SettingsDTO configSettings = SettingsService.load();
        List<KeyValueDTO> settings = configSettings.getSettings();

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
        List<KeyValueDTO> textSettings = getTextSettings(settings, comboSettings);

        TextField[] textValueFields = new TextField[textSettings.size()];
        ComboBox<String>[] comboValueFields = new ComboBox[comboSettings.size()];

        int globalPosition = 0;
        for (int i = 0; i < textSettings.size(); i++, globalPosition++) {
            KeyValueDTO setting = textSettings.get(i);
            Label label = new Label(getLabel(setting) + ":");
            TextField field = createTextField(setting.getValue(), textValueFields, i);
            gridPane.add(label, 0, globalPosition);
            gridPane.add(field, 1, globalPosition);
        }

        for (int i = 0; i < comboSettings.size(); i++, globalPosition++) {
            KeyValueDTO setting = comboSettings.get(i);
            Label label = new Label(getLabel(setting) + ":");
            ComboBox<String> field = createComboBox(setting.getValue(), comboValueFields, i);
            gridPane.add(label, 0, globalPosition);
            gridPane.add(field, 1, globalPosition);
        }

        return new FieldOnPageDTO(textValueFields, comboValueFields);
    }

    private List<KeyValueDTO> getComboSettings(List<KeyValueDTO> settings) {
        return settings.stream().filter(elem -> elem.getKey().equals(Setting.IMAGE_NAME.getName()))
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

    private ComboBox<String> createComboBox(String value, ComboBox<String>[] valueFields, int i) {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setPrefWidth(500);

        String[] options = AnimalNamesProvider.ALL.toArray(new String[0]);
        comboBox.getItems().addAll(options);

        if (options.length > 0) {
            comboBox.setValue(value);
        }

        valueFields[i] = comboBox;
        return comboBox;
    }

    private void doActionOnSave(List<KeyValueDTO> settings, FieldOnPageDTO valueFields, Stage settingsStage) {
        List<KeyValueDTO> comboSettings = getComboSettings(settings);
        List<KeyValueDTO> textSettings = getTextSettings(settings, comboSettings);

        for (int i = 0; i < textSettings.size(); i++) {
            KeyValueDTO setting = textSettings.get(i);
            String newValue = valueFields.getTextFields()[i].getText();
            setting.setValue(newValue);
        }

        for (int i = 0; i < comboSettings.size(); i++) {
            KeyValueDTO setting = comboSettings.get(i);
            String newValue = valueFields.getComboBoxes()[i].getValue();
            setting.setValue(newValue);
        }

        SettingsService.saveSettings(new SettingsDTO(settings));
        loader.loadContent();
        resizeMainWindow.resizeMainWindow();
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
