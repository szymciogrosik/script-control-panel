package org.codefromheaven.dto.data;

import org.codefromheaven.dto.ElementType;

import java.util.List;

public record ButtonDTO(
        String buttonName,
        String scriptLocationParamName,
        List<String> commands,
        ElementType elementType,
        boolean autoCloseConsole,
        boolean popupInputDisplayed,
        String popupInputMessage,
        String description
) {
}
