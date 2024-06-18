package org.codefromheaven.service.command;

import org.codefromheaven.dto.CommandDTO;
import org.codefromheaven.dto.Setting;

import java.io.IOException;

public class PowerShellSetupService implements Runnable {

    private static final String SEPARATOR = " ; ";

    private final CommandDTO commandDTO;

    public PowerShellSetupService(CommandDTO commandDTO) {
        this.commandDTO = commandDTO;
    }

    public void run() {
        runCommand(commandDTO.getScriptPathVarName(), commandDTO.isAutoCloseConsole(), commandDTO.getCommand());
    }

    private static void runCommand(String scriptPathVarName, boolean autoCloseConsole, String command) {
        try {
            String finalCommand = getOpenDirectoryCommand(scriptPathVarName) + SEPARATOR + command;

            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command(
                    "powershell.exe",
                    "Start-Process powershell.exe '" +
                            (!autoCloseConsole ? "-NoExit" : "")
                            + " \"[Console]::Title = ''Buenos Dias PowerShell run''; " + finalCommand + "\"'"
            );
            processBuilder.start();
        } catch (IOException e) {
            System.out.println(" --- Interruption in RunCommand: " + e);
            Thread.currentThread().interrupt();
        }
    }

    private static String getOpenDirectoryCommand(String scriptPathVarName) {
        return "cd \"" + Setting.getSettingByName(scriptPathVarName).getValue() + "\"";
    }

}
