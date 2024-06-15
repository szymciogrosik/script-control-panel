package org.codefromheaven.controller;

import java.io.IOException;

import javafx.fxml.FXML;
import org.codefromheaven.App;
import org.codefromheaven.dto.ScriptType;
import org.codefromheaven.service.GitBashService;

public class SecondaryController {

    @FXML
    private void switchToPrimary() throws IOException, InterruptedException {
        App.setRoot("primary");
        GitBashService.runCommand(ScriptType.SERVICE_SCRIPT, "echo '!'");
    }

}
