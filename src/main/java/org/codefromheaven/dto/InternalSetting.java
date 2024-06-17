package org.codefromheaven.dto;

import org.codefromheaven.service.settings.InternalSettingsService;

public enum InternalSetting implements BaseSetting {

    IMAGE_NAME;

    @Override
    public String getName() {
        return name();
    }

    @Override
    public String getValue() {
        return InternalSettingsService.getVariable(this);
    }

    @Override
    public FileType getElementType() {
        return FileType.INTERNAL_SETTINGS;
    }

}
