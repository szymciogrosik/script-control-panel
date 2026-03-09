package org.codefromheaven.dto.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record SubSectionDTO(
        @JsonProperty("subSectionName") String subSectionName,
        @JsonProperty("commands") List<ButtonDTO> buttons) {

    @JsonProperty("commands")
    @Override
    public List<ButtonDTO> buttons() {
        return buttons;
    }
}
