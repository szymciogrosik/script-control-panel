package org.codefromheaven.dto;

public enum Link {

    ABOUT_AUTHOR("https://github.com/szymciogrosik"),
    GH_PROJECT( ABOUT_AUTHOR.getUrl() + "/script-control-panel"),
    GH_RELEASES(GH_PROJECT.getUrl() + "/releases"),
    WIKI(GH_PROJECT.getUrl() + "/wiki"),
    WIKI_CONFIGURATION(WIKI.getUrl() + "/Configuration"),
    PIN_JAR_TO_TASKBAR(WIKI.getUrl() + "/Guide-how-to-pin-executable-JAR-to-the-taskbar-or-Windows-start-for-Windows-11"),
    PIN_BASH_TO_TASKBAR(WIKI.getUrl() + "/Guide-how-to-pin-bash-script-to-the-taskbar-or-Windows-start-for-Windows-11"),
    RUN_BASH_ON_WINDOWS_STARTUP(WIKI.getUrl() + "/Guide-how-to-add-shortcut-to-the-Windows-startup"),
    ISSUES(GH_PROJECT.getUrl() + "/issues"),
    API_ALL_RELEASES("https://api.github.com/repos/szymciogrosik/script-control-panel/releases");

    private final String url;
    Link(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
