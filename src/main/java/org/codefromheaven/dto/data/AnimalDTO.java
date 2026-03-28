package org.codefromheaven.dto.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.codefromheaven.resources.ImageType;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AnimalDTO(String name, ImageType imageType) {
}
