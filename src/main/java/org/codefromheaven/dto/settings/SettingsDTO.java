package org.codefromheaven.dto.settings;

import java.util.ArrayList;
import java.util.List;

public class SettingsDTO {

    private List<SettingDTO> settings = new ArrayList<>();

    public SettingsDTO() {
    }

    public SettingsDTO(List<SettingDTO> settings) {
        this.settings = settings;
    }

    public List<SettingDTO> getSettings() {
        return settings;
    }
}
