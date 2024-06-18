package org.codefromheaven.service.command;

import org.codefromheaven.dto.CommandDTO;
import org.codefromheaven.dto.ScriptType;
import org.codefromheaven.dto.Setting;

import java.io.IOException;

public class GitBashSetupService implements Runnable {

    private static final String SEPARATOR = " ; ";

    private final CommandDTO commandDTO;

    public GitBashSetupService(CommandDTO commandDTO) {
        this.commandDTO = commandDTO;
    }

    public void run() {
        runCommand(commandDTO.getScriptType(), commandDTO.getCommand());
    }

    private static void runCommand(ScriptType scriptType, String command) {
        try {
            String finalCommand = getOpenDirectoryCommand(scriptType) + SEPARATOR + command;
            if (scriptType.isNotAutoCloseConsole()) {
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

    private static String getOpenDirectoryCommand(ScriptType scriptType) {
        return "cd " + scriptType.getPath();
    }

    private static String getWaitingForButtonCommand() {
        return "echo '\n\nPress any button to exit...'" + SEPARATOR + "read";
    }

}
