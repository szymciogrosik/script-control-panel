package org.codefromheaven;

import com.sun.jna.WString;
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
import com.sun.jna.platform.win32.Shell32;

public class App extends Application {

    public static final int MIN_WIDTH = 400;
    private AnnotationConfigApplicationContext context;

    @Override
    public void start(Stage stage) throws IOException {
        setupJNAToSupportCustomIconDisplayForPinnedApp();

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

    private static void setupJNAToSupportCustomIconDisplayForPinnedApp() {
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            try {
                WString dynamicAppId = new WString("CodeFromHeaven.ScriptControlPanel." + System.currentTimeMillis());
                Shell32.INSTANCE.SetCurrentProcessExplicitAppUserModelID(dynamicAppId);
            } catch (Throwable t) {
                System.err.println("Failed to set explicit AppUserModelID: " + t.getMessage());
            }
        }
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
