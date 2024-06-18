package org.codefromheaven.dto;

public class CommandDTO {

    private final String scriptPathVarName;
    private final boolean autoCloseConsole;
    private final String command;

    public CommandDTO(String scriptPathVarName, boolean autoCloseConsole, String command) {
        this.scriptPathVarName = scriptPathVarName;
        this.autoCloseConsole = autoCloseConsole;
        this.command = command;
    }

    public String getScriptPathVarName() {
        return scriptPathVarName;
    }

    public boolean isAutoCloseConsole() {
        return autoCloseConsole;
    }

    public String getCommand() {
        return command;
    }

}