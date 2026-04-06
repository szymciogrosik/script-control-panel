package org.codefromheaven.dto.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.codefromheaven.dto.ElementType;
import org.codefromheaven.dto.settings.VisibilitySettingKey;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode
public class ButtonDTO {

    @Getter
    private final String buttonName;
    @Getter
    private final String scriptLocationParamName;
    @Getter
    private final List<String> commands;
    @Getter
    private final ElementType elementType;
    @Getter
    private final boolean autoCloseConsole;
    @Getter
    @JsonInclude(Include.NON_NULL)
    private final SingleInputPopupDTO singleInputPopup;
    @Getter
    private final String description;
    @Getter
    private final boolean visibleAsDefault;
    private final VisibilitySettingKey visibilitySettingKey;

    @JsonCreator
    public ButtonDTO(
            String buttonName,
            String scriptLocationParamName,
            List<String> commands,
            ElementType elementType,
            boolean autoCloseConsole,
            SingleInputPopupDTO singleInputPopup,
            String description,
            boolean visibleAsDefault,
            String sectionName,
            String subSectionName) {
        this.buttonName = buttonName;
        this.scriptLocationParamName = scriptLocationParamName;
        this.commands = commands;
        this.elementType = elementType;
        this.autoCloseConsole = autoCloseConsole;
        this.singleInputPopup = singleInputPopup;
        this.description = description;
        this.visibleAsDefault = visibleAsDefault;
        this.visibilitySettingKey = new VisibilitySettingKey(sectionName, subSectionName, buttonName);
    }

    @JsonIgnore
    public VisibilitySettingKey getVisibilitySettingKey() {
        return visibilitySettingKey;
    }

}
