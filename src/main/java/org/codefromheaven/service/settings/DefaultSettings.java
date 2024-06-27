package org.codefromheaven.service.settings;

import org.codefromheaven.dto.Setting;
import org.codefromheaven.dto.settings.KeyValueDTO;
import org.codefromheaven.dto.settings.SettingsDTO;

import java.util.Arrays;

public class DefaultSettings {

    public static final SettingsDTO ALL = new SettingsDTO(Arrays.asList(
            new KeyValueDTO(
                    Setting.BASH_PATH.getName(),
                    "C:/Program Files/Git/git-bash.exe",
                    "Location of Git bash"
            ),
            new KeyValueDTO(
                    Setting.MAX_WINDOW_HEIGHT.getName(),
                    "",
                    "Max window height (if empty, height will be adjusted to your screen)"
            ),
            new KeyValueDTO(
                    Setting.IMAGE_NAME.getName(),
                    "",
                    "Select image which displays in application"
            ),
            new KeyValueDTO(
                    Setting.APP_NAME.getName(),
                    "Script control panel",
                    "Override your app name"
            )
    ));

    private DefaultSettings() {
    }

}
