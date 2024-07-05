package org.codefromheaven.dto.data;

import java.util.List;

public record SectionDTO(String sectionName, List<SubSectionDTO> subSections) {
}
