package org.codefromheaven.dto;

public enum Style {

    CLASSIC("classic-style.css"),
    CYBER_PUNK("cyber-punk-style.css");

    private final String fileName;

    Style(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

}
