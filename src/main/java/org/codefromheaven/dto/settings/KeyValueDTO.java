package org.codefromheaven.dto.settings;

import java.util.Objects;

public class KeyValueDTO {
    private String key;
    private String value;
    private String description;

    public KeyValueDTO() {
    }

    public KeyValueDTO(String key, String value, String description) {
        this.key = key;
        this.value = value;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {return true;}
        if (o == null || getClass() != o.getClass()) {return false;}
        KeyValueDTO that = (KeyValueDTO) o;
        return Objects.equals(key, that.key) && Objects.equals(value, that.value) && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value, description);
    }

}
