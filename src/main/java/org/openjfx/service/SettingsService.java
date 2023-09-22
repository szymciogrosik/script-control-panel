package org.openjfx.service;

import org.openjfx.dto.ElementType;
import org.openjfx.dto.Setting;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Optional;

public class SettingsService {

    private static final String DELIMITER = ";";

    public static String getVariable(Setting setting) {
        Optional<String> value = getVariableBase(setting, ElementType.SETTING.getPersonalizedConfigName());
        return value.orElseGet(() -> getVariableBase(setting, ElementType.SETTING.getDefaultFileName()).get());
    }

    public static Optional<String> getVariableBase(Setting setting, String settingCsvPath) {
        try (BufferedReader br = new BufferedReader(new FileReader(settingCsvPath))) {
            String line;
            // skip first line
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] values = line.split(DELIMITER);
                if (setting.name().equals(values[0])) {
                    return Optional.of(values[1]);
                }
            }
        } catch (IOException e) {
            return Optional.empty();
        }
        return Optional.empty();
    }

}
