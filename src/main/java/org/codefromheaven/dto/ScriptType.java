package org.codefromheaven.dto;

public enum ScriptType {
    SERVICE_SCRIPT(Setting.SERVICE_SCRIPT_PATH, ConsoleType.BASH, false),
    UPDATE_DAP_FOR_TESTS_SCRIPT(Setting.UPDATE_TEST_DAP_SCRIPT_PATH, ConsoleType.BASH, false),
    OPEN_REMOTE_APP_SCRIPT(Setting.OPEN_REMOTE_APP_SCRIPT_PATH, ConsoleType.BASH, true),
    SKAT_VPN_SCRIPT(Setting.SKAT_VPN_PATH, ConsoleType.POWERSHELL, false);

    private final Setting path;
    private final ConsoleType console;
    private final boolean autoCloseConsole;

    ScriptType(Setting path, ConsoleType console, boolean autoCloseConsole) {
        this.path = path;
        this.console = console;
        this.autoCloseConsole = autoCloseConsole;
    }

    public String getPath() {
        return path.getValue();
    }

    public ConsoleType getConsole() {
        return console;
    }

    public boolean isNotAutoCloseConsole() {
        return !autoCloseConsole;
    }

}
