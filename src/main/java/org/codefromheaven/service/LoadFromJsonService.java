package org.codefromheaven.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.codefromheaven.dto.ElementType;
import org.codefromheaven.dto.FileType;
import org.codefromheaven.dto.LoadedElementDTO;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LoadFromJsonService {

    private LoadFromJsonService() { }

    public static List<LoadedElementDTO> load(FileType type) throws IOException {
        Optional<List<LoadedElementDTO>> commands = loadBase(type.getPersonalizedConfigName());
        return commands.orElseGet(() -> loadBase(type.getDefaultFileName()).get());
    }

    public static Optional<List<LoadedElementDTO>> loadBase(String configPath) {
        List<LoadedElementDTO> commands = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();

        try {
            JsonNode root = mapper.readTree(new File(configPath));
            for (JsonNode sectionNode : root) {
                String sectionName = sectionNode.get("sectionName").asText();

                for (JsonNode subSectionNode : sectionNode.get("subSections")) {
                    String subSectionName = subSectionNode.get("subSectionName").asText();

                    for (JsonNode commandNode : subSectionNode.get("commands")) {
                        commands.add(new LoadedElementDTO(
                                sectionName,
                                subSectionName,
                                commandNode.get("buttonName").asText(),
                                commandNode.get("scriptLocationParamName").asText(),
                                commandNode.get("command").asText(),
                                ElementType.getEnumType(commandNode.get("elementType").asText()),
                                commandNode.get("autoCloseConsole").asBoolean(),
                                commandNode.get("popupInputDisplayed").asBoolean(),
                                commandNode.get("popupInputMessage").asText(),
                                commandNode.get("description").asText()
                        ));
                    }
                }
            }
        } catch (IOException e) {
            return Optional.empty();
        }
        return commands.isEmpty() ? Optional.empty() : Optional.of(commands);
    }

}
