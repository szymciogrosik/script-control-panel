package org.codefromheaven.dto;

public enum Style {

    CLASSIC("classic-style.css");

    private final String fileName;

    Style(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

}
