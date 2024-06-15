package org.codefromheaven.dto;

public class LoadedElement {

    private final int sectionDisplayOrder;
    private final String sectionName;
    private final int commandOrder;
    private final String buttonName;
    private final String command;
    private final boolean popupInputDisplayed;
    private final String popupInputMessage;
    private final String description;

    public LoadedElement(
            int sectionDisplayOrder, String sectionName, int commandOrder, String buttonName, String command,
            boolean popupInputDisplayed, String popupInputMessage, String description) {
        this.sectionDisplayOrder = sectionDisplayOrder;
        this.sectionName = sectionName;
        this.commandOrder = commandOrder;
        this.buttonName = buttonName;
        this.command = command;
        this.popupInputDisplayed = popupInputDisplayed;
        this.popupInputMessage = popupInputMessage;
        this.description = description;
    }

    public int getSectionDisplayOrder() {
        return sectionDisplayOrder;
    }

    public String getSectionName() {
        return sectionName;
    }

    public int getCommandOrder() {
        return commandOrder;
    }

    public String getButtonName() {
        return buttonName;
    }

    public String getCommand() {
        return command;
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
