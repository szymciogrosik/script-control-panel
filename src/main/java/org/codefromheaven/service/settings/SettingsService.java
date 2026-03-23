package org.codefromheaven.service.settings;

import org.codefromheaven.dto.FileType;
import org.codefromheaven.dto.Setting;
import org.codefromheaven.dto.settings.BaseSetting;
import org.codefromheaven.dto.settings.SettingDTO;
import org.codefromheaven.dto.settings.SettingType;
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
        saveMyOwnSettings(FILE_TYPE, settings);
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
            String animal = AnimalProvider.getNextAnimalOrRandomIfNotPresent().name();
            replaceOrCreateConfigVariable(Setting.IMAGE_NAME, animal);
            return animal;
        }
    }

    public void replaceOrCreateConfigVariable(BaseSetting elementToReplace, String newValue) {
        SettingsDTO settings = loadSettingsFile(FILE_TYPE);
        String key = elementToReplace.getName();

        Optional<SettingDTO> keyValue = settings.getSettings().stream().filter(elem -> elem.getKey().equals(key))
                .findFirst();
        if (keyValue.isPresent()) {
            keyValue.get().setValue(newValue);
        } else {
            Optional<SettingDTO> defaultElem = DefaultSettings.ALL.getSettings().stream()
                    .filter(e -> e.getKey().equals(key)).findFirst();
            SettingType type = defaultElem.isPresent() ? defaultElem.get().getType() : SettingType.TEXT;
            settings.getSettings().add(new SettingDTO(key, newValue, type, ""));
        }

        saveMyOwnSettings(FILE_TYPE, settings);
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
