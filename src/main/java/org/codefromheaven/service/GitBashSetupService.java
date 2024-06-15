package org.codefromheaven.service;

import org.codefromheaven.dto.GitBashDTO;
import org.codefromheaven.dto.ScriptType;
import org.codefromheaven.dto.Setting;

import java.io.IOException;

public class GitBashSetupService implements Runnable {

    private static final String SEPARATOR = " ; ";

    private final GitBashDTO gitBashDTO;

    public GitBashSetupService(GitBashDTO gitBashDTO) {
        this.gitBashDTO = gitBashDTO;
    }

    public void run() {
        runCommand(gitBashDTO.getScriptType(), gitBashDTO.getCommand());
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
