package org.codefromheaven.dto.settings;

import java.util.Objects;

public class SettingDTO {
    private String key;
    private String value;
    private SettingType type;
    private boolean editable = true;
    private String description;

    public SettingDTO() {
    }

    public SettingDTO(String key, String value, SettingType type, String description, boolean editable) {
        this(key, value, type, description);
        this.editable = editable;
    }

    public SettingDTO(String key, String value, SettingType type, String description) {
        this.key = key;
        this.value = value;
        this.type = type;
        this.description = description;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public SettingType getType() {
        return type;
    }

    public void setType(SettingType type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SettingDTO that = (SettingDTO) o;
        return editable == that.editable && type == that.type && Objects.equals(key, that.key)
                && Objects.equals(value, that.value) && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value, type, editable, description);
    }

}
