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
package io.meeds.deeds.blockchain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = BlockchainConfigurationProperties.class)
@EnableConfigurationProperties(value = BlockchainConfigurationProperties.class)
@TestPropertySource(
    properties = {
        "meeds.blockchain.networkUrl=" + BlockchainConfigurationPropertiesTest.NETWORK_URL_VALUE,
        "meeds.blockchain.tenantProvisioningAddress=" + BlockchainConfigurationPropertiesTest.TENANT_PROVISIONING_ADDRESS_VALUE,
        "meeds.blockchain.deedAddress=" + BlockchainConfigurationPropertiesTest.DEED_ADDRESS_VALUE,
    }
)
class BlockchainConfigurationPropertiesTest {

  public static final String                NETWORK_URL_VALUE                 = "NETWORK_URL";

  public static final String                TENANT_PROVISIONING_ADDRESS_VALUE = "TENANT_PROVISIONING_ADDRESS";

  public static final String                DEED_ADDRESS_VALUE                = "DEED_ADDRESS";

  @Autowired
  private BlockchainConfigurationProperties blockchainConfigurationProperties;

  @Test
  void testProperties() {
    assertNotNull(blockchainConfigurationProperties);
    assertEquals(NETWORK_URL_VALUE, blockchainConfigurationProperties.getNetworkUrl());
    assertEquals(TENANT_PROVISIONING_ADDRESS_VALUE, blockchainConfigurationProperties.getTenantProvisioningAddress());
    assertEquals(DEED_ADDRESS_VALUE, blockchainConfigurationProperties.getDeedAddress());

    BlockchainConfigurationProperties properties = new BlockchainConfigurationProperties(NETWORK_URL_VALUE,
                                                                                         TENANT_PROVISIONING_ADDRESS_VALUE,
                                                                                         DEED_ADDRESS_VALUE);
    assertEquals(properties, blockchainConfigurationProperties);
    assertEquals(properties.hashCode(), blockchainConfigurationProperties.hashCode());
    assertEquals(properties.toString(), blockchainConfigurationProperties.toString());
  }

}
