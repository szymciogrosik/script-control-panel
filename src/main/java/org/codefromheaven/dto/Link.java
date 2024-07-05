package org.codefromheaven.dto;

public enum Link {

    GH_PROJECT("https://github.com/szymciogrosik/script-control-panel"),
    GH_RELEASES(GH_PROJECT.getUrl() + "/releases");

    private final String url;
    Link(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
