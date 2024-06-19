package org.codefromheaven.dto.settings;

import java.util.ArrayList;
import java.util.List;

public class SettingsDTO {

    private List<KeyValueDTO> settings = new ArrayList<>();

    public SettingsDTO() {
    }

    public SettingsDTO(List<KeyValueDTO> settings) {
        this.settings = settings;
    }

    public List<KeyValueDTO> getSettings() {
        return settings;
    }
}
