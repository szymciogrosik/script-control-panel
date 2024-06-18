package org.codefromheaven.service.command;

import org.codefromheaven.dto.CommandDTO;
import org.codefromheaven.dto.ScriptType;

public class PowerShellService {

    private PowerShellService() { }

    public static void runCommand(ScriptType scriptType, String command) {
        PowerShellSetupService myRunnable = new PowerShellSetupService(new CommandDTO(scriptType, command));
        Thread t = new Thread(myRunnable);
        t.start();
    }

}
