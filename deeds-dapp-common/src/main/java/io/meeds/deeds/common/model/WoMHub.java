package io.meeds.deeds.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class WoMHub {

  private long    deedId;

  private String  owner;

  private boolean enabled;

}
