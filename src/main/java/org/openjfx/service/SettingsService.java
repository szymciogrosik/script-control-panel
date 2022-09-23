package org.openjfx.service;

import org.openjfx.dto.Setting;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SettingsService {

    private static final String SETTINGS_FILE_NAME = "settings.csv";
    private static final String DELIMITER = ";";

    public static String getVariable(Setting setting) {
        try (BufferedReader br = new BufferedReader(new FileReader(SETTINGS_FILE_NAME))) {
            String line;
            // skip first line
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] values = line.split(DELIMITER);
                if(setting.name().equals(values[0])) {
                    return values[1];
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

}
