package org.codefromheaven.helpers;

import javafx.stage.Screen;
import org.codefromheaven.dto.Setting;
import org.codefromheaven.service.settings.SettingsService;

import java.util.Optional;

public class MaxHighUtils {

    private MaxHighUtils() {
    }

    public static double getMaxHeight() {
        Optional<String> maxWindowHeightString = SettingsService.loadValue(Setting.MAX_WINDOW_HEIGHT);
        if (maxWindowHeightString.isPresent() && !maxWindowHeightString.get().isEmpty()) {
            return Integer.parseInt(maxWindowHeightString.get());
        }
        // Calculate size based on screen height
        double screenHeight = Screen.getPrimary().getVisualBounds().getHeight();
        return screenHeight - 65;
    }

}
