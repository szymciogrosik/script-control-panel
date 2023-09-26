package org.openjfx.service;

import org.openjfx.dto.PowerShell;
import org.openjfx.dto.ScriptType;

public class PowerShellService {

    private PowerShellService() { }

    public static void runCommand(ScriptType scriptType, String command) {
        PowerShell myRunnable = new PowerShell(scriptType, command);
        Thread t = new Thread(myRunnable);
        t.start();
    }

}
