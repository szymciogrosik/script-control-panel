package org.codefromheaven.service.settings;

import org.codefromheaven.dto.settings.BaseSetting;
import org.codefromheaven.dto.FileType;
import org.codefromheaven.dto.settings.KeyValueDTO;
import org.codefromheaven.dto.settings.SettingsDTO;
import org.codefromheaven.helpers.JsonUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class SettingsServiceBase {

    private static final String DEFAULT_PREFIX = "default_";
    private static final String MY_OWN_PREFIX = "my_own_";
    private static final String CONFIG_FILE_EXTENSION = ".json";

    public static SettingsDTO loadSettingsFile(FileType fileType) {
        Optional<SettingsDTO> myOwnSettings = loadSettingsFile(getMyOwnFileName(fileType.name()));
        SettingsDTO defaultSettings = mergeDefaultSettings(loadSettingsFile(getDefaultFileName(fileType.name())).orElse(new SettingsDTO()), fileType);

        if (myOwnSettings.isEmpty()) {
            return defaultSettings;
        }

        List<KeyValueDTO> settingsToReturn = new ArrayList<>();

        // Iterate through default setting and if you will be found my_own_value then add it
        defaultSettings.getSettings().forEach(defaultSetting -> {
            Optional<KeyValueDTO> myOwn =
                    myOwnSettings.get().getSettings().stream()
                                 .filter(elem -> defaultSetting.getKey().equals(elem.getKey())).findFirst();
            if (myOwn.isPresent() && defaultSetting.isEditable()) {
                // Override my own description with the newest one from default
                myOwn.get().setDescription(defaultSetting.getDescription());
                settingsToReturn.add(myOwn.get());
            } else {
                settingsToReturn.add(defaultSetting);
            }
        });

        // Add all my_own settings which was not present in default file
        myOwnSettings.get().getSettings().forEach(myOwnSetting -> {
            if (settingsToReturn.stream().noneMatch(elem -> myOwnSetting.getKey().equals(elem.getKey()))) {
                settingsToReturn.add(myOwnSetting);
            }
        });

        return new SettingsDTO(settingsToReturn);
    }

    private static SettingsDTO mergeDefaultSettings(SettingsDTO defaultSettingsFromFile, FileType fileType) {
        if (fileType != FileType.SETTINGS) {
            return defaultSettingsFromFile;
        }
        List<KeyValueDTO> keyValueList = new ArrayList<>();
        // Add all default settings from class which does not present in the file
        DefaultSettings.ALL.getSettings().forEach(elem -> {
            if (defaultSettingsFromFile.getSettings().stream().noneMatch(elem2 -> elem2.getKey().equals(elem.getKey()))) {
                keyValueList.add(new KeyValueDTO(elem.getKey(), elem.getValue(), elem.isEditable(), elem.getDescription()));
            }
        });
        // Add all default settings from file
        keyValueList.addAll(defaultSettingsFromFile.getSettings());
        return new SettingsDTO(keyValueList);
    }

    private static Optional<SettingsDTO> loadSettingsFile(String settingPath) {
        try {
            return Optional.of(JsonUtils.deserialize(new File(settingPath), SettingsDTO.class));
        } catch (FileNotFoundException e) {
            return Optional.empty();
        }
    }

    public static void saveSettings(FileType fileType, SettingsDTO customSettings) {
        SettingsDTO settingsToSave = getCustomSettingsDifferentThanDefault(fileType, customSettings);
        Path path = Paths.get(getMyOwnFileName(fileType.name()));
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write(JsonUtils.serialize(settingsToSave));
        } catch (IOException e) {
            throw new RuntimeException("Failed to save settings", e);
        }
    }

    private static SettingsDTO getCustomSettingsDifferentThanDefault(FileType fileType, SettingsDTO customSettings) {
        SettingsDTO defaultSettings = mergeDefaultSettings(loadSettingsFile(getDefaultFileName(fileType.name())).orElse(new SettingsDTO()), fileType);
        return new SettingsDTO(
                customSettings.getSettings().stream()
                           .filter(setting -> defaultSettings.getSettings().stream().noneMatch(defaultSetting -> defaultSetting.equals(setting)))
                           .collect(Collectors.toList()));
    }

    public static Optional<String> loadValue(String setting, FileType fileType) {
        SettingsDTO settingsList = loadSettingsFile(fileType);
        return settingsList.getSettings().stream()
                           .filter(elem -> elem.getKey().equals(setting))
                           .map(KeyValueDTO::getValue).findFirst();
    }

    protected static boolean isPresentMyOwnSettingFile(FileType fileType) {
        try (BufferedReader ignored = new BufferedReader(new FileReader(getMyOwnFileName(fileType.name())))) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static String getDefaultFileName(String fileName) {
        return DEFAULT_PREFIX + getDefaultFileNameBase(fileName);
    }

    public static String getMyOwnFileName(String fileName) {
        return MY_OWN_PREFIX + getDefaultFileNameBase(fileName);
    }

    private static String getDefaultFileNameBase(String fileName) {
        return fileName.toLowerCase() + CONFIG_FILE_EXTENSION;
    }

}
