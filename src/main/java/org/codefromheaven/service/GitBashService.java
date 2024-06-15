package org.codefromheaven.service;

import org.codefromheaven.dto.GitBash;
import org.codefromheaven.dto.ScriptType;

public class GitBashService {

    private GitBashService() { }

    public static void runCommand(ScriptType scriptType, String command) {
        GitBash myRunnable = new GitBash(scriptType, command);
        Thread t = new Thread(myRunnable);
        t.start();
    }

}
