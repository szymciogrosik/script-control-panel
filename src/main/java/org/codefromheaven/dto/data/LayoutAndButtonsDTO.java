package org.codefromheaven.dto.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LayoutAndButtonsDTO(List<DirectoryDTO> directories, List<SectionDTO> layout) {
}
