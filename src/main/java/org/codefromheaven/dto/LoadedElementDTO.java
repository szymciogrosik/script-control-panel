package org.codefromheaven.dto;

public class LoadedElementDTO {

    private final String sectionName;
    private final String subSectionName;
    private final String buttonName;
    private final String scriptLocationParamName;
    private final String command;
    private final ElementType elementType;
    private final boolean autoCloseConsole;
    private final boolean popupInputDisplayed;
    private final String popupInputMessage;
    private final String description;

    public LoadedElementDTO(
            String sectionName, String subSectionName, String buttonName,
            String scriptLocationParamName, String command, ElementType elementType, boolean autoCloseConsole,
            boolean popupInputDisplayed, String popupInputMessage, String description) {
        this.sectionName = sectionName;
        this.subSectionName = subSectionName;
        this.scriptLocationParamName = scriptLocationParamName;
        this.buttonName = buttonName;
        this.command = command;
        this.elementType = elementType;
        this.autoCloseConsole = autoCloseConsole;
        this.popupInputDisplayed = popupInputDisplayed;
        this.popupInputMessage = popupInputMessage;
        this.description = description;
    }

    public String getSectionName() {
        return sectionName;
    }

    public String getSubSectionName() {
        return subSectionName;
    }

    public String getButtonName() {
        return buttonName;
    }

    public String getScriptLocationParamName() {
        return scriptLocationParamName;
    }

    public String getCommand() {
        return command;
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

}
