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

    private static final String DEFAULT_PREFIX = "default_";
    private static final String MY_OWN_PREFIX = "my_own_";
    private static final String CONFIG_FILE_EXTENSION = ".json";

    public static SettingsDTO loadSettingsFile(FileType fileType) {
        Optional<SettingsDTO> value = loadSettingsFile(getMyOwnFileName(fileType.name()));
        return value.orElseGet(() -> loadSettingsFile(getDefaultFileName(fileType.name())).get());
    }

    private static Optional<SettingsDTO> loadSettingsFile(String settingPath) {
        try {
            return Optional.of(JsonUtils.deserialize(new File(settingPath), SettingsDTO.class));
        } catch (FileNotFoundException e) {
            return Optional.empty();
        }
    }

    public static void saveSettings(FileType fileType, SettingsDTO settings) {
        Path path = Paths.get(getMyOwnFileName(fileType.name()));
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write(JsonUtils.serialize(settings));
        } catch (IOException e) {
            throw new RuntimeException("Failed to save settings", e);
        }
    }

    public static String loadValue(BaseSetting setting) {
        Optional<String> value = loadValue(setting, getMyOwnFileName(setting.getElementType().name()));
        return value.orElseGet(() -> loadValue(setting, getDefaultFileName(setting.getElementType().name())).get());
    }

    private static Optional<String> loadValue(BaseSetting setting, String settingPath) {
        try {
            SettingsDTO settingsList = JsonUtils.deserialize(new File(settingPath), SettingsDTO.class);
            return settingsList.getSettings().stream()
                               .filter(elem -> elem.getKey().equals(setting.getName()))
                               .map(KeyValueDTO::getValue).findFirst();
        } catch (FileNotFoundException e) {
            return Optional.empty();
        }
    }

    protected static boolean isPresentMyOwnSettingFile(FileType fileType) {
        try (BufferedReader ignored = new BufferedReader(new FileReader(getMyOwnFileName(fileType.name())))) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static void createEmptyMyOwnSettingsFile(FileType fileType) {
        try {
            PrintWriter writer = new PrintWriter(getMyOwnFileName(fileType.name()), StandardCharsets.UTF_8);
            writer.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
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
