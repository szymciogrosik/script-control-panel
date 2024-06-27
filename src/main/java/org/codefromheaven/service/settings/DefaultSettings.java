package org.codefromheaven.service.settings;

import org.codefromheaven.dto.settings.KeyValueDTO;
import org.codefromheaven.dto.settings.SettingsDTO;

import java.util.Arrays;

public class DefaultSettings {

    public static SettingsDTO ALL = new SettingsDTO(Arrays.asList(
            new KeyValueDTO(
                    "BASH_PATH",
                    "C:/Program Files/Git/git-bash.exe",
                    "Location of Git bash"
            ),
            new KeyValueDTO(
                    "MAX_WINDOW_HEIGHT",
                    "",
                    "Max window height (if empty, height will be adjusted to your screen)"
            ),
            new KeyValueDTO(
                    "IMAGE_NAME",
                    "",
                    "Select image which displays in application"
            )
    ));

    private DefaultSettings() {
    }

}
