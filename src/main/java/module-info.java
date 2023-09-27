module hellofx {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    opens org.openjfx to javafx.fxml;
    exports org.openjfx;
    exports org.openjfx.service;
    opens org.openjfx.service to javafx.fxml;
    exports org.openjfx.controller;
    opens org.openjfx.controller to javafx.fxml;
    exports org.openjfx.dto;
    opens org.openjfx.dto to javafx.fxml;
    exports org.openjfx.helpers;
    opens org.openjfx.helpers to javafx.fxml;
    exports org.openjfx.resources;
    opens org.openjfx.resources to javafx.fxml;
}