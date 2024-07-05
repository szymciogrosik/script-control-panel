package org.codefromheaven.dto.data;

import org.codefromheaven.dto.ElementType;

public record ButtonDTO(
        String buttonName,
        String scriptLocationParamName,
        String command,
        ElementType elementType,
        boolean autoCloseConsole,
        boolean popupInputDisplayed,
        String popupInputMessage,
        String description
) {
}
