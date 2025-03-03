package org.codefromheaven.service.settings;

import org.codefromheaven.dto.Setting;
import org.codefromheaven.dto.Style;
import org.codefromheaven.dto.settings.KeyValueDTO;
import org.codefromheaven.dto.settings.SettingsDTO;

import java.util.Arrays;

public class DefaultSettings {

    public static final SettingsDTO ALL = new SettingsDTO(Arrays.asList(
            new KeyValueDTO(
                    Setting.TMP_DIRECTORY.getName(),
                    "tmp",
                    false,
                    "Location of temporary files f.e. for updating application"
            ),
            new KeyValueDTO(
                    Setting.CONFIG_DIR.getName(),
                    "config",
                    false,
                    "Location of configuration files f.e. 'config' or empty when in the same location"
            ),
            new KeyValueDTO(
                    Setting.ALLOW_PRE_RELEASES.getName(),
                    "false",
                    "Allow for downloading pre-releases (can be unstable)"
            ),
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
            ),
            new KeyValueDTO(
                    Setting.ALLOW_FOR_UPGRADES.getName(),
                    "true",
                    "Allow for manual upgrades by user"
            ),
            new KeyValueDTO(
                    Setting.STYLE_NAME.getName(),
                    Style.CLASSIC.name(),
                    "Style of your application"
            )
    ));

    private DefaultSettings() {
    }

}
