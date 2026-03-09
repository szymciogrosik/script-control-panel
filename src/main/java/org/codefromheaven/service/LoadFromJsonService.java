package org.codefromheaven.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.codefromheaven.dto.ElementType;
import org.codefromheaven.dto.data.ButtonDTO;
import org.codefromheaven.dto.data.SectionDTO;
import org.codefromheaven.dto.data.SubSectionDTO;
import org.codefromheaven.service.settings.SettingsServiceBase;

import org.codefromheaven.dto.data.DirectoryDTO;
import org.codefromheaven.dto.data.LayoutAndButtonsDTO;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LoadFromJsonService {

    private LoadFromJsonService() {
    }

    public static LayoutAndButtonsDTO loadLayoutAndButtons(String configName) {
        Optional<LayoutAndButtonsDTO> defaultLayout = loadBase(SettingsServiceBase.getDefaultFileDir(configName));
        Optional<LayoutAndButtonsDTO> customLayout = loadBase(SettingsServiceBase.getMyOwnFileDir(configName));

        if (defaultLayout.isEmpty()) {
            return customLayout.orElseGet(() -> new LayoutAndButtonsDTO(new ArrayList<>(), new ArrayList<>()));
        }
        if (customLayout.isEmpty()) {
            return defaultLayout.get();
        }
        return mergeLayouts(defaultLayout.get(), customLayout.get());
    }

    private static LayoutAndButtonsDTO mergeLayouts(LayoutAndButtonsDTO defaultDto, LayoutAndButtonsDTO customDto) {
        List<DirectoryDTO> mergedDirs = new ArrayList<>(defaultDto.directories());
        for (DirectoryDTO customDir : customDto.directories()) {
            mergedDirs.removeIf(d -> d.name().equals(customDir.name()));
            mergedDirs.add(customDir);
        }

        List<SectionDTO> mergedSections = new ArrayList<>();
        for (SectionDTO defaultSec : defaultDto.layout()) {
            Optional<SectionDTO> customSecOpt = customDto.layout().stream()
                    .filter(s -> s.sectionName().equals(defaultSec.sectionName())).findFirst();

            if (customSecOpt.isEmpty()) {
                mergedSections.add(defaultSec);
            } else {
                List<SubSectionDTO> mergedSubSecs = new ArrayList<>();
                for (SubSectionDTO defaultSubSec : defaultSec.subSections()) {
                    Optional<SubSectionDTO> customSubSecOpt = customSecOpt.get().subSections().stream()
                            .filter(ss -> ss.subSectionName().equals(defaultSubSec.subSectionName())).findFirst();

                    if (customSubSecOpt.isEmpty()) {
                        mergedSubSecs.add(defaultSubSec);
                    } else {
                        List<ButtonDTO> mergedButtons = new ArrayList<>();
                        for (ButtonDTO defaultBtn : defaultSubSec.buttons()) {
                            Optional<ButtonDTO> customBtnOpt = customSubSecOpt.get().buttons().stream()
                                    .filter(b -> b.getName().equals(defaultBtn.getName())
                                            && b.getElementType() == defaultBtn.getElementType())
                                    .findFirst();
                            mergedButtons.add(customBtnOpt.orElse(defaultBtn));
                        }

                        // Add new buttons that exist only in custom layout
                        for (ButtonDTO customBtn : customSubSecOpt.get().buttons()) {
                            if (mergedButtons.stream().noneMatch(b -> b.getName().equals(customBtn.getName())
                                    && b.getElementType() == customBtn.getElementType())) {
                                mergedButtons.add(customBtn);
                            }
                        }

                        mergedSubSecs.add(new SubSectionDTO(defaultSubSec.subSectionName(), mergedButtons));
                    }
                }

                // Add new subsections that exist only in custom layout
                for (SubSectionDTO customSubSec : customSecOpt.get().subSections()) {
                    if (mergedSubSecs.stream()
                            .noneMatch(ss -> ss.subSectionName().equals(customSubSec.subSectionName()))) {
                        mergedSubSecs.add(customSubSec);
                    }
                }

                mergedSections.add(new SectionDTO(defaultSec.sectionName(), mergedSubSecs));
            }
        }

        // Add new sections that exist only in custom layout
        for (SectionDTO customSec : customDto.layout()) {
            if (mergedSections.stream().noneMatch(s -> s.sectionName().equals(customSec.sectionName()))) {
                mergedSections.add(customSec);
            }
        }

        return new LayoutAndButtonsDTO(mergedDirs, mergedSections);
    }

    public static Optional<LayoutAndButtonsDTO> loadBase(String configPath) {
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
                                    fetchCommandsWithPrefix(commandsArray, elementType),
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

    private static List<String> fetchCommandsWithPrefix(JsonNode commandsArray, ElementType elementType) {
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
