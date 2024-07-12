package org.codefromheaven.controller;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.codefromheaven.service.animal.AnimalService;

public class PopupController {

    private PopupController() {
    }

    public static void showPopup(
            String message,
            Alert.AlertType alertType
    ) {
        Alert alert = new Alert(alertType);
        alert.setTitle("Information");
        alert.setHeaderText(null);

        // Create an HBox to hold the image and text side by side
        HBox hbox = new HBox();
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.setSpacing(10);

        // Add the confirmation message text
        Text text = new Text(message);

        // Add margin to the text
        HBox.setMargin(text, new Insets(10, 20, 0, 0));

        // Add text and image to the HBox
        hbox.getChildren().addAll(text);

        // Create a VBox to center the HBox vertically
        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.setSpacing(10);
        vbox.getChildren().add(hbox);

        // Set the VBox as the content of the dialog pane
        alert.getDialogPane().setContent(vbox);

        ButtonType buttonTypeYes = new ButtonType("OK", ButtonBar.ButtonData.YES);
        alert.getButtonTypes().setAll(buttonTypeYes);

        // Set the window icon
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(AnimalService.getInstance().getRandomAnimalImage());

        alert.showAndWait();
    }

}
