package org.openjfx.controller;

import java.io.IOException;

import javafx.fxml.FXML;
import org.openjfx.App;
import org.openjfx.dto.ScriptType;
import org.openjfx.service.GitBashService;

public class SecondaryController {

    @FXML
    private void switchToPrimary() throws IOException, InterruptedException {
        App.setRoot("primary");
        GitBashService.runCommand(ScriptType.SERVICE_SCRIPT, "echo '!'");
    }

}
