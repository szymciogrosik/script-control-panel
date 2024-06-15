package org.codefromheaven.service.command;

import org.codefromheaven.dto.PowerShellDTO;
import org.codefromheaven.dto.ScriptType;

import java.io.IOException;

public class PowerShellSetupService implements Runnable {

    private static final String SEPARATOR = " ; ";

    private final PowerShellDTO powerShellDTO;

    public PowerShellSetupService(PowerShellDTO powerShellDTO) {
        this.powerShellDTO = powerShellDTO;
    }

    public void run() {
        runCommand(powerShellDTO.getScriptType(), powerShellDTO.getCommand());
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
