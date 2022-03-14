package io.meeds.deeds.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.meeds.deeds.constant.DisplayType;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(value = Include.NON_EMPTY)
public class DeedMetadataAttribute {

  @JsonProperty("trait_type")
  private String      traitType;

  @JsonProperty("display_type")
  private DisplayType displayType;

  private Object      value;

  @JsonProperty("max_value")
  private Double      maxValue;

}
