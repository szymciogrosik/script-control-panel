package org.codefromheaven.dto.settings;

/**
 * Lightweight DTO used exclusively for serializing settings to JSON.
 * Only persists key, value, and editable — all other fields (type, description)
 * are always derived from the application's built-in defaults at load-time.
 */
public class SettingSaveDTO {

    private String key;
    private String value;
    private boolean editable;

    public SettingSaveDTO() {
    }

    public SettingSaveDTO(SettingDTO source) {
        this.key = source.getKey();
        this.value = source.getValue();
        this.editable = source.isEditable();
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public boolean isEditable() {
        return editable;
    }

}
