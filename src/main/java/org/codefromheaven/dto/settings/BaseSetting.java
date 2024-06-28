package org.codefromheaven.dto.settings;

import org.codefromheaven.dto.FileType;

public interface BaseSetting {

    String getName();

    String getValue();

    FileType getElementType();

}
