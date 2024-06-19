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
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public abstract class SettingsServiceBase {

    public static SettingsDTO getSettingsFile(FileType fileType) {
        Optional<SettingsDTO> value = getSettingsFile(fileType.getPersonalizedConfigName());
        return value.orElseGet(() -> getSettingsFile(fileType.getDefaultFileName()).get());
    }

    private static Optional<SettingsDTO> getSettingsFile(String settingCsvPath) {
        try {
            return Optional.of(JsonUtils.deserialize(new File(settingCsvPath), SettingsDTO.class));
        } catch (FileNotFoundException e) {
            return Optional.empty();
        }
    }

    public static void saveSettings(FileType fileType, SettingsDTO settings) {
        Path path = Paths.get(fileType.getPersonalizedConfigName());
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write(JsonUtils.serialize(settings));
        } catch (IOException e) {
            throw new RuntimeException("Failed to save settings", e);
        }
    }

    public static String getValue(BaseSetting setting) {
        Optional<String> value = getValue(setting, setting.getElementType().getPersonalizedConfigName());
        return value.orElseGet(() -> getValue(setting, setting.getElementType().getDefaultFileName()).get());
    }

    private static Optional<String> getValue(BaseSetting setting, String settingCsvPath) {
        try {
            SettingsDTO settingsList = JsonUtils.deserialize(new File(settingCsvPath), SettingsDTO.class);
            return settingsList.getSettings().stream()
                               .filter(elem -> elem.getKey().equals(setting.getName()))
                               .map(KeyValueDTO::getValue).findFirst();
        } catch (FileNotFoundException e) {
            return Optional.empty();
        }
    }

    protected static boolean isPresentMyOwnSettingFile(FileType fileType) {
        try (BufferedReader ignored = new BufferedReader(new FileReader(fileType.getPersonalizedConfigName()))) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static void createEmptyMyOwnSettingsFile(FileType fileType) {
        try {
            PrintWriter writer = new PrintWriter(fileType.getPersonalizedConfigName(), StandardCharsets.UTF_8);
            writer.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

}
