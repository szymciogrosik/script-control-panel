package org.codefromheaven.service.settings;

import org.codefromheaven.dto.FileType;
import org.codefromheaven.dto.Setting;
import org.codefromheaven.dto.settings.BaseSetting;
import org.codefromheaven.dto.settings.KeyValueDTO;
import org.codefromheaven.dto.settings.SettingsDTO;
import org.codefromheaven.resources.AnimalProvider;

import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SettingsService extends SettingsServiceBase {

    private static final FileType FILE_TYPE = FileType.SETTINGS;

    public SettingsDTO load() {
        return loadSettingsFile(FILE_TYPE);
    }

    public void saveSettings(SettingsDTO settings) {
        saveSettings(FILE_TYPE, settings);
    }

    public String getAppName() {
        return loadValue(Setting.APP_NAME).get();
    }

    public String getPythonScriptsPrefix() {
        return loadValue(Setting.PYTHON_SCRIPTS_PREFIX).get();
    }

    public String getAnimalImageFromSettingsOrAddIfDoesNotExist() {
        Optional<String> currentAnimalOptional = loadValue(Setting.IMAGE_NAME);
        if (currentAnimalOptional.isPresent() && !currentAnimalOptional.get().isEmpty() &&
                AnimalProvider.doesAnimalNameExist(currentAnimalOptional.get())) {
            return currentAnimalOptional.get();
        } else {
            String animal = AnimalProvider.getNextAnimal().name();
            replaceOrCreateConfigVariable(Setting.IMAGE_NAME, animal);
            return animal;
        }
    }

    public void replaceOrCreateConfigVariable(BaseSetting elementToReplace, String newValue) {
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

    public Optional<String> loadValue(String setting) {
        return SettingsServiceBase.loadValue(setting, FILE_TYPE);
    }

    public Optional<String> loadValue(BaseSetting setting) {
        return SettingsServiceBase.loadValue(setting.getName(), FILE_TYPE);
    }

    public boolean isAllowedToDownloadPreReleases() {
        return "true".equals(loadValue(Setting.ALLOW_PRE_RELEASES).orElse(""));
    }

    public boolean isAllowedToUpdate() {
        return "true".equals(loadValue(Setting.ALLOW_FOR_UPGRADES).orElse(""));
    }

}
