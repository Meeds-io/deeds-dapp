/*
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package io.meeds.deeds.contract;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigInteger;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.web3j.EVMTest;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.utils.EnsUtils;

import io.meeds.deeds.contract.WoM.HubConnectedEventResponse;

@EVMTest
public class WoMTest extends BaseWoMContractTest {

  @BeforeEach
  @Override
  public void init(Web3j web3j,
                   TransactionManager transactionManager,
                   ContractGasProvider contractGasProvider) throws Exception {
    super.init(web3j, transactionManager, contractGasProvider);
  }

  @Test
  public void updateDeed() throws Exception {

    assertThrows(Exception.class,
                 () -> bridgeDeed(BigInteger.ZERO, OWNER1, TENANT1),
                 "Deed Id is mandatory");
    assertThrows(Exception.class,
                 () -> bridgeDeed(DEED1, EnsUtils.EMPTY_ADDRESS, TENANT1),
                 "Deed Owner is mandatory");
    assertThrows(Exception.class,
                 () -> bridgeDeed(DEED1, OWNER1, EnsUtils.EMPTY_ADDRESS),
                 "Deed Tenant is mandatory");

    TransactionReceipt receipt = bridgeDeed(DEED1, OWNER1, TENANT1);

    checkReceipt(receipt);
    checkDeedUpdatedEvent(receipt, DEED1);
    checkDeedProperties(DEED1, EnsUtils.EMPTY_ADDRESS, OWNER1, TENANT1);
    assertFalse(wom.isDeedConnected(DEED1).send());
  }

  @Test
  public void autoConnect() throws Exception {
    sendGasFees();

    long txTimeStart = now();
    TransactionReceipt receipt = bridgeDeedAndAutoConnect(DEED1, HUB1, OWNER1, TENANT1);
    long txTimeEnd = now();

    checkReceipt(receipt);
    checkDeedUpdatedEvent(receipt, DEED1);
    checkHubConnectedEvent(receipt, DEED1, HUB1, TENANT1);
    checkDeedProperties(DEED1, HUB1, OWNER1, TENANT1);
    checkHubConnected(DEED1, HUB1);
    checkHubJoinDate(HUB1, txTimeStart, txTimeEnd);
    checkHubProperties(HUB1, TENANT1, DEED1);

    // Update Deed Tenant only
    receipt = bridgeDeedAndAutoConnect(DEED1, HUB1, OWNER1, TENANT2);

    checkReceipt(receipt);
    checkDeedUpdatedEvent(receipt, DEED1);

    List<HubConnectedEventResponse> hubConnectedEvents = WoM.getHubConnectedEvents(receipt);
    assertNotNull(hubConnectedEvents);
    assertEquals(0,
                 hubConnectedEvents.size(),
                 "Shouldn't connect automatically the Hub to the new Tenant since the old Tenant holds Hub ownership");

    assertFalse(wom.isHubConnected(HUB1).send(),
                "Should automatically disconnect the Hub when the new Deed Tenant isn't owner neither Tenant of the Deed");
    assertEquals(EnsUtils.EMPTY_ADDRESS.toLowerCase().replace("0x", ""),
                 wom.getConnectedHub(DEED1).send().toLowerCase().replace("0x", ""),
                 "Must return 0x address. Should automatically disconnect the Hub when the new Deed Tenant isn't owner neither Tenant of the Deed");

    assertFalse(wom.isDeedConnected(DEED1).send(),
                "Should automatically disconnect the Deed when the new Deed Tenant isn't owner neither Tenant of the Deed");
    assertEquals(BigInteger.ZERO,
                 wom.getConnectedDeed(HUB1).send(),
                 "Must return 0 Deed Id. Should automatically disconnect the Deed when the new Deed Tenant isn't owner neither Tenant of the Deed");

    checkDeedProperties(DEED1, HUB1, OWNER1, TENANT2);
    checkHubProperties(HUB1, TENANT1, DEED1);
    checkHubJoinDate(HUB1, txTimeStart, txTimeEnd);

    receipt = transferHubOwnership(TENANT1_CREDENTIALS,
                                   HUB1,
                                   TENANT2);

    checkReceipt(receipt);
    checkHubOwnershipTransferEvent(receipt, TENANT1, TENANT2);
    checkHubProperties(HUB1, TENANT2, DEED1);

    // The Hub should be connected again automatically once the Hub ownership is
    // transferred
    checkHubConnected(DEED1, HUB1);

    // Should be able to automatically connect
    // when updating the Deed with to another Hub
    // and disconnect the first Hub
    long txTime2Start = now();
    receipt = bridgeDeedAndAutoConnect(DEED1, HUB2, OWNER1, TENANT2);
    long txTime2End = now();

    checkReceipt(receipt);
    checkDeedUpdatedEvent(receipt, DEED1);
    checkHubDisconnectedEvent(receipt, DEED1, HUB1);
    checkHubConnectedEvent(receipt, DEED1, HUB2, TENANT2);
    checkDeedProperties(DEED1, HUB2, OWNER1, TENANT2);
    checkHubConnected(DEED1, HUB2);
    checkHubJoinDate(HUB2, txTime2Start, txTime2End);
    checkHubProperties(HUB2, TENANT2, DEED1);

    assertFalse(wom.isHubConnected(HUB1).send(), "Hub 1 should be disconnected automatically");

    // When transferring the ownership of the Deed
    // Nothing should be affected about WoM connection
    receipt = bridgeDeed(DEED1, OWNER2, TENANT2);
    checkReceipt(receipt);
    checkDeedUpdatedEvent(receipt, DEED1);
    checkNoHubDisconnectedEvent(receipt);
    checkNoHubConnectedEvent(receipt);
    checkDeedProperties(DEED1, HUB2, OWNER2, TENANT2);
    checkHubConnected(DEED1, HUB2);
    checkHubJoinDate(HUB2, txTime2Start, txTime2End);
    checkHubProperties(HUB2, TENANT2, DEED1);
  }

  @Test
  public void transferHubOwnership() throws Exception {
    sendGasFees();

    bridgeDeed(DEED1, OWNER1, TENANT1);
    checkDeedProperties(DEED1, OWNER1, TENANT1);
    connectHub(TENANT1_CREDENTIALS, HUB1, DEED1);
    checkHubConnected(DEED1, HUB1);
    checkHubProperties(HUB1, TENANT1, DEED1);

    disconnectHub(TENANT1_CREDENTIALS, HUB1);
    checkHubDisconnected(DEED1, HUB1);

    // The Deed Tenant changed to TENANT2
    bridgeDeed(DEED1, OWNER1, TENANT2);
    // Deed Tenant changed but the Hub Owner Must remain TENANT1
    checkHubProperties(HUB1, TENANT1, DEED1, false);

    // Test transfer ownership which make connection established again
    assertThrows(Exception.class,
                 () -> transferHubOwnership(TENANT2_CREDENTIALS,
                                            HUB1,
                                            TENANT2),
                 "Only Hub Owner must be allowed. The new Deed Tenant doesn't hold Hub ownership");
    assertThrows(Exception.class,
                 () -> transferHubOwnership(OWNER1_CREDENTIALS,
                                            HUB1,
                                            TENANT2),
                 "Only Hub Owner must be allowed. The Deed Owner doesn't hold Hub ownership");

    WoM womTenant1 = WoM.load(wom.getContractAddress(), web3j, TENANT1_CREDENTIALS, contractGasProvider);
    assertThrows(Exception.class,
                 () -> womTenant1.transferHubOwnership(EnsUtils.EMPTY_ADDRESS, TENANT2).send(),
                 "Hub Address is mandatory");
    assertThrows(Exception.class,
                 () -> womTenant1.transferHubOwnership(HUB1, EnsUtils.EMPTY_ADDRESS).send(),
                 "New Owner Address is mandatory");
    assertThrows(Exception.class,
                 () -> womTenant1.transferHubOwnership(HUB1, TENANT1).send(),
                 "Can't transfer to the same owner");

    TransactionReceipt receipt = transferHubOwnership(TENANT1_CREDENTIALS,
                                                      HUB1,
                                                      TENANT2);
    checkReceipt(receipt);
    checkHubOwnershipTransferEvent(receipt, TENANT1, TENANT2);
    checkHubProperties(HUB1, TENANT2, DEED1, false);

    assertThrows(Exception.class,
                 () -> womTenant1.transferHubOwnership(HUB1, TENANT1).send(),
                 "TENANT1 shouldn't be allowed to redo the transfer again");

    // TENANT2 should be able to transfer ownership of the Hub to any
    // address but 0x address
    receipt = transferHubOwnership(
                                   TENANT2_CREDENTIALS,
                                   HUB1,
                                   OWNER1);
    checkReceipt(receipt);
    checkHubOwnershipTransferEvent(receipt, TENANT2, OWNER1);
    checkHubProperties(HUB1, OWNER1, DEED1, false);
  }

  @Test
  public void connect() throws Exception {
    sendGasFees();

    // Connect DEED1 & HUB1
    bridgeDeed(DEED1, OWNER1, TENANT1);
    checkDeedProperties(DEED1, OWNER1, TENANT1);
    assertThrows(Exception.class,
                 () -> connectHub(OWNER1_CREDENTIALS,
                                  HUB1,
                                  DEED1),
                 "Deed Owner 1 shouldn't be able to manage Hub connection while lease is in progress for another Deed Tenant");

    // Connect DEED2 & HUB2
    TransactionReceipt receipt =
                               connectHub(TENANT1_CREDENTIALS, HUB1, DEED1);
    checkHubConnectedEvent(receipt, DEED1, HUB1, TENANT1);
    checkHubProperties(HUB1, TENANT1, DEED1);

    bridgeDeed(DEED2, OWNER2, TENANT2);
    checkDeedProperties(DEED2, OWNER2, TENANT2);
    assertThrows(Exception.class,
                 () -> connectHub(OWNER2_CREDENTIALS,
                                  HUB2,
                                  DEED2),
                 "Deed Owner 2 shouldn't be able to manage Hub connection while lease is in progress for another Deed Tenant");

    // When Connecting DEED2 and HUB2 with TENANT2 as Deed Tenant
    receipt = connectHub(TENANT2_CREDENTIALS, HUB2, DEED2);
    checkHubConnectedEvent(receipt, DEED2, HUB2, TENANT2);

    // Then TENANT2 has become owner of the HUB2 and is connected via DEED2
    checkHubProperties(HUB2, TENANT2, DEED2);
    // And the Hub is connected
    checkHubConnected(DEED2, HUB2);

    // When Change Owner 1 of DEED2
    bridgeDeed(DEED2, OWNER1, TENANT2);

    // Then Nothing should change on Hub properties
    // after Deed Ownership changes
    checkHubProperties(HUB2, TENANT2, DEED2);
    // And the Hub still connected
    checkHubConnected(DEED2, HUB2);

    // When Change Tenant 1 of DEED2
    bridgeDeed(DEED2, OWNER1, TENANT1);

    // Then Nothing should change about Hub ownership
    checkHubProperties(HUB2, TENANT2, DEED2);
    // And Hub and Deed should be considered as disconnected
    checkHubDisconnected(DEED2, HUB2);

    // When Hub ownership is transferred as well to TENANT1
    // by old Hub owner (TENANT2)
    transferHubOwnership(TENANT2_CREDENTIALS,
                         HUB2,
                         TENANT1);

    // Then HUB2 & DEED2 are reconnected automatically
    checkHubConnected(DEED2, HUB2);
    checkHubProperties(HUB2, TENANT1, DEED2);
  }

  @Test
  public void disconnect() throws Exception {
    sendGasFees();

    bridgeDeed(DEED1, OWNER1, TENANT1);
    checkDeedProperties(DEED1, OWNER1, TENANT1);

    // Connect DEED1 & HUB1
    connectHub(TENANT1_CREDENTIALS, HUB1, DEED1);
    checkHubProperties(HUB1, TENANT1, DEED1);

    assertThrows(Exception.class,
                 () -> disconnectHub(OWNER1_CREDENTIALS,
                                     HUB1),
                 "Deed Owner 1 shouldn't be able to disconnect Hub while lease is in progress for another Deed Tenant");
    checkHubConnected(DEED1, HUB1);

    TransactionReceipt receipt = disconnectHub(TENANT1_CREDENTIALS, HUB1);
    checkHubDisconnectedEvent(receipt, DEED1, HUB1);

    checkHubDisconnected(DEED1, HUB1);

    // Connect DEED2 & HUB2
    bridgeDeed(DEED2, OWNER2, TENANT2);
    checkDeedProperties(DEED2, OWNER2, TENANT2);

    // When Connecting DEED2 and HUB2 with TENANT2 as Deed Tenant
    connectHub(TENANT2_CREDENTIALS, HUB2, DEED2);

    assertThrows(Exception.class,
                 () -> disconnectHub(OWNER2_CREDENTIALS,
                                     HUB2),
                 "Deed Owner 2 shouldn't be able to disconnect Hub while lease is in progress for another Deed Tenant");

    // Then TENANT2 has become owner of the HUB2 and is connected via DEED2
    checkHubProperties(HUB2, TENANT2, DEED2);
    // And the Hub is connected
    checkHubConnected(DEED2, HUB2);

    // When changing Deed DEED2 Tenant to TENANT1
    bridgeDeed(DEED2, OWNER2, TENANT1);
    assertThrows(Exception.class,
                 () -> disconnectHub(TENANT1_CREDENTIALS,
                                     HUB2),
                 "New Deed Tenant TENANT1 shouldn't be able to disconnect Hub while it's owned by TENANT2");

    disconnectHub(TENANT2_CREDENTIALS,
                  HUB2);

    // When Hub ownership is transferred as well to TENANT1
    // by old Hub owner (TENANT2)
    transferHubOwnership(TENANT2_CREDENTIALS,
                         HUB2,
                         TENANT1);

    // Then TENANT1 can connect to its managed Deed
    connectHub(TENANT1_CREDENTIALS, HUB2, DEED2);

    // Then HUB2 & DEED2 are reconnected automatically
    checkHubConnected(DEED2, HUB2);
    checkHubProperties(HUB2, TENANT1, DEED2);
  }

}
