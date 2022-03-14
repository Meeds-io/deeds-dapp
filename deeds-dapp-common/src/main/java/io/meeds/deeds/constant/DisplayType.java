package io.meeds.deeds.constant;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum DisplayType {
  @JsonProperty("boost_number")
  BOOST_NUMBER,

  @JsonProperty("boost_percentage")
  BOOST_PERCENTAGE,

  @JsonProperty("number")
  NUMBER,

  @JsonProperty("date")
  DATE;
}
