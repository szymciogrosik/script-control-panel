package org.codefromheaven.dto;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum ElementType {

    /*
     * Those paramNames are also parameters names in config files, so please be careful when you're editing them
     * */
    BASH("BASH"),
    POWERSHELL("POWERSHELL"),
    PYTHON("PYTHON"),
    LINK("LINK");

    private final String paramName;

    ElementType(String paramName) {
        this.paramName = paramName;
    }

    public String getParamName() {
        return paramName;
    }

    public static ElementType getEnumType(String paramName) {
        List<ElementType> foundEnumTypes = Arrays.stream(ElementType.values()).filter(elem -> elem.paramName.equals(paramName)).collect(Collectors.toList());
        if (foundEnumTypes.size() > 1) {
            throw new RuntimeException("Found multiple enum types for " + paramName);
        } else if (foundEnumTypes.isEmpty()) {
            throw new RuntimeException("No enum type found for " + paramName);
        }
        return foundEnumTypes.get(0);
    }

}
