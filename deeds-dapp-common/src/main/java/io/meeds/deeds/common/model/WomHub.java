package io.meeds.deeds.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class WomHub {

  private long    deedId;

  private String  owner;

  private boolean enabled;

  private long    joinDate;

}
