package org.codefromheaven.service.settings;

import org.codefromheaven.dto.FileType;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SettingsServiceBase {

    private SettingsServiceBase() { }

    public static boolean isPresentMyOwnSettingFile(FileType fileType) {
        try (BufferedReader ignored = new BufferedReader(new FileReader(fileType.getPersonalizedConfigName()))) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

}
