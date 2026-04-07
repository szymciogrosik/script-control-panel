package org.codefromheaven.dto;

import lombok.Getter;

@Getter
public enum Link {

    ABOUT_AUTHOR("https://github.com/szymciogrosik"),
    GH_PROJECT( ABOUT_AUTHOR.getUrl() + "/script-control-panel"),
    GH_RELEASES(GH_PROJECT.getUrl() + "/releases"),
    WIKI(GH_PROJECT.getUrl() + "/wiki"),
    WIKI_CONFIGURATION(WIKI.getUrl() + "/Configuration"),
    RUN_BASH_ON_WINDOWS_STARTUP(WIKI.getUrl() + "/Guide-how-to-add-shortcut-to-the-Windows-startup"),
    PIN_APP_TO_START_MENU_OR_TASKBAR(WIKI.getUrl() + "/Guide-how-to-pin-shortcut-to-the-Windows-start-menu-or-taskbar"),
    ISSUES(GH_PROJECT.getUrl() + "/issues"),
    API_LATEST_RELEASE_JSON("https://szymciogrosik.github.io/script-control-panel/latest_release.json");

    private final String url;

    Link(String url) {
        this.url = url;
    }
}
