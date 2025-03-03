package org.codefromheaven;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.codefromheaven.service.animal.AnimalService;
import org.codefromheaven.service.settings.SettingsService;
import org.codefromheaven.service.version.AppVersionService;

import java.io.IOException;
import java.util.Objects;

public class App extends Application {

    public static final int MIN_WIDTH = 400;

    @Override
    public void start(Stage stage) throws IOException {
        String appName = SettingsService.getAppName() + " - " + AppVersionService.getCurrentVersion();
        stage.setTitle(appName);
        stage.setResizable(false);
        stage.setMinWidth(MIN_WIDTH);
        Image animalImage = AnimalService.getInstance().getCurrentAnimalImage();
        stage.getIcons().add(animalImage);
        Scene scene = new Scene(loadFXML("mainWindow"));
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(SettingsService.getStyle().getFileName())).toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }

}
