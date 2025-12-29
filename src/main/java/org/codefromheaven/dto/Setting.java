package org.codefromheaven.dto;

import org.codefromheaven.dto.settings.BaseSetting;
import org.codefromheaven.service.settings.SettingsServiceBase;

public enum Setting implements BaseSetting {
    TMP_DIRECTORY,
    CONFIG_DIR,
    ALLOW_PRE_RELEASES,
    BASH_PATH,
    MAX_WINDOW_HEIGHT,
    IMAGE_NAME,
    APP_STYLE,
    APP_NAME,
    ALLOW_FOR_UPGRADES,
    PYTHON_SCRIPTS_PREFIX;

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
