package org.codefromheaven.dto;

import java.util.Arrays;

public enum Style {
    CLASSIC("classic.css"),
    WINTER("winter.css");

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
