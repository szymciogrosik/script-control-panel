package org.openjfx.dto;

import org.openjfx.service.SettingsService;

import java.io.*;

public class GitBash implements Runnable {

    private static final String SEPARATOR = " ; ";

    private final ScriptType scriptType;
    private final String command;

    public GitBash(ScriptType scriptType, String command) {
        this.scriptType = scriptType;
        this.command = command;
    }

    public void run() {
        runCommand(scriptType, command);
    }

    private static void runCommand(ScriptType scriptType, String command) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command(Setting.BASH_PATH.getValue(), "-c", getOpenDirectoryCommand(scriptType) + SEPARATOR + command + SEPARATOR + getWaitingForButtonCommand());
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