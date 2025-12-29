package org.codefromheaven;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.codefromheaven.service.animal.AnimalService;
import org.codefromheaven.service.settings.SettingsService;
import org.codefromheaven.config.AppConfig;
import org.codefromheaven.context.SpringContext;
import org.codefromheaven.service.style.StyleService;
import org.codefromheaven.service.version.AppVersionService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;
import java.util.Objects;

public class App extends Application {

    public static final int MIN_WIDTH = 400;
    private AnnotationConfigApplicationContext context;

    @Override
    public void start(Stage stage) throws IOException {
        context = new AnnotationConfigApplicationContext(AppConfig.class);
        SpringContext.setContext(context);

        SettingsService settingsService = SpringContext.getBean(SettingsService.class);
        AnimalService animalService = SpringContext.getBean(AnimalService.class);
        AppVersionService appVersionService = SpringContext.getBean(AppVersionService.class);
        StyleService styleService = SpringContext.getBean(StyleService.class);

        String appName = settingsService.getAppName() + " - " + appVersionService.getCurrentVersion();
        stage.setTitle(appName);
        stage.setResizable(false);
        stage.setMinWidth(MIN_WIDTH);
        Image animalImage = animalService.getCurrentAnimalImage();
        stage.getIcons().add(animalImage);
        Scene scene = new Scene(loadFXML("mainWindow", context));
        scene.getStylesheets().add(styleService.getCurrentStyleUrl());
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        context.close();
    }

    private static Parent loadFXML(String fxml, AnnotationConfigApplicationContext context) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        fxmlLoader.setControllerFactory(context::getBean);
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }

}
