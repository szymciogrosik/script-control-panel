package org.codefromheaven.dto.settings;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record VisibilitySettingDTO(String key, String value) {
}
