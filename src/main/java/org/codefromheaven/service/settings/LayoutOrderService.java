package org.codefromheaven.service.settings;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.codefromheaven.dto.ConfigType;
import org.codefromheaven.dto.FileType;
import org.codefromheaven.dto.data.ButtonDTO;
import org.codefromheaven.dto.data.LayoutOrderDTO;
import org.codefromheaven.dto.data.SectionDTO;
import org.codefromheaven.dto.data.SectionOrderDTO;
import org.codefromheaven.dto.data.SubSectionDTO;
import org.codefromheaven.dto.data.SubSectionOrderDTO;
import org.codefromheaven.service.LoadFromJsonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class LayoutOrderService {

    private static final FileType FILE_TYPE = FileType.LAYOUT_ORDER;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final LoadFromJsonService loadFromJsonService;

    @Autowired
    public LayoutOrderService(LoadFromJsonService loadFromJsonService) {
        this.loadFromJsonService = loadFromJsonService;
    }

    public Optional<LayoutOrderDTO> getLayoutOrder() {
        return loadFromJsonService.loadLayoutOrder(SettingsServiceBase.getFileDir(FILE_TYPE.name(), ConfigType.DEFAULT));
    }

    public void saveToMyOwnFile(List<SectionDTO> sections) {
        LayoutOrderDTO orderDto = buildOrderFrom(sections);
        save(orderDto, ConfigType.MY_OWN);
    }

    private void save(LayoutOrderDTO orderDto, ConfigType configType) {
        try {
            MAPPER.writerWithDefaultPrettyPrinter().writeValue(
                    new File(SettingsServiceBase.getFileDir(FILE_TYPE.name(), configType)), orderDto);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Builds a LayoutOrderDTO from the current live sections list (as seen in the editor).
     */
    private LayoutOrderDTO buildOrderFrom(List<SectionDTO> sections) {
        List<SectionOrderDTO> sectionOrders = new ArrayList<>();
        for (SectionDTO section : sections) {
            List<SubSectionOrderDTO> subOrders = new ArrayList<>();
            for (SubSectionDTO sub : section.subSections()) {
                List<String> btnNames = new ArrayList<>();
                for (ButtonDTO btn : sub.buttons()) {
                    btnNames.add(btn.getButtonName());
                }
                subOrders.add(new SubSectionOrderDTO(sub.subSectionName(), btnNames));
            }
            sectionOrders.add(new SectionOrderDTO(section.sectionName(), subOrders));
        }
        return new LayoutOrderDTO(sectionOrders);
    }

    /**
     * Reorders the given sections list in-place according to the saved LayoutOrderDTO.
     * Items not present in the order are appended at the end (preserves new default entries).
     */
    public List<SectionDTO> applyOrder(List<SectionDTO> sections, LayoutOrderDTO order) {
        List<SectionDTO> result = new ArrayList<>();

        // Add sections in the saved order
        for (SectionOrderDTO sectionOrder : order.sectionsOrder()) {
            Optional<SectionDTO> secOpt = sections.stream()
                    .filter(s -> s.sectionName().equals(sectionOrder.sectionName()))
                    .findFirst();
            if (secOpt.isEmpty()) continue;
            SectionDTO sec = secOpt.get();

            List<SubSectionDTO> orderedSubs = applySubSectionOrder(sec.subSections(), sectionOrder.subSectionsOrder());
            result.add(new SectionDTO(sec.sectionName(), orderedSubs));
        }

        // Append sections not mentioned in the saved order
        for (SectionDTO sec : sections) {
            boolean alreadyAdded = result.stream().anyMatch(r -> r.sectionName().equals(sec.sectionName()));
            if (!alreadyAdded) {
                result.add(sec);
            }
        }

        return result;
    }

    private static List<SubSectionDTO> applySubSectionOrder(
            List<SubSectionDTO> subSections, List<SubSectionOrderDTO> subOrder) {

        List<SubSectionDTO> result = new ArrayList<>();

        // Add subsections in saved order
        for (SubSectionOrderDTO subOrderEntry : subOrder) {
            Optional<SubSectionDTO> subOpt = subSections.stream()
                    .filter(ss -> ss.subSectionName().equals(subOrderEntry.subSectionName()))
                    .findFirst();
            if (subOpt.isEmpty()) continue;
            SubSectionDTO sub = subOpt.get();

            List<ButtonDTO> orderedButtons = applyButtonOrder(sub.buttons(), subOrderEntry.buttonsOrder());
            result.add(new SubSectionDTO(sub.subSectionName(), orderedButtons));
        }

        // Append subsections not mentioned
        for (SubSectionDTO sub : subSections) {
            boolean alreadyAdded = result.stream()
                    .anyMatch(r -> r.subSectionName().equals(sub.subSectionName()));
            if (!alreadyAdded) {
                result.add(sub);
            }
        }

        return result;
    }

    private static List<ButtonDTO> applyButtonOrder(List<ButtonDTO> buttons, List<String> buttonsOrder) {
        List<ButtonDTO> result = new ArrayList<>();

        // Add buttons in saved order
        for (String btnName : buttonsOrder) {
            Optional<ButtonDTO> btnOpt = buttons.stream()
                    .filter(b -> b.getButtonName().equals(btnName))
                    .findFirst();
            btnOpt.ifPresent(result::add);
        }

        // Append buttons not mentioned
        for (ButtonDTO btn : buttons) {
            boolean alreadyAdded = result.stream().anyMatch(r -> r.getButtonName().equals(btn.getButtonName()));
            if (!alreadyAdded) {
                result.add(btn);
            }
        }

        return result;
    }
}
