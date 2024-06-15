package org.codefromheaven.dto;

public class PowerShellDTO {

    private final ScriptType scriptType;
    private final String command;

    public PowerShellDTO(ScriptType scriptType, String command) {
        this.scriptType = scriptType;
        this.command = command;
    }

    public ScriptType getScriptType() {
        return scriptType;
    }

    public String getCommand() {
        return command;
    }

}
