package org.codefromheaven.service.command;

import org.codefromheaven.dto.CommandDTO;
import org.codefromheaven.dto.Setting;
import org.codefromheaven.service.settings.SettingsService;

import java.io.IOException;

public class GitBashSetupService implements Runnable {

    private static final String SEPARATOR = " ; ";

    private final CommandDTO commandDTO;

    public GitBashSetupService(CommandDTO commandDTO) {
        this.commandDTO = commandDTO;
    }

    public void run() {
        runCommand(commandDTO.getScriptPathVarName(), commandDTO.isAutoCloseConsole(), commandDTO.getCommand());
    }

    private static void runCommand(String scriptPathVarName, boolean autoCloseConsole, String command) {
        try {
            String finalCommand = getOpenDirectoryCommand(scriptPathVarName) + SEPARATOR + command;
            if (!autoCloseConsole) {
                finalCommand += (SEPARATOR + getWaitingForButtonCommand());
            }

            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command(Setting.BASH_PATH.getValue(), "-c", finalCommand);
            processBuilder.start();
        } catch (IOException e) {
            System.out.println(" --- Interruption in RunCommand: " + e);
            Thread.currentThread().interrupt();
        }
    }

    private static String getOpenDirectoryCommand(String scriptPathVarName) {
        return "cd " + SettingsService.loadValue(scriptPathVarName).get();
    }

    private static String getWaitingForButtonCommand() {
        return "echo '\n\nPress any button to exit...'" + SEPARATOR + "read";
    }

}
