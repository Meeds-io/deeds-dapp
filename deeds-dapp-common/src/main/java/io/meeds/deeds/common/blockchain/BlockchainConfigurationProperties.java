/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 - 2022 Meeds Association contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.deeds.common.blockchain;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Configuration
@ConfigurationProperties(prefix = "meeds.blockchain")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlockchainConfigurationProperties {

  private String networkUrl;

  private String polygonNetworkUrl;

  private String tenantProvisioningAddress = "0x49C0cF46C0Eb6FdF05A4E8C1FE344d510422E1F0";

  private String tenantRentingAddress      = "0x427aa8F31013960E0E5e73977c1918e15d693BAa";

  private String deedAddress               = "0x0143b71443650aa8efa76bd82f35c22ebd558090";

  private String meedAddress               = "0x8503a7b00b4b52692cc6c14e5b96f142e30547b7";

  private String polygonMeedAddress        = "0x6aca77cf3bab0c4e8210a09b57b07854a995289a";

  private String xMeedAddress              = "0x44d6d6ab50401dd846336e9c706a492f06e1bcd4";

  private String tokenFactoryAddress       = "0x1B37D04759aD542640Cc44Ff849a373040386050";

  private String sushiPairAddress          = "0x960bd61d0b960b107ff5309a2dcced4705567070";

}
