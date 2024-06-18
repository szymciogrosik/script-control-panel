package org.codefromheaven.service.command;

import org.codefromheaven.dto.CommandDTO;

public class PowerShellService {

    private PowerShellService() { }

    public static void runCommand(String scriptPathVarName, boolean autoCloseConsole, String command) {
        PowerShellSetupService myRunnable = new PowerShellSetupService(new CommandDTO(scriptPathVarName, autoCloseConsole, command));
        Thread t = new Thread(myRunnable);
        t.start();
    }

}
