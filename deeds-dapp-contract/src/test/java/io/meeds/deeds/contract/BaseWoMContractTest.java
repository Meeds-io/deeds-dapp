/**
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.deeds.contract;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigInteger;
import java.time.ZonedDateTime;
import java.util.List;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.utils.EnsUtils;

import io.meeds.deeds.contract.TestWoM.Deed;
import io.meeds.deeds.contract.TestWoM.Hub;
import io.meeds.deeds.contract.WoM.DeedUpdatedEventResponse;
import io.meeds.deeds.contract.WoM.HubConnectedEventResponse;
import io.meeds.deeds.contract.WoM.HubDisconnectedEventResponse;
import io.meeds.deeds.contract.WoM.HubOwnershipTransferredEventResponse;

public abstract class BaseWoMContractTest {

  protected static final String      UNKOWN_DEED_ID                = "Unkown DeedId : ";

  protected static final BigInteger  CITY                          = BigInteger.valueOf(3l);

  protected static final BigInteger  WRONG_TENANT_PERCENTAGE       = BigInteger.valueOf(8l);

  protected static final String      JOIN_DATE_SHOULD_NEVER_CHANGE = "Join Date should Never Change!";

  protected static final Credentials OWNER1_CREDENTIALS            =
                                                        Credentials.create("0x1da4ef21b864d2cc526dbdb2a120bd2874c36c9d0a1fb7f8c63d7f7a8b41de8f");

  protected static final String      OWNER1                        = OWNER1_CREDENTIALS.getAddress();

  protected static final Credentials OWNER2_CREDENTIALS            =
                                                        Credentials.create("0x2da4ef21b864d2cc786dbdb2a120bd2874c36c9d0a1fb7f8c63d7f7a8b41de8f");

  protected static final String      OWNER2                        = OWNER2_CREDENTIALS.getAddress();

  protected static final Credentials OWNER3_CREDENTIALS            =
                                                        Credentials.create("0x5da4ef21b864d2cc786dbdb2a120bd2874c36c9d0a1fb7f8c63d7f7a8b41de8f");

  protected static final String      OWNER3                        = OWNER3_CREDENTIALS.getAddress();

  protected static final Credentials TENANT1_CREDENTIALS           =
                                                         Credentials.create("0x3da4ef21b864d2cc526dbdb2a120bd2874c36c9d0a1fb7f8c63d7f7a8b41de8f");

  protected static final String      TENANT1                       = TENANT1_CREDENTIALS.getAddress();

  protected static final Credentials TENANT2_CREDENTIALS           =
                                                         Credentials.create("0x4da4ef21b864d2cc786dbdb2a120bd2874c36c9d0a1fb7f8c63d7f7a8b41de8f");

  protected static final String      TENANT2                       = TENANT2_CREDENTIALS.getAddress();

  protected static final Credentials TENANT3_CREDENTIALS           =
                                                         Credentials.create("0x6da4ef21b864d2cc786dbdb2a120bd2874c36c9d0a1fb7f8c63d7f7a8b41de8f");

  protected static final String      TENANT3                       = TENANT3_CREDENTIALS.getAddress();

  protected static final Credentials HUB1_CREDENTIALS              =
                                                      Credentials.create("0xaaa4ef21b864d2cc526dbdb2a120bd2874c36c9d0a1fb7f8c63d7f7a8b41de8f");

  protected static final String      HUB1                          = HUB1_CREDENTIALS.getAddress();

  protected static final Credentials HUB2_CREDENTIALS              =
                                                      Credentials.create("0xbba4ef21b864d2cc526dbdb2a120bd2874c36c9d0a1fb7f8c63d7f7a8b41de8f");

  protected static final String      HUB2                          = HUB2_CREDENTIALS.getAddress();

  protected static final Credentials HUB3_CREDENTIALS              =
                                                      Credentials.create("0xcca4ef21b864d2cc526dbdb2a120bd2874c36c9d0a1fb7f8c63d7f7a8b41de8f");

  protected static final String      HUB3                          = HUB3_CREDENTIALS.getAddress();

  protected static final BigInteger  DEED1                         = BigInteger.valueOf(2l);

  protected static final BigInteger  DEED2                         = BigInteger.valueOf(33l);

  protected static final BigInteger  DEED3                         = BigInteger.valueOf(50l);

  protected Web3j                    web3j;

  protected TransactionManager       transactionManager;

  protected ContractGasProvider      contractGasProvider;

  protected TestWoM                  wom;

  public void init(Web3j web3j,
                   TransactionManager transactionManager,
                   ContractGasProvider contractGasProvider) throws Exception {
    this.wom = TestWoM.deploy(web3j, transactionManager, contractGasProvider).send();
    this.web3j = web3j;
    this.transactionManager = transactionManager;
    this.contractGasProvider = contractGasProvider;
    assertThrows(Exception.class,
                 () -> bridgeDeed(DEED1, OWNER1, TENANT1),
                 "Only Contract Manager should be able to update The Deed");
    wom.addManager(transactionManager.getFromAddress()).send();
  }

  protected void checkHubJoinDate(String hubAddress, long boundary1, long boundary2) throws Exception {
    Hub hub = wom.getHub(hubAddress).send();
    assertTrue(hub.joinDate.longValue() >= boundary1, JOIN_DATE_SHOULD_NEVER_CHANGE);
    assertTrue(hub.joinDate.longValue() <= boundary2, JOIN_DATE_SHOULD_NEVER_CHANGE);
  }

  protected void checkHubOwnershipTransferEvent(TransactionReceipt receipt, String previousOwner, String newOwner) {
    List<HubOwnershipTransferredEventResponse> transferHubOwnershipEvents = WoM.getHubOwnershipTransferredEvents(receipt);
    assertNotNull(transferHubOwnershipEvents);
    assertEquals(1, transferHubOwnershipEvents.size());

    HubOwnershipTransferredEventResponse hubOwnershipTransferredEvent = transferHubOwnershipEvents.get(0);
    assertEquals(previousOwner.toLowerCase().replace("0x", ""),
                 hubOwnershipTransferredEvent.previousOwner.toLowerCase().replace("0x", ""));
    assertEquals(newOwner.toLowerCase().replace("0x", ""),
                 hubOwnershipTransferredEvent.newOwner.toLowerCase().replace("0x", ""));
  }

  protected void checkHubProperties(String hubAddress, String hubOwnerAddress, BigInteger deedId) throws Exception {
    Hub hub = wom.getHub(hubAddress).send();
    assertEquals(deedId, hub.deedId);
    assertEquals(hubOwnerAddress.toLowerCase().replace("0x", ""),
                 hub.owner.toLowerCase().replace("0x", ""));
    assertEquals(hub.joinDate, wom.getHubJoinDate(hubAddress).send());
    assertTrue(hub.enabled);
  }

  protected void checkHubProperties(String hubAddress,
                                    String hubOwnerAddress,
                                    BigInteger deedId,
                                    boolean enabled) throws Exception {
    Hub hub = wom.getHub(hubAddress).send();
    assertEquals(deedId, hub.deedId);
    assertEquals(enabled, hub.enabled);
    assertEquals(hubOwnerAddress.toLowerCase().replace("0x", ""),
                 hub.owner.toLowerCase().replace("0x", ""));
    assertEquals(hub.joinDate, wom.getHubJoinDate(hubAddress).send());
  }

  protected void checkHubConnected(BigInteger deedId, String hubAddress) throws Exception {
    assertTrue(wom.isHubConnected(hubAddress).send());
    assertTrue(wom.isDeedConnected(deedId).send());
    assertEquals(hubAddress.toLowerCase().replace("0x", ""), wom.getConnectedHub(deedId).send().toLowerCase().replace("0x", ""));
    assertEquals(deedId, wom.getConnectedDeed(hubAddress).send());
  }

  protected void checkHubDisconnected(BigInteger deedId, String hubAddress) throws Exception {
    assertFalse(wom.isHubConnected(hubAddress).send());
    assertFalse(wom.isDeedConnected(deedId).send());
  }

  protected void checkDeedProperties(BigInteger deedId,
                                     String owner,
                                     String tenant) throws Exception {
    checkDeedProperties(deedId, null, owner, tenant);
  }

  protected void checkDeedProperties(BigInteger deedId,
                                     String hubAddress,
                                     String owner,
                                     String tenant) throws Exception {
    Deed deed = wom.getDeed(deedId).send();
    assertEquals(CITY, deed.city);
    assertEquals(cardType(deedId), deed.cardType);
    assertEquals(mintingPower(deedId), deed.mintingPower);
    assertEquals(maxUsers(deedId), deed.maxUsers);
    assertEquals(owner.toLowerCase().replace("0x", ""), deed.owner.toLowerCase().replace("0x", ""));
    assertEquals(tenant.toLowerCase().replace("0x", ""), deed.tenant.toLowerCase().replace("0x", ""));
    assertEquals(BigInteger.valueOf(100l).subtract(ownerPercentage(deedId)), deed.tenantPercentage);

    if (hubAddress != null) {
      assertEquals(hubAddress.toLowerCase().replace("0x", ""), deed.hub.toLowerCase().replace("0x", ""));
    }
  }

  protected void checkHubConnectedEvent(TransactionReceipt receipt,
                                        BigInteger deedId,
                                        String hubAddress,
                                        String deedTenantAddress) {
    List<HubConnectedEventResponse> hubConnectedEvents = WoM.getHubConnectedEvents(receipt);
    assertNotNull(hubConnectedEvents);
    assertEquals(1, hubConnectedEvents.size());

    HubConnectedEventResponse hubConnectedEvent = hubConnectedEvents.get(0);
    assertEquals(deedId, hubConnectedEvent.nftId);
    assertEquals(hubAddress.toLowerCase().replace("0x", ""), hubConnectedEvent.hub.toLowerCase().replace("0x", ""));
    assertEquals(deedTenantAddress.toLowerCase().replace("0x", ""), hubConnectedEvent.hubOwner.toLowerCase().replace("0x", ""));
  }

  protected void checkHubDisconnectedEvent(TransactionReceipt receipt, BigInteger deedId, String hubAddress) {
    List<HubDisconnectedEventResponse> hubDisconnectedEvents = WoM.getHubDisconnectedEvents(receipt);
    assertNotNull(hubDisconnectedEvents);
    assertEquals(1, hubDisconnectedEvents.size());

    HubDisconnectedEventResponse hubDisconnectedEvent = hubDisconnectedEvents.get(0);
    assertEquals(deedId, hubDisconnectedEvent.nftId);
    assertEquals(hubAddress.toLowerCase().replace("0x", ""), hubDisconnectedEvent.hub.toLowerCase().replace("0x", ""));
  }

  protected void checkDeedUpdatedEvent(TransactionReceipt receipt, BigInteger deedId) {
    List<DeedUpdatedEventResponse> deedUpdatedEvents = WoM.getDeedUpdatedEvents(receipt);
    assertNotNull(deedUpdatedEvents);
    assertEquals(1, deedUpdatedEvents.size());
    assertEquals(deedId, deedUpdatedEvents.get(0).nftId);
  }

  protected void checkNoHubConnectedEvent(TransactionReceipt receipt) {
    List<HubConnectedEventResponse> hubConnectedEvents = WoM.getHubConnectedEvents(receipt);
    assertNotNull(hubConnectedEvents);
    assertEquals(0, hubConnectedEvents.size());
  }

  protected void checkNoHubDisconnectedEvent(TransactionReceipt receipt) {
    List<HubDisconnectedEventResponse> hubDisconnectedEvents = WoM.getHubDisconnectedEvents(receipt);
    assertNotNull(hubDisconnectedEvents);
    assertEquals(0, hubDisconnectedEvents.size());
  }

  protected void checkReceipt(TransactionReceipt receipt) {
    assertNotNull(receipt);
    assertTrue(receipt.isStatusOK());
  }

  protected void sendGasFees() throws IOException {
    sendGasFee(OWNER1);
    sendGasFee(OWNER2);
    sendGasFee(TENANT1);
    sendGasFee(TENANT2);
  }

  protected void sendGasFeesForAll() throws IOException {
    sendGasFee(HUB1);
    sendGasFee(HUB2);
    sendGasFee(HUB3);
    sendGasFee(OWNER1);
    sendGasFee(OWNER2);
    sendGasFee(OWNER3);
    sendGasFee(TENANT1);
    sendGasFee(TENANT2);
    sendGasFee(TENANT3);
  }

  @SuppressWarnings("deprecation")
  protected void sendGasFee(String receiver) throws IOException {
    if (new BigInteger(web3j.ethGetBalance(receiver, DefaultBlockParameterName.LATEST)
                            .send()
                            .getResult()
                            .replace("0x", ""),
                       16).divide(BigInteger.valueOf(10).pow(17)).longValue()
        < 2) {
      transactionManager.sendTransaction(contractGasProvider.getGasPrice(),
                                         contractGasProvider.getGasLimit(),
                                         receiver,
                                         "",
                                         BigInteger.valueOf(5).multiply(BigInteger.valueOf(10).pow(17)));
    }
  }

  protected TransactionReceipt disconnectHub(
                                             Credentials from,
                                             String hubAddress) throws Exception {
    WoM contract = WoM.load(wom.getContractAddress(), web3j, from, contractGasProvider);
    return contract.disconnect(hubAddress).send();
  }

  protected TransactionReceipt connectHub(
                                          Credentials from,
                                          String hubAddress,
                                          BigInteger deedId) throws Exception {
    WoM contract = WoM.load(wom.getContractAddress(), web3j, from, contractGasProvider);
    return contract.connect(hubAddress, deedId).send();
  }

  protected TransactionReceipt transferHubOwnership(
                                                    Credentials from,
                                                    String hubAddress,
                                                    String to) throws Exception {
    WoM contract = WoM.load(wom.getContractAddress(), web3j, from, contractGasProvider);
    return contract.transferHubOwnership(hubAddress, to).send();
  }

  protected TransactionReceipt bridgeDeedAndAutoConnect(BigInteger deedId,
                                                        String hubAddress,
                                                        String owner,
                                                        String tenant) throws Exception {
    return updateDeed(deedId, hubAddress, owner, tenant);
  }

  protected TransactionReceipt bridgeDeed(BigInteger deedId, String owner, String tenant) throws Exception {
    return updateDeed(deedId, EnsUtils.EMPTY_ADDRESS, owner, tenant);
  }

  protected TransactionReceipt updateDeed(
                                          BigInteger deedId,
                                          String hubAddress,
                                          String owner,
                                          String tenant) throws Exception {
    return wom.updateDeed(deedId,
                          new Deed(CITY,
                                   cardType(deedId),
                                   mintingPower(deedId),
                                   maxUsers(deedId),
                                   owner,
                                   tenant,
                                   hubAddress,
                                   ownerPercentage(deedId),
                                   WRONG_TENANT_PERCENTAGE))
              .send();
  }

  protected BigInteger ownerPercentage(BigInteger deedId) {
    if (DEED1.equals(deedId)) {
      return BigInteger.valueOf(20l);
    } else if (DEED2.equals(deedId)) {
      return BigInteger.valueOf(30l);
    } else if (DEED3.equals(deedId)) {
      return BigInteger.valueOf(0l);
    } else {
      throw new IllegalStateException(UNKOWN_DEED_ID + deedId);
    }
  }

  protected BigInteger maxUsers(BigInteger deedId) {
    if (DEED1.equals(deedId)) {
      return BigInteger.valueOf(100l);
    } else if (DEED2.equals(deedId)) {
      return BigInteger.valueOf(1000l);
    } else if (DEED3.equals(deedId)) {
      return BigInteger.valueOf(Long.MAX_VALUE);
    } else {
      throw new IllegalStateException(UNKOWN_DEED_ID + deedId);
    }
  }

  protected BigInteger mintingPower(BigInteger deedId) {
    if (DEED1.equals(deedId)) {
      return BigInteger.valueOf(100l);
    } else if (DEED2.equals(deedId)) {
      return BigInteger.valueOf(110l);
    } else if (DEED3.equals(deedId)) {
      return BigInteger.valueOf(200);
    } else {
      throw new IllegalStateException(UNKOWN_DEED_ID + deedId);
    }
  }

  protected BigInteger cardType(BigInteger deedId) {
    if (DEED1.equals(deedId)) {
      return BigInteger.valueOf(0);
    } else if (DEED2.equals(deedId)) {
      return BigInteger.valueOf(1);
    } else if (DEED3.equals(deedId)) {
      return BigInteger.valueOf(3);
    } else {
      throw new IllegalStateException(UNKOWN_DEED_ID + deedId);
    }
  }

  protected long now() {
    return ZonedDateTime.now().toEpochSecond();
  }

}
