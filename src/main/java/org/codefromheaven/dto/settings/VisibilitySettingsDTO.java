package org.codefromheaven.dto.settings;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record VisibilitySettingsDTO(List<VisibilitySettingDTO> settings) {
}
