package org.codefromheaven.controller;

import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import org.codefromheaven.service.animal.AnimalService;

public class ConfirmationPopupController {

    private final String confirmationMessage;

    public ConfirmationPopupController(String confirmationMessage) {
        this.confirmationMessage = confirmationMessage;
    }

    public void setupPage() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setWidth(300);

        // Create an HBox to hold the image and text side by side
        HBox hbox = new HBox();
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.setSpacing(10);

        // Add the confirmation message text
        Text text = new Text(confirmationMessage);

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
