package org.codefromheaven.resources;

public enum ImageType {
    STANDARD("animals/standard"),
    CHRISTMAS("animals/christmas");

    private final String path;

    ImageType(final String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
