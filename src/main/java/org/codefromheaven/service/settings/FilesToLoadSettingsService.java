package org.codefromheaven.service.settings;

import org.codefromheaven.dto.FileType;
import org.codefromheaven.dto.settings.SettingsDTO;

public class FilesToLoadSettingsService extends SettingsServiceBase {

    private static final FileType FILE_TYPE = FileType.FILES_TO_LOAD_SETTINGS;

    private FilesToLoadSettingsService() {}

    public static SettingsDTO load() {
        return loadSettingsFile(FILE_TYPE);
    }

}
