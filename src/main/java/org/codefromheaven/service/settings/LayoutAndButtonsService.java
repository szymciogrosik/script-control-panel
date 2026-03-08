package org.codefromheaven.service.settings;

import org.codefromheaven.dto.FileType;
import org.codefromheaven.dto.data.LayoutAndButtonsDTO;
import org.codefromheaven.dto.data.DirectoryDTO;
import org.codefromheaven.service.LoadFromJsonService;

import java.util.Optional;

public class LayoutAndButtonsService {

    private static final FileType FILE_TYPE = FileType.LAYOUT_AND_BUTTONS;
    private static LayoutAndButtonsDTO cachedLayoutAndButtons;

    private LayoutAndButtonsService() {
    }

    public static LayoutAndButtonsDTO load() {
        return LoadFromJsonService.loadLayoutAndButtons(FILE_TYPE.name());
    }

    public static String getDirectoryPath(String directoryName) {
        if (cachedLayoutAndButtons == null) {
            cachedLayoutAndButtons = load();
        }

        Optional<DirectoryDTO> dir = cachedLayoutAndButtons.directories().stream()
                .filter(d -> d.name().equals(directoryName))
                .findFirst();

        return dir.map(DirectoryDTO::path).orElse("");
    }
}
