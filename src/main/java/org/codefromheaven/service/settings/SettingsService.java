package org.codefromheaven.service.settings;

import org.codefromheaven.dto.FileType;
import org.codefromheaven.dto.Setting;
import org.codefromheaven.dto.settings.BaseSetting;
import org.codefromheaven.dto.settings.KeyValueDTO;
import org.codefromheaven.dto.settings.SettingsDTO;
import org.codefromheaven.resources.AnimalNamesProvider;

import java.util.Optional;

public class SettingsService extends SettingsServiceBase {

    private static final FileType FILE_TYPE = FileType.SETTINGS;

    private SettingsService() {}

    public static SettingsDTO load() {
        return loadSettingsFile(FILE_TYPE);
    }

    public static void saveSettings(SettingsDTO settings) {
        saveSettings(FILE_TYPE, settings);
    }

    public static String getAppName() {
        return loadValue(Setting.APP_NAME).get();
    }

    public static String getAnimalImageFromSettingsOrAddIfDoesNotExist() {
        Optional<String> currentAnimalOptional = loadValue(Setting.IMAGE_NAME);
        if (currentAnimalOptional.isPresent() && !currentAnimalOptional.get().isEmpty()) {
            return currentAnimalOptional.get();
        } else {
            String animal = AnimalNamesProvider.getRandomAnimalName();
            replaceOrCreateConfigVariable(Setting.IMAGE_NAME, animal);
            return animal;
        }
    }

    public static void replaceOrCreateConfigVariable(BaseSetting elementToReplace, String newValue) {
        SettingsDTO settings = loadSettingsFile(FILE_TYPE);
        String key = elementToReplace.getName();

        Optional<KeyValueDTO> keyValue =
                settings.getSettings().stream().filter(elem -> elem.getKey().equals(key)).findFirst();
        if (keyValue.isPresent()) {
            keyValue.get().setValue(newValue);
        } else {
            settings.getSettings().add(new KeyValueDTO(key, newValue, ""));
        }

        saveSettings(FILE_TYPE, settings);
    }

    public static Optional<String> loadValue(String setting) {
        return SettingsServiceBase.loadValue(setting, FILE_TYPE);
    }

    public static Optional<String> loadValue(BaseSetting setting) {
        return SettingsServiceBase.loadValue(setting.getName(), FILE_TYPE);
    }

    public static boolean isAllowedToDownloadPreReleases() {
        return "true".equals(loadValue(Setting.ALLOW_PRE_RELEASES).orElse(""));
    }

}
