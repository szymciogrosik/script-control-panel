package org.openjfx.service;

import org.openjfx.dto.GitBash;
import org.openjfx.dto.ScriptType;

public class GitBashService {

    private GitBashService() { }

    public static void runCommand(ScriptType scriptType, String command) {
        GitBash myRunnable = new GitBash(scriptType, command);
        Thread t = new Thread(myRunnable);
        t.start();
    }

}
