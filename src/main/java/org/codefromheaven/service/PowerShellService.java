package org.codefromheaven.service;

import org.codefromheaven.dto.PowerShell;
import org.codefromheaven.dto.ScriptType;

public class PowerShellService {

    private PowerShellService() { }

    public static void runCommand(ScriptType scriptType, String command) {
        PowerShell myRunnable = new PowerShell(scriptType, command);
        Thread t = new Thread(myRunnable);
        t.start();
    }

}
