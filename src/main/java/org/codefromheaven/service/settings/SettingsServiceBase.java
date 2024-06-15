package org.codefromheaven.service.settings;

import org.codefromheaven.dto.ElementType;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SettingsServiceBase {

    private SettingsServiceBase() { }

    public static boolean isPresentMyOwnSettingFile(ElementType elementType) {
        try (BufferedReader ignored = new BufferedReader(new FileReader(elementType.getPersonalizedConfigName()))) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

}
