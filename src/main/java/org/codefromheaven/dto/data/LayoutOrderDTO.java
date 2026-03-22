package org.codefromheaven.dto.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LayoutOrderDTO(List<SectionOrderDTO> sectionsOrder) {
}
