package org.codefromheaven.service.style;

import org.codefromheaven.dto.Style;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static org.codefromheaven.dto.Setting.APP_STYLE;

@Service
public class StyleService {

    private static final String STYLE_PATH = "/styles/";

    public String getCurrentStylePath() {
        String styleName = APP_STYLE.getValue();
        return STYLE_PATH + Style.getStyle(styleName).getFileName();
    }

    public String getCurrentStyleUrl() {
        return Objects.requireNonNull(getClass().getResource(getCurrentStylePath())).toExternalForm();
    }

}
