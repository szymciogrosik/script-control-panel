package org.codefromheaven.dto;

public class LoadedElementDTO {

    private final int sectionDisplayOrder;
    private final int commandOrder;
    private final String sectionName;
    private final String subSectionName;
    private final String buttonName;
    private final String command;
    private final ElementType elementType;
    private final boolean popupInputDisplayed;
    private final String popupInputMessage;
    private final String description;

    public LoadedElementDTO(
            int sectionDisplayOrder, int commandOrder, String sectionName, String subSectionName, String buttonName, String command, ElementType elementType,
            boolean popupInputDisplayed, String popupInputMessage, String description) {
        this.sectionDisplayOrder = sectionDisplayOrder;
        this.commandOrder = commandOrder;
        this.sectionName = sectionName;
        this.subSectionName = subSectionName;
        this.buttonName = buttonName;
        this.command = command;
        this.elementType = elementType;
        this.popupInputDisplayed = popupInputDisplayed;
        this.popupInputMessage = popupInputMessage;
        this.description = description;
    }

    public int getSectionDisplayOrder() {
        return sectionDisplayOrder;
    }

    public int getCommandOrder() {
        return commandOrder;
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

    public String getCommand() {
        return command;
    }

    public ElementType getElementType() {
        return elementType;
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
