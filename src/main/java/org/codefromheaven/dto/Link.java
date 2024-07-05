package org.codefromheaven.dto;

public enum Link {

    GH_PROJECT("https://github.com/szymciogrosik/script-control-panel"),
    GH_RELEASES(GH_PROJECT.getUrl() + "/releases"),
    PIN_JAR_TO_TASKBAR(GH_PROJECT.getUrl() + "/wiki/Guide-how-to-pin-executable-JAR-to-the-taskbar-or-Windows-start-for-Windows-11");

    private final String url;
    Link(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
