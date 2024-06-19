module hellofx {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires com.fasterxml.jackson.databind;

    opens org.codefromheaven to javafx.fxml;
    exports org.codefromheaven;
    exports org.codefromheaven.service;
    opens org.codefromheaven.service to javafx.fxml;
    exports org.codefromheaven.controller;
    opens org.codefromheaven.controller to javafx.fxml;
    exports org.codefromheaven.dto;
    opens org.codefromheaven.dto to javafx.fxml;
    exports org.codefromheaven.helpers;
    opens org.codefromheaven.helpers to javafx.fxml;
    exports org.codefromheaven.resources;
    opens org.codefromheaven.resources to javafx.fxml;
    exports org.codefromheaven.service.command;
    opens org.codefromheaven.service.command to javafx.fxml;
    exports org.codefromheaven.service.animal;
    opens org.codefromheaven.service.animal to javafx.fxml;
    exports org.codefromheaven.service.settings;
    opens org.codefromheaven.service.settings to javafx.fxml;
    exports org.codefromheaven.dto.settings;
    opens org.codefromheaven.dto.settings to com.fasterxml.jackson.databind;
}
