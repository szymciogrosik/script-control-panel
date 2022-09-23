package org.openjfx.controller;

import java.io.IOException;

import javafx.fxml.FXML;
import org.openjfx.App;
import org.openjfx.service.GitBashService;

public class SecondaryController {

    @FXML
    private void switchToPrimary() throws IOException, InterruptedException {
        App.setRoot("primary");
        String git_command2 = "./press_button.sh";
        GitBashService.runCommand("echo '!' && " + git_command2);
    }

}