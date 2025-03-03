package org.codefromheaven.dto;

import org.codefromheaven.dto.settings.BaseSetting;
import org.codefromheaven.service.settings.SettingsServiceBase;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum Setting implements BaseSetting {
    TMP_DIRECTORY,
    CONFIG_DIR,
    ALLOW_PRE_RELEASES,
    BASH_PATH,
    MAX_WINDOW_HEIGHT,
    IMAGE_NAME,
    APP_NAME,
    ALLOW_FOR_UPGRADES,
    STYLE_NAME;

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

    private static List<Setting> getComboSettings() {
        return List.of(IMAGE_NAME, STYLE_NAME);
    }

    private static List<Setting> getCheckboxSettings() {
        return List.of(ALLOW_PRE_RELEASES);
    }

    private static List<Setting> getTextSettings() {
        return Stream.of(Setting.values()).filter(
                setting -> !isComboSetting(setting.getName()) && !isCheckboxSetting(setting.getName())
        ).collect(Collectors.toList());
    }

    public static boolean isComboSetting(String settingName) {
        return getComboSettings().stream().anyMatch(setting -> setting.name().equals(settingName));
    }

    public static boolean isCheckboxSetting(String settingName) {
        return getCheckboxSettings().stream().anyMatch(setting -> setting.name().equals(settingName));
    }

    public static boolean isTextSetting(String settingName) {
        return getTextSettings().stream().anyMatch(setting -> setting.name().equals(settingName));
    }

}
