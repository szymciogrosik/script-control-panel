package org.codefromheaven.service.command;

import org.codefromheaven.context.SpringContext;
import org.codefromheaven.dto.Command;
import org.codefromheaven.dto.Setting;
import org.codefromheaven.service.settings.SettingsService;

import java.io.IOException;

public class GitBashSetupService implements Runnable {

    private static final String SEPARATOR = " ; ";

    private final Command command;

    public GitBashSetupService(Command command) {
        this.command = command;
    }

    public void run() {
        runCommand(command.scriptPathVarName(), command.autoCloseConsole(), command.command());
    }

    private static void runCommand(String scriptPathVarName, boolean autoCloseConsole, String command) {
        try {
            String finalCommand = "";
            if (!scriptPathVarName.isEmpty()) {
                finalCommand += getOpenDirectoryCommand(scriptPathVarName) + SEPARATOR;
            }
            finalCommand += command;

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
        return "cd " + SpringContext.getBean(SettingsService.class).loadValue(scriptPathVarName).get();
    }

    private static String getWaitingForButtonCommand() {
        return "echo '\n\nPress any button to exit...'" + SEPARATOR + "read";
    }

}
