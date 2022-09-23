package org.openjfx.service;

import org.openjfx.dto.GitBash;

public class GitBashService {

    private GitBashService() { }

    public static void runCommand(String command) {
        GitBash myRunnable = new GitBash(command);
        Thread t = new Thread(myRunnable);
        t.start();
    }

}
