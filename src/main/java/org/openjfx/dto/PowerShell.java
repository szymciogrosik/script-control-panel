package org.openjfx.dto;

import java.io.IOException;

public class PowerShell implements Runnable {

    private static final String SEPARATOR = " ; ";

    private final ScriptType scriptType;
    private final String command;

    public PowerShell(ScriptType scriptType, String command) {
        this.scriptType = scriptType;
        this.command = command;
    }

    public void run() {
        runCommand(scriptType, command);
    }

    private static void runCommand(ScriptType scriptType, String command) {
        try {
            String finalCommand = getOpenDirectoryCommand(scriptType) + SEPARATOR + command;

            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command(
                    "powershell.exe",
                    "Start-Process powershell.exe '" +
                            (scriptType.isNotAutoCloseConsole() ? "-NoExit" : "")
                            + " \"[Console]::Title = ''Buenos Dias PowerShell run''; " + finalCommand + "\"'"
            );
            processBuilder.start();
        } catch (IOException e) {
            System.out.println(" --- Interruption in RunCommand: " + e);
            Thread.currentThread().interrupt();
        }
    }

    private static String getOpenDirectoryCommand(ScriptType scriptType) {
        return "cd \"" + scriptType.getPath() + "\"";
    }

}
