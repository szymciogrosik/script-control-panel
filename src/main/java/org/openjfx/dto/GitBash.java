package org.openjfx.dto;

import org.openjfx.service.SettingsService;

import java.io.*;

public class GitBash implements Runnable {

    private static final String BASH_PATH = SettingsService.getVariable(Setting.BASH_PATH);
    private static final String DIAS_DOCKER_PATH = SettingsService.getVariable(Setting.DIAS_DOCKER_PATH);

    private static final String SEPARATOR = " ; ";

    private final String command;

    public GitBash(String command) {
        this.command = command;
    }

    public void run() {
        runCommand(command);
    }

    private static void runCommand(String command) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command(BASH_PATH, "-c", getOpenDirectoryCommand() + SEPARATOR + command + SEPARATOR + getWaitingForButtonCommand());
            processBuilder.start();
        } catch (IOException e) {
            System.out.println(" --- Interruption in RunCommand: " + e);
            Thread.currentThread().interrupt();
        }
    }

    private static String getOpenDirectoryCommand() {
        return "cd " + DIAS_DOCKER_PATH;
    }

    private static String getWaitingForButtonCommand() {
        return "echo '\n\nPress any button to exit...'" + SEPARATOR + "read";
    }

}