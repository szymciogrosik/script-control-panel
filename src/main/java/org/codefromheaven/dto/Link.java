package org.codefromheaven.dto;

public enum Link {

    ABOUT_AUTHOR("https://github.com/szymciogrosik"),
    GH_PROJECT( ABOUT_AUTHOR.getUrl() + "/script-control-panel"),
    GH_RELEASES(GH_PROJECT.getUrl() + "/releases"),
    DOCUMENTATION(GH_PROJECT.getUrl() + "/wiki"),
    ISSUES(GH_PROJECT.getUrl() + "/issues"),
    PIN_JAR_TO_TASKBAR(GH_PROJECT.getUrl() + "/wiki/Guide-how-to-pin-executable-JAR-to-the-taskbar-or-Windows-start-for-Windows-11"),
    API_LATEST_RELEASE("https://api.github.com/repos/szymciogrosik/script-control-panel/releases/latest"),
    API_ALL_RELEASES("https://api.github.com/repos/szymciogrosik/script-control-panel/releases");

    private final String url;
    Link(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
