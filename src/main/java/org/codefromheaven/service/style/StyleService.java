package org.codefromheaven.service.style;

import org.codefromheaven.dto.Setting;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class StyleService {

    private static final String STANDARD_STYLE = "/org/codefromheaven/styles.css";
    private static final String ANGULAR_MATERIAL_STYLE = "/org/codefromheaven/angular-material.css";
    private static final String WINTER_STYLE = "/org/codefromheaven/winter.css";

    public String getCurrentStyle() {
        String styleName = Setting.APP_STYLE.getValue();
        if ("Angular Material".equals(styleName)) {
            return ANGULAR_MATERIAL_STYLE;
        } else if ("Winter".equals(styleName)) {
            return WINTER_STYLE;
        }
        return STANDARD_STYLE;
    }

    public String getCurrentStyleUrl() {
        return Objects.requireNonNull(getClass().getResource(getCurrentStyle())).toExternalForm();
    }
}
