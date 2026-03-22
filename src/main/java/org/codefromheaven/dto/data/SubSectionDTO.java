package org.codefromheaven.dto.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SubSectionDTO(
        @JsonProperty("subSectionName") String subSectionName,
        @JsonProperty("commands") List<ButtonDTO> buttons) {

    @JsonProperty("commands")
    @Override
    public List<ButtonDTO> buttons() {
        return buttons;
    }
}
