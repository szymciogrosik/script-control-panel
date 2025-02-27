package org.codefromheaven.service.command;

import org.codefromheaven.dto.Command;

public class GitBashService {

    private GitBashService() { }

    public static void runCommand(String scriptPathVarName, boolean autoCloseConsole, String command) {
        GitBashSetupService myRunnable = new GitBashSetupService(new Command(scriptPathVarName, autoCloseConsole, command));
        Thread t = new Thread(myRunnable);
        t.start();
    }

}
