package org.codefromheaven.dto.settings;

import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public record FieldOnPageDTO(TextField[] textFields, ComboBox<String>[] comboBoxes, CheckBox[] checkBoxes, CheckBox[] editableCheckboxes) {
    public FieldOnPageDTO(TextField[] textFields, ComboBox<String>[] comboBoxes, CheckBox[] checkBoxes) {
        this(textFields, comboBoxes, checkBoxes, null);
    }
}
