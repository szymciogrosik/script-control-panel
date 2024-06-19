package org.codefromheaven.dto.settings;

import org.codefromheaven.dto.FileType;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public interface BaseSetting {

    String getName();

    String getValue();

    FileType getElementType();

    BaseSetting[] getAll();

    default BaseSetting getElementByName(String elementName) {
        List<BaseSetting> settingList = Arrays.stream(getAll()).filter(setting -> setting.getName().equals(elementName)).collect(Collectors.toList());
        if (settingList.isEmpty()) {
            throw new NoSuchElementException(elementName);
        } else if (settingList.size() > 1) {
            throw new RuntimeException("More than one setting found for '" + elementName + "'");
        } else {
            return settingList.get(0);
        }
    }

}
