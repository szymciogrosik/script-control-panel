package org.codefromheaven.controller;

import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import org.codefromheaven.service.animal.AnimalService;

public class ConfirmationPopupController {

    private final String confirmationMessage;

    public ConfirmationPopupController(String confirmationMessage) {
        this.confirmationMessage = confirmationMessage;
    }

    public void setupPage() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.setWidth(300);

        // Add an image to the window bar
        ImageView imageView = new ImageView(AnimalService.getInstance().getRandomAnimalImage());
        imageView.setFitWidth(50);
        imageView.setFitHeight(50);
        alert.setGraphic(imageView);

        // Create a VBox to hold the text vertically centered
        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.setSpacing(10);

        // Add the confirmation message text
        Text text = new Text(confirmationMessage);
        vbox.getChildren().add(text);

        // Set the VBox as the content of the dialog pane
        alert.getDialogPane().setContent(vbox);

        ButtonType buttonTypeYes = new ButtonType("OK", ButtonBar.ButtonData.YES);
        alert.getButtonTypes().setAll(buttonTypeYes);

        alert.showAndWait();
    }

}
