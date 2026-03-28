package org.codefromheaven.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.codefromheaven.dto.ElementType;
import org.codefromheaven.dto.data.ButtonDTO;
import org.codefromheaven.dto.data.SectionDTO;
import org.codefromheaven.dto.data.SubSectionDTO;

import org.codefromheaven.dto.data.DirectoryDTO;
import org.codefromheaven.dto.data.LayoutAndButtonsDTO;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class LoadFromJsonService {

    public Optional<LayoutAndButtonsDTO> loadLayout(String configPath) {
        ObjectMapper mapper = new ObjectMapper();
        List<DirectoryDTO> directories = new ArrayList<>();
        List<SectionDTO> allSections = new ArrayList<>();

        try {
            JsonNode root = mapper.readTree(new File(configPath));

            // Parse directories
            JsonNode directoriesNode = root.get("directories");
            if (directoriesNode != null && directoriesNode.isArray()) {
                for (JsonNode dirNode : directoriesNode) {
                    directories.add(new DirectoryDTO(
                            dirNode.get("name") != null ? dirNode.get("name").asText() : "",
                            dirNode.get("path") != null ? dirNode.get("path").asText() : "",
                            dirNode.get("description") != null ? dirNode.get("description").asText() : ""));
                }
            }

            // Parse layout components
            JsonNode layoutNode = root.get("layout");
            if (layoutNode != null && layoutNode.isArray()) {
                for (JsonNode sectionNode : layoutNode) {
                    String sectionName = sectionNode.get("sectionName").asText();
                    SectionDTO section = getOrCreateSection(allSections, sectionName);

                    for (JsonNode subSectionNode : sectionNode.get("subSections")) {
                        String subSectionName = subSectionNode.get("subSectionName").asText();
                        SubSectionDTO subSection = getOrCreateSubSection(section.subSections(), subSectionName);

                        JsonNode commandsNode = subSectionNode.get("commands");
                        if (commandsNode == null || !commandsNode.isArray()) {
                            continue;
                        }

                        for (JsonNode commandNode : commandsNode) {
                            ElementType elementType = ElementType.getEnumType(commandNode.get("elementType").asText());
                            JsonNode commandsArray = commandNode.get("commands");

                            String visibleAsDefaultPropertyName = "visibleAsDefault";
                            boolean visibleAsDefault = commandNode.get(visibleAsDefaultPropertyName) == null ||
                                    commandNode.get(visibleAsDefaultPropertyName).asBoolean();
                            subSection.buttons().add(new ButtonDTO(
                                    commandNode.get("buttonName").asText(),
                                    commandNode.get("scriptLocationParamName") != null
                                            ? commandNode.get("scriptLocationParamName").asText()
                                            : "",
                                    fetchCommandsWithPrefix(commandsArray),
                                    elementType,
                                    commandNode.get("autoCloseConsole") != null
                                            ? commandNode.get("autoCloseConsole").asBoolean()
                                            : false,
                                    commandNode.get("popupInputDisplayed") != null
                                            ? commandNode.get("popupInputDisplayed").asBoolean()
                                            : false,
                                    commandNode.get("popupInputMessage") != null
                                            ? commandNode.get("popupInputMessage").asText()
                                            : "",
                                    commandNode.get("description") != null ? commandNode.get("description").asText()
                                            : "",
                                    visibleAsDefault,
                                    sectionName,
                                    subSectionName));
                        }
                    }
                }
            }
        } catch (IOException e) {
            return Optional.empty();
        }

        if (allSections.isEmpty() && directories.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new LayoutAndButtonsDTO(directories, allSections));
    }

    private static List<String> fetchCommandsWithPrefix(JsonNode commandsArray) {
        List<String> commands = new ArrayList<>();
        if (commandsArray.isArray()) {
            for (JsonNode commandElement : commandsArray) {
                commands.add(commandElement.asText());
            }
        }
        return commands;
    }

    private static SectionDTO getOrCreateSection(List<SectionDTO> allSections, String sectionName) {
        Optional<SectionDTO> sectionOptional = allSections.stream().filter(s -> s.sectionName().equals(sectionName))
                .findFirst();
        SectionDTO section;
        if (sectionOptional.isPresent()) {
            section = sectionOptional.get();
        } else {
            section = new SectionDTO(sectionName, new ArrayList<>());
            allSections.add(section);
        }
        return section;
    }

    private static SubSectionDTO getOrCreateSubSection(List<SubSectionDTO> allSubSections, String subSectionName) {
        Optional<SubSectionDTO> subSectionOptional = allSubSections.stream()
                .filter(s -> s.subSectionName().equals(subSectionName)).findFirst();
        SubSectionDTO section;
        if (subSectionOptional.isPresent()) {
            section = subSectionOptional.get();
        } else {
            section = new SubSectionDTO(subSectionName, new ArrayList<>());
            allSubSections.add(section);
        }
        return section;
    }

}
