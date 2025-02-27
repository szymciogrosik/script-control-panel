package org.codefromheaven.dto.data;

import org.codefromheaven.dto.ElementType;
import org.codefromheaven.dto.settings.VisibilitySettingKey;

import java.util.List;

public class ButtonDTO {

    private final String name;
    private final String scriptLocationParamName;
    private final List<String> commands;
    private final ElementType elementType;
    private final boolean autoCloseConsole;
    private final boolean popupInputDisplayed;
    private final String popupInputMessage;
    private final String description;
    private final boolean visibleAsDefault;
    private final VisibilitySettingKey visibilitySettingKey;

    public ButtonDTO(
            String name,
            String scriptLocationParamName,
            List<String> commands,
            ElementType elementType,
            boolean autoCloseConsole,
            boolean popupInputDisplayed,
            String popupInputMessage,
            String description,
            boolean visibleAsDefault,
            String sectionName,
            String subSectionName) {
        this.name = name;
        this.scriptLocationParamName = scriptLocationParamName;
        this.commands = commands;
        this.elementType = elementType;
        this.autoCloseConsole = autoCloseConsole;
        this.popupInputDisplayed = popupInputDisplayed;
        this.popupInputMessage = popupInputMessage;
        this.description = description;
        this.visibleAsDefault = visibleAsDefault;
        this.visibilitySettingKey = new VisibilitySettingKey(sectionName, subSectionName, name);
    }

    public String getName() {
        return name;
    }

    public String getScriptLocationParamName() {
        return scriptLocationParamName;
    }

    public List<String> getCommands() {
        return commands;
    }

    public ElementType getElementType() {
        return elementType;
    }

    public boolean isAutoCloseConsole() {
        return autoCloseConsole;
    }

    public boolean isPopupInputDisplayed() {
        return popupInputDisplayed;
    }

    public String getPopupInputMessage() {
        return popupInputMessage;
    }

    public String getDescription() {
        return description;
    }

    public boolean isVisibleAsDefault() {
        return visibleAsDefault;
    }

    public VisibilitySettingKey getKey() {
        return visibilitySettingKey;
    }

}
