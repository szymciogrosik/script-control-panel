package org.codefromheaven.dto;

import org.codefromheaven.dto.settings.BaseSetting;
import org.codefromheaven.service.settings.SettingsServiceBase;

public enum Setting implements BaseSetting {
    TMP_DIRECTORY,
    CONFIG_DIR,
    ADD_TO_WINDOWS_STARTUP,
    ALLOW_PRE_RELEASES,
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
        return SettingsServiceBase.loadValue(getName(), getElementType()).get();
    }

    @Override
    public FileType getElementType() {
        return FileType.SETTINGS;
    }

}
