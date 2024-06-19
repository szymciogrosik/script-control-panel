package org.codefromheaven.dto;

import org.codefromheaven.dto.settings.BaseSetting;
import org.codefromheaven.service.settings.SettingsServiceBase;

public enum InternalSetting implements BaseSetting {

    IMAGE_NAME;

    @Override
    public String getName() {
        return name();
    }

    @Override
    public String getValue() {
        return SettingsServiceBase.loadValue(this);
    }

    @Override
    public FileType getElementType() {
        return FileType.INTERNAL_SETTINGS;
    }

    @Override
    public BaseSetting[] getAll() {
        return InternalSetting.values();
    }

    public static BaseSetting getSettingByName(String settingName) {
        // No matter what setting selected
        return InternalSetting.IMAGE_NAME.getElementByName(settingName);
    }

}
