package org.codefromheaven.service.settings;

import org.codefromheaven.dto.FileType;
import org.codefromheaven.dto.data.LayoutAndButtonsDTO;
import org.codefromheaven.dto.data.DirectoryDTO;
import org.codefromheaven.service.LoadFromJsonService;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class LayoutAndButtonsService {

    private static final FileType FILE_TYPE = FileType.LAYOUT_AND_BUTTONS;
    private static LayoutAndButtonsDTO cachedLayoutAndButtons;

    private LayoutAndButtonsService() {
    }

    public static LayoutAndButtonsDTO load() {
        return LoadFromJsonService.loadLayoutAndButtons(FILE_TYPE.name());
    }

    public static void save(LayoutAndButtonsDTO dto) {
        LayoutAndButtonsDTO defaultLayout = LoadFromJsonService
                .loadBase(SettingsServiceBase.getDefaultFileDir(FILE_TYPE.name()))
                .orElse(new LayoutAndButtonsDTO(new ArrayList<>(), new ArrayList<>()));
        LayoutAndButtonsDTO diffDto = extractDifferences(defaultLayout, dto);

        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(
                    new File(SettingsServiceBase.getMyOwnFileDir(FILE_TYPE.name())),
                    diffDto);
            // Force re-load on next fetch
            cachedLayoutAndButtons = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static LayoutAndButtonsDTO extractDifferences(LayoutAndButtonsDTO defaultDto,
            LayoutAndButtonsDTO customDto) {
        List<DirectoryDTO> customDirs = new ArrayList<>();
        if (customDto.directories() != null) {
            for (DirectoryDTO checkDir : customDto.directories()) {
                boolean isDefault = defaultDto.directories().stream()
                        .anyMatch(d -> d.name().equals(checkDir.name()) &&
                                d.path().equals(checkDir.path()) &&
                                d.description().equals(checkDir.description()));
                if (!isDefault)
                    customDirs.add(checkDir);
            }
        }

        List<org.codefromheaven.dto.data.SectionDTO> customSections = new ArrayList<>();
        if (customDto.layout() != null) {
            for (org.codefromheaven.dto.data.SectionDTO checkSec : customDto.layout()) {
                Optional<org.codefromheaven.dto.data.SectionDTO> defaultSecOpt = defaultDto.layout().stream()
                        .filter(s -> s.sectionName().equals(checkSec.sectionName())).findFirst();

                if (defaultSecOpt.isEmpty()) {
                    customSections.add(checkSec);
                } else {
                    org.codefromheaven.dto.data.SectionDTO defSec = defaultSecOpt.get();
                    List<org.codefromheaven.dto.data.SubSectionDTO> customSubSections = new ArrayList<>();

                    for (org.codefromheaven.dto.data.SubSectionDTO checkSubSec : checkSec.subSections()) {
                        Optional<org.codefromheaven.dto.data.SubSectionDTO> defaultSubSecOpt = defSec.subSections()
                                .stream()
                                .filter(ss -> ss.subSectionName().equals(checkSubSec.subSectionName())).findFirst();

                        if (defaultSubSecOpt.isEmpty()) {
                            customSubSections.add(checkSubSec);
                        } else {
                            org.codefromheaven.dto.data.SubSectionDTO defSubSec = defaultSubSecOpt.get();
                            List<org.codefromheaven.dto.data.ButtonDTO> customButtons = new ArrayList<>();

                            for (org.codefromheaven.dto.data.ButtonDTO checkBtn : checkSubSec.buttons()) {
                                boolean isDefault = defSubSec.buttons().stream()
                                        .anyMatch(b -> b.getName().equals(checkBtn.getName()) &&
                                                b.getElementType() == checkBtn.getElementType() &&
                                                Objects.equals(b.getScriptLocationParamName(),
                                                        checkBtn.getScriptLocationParamName())
                                                &&
                                                Objects.equals(b.getCommands(), checkBtn.getCommands()) &&
                                                Objects.equals(b.getDescription(), checkBtn.getDescription())
                                                &&
                                                b.isPopupInputDisplayed() == checkBtn.isPopupInputDisplayed() &&
                                                b.isVisibleAsDefault() == checkBtn.isVisibleAsDefault());
                                if (!isDefault)
                                    customButtons.add(checkBtn);
                            }

                            if (!customButtons.isEmpty()) {
                                customSubSections.add(new org.codefromheaven.dto.data.SubSectionDTO(
                                        checkSubSec.subSectionName(), customButtons));
                            }
                        }
                    }

                    if (!customSubSections.isEmpty()) {
                        customSections.add(
                                new org.codefromheaven.dto.data.SectionDTO(checkSec.sectionName(), customSubSections));
                    }
                }
            }
        }

        return new LayoutAndButtonsDTO(customDirs, customSections);
    }

    public static String getDirectoryPath(String directoryName) {
        if (cachedLayoutAndButtons == null) {
            cachedLayoutAndButtons = load();
        }

        Optional<DirectoryDTO> dir = cachedLayoutAndButtons.directories().stream()
                .filter(d -> d.name().equals(directoryName))
                .findFirst();

        return dir.map(DirectoryDTO::path).orElse("");
    }
}
