package org.codefromheaven.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.codefromheaven.dto.data.SectionDTO;
import org.codefromheaven.service.settings.SettingsServiceBase;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class SaveJsonService {

    private SaveJsonService() { }

    public static void save(String fileToLoad, List<SectionDTO> sections) {
        String path = SettingsServiceBase.getMyOwnFileDir(fileToLoad);
        saveBase(path, sections);
    }

    public static void saveBase(String configPath, List<SectionDTO> sections) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        try {
            mapper.writeValue(new File(configPath), sections);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save layout to " + configPath, e);
        }
    }
}
