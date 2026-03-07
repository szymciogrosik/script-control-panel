package org.codefromheaven.controller;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import org.codefromheaven.context.SpringContext;
import org.codefromheaven.service.animal.AnimalService;
import org.codefromheaven.service.style.StyleService;

public class PopupController {

    private PopupController() {
    }

    public static void showPopup(
            String message,
            Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle("Information");

        setupDialogAppearance(alert, message);

        ButtonType buttonTypeYes = new ButtonType("OK", ButtonBar.ButtonData.YES);
        alert.getButtonTypes().setAll(buttonTypeYes);

        alert.getDialogPane().lookupButton(buttonTypeYes).getStyleClass().add("button-default");

        alert.showAndWait();
    }

    public static void showConfirmationPopup(
            String message,
            String confirmButtonText,
            Runnable onConfirm) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Information required");

        setupDialogAppearance(alert, message);

        ButtonType buttonTypeConfirm = new ButtonType(confirmButtonText, ButtonBar.ButtonData.YES);
        ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(buttonTypeConfirm, buttonTypeCancel);

        alert.getDialogPane().lookupButton(buttonTypeConfirm).getStyleClass().add("button-default");
        alert.getDialogPane().lookupButton(buttonTypeCancel).getStyleClass().add("button-default");

        alert.showAndWait().ifPresent(response -> {
            if (response == buttonTypeConfirm) {
                onConfirm.run();
            }
        });
    }

    private static void setupDialogAppearance(Alert alert, String message) {
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.getDialogPane().setMinWidth(460); // Ensure identical consistent width
        alert.getDialogPane().setMinHeight(160);

        // Setup the window icon and custom styles
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(SpringContext.getBean(AnimalService.class).getRandomAnimalImage());
        alert.getDialogPane().getScene().getStylesheets()
                .add(SpringContext.getBean(StyleService.class).getCurrentStyleUrl());
        alert.getDialogPane().getStyleClass().add("background-primary");

        // Apply our custom text color to the native label right as the window is shown
        alert.setOnShown(e -> {
            javafx.scene.Node content = alert.getDialogPane().lookup(".content.label");
            if (content != null) {
                content.setStyle(
                        "-fx-text-fill: white; -fx-font-family: \"Segoe UI\", Helvetica, Arial, sans-serif; -fx-font-size: 14px;");
            }
        });
    }

}
