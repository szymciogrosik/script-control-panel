package org.codefromheaven.dto.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SectionWrapper(List<SectionDTO> sectionList) {
}
