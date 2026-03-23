package org.codefromheaven.service.settings;

import org.codefromheaven.dto.ConfigType;
import org.codefromheaven.dto.FileType;
import org.codefromheaven.dto.data.ButtonDTO;
import org.codefromheaven.dto.settings.VisibilitySettingDTO;
import org.codefromheaven.dto.settings.VisibilitySettingsDTO;
import org.codefromheaven.dto.settings.VisibilitySettingKey;
import org.codefromheaven.helpers.FileUtils;
import org.codefromheaven.helpers.JsonUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class VisibilitySettings {

    private final VisibilitySettingsDTO hiddenElements;
    private final VisibilitySettingsDTO visibleElements;

    public VisibilitySettings() {
        this.hiddenElements = loadVisibleElementSettings(FileType.HIDDEN_ELEMENTS_SETTINGS);
        this.visibleElements = loadVisibleElementSettings(FileType.VISIBLE_ELEMENTS_SETTINGS);
    }

    public boolean isVisible(ButtonDTO button) {
        boolean visibleAsDefault = button.isVisibleAsDefault();
        if (visibleAsDefault) {
            return doesNotExistMatchingSetting(hiddenElements, button);
        } else {
            return doesExistMatchingSetting(visibleElements, button);
        }
    }

    public void saveSettings() {
        saveVisibilitySettings(FileType.HIDDEN_ELEMENTS_SETTINGS, hiddenElements);
        saveVisibilitySettings(FileType.VISIBLE_ELEMENTS_SETTINGS, visibleElements);
    }

    private void saveVisibilitySettings(FileType fileType, VisibilitySettingsDTO settings) {
        FileUtils.createOrReplaceConfigDirectory(SettingsServiceBase.getDynamicConfigDir());
        Path path = Paths.get(SettingsServiceBase.getFileDir(fileType.name(), ConfigType.MY_OWN));
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write(JsonUtils.serialize(settings));
        } catch (IOException e) {
            throw new RuntimeException("Failed to save settings", e);
        }
    }

    public void updateVisibilitySetting(ButtonDTO button, boolean visible) {
        removeMatchingSetting(hiddenElements, button);
        removeMatchingSetting(visibleElements, button);

        boolean visibleAsDefault = button.isVisibleAsDefault();
        VisibilitySettingsDTO settingsToUpdate = null;
        if (visible && !visibleAsDefault) {
            settingsToUpdate = visibleElements;
        } else if (!visible && visibleAsDefault) {
            settingsToUpdate = hiddenElements;
        }

        if (settingsToUpdate != null) {
            addMatchingSetting(settingsToUpdate, button);
        }
    }

    private static void addMatchingSetting(VisibilitySettingsDTO settings, ButtonDTO button) {
        if (settings.settings() == null) {
            return;
        }
        settings.settings().add(new VisibilitySettingDTO(button.getVisibilitySettingKey().getStringKey(), button.getVisibilitySettingKey().buttonName()));
    }

    private static void removeMatchingSetting(VisibilitySettingsDTO settings, ButtonDTO button) {
        if (settings.settings() == null)
            return;
        settings.settings().removeIf(elem -> isMatchingSetting(elem, button.getVisibilitySettingKey()));
    }

    private static boolean doesExistMatchingSetting(VisibilitySettingsDTO settings, ButtonDTO button) {
        if (settings.settings() == null)
            return false;
        return settings.settings().stream().anyMatch(elem -> isMatchingSetting(elem, button.getVisibilitySettingKey()));
    }

    private static boolean doesNotExistMatchingSetting(VisibilitySettingsDTO settings, ButtonDTO button) {
        if (settings.settings() == null)
            return true;
        return settings.settings().stream().noneMatch(elem -> isMatchingSetting(elem, button.getVisibilitySettingKey()));
    }

    private static boolean isMatchingSetting(VisibilitySettingDTO keyValue, VisibilitySettingKey visibilitySettingKey) {
        return keyValue.key().equals(visibilitySettingKey.getStringKey())
                && keyValue.value().equals(visibilitySettingKey.buttonName());
    }

    private static VisibilitySettingsDTO loadVisibleElementSettings(FileType fileType) {
        boolean presentMyOwnSettings = SettingsServiceBase.isPresentMyOwnSettingFile(fileType);
        if (presentMyOwnSettings) {
            try {
                VisibilitySettingsDTO loaded = JsonUtils.deserialize(
                        new File(SettingsServiceBase.getFileDir(fileType.name(), ConfigType.MY_OWN)), VisibilitySettingsDTO.class);
                if (loaded != null && loaded.settings() != null) {
                    return loaded;
                }
            } catch (FileNotFoundException ignored) {
            }
        }
        return new VisibilitySettingsDTO(new ArrayList<>());
    }

}
