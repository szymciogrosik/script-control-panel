package org.openjfx.dto;

public enum ElementType {
    SERVICE_COMMAND("commands.csv"),
    LINK("links.csv");

    private final String fileName;

    ElementType(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

}
