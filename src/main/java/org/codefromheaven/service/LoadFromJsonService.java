package org.codefromheaven.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.codefromheaven.dto.ElementType;
import org.codefromheaven.dto.data.ButtonDTO;
import org.codefromheaven.dto.data.SectionDTO;
import org.codefromheaven.dto.data.SubSectionDTO;
import org.codefromheaven.service.settings.SettingsService;
import org.codefromheaven.service.settings.SettingsServiceBase;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LoadFromJsonService {

    private LoadFromJsonService() { }

    public static List<SectionDTO> load(String fileToLoad) {
        Optional<List<SectionDTO>> commands = loadBase(SettingsServiceBase.getMyOwnFileDir(fileToLoad));
        return commands.orElseGet(() -> loadBase(SettingsServiceBase.getDefaultFileDir(fileToLoad)).get());
    }

    public static Optional<List<SectionDTO>> loadBase(String configPath) {
        ObjectMapper mapper = new ObjectMapper();
        List<SectionDTO> allSections = new ArrayList<>();

        try {
            JsonNode root = mapper.readTree(new File(configPath));
            for (JsonNode sectionNode : root) {
                String sectionName = sectionNode.get("sectionName").asText();
                SectionDTO section = getOrCreateSection(allSections, sectionName);

                for (JsonNode subSectionNode : sectionNode.get("subSections")) {
                    String subSectionName = subSectionNode.get("subSectionName").asText();
                    SubSectionDTO subSection = getOrCreateSubSection(section.subSections(), subSectionName);

                    for (JsonNode commandNode : subSectionNode.get("commands")) {
                        ElementType elementType = ElementType.getEnumType(commandNode.get("elementType").asText());
                        JsonNode commandsArray = commandNode.get("commands");

                        String visibleAsDefaultPropertyName = "visibleAsDefault";
                        boolean visibleAsDefault =
                                commandNode.get(visibleAsDefaultPropertyName) == null ||
                                        commandNode.get(visibleAsDefaultPropertyName).asBoolean();
                        subSection.buttons().add(new ButtonDTO(
                                commandNode.get("buttonName").asText(),
                                commandNode.get("scriptLocationParamName").asText(),
                                fetchCommandsWithPrefix(commandsArray, elementType),
                                elementType,
                                commandNode.get("autoCloseConsole").asBoolean(),
                                commandNode.get("popupInputDisplayed").asBoolean(),
                                commandNode.get("popupInputMessage").asText(),
                                commandNode.get("description").asText(),
                                visibleAsDefault,
                                sectionName,
                                subSectionName
                        ));
                    }
                }
            }
        } catch (IOException e) {
            return Optional.empty();
        }
        return allSections.isEmpty() ? Optional.empty() : Optional.of(allSections);
    }

    private static List<String> fetchCommandsWithPrefix(JsonNode commandsArray, ElementType elementType) {
        List<String> commands = new ArrayList<>();
        if (commandsArray.isArray()) {
            for (JsonNode commandElement : commandsArray) {
                String command = addPrefixToCommand(commandElement.asText(), elementType);
                commands.add(command);
            }
        }
        return commands;
    }

    private static String addPrefixToCommand(String command, ElementType elementType) {
        return switch (elementType) {
            case BASH -> "./" + command;
            case POWERSHELL -> ".\\" + command;
            case PYTHON -> SettingsService.getPythonScriptsPrefix() + " " + command;
            default -> command;
        };
    }

    private static SectionDTO getOrCreateSection(List<SectionDTO> allSections, String sectionName) {
        Optional<SectionDTO> sectionOptional = allSections.stream().filter(s -> s.sectionName().equals(sectionName)).findFirst();
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
        Optional<SubSectionDTO> subSectionOptional = allSubSections.stream().filter(s -> s.subSectionName().equals(subSectionName)).findFirst();
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
