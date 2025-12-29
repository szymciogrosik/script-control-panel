package org.codefromheaven.dto;

import java.util.Arrays;

public enum Style {
    CLASSIC("classic.css"),
    WINTER_LIGHT("winter_light.css"),
    WINTER_DARK("winter_dark.css");

    private final String fileName;

    Style(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public static Style getStyle(String styleName) {
        return Arrays.stream(values())
                     .filter(style -> style.name().equals(styleName))
                     .findFirst()
                     .orElse(CLASSIC);
    }

}
