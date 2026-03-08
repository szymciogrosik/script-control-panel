package org.codefromheaven.dto.data;

import java.util.List;

public record LayoutAndButtonsDTO(List<DirectoryDTO> directories, List<SectionDTO> layout) {
}
