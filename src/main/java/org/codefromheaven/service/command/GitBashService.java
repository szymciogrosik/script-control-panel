package org.codefromheaven.service.command;

import org.codefromheaven.dto.CommandDTO;

public class GitBashService {

    private static final String SEPARATOR = " ; ";

    private GitBashService() { }

    public static void runCommand(String scriptPathVarName, boolean autoCloseConsole, String command) {
        GitBashSetupService myRunnable = new GitBashSetupService(new CommandDTO(scriptPathVarName, autoCloseConsole, command));
        Thread t = new Thread(myRunnable);
        t.start();
    }

}
