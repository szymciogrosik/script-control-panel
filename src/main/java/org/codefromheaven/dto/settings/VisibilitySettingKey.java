package org.codefromheaven.dto.settings;

public record VisibilitySettingKey(String sectionName, String subSectionName, String buttonName) {

    public String getStringKey() {
        return sectionName + " - " + subSectionName + " - " + buttonName;
    }

}
