package org.codefromheaven.dto.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode
public class SingleInputPopupDTO {

    @Getter
    private final String message;
    
    @Getter
    private final String defaultValue;

    @JsonCreator
    public SingleInputPopupDTO(
            @JsonProperty("message") String message,
            @JsonProperty("defaultValue") String defaultValue) {
        this.message = message;
        this.defaultValue = defaultValue;
    }
}
