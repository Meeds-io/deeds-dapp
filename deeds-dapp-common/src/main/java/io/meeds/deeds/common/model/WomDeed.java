package io.meeds.deeds.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WomDeed {

  private short  city;

  private short  cardType;

  private double mintingPower;

  private long   maxUsers;

  private String ownerAddress;

  private String managerAddress;

  private String hubAddress;

  private short  ownerPercentage;

}
