package org.codefromheaven.service.command;

import org.codefromheaven.dto.CommandDTO;
import org.codefromheaven.dto.ScriptType;

public class GitBashService {

    private static final String SEPARATOR = " ; ";

    private GitBashService() { }

    public static void runCommand(ScriptType scriptType, String command) {
        GitBashSetupService myRunnable = new GitBashSetupService(new CommandDTO(scriptType, command));
        Thread t = new Thread(myRunnable);
        t.start();
    }

}
