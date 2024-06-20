package org.codefromheaven.dto.settings;

import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class FieldOnPageDTO {

    private final TextField[] textFields;
    private final ComboBox<String>[] comboBoxes;

    public FieldOnPageDTO(TextField[] textFields, ComboBox<String>[] comboBoxes) {
        this.textFields = textFields;
        this.comboBoxes = comboBoxes;
    }

    public TextField[] getTextFields() {
        return textFields;
    }

    public ComboBox<String>[] getComboBoxes() {
        return comboBoxes;
    }
}
