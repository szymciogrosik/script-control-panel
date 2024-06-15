package org.codefromheaven.service;

import org.codefromheaven.dto.GitBashDTO;
import org.codefromheaven.dto.ScriptType;

public class GitBashService {

    private static final String SEPARATOR = " ; ";

    private GitBashService() { }

    public static void runCommand(ScriptType scriptType, String command) {
        GitBashSetupService myRunnable = new GitBashSetupService(new GitBashDTO(scriptType, command));
        Thread t = new Thread(myRunnable);
        t.start();
    }

}
