package org.codefromheaven.dto.settings;

public class KeyValueDTO {
    private String key;
    private String value;

    public KeyValueDTO() {
    }

    public KeyValueDTO(String key, String value) {
        this.key = key;
        this.value = value;
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
}
