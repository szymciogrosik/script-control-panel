package org.codefromheaven.dto;

import org.codefromheaven.dto.settings.BaseSetting;
import org.codefromheaven.service.settings.SettingsServiceBase;

public enum Setting implements BaseSetting {
    BASH_PATH,
    MAX_WINDOW_HEIGHT,
    IMAGE_NAME,
    APP_NAME;

    @Override
    public String getName() {
        return name();
    }

    @Override
    public String getValue() {
        return SettingsServiceBase.loadValue(this).get();
    }

    @Override
    public FileType getElementType() {
        return FileType.SETTINGS;
    }

    @Override
    public BaseSetting[] getAll() {
        return Setting.values();
    }

    public static BaseSetting getSettingByName(String settingName) {
        // No matter what setting selected
        return Setting.BASH_PATH.getElementByName(settingName);
    }

}
