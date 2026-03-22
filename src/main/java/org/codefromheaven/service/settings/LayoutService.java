package org.codefromheaven.service.settings;

import org.codefromheaven.dto.ConfigType;
import org.codefromheaven.dto.FileType;
import org.codefromheaven.dto.data.ButtonDTO;
import org.codefromheaven.dto.data.LayoutAndButtonsDTO;
import org.codefromheaven.dto.data.DirectoryDTO;
import org.codefromheaven.dto.data.LayoutOrderDTO;
import org.codefromheaven.dto.data.SectionDTO;
import org.codefromheaven.dto.data.SubSectionDTO;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.codefromheaven.service.LoadFromJsonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class LayoutService {

    private static final FileType FILE_TYPE = FileType.LAYOUT_AND_BUTTONS;
    private LayoutAndButtonsDTO cachedLayoutAndButtons;

    private final LayoutOrderService layoutOrderService;
    private LoadFromJsonService loadFromJsonService;

    @Autowired
    public LayoutService(LayoutOrderService layoutOrderService, LoadFromJsonService loadFromJsonService) {
        this.layoutOrderService = layoutOrderService;
        this.loadFromJsonService = loadFromJsonService;
    }

    public LayoutAndButtonsDTO load() {
        return loadLayoutAndButtons(FILE_TYPE.name());
    }

    private LayoutAndButtonsDTO loadLayoutAndButtons(String configName) {
        Optional<LayoutAndButtonsDTO> defaultLayout = loadFromJsonService.loadLayout(SettingsServiceBase.getFileDir(configName, ConfigType.DEFAULT));
        Optional<LayoutAndButtonsDTO> customLayout = loadFromJsonService.loadLayout(SettingsServiceBase.getFileDir(configName, ConfigType.MY_OWN));

        // Merge variables and layouts
        LayoutAndButtonsDTO merged;
        if (defaultLayout.isEmpty()) {
            merged = customLayout.orElseGet(() -> new LayoutAndButtonsDTO(new ArrayList<>(), new ArrayList<>()));
        } else if (customLayout.isEmpty()) {
            merged = defaultLayout.get();
        } else {
            merged = mergeDirectoriesAndLayouts(defaultLayout.get(), customLayout.get());
        }

        // Apply saved display order (sections, subsections, buttons) if it exists
        Optional<LayoutOrderDTO> order = layoutOrderService.getLayoutOrder();
        if (order.isPresent()) {
            List<SectionDTO> orderedSections = layoutOrderService.applyOrder(merged.layout(), order.get());
            merged = new LayoutAndButtonsDTO(merged.directories(), orderedSections);
        }

        return merged;
    }

    private LayoutAndButtonsDTO mergeDirectoriesAndLayouts(LayoutAndButtonsDTO defaultDto, LayoutAndButtonsDTO customDto) {
        List<DirectoryDTO> mergedDirs = mergeDirectories(defaultDto, customDto);
        List<SectionDTO> mergedSections = mergeSections(defaultDto, customDto);
        return new LayoutAndButtonsDTO(mergedDirs, mergedSections);
    }

    private static List<SectionDTO> mergeSections(LayoutAndButtonsDTO defaultDto, LayoutAndButtonsDTO customDto) {
        // Sections: iterate DEFAULT order as the baseline.
        // Custom-only buttons/subsections/sections are appended at the end of their parent.
        List<SectionDTO> mergedSections = new ArrayList<>();

        for (SectionDTO defaultSection : defaultDto.layout()) {
            Optional<SectionDTO> customSectionOpt =
                    customDto.layout().stream()
                             .filter(s -> s.sectionName().equals(defaultSection.sectionName()))
                             .findFirst();

            List<SubSectionDTO> mergedSubSection = new ArrayList<>();

            // Iterate default subsections first
            for (SubSectionDTO defaultSubSection : defaultSection.subSections()) {
                Optional<SubSectionDTO> customSubSectionOpt = customSectionOpt
                        .flatMap(cs -> cs.subSections().stream()
                                         .filter(ss -> ss.subSectionName().equals(defaultSubSection.subSectionName()))
                                         .findFirst());

                // Default buttons first
                List<ButtonDTO> mergedButtons = new ArrayList<>();
                List<ButtonDTO> defaultButtons = defaultSubSection.buttons();
                List<ButtonDTO> customButtons = customSubSectionOpt.isPresent() ? customSubSectionOpt.get().buttons() : List.of();

                // Append default or override by custom
                for (ButtonDTO defaultButton : defaultButtons) {
                    Optional<ButtonDTO> customButton = customButtons.stream()
                                                                    .filter(custom -> custom.getButtonName().equals(defaultButton.getButtonName()))
                                                                    .findFirst();
                    mergedButtons.add(customButton.orElse(defaultButton));
                }

                // Add missing custom buttons
                for (ButtonDTO customButton : customButtons) {
                    boolean notFoundDefault = defaultButtons.stream()
                                                            .noneMatch(b -> b.getButtonName().equals(customButton.getButtonName())
                                                                    && b.getElementType() == customButton.getElementType());
                    if (notFoundDefault) {
                        mergedButtons.add(customButton);
                    }
                }

                mergedSubSection.add(new SubSectionDTO(defaultSubSection.subSectionName(), mergedButtons));
            }

            // Append custom-only subsections (not in default) at the end
            if (customSectionOpt.isPresent()) {
                for (SubSectionDTO customSubSection : customSectionOpt.get().subSections()) {
                    boolean alreadyAdded = mergedSubSection.stream()
                                                           .anyMatch(ss -> ss.subSectionName().equals(customSubSection.subSectionName()));
                    if (!alreadyAdded) {
                        mergedSubSection.add(customSubSection);
                    }
                }
            }

            mergedSections.add(new SectionDTO(defaultSection.sectionName(), mergedSubSection));
        }

        // Append custom-only sections (not in default) at the end
        for (SectionDTO customSec : customDto.layout()) {
            boolean alreadyAdded = mergedSections.stream()
                                                 .anyMatch(s -> s.sectionName().equals(customSec.sectionName()));
            if (!alreadyAdded) {
                mergedSections.add(customSec);
            }
        }
        return mergedSections;
    }

    private static List<DirectoryDTO> mergeDirectories(LayoutAndButtonsDTO defaultDto, LayoutAndButtonsDTO customDto) {
        List<DirectoryDTO> mergedDirs = new ArrayList<>(customDto.directories());
        for (DirectoryDTO defDir : defaultDto.directories()) {
            if (mergedDirs.stream().noneMatch(d -> d.name().equals(defDir.name()))) {
                mergedDirs.add(defDir);
            }
        }
        // Order by name ASC
        mergedDirs = mergedDirs.stream().sorted(Comparator.comparing(DirectoryDTO::name)).toList();
        return mergedDirs;
    }

    public void save(LayoutAndButtonsDTO dto) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(
                    new File(SettingsServiceBase.getFileDir(FILE_TYPE.name(), ConfigType.MY_OWN)), dto);
            // Force re-load on next fetch
            cachedLayoutAndButtons = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getDirectoryPath(String directoryName) {
        if (cachedLayoutAndButtons == null) {
            cachedLayoutAndButtons = load();
        }

        Optional<DirectoryDTO> dir = cachedLayoutAndButtons.directories().stream()
                .filter(d -> d.name().equals(directoryName))
                .findFirst();

        return dir.map(DirectoryDTO::path).orElse("");
    }

}
