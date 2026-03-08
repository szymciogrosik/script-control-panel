package org.codefromheaven.service.settings;

import org.codefromheaven.dto.Setting;
import org.codefromheaven.dto.Style;
import org.codefromheaven.dto.settings.SettingDTO;
import org.codefromheaven.dto.settings.SettingType;
import org.codefromheaven.dto.settings.SettingsDTO;

import java.util.Arrays;

public class DefaultSettings {

    public static final SettingsDTO ALL = new SettingsDTO(
            Arrays.asList(
                    new SettingDTO(Setting.APP_NAME.getName(), "Script Control Panel", SettingType.TEXT,
                                   "App name"),
                    new SettingDTO(Setting.APP_STYLE.getName(), Style.CLASSIC.name(), SettingType.SELECT,
                                   "Application style"),
                    new SettingDTO(Setting.IMAGE_NAME.getName(), "", SettingType.SELECT,
                                   "Image which displays in application"),
                    new SettingDTO(Setting.MAX_WINDOW_HEIGHT.getName(), "", SettingType.NUMBER,
                                   "Max window height (if empty, height will be adjusted to your screen)"),
                    new SettingDTO(Setting.PYTHON_SCRIPTS_PREFIX.getName(), "py", SettingType.TEXT,
                                   "Python scripts prefix"),
                    new SettingDTO(Setting.BASH_PATH.getName(), "C:/Program Files/Git/git-bash.exe", SettingType.PATH,
                                   "Location of Git bash"),
                    new SettingDTO(Setting.TMP_DIRECTORY.getName(), "tmp", SettingType.PATH,
                                   "Location of temporary files f.e. for updating application", false),
                    new SettingDTO(Setting.CONFIG_DIR.getName(), "config", SettingType.PATH,
                                   "Location of configuration files f.e. 'config' or empty when in the same location", false)
            )
    );

    private DefaultSettings() {
    }

}
