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
package io.meeds.deeds.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.meeds.deeds.constant.ObjectNotFoundException;
import io.meeds.deeds.constant.TenantProvisioningStatus;
import io.meeds.deeds.constant.UnauthorizedOperationException;
import io.meeds.deeds.model.DeedTenant;
import io.meeds.deeds.storage.DeedTenantManagerRepository;

@Component
public class TenantService {

  public static final String          DEED_EVENT_TENANT_EMAIL_UPDATED = "deed.event.tenantEmailUpdated";

  public static final String          DEED_EVENT_TENANT_STOP          = "deed.event.tenantStop";

  public static final String          DEED_EVENT_TENANT_START         = "deed.event.tenantStart";

  @Autowired(required = false)
  private DeedTenantManagerRepository deedTenantManagerRepository;

  @Autowired
  private BlockchainService           blockchainService;

  @Autowired
  private ListenerService             listenerService;

  /**
   * Retrieves the {@link DeedTenant} information
   * 
   * @param nftId DEED NFT id in the blockchain
   * @param address wallet address
   * @return {@link DeedTenant}
   * @throws UnauthorizedOperationException when the wallet isn't the DEED
   *           manager nor the owner
   */
  public DeedTenant getDeedTenant(String address, long nftId) throws UnauthorizedOperationException {
    if (!isDeedManager(address, nftId) && !isDeedOwner(address, nftId)) {
      throw new UnauthorizedOperationException(getUnauthorizedMessage(address, nftId));
    }
    return getDeedTenant(nftId);
  }

  /**
   * Retrieves the {@link DeedTenant} information
   * 
   * @param nftId DEED NFT id in the blockchain
   * @return {@link DeedTenant}
   */
  public DeedTenant getDeedTenant(long nftId) {
    return deedTenantManagerRepository.findById(nftId).orElse(null);
  }

  /**
   * Stores User Email to allow support Team Contact him and notify him about
   * Tenant Status
   * 
   * @param nftId DEED NFT id in the blockchain
   * @param managerAddress DEED Provisioning Manager wallet address
   * @param email Email of the manager
   * @throws UnauthorizedOperationException when the wallet isn't the DEED
   *           manager
   * @throws ObjectNotFoundException when NFT with selected identifier doesn't
   *           exists
   */
  public void saveEmail(String managerAddress, long nftId, String email) throws UnauthorizedOperationException,
                                                                         ObjectNotFoundException {
    if (!isDeedManager(managerAddress, nftId)) {
      throw new UnauthorizedOperationException(getUnauthorizedMessage(managerAddress, nftId));
    }
    DeedTenant deedTenant = deedTenantManagerRepository.findById(nftId)
                                                       .orElse(new DeedTenant());
    deedTenant.setNftId(nftId);
    deedTenant.setManagerAddress(managerAddress.toLowerCase());
    deedTenant.setManagerEmail(email);
    if (deedTenant.getDate() == null) {
      deedTenant.setDate(LocalDateTime.now(ZoneOffset.UTC));
    }
    setDeedNftProperties(deedTenant);

    deedTenantManagerRepository.save(deedTenant);
    listenerService.publishEvent(DEED_EVENT_TENANT_EMAIL_UPDATED, deedTenant);
  }

  /**
   * Stores User Email to allow support Team Contact him and notify him about
   * Tenant Status. In addition, this will collect information about DEED Nft
   * and transaction hash to command Tenant startup.
   * 
   * @param nftId DEED NFT id in the blockchain
   * @param managerAddress DEED Provisioning Manager wallet address
   * @param transactionHash Ethereum Blockchain Deed Start command Transaction
   *          Hash
   * @param email Email of the manager
   * @throws UnauthorizedOperationException when the wallet isn't the DEED
   *           manager
   * @throws ObjectNotFoundException when NFT with selected identifier doesn't
   *           exists
   */
  public void startTenant(String managerAddress,
                          String transactionHash,
                          long nftId,
                          String email) throws UnauthorizedOperationException, ObjectNotFoundException {
    if (!isDeedManager(managerAddress, nftId)) {
      throw new UnauthorizedOperationException(getUnauthorizedMessage(managerAddress, nftId));
    }
    DeedTenant deedTenant = deedTenantManagerRepository.findById(nftId)
                                                       .orElse(new DeedTenant());
    deedTenant.setNftId(nftId);
    deedTenant.setManagerAddress(managerAddress.toLowerCase());
    deedTenant.setManagerEmail(email);
    deedTenant.setStartupTransactionHash(transactionHash);
    deedTenant.setShutdownTransactionHash(null);
    deedTenant.setDate(LocalDateTime.now(ZoneOffset.UTC));
    setDeedNftProperties(deedTenant);

    deedTenantManagerRepository.save(deedTenant);
    listenerService.publishEvent(DEED_EVENT_TENANT_START, deedTenant);
  }

  /**
   * Collects information about DEED Nft and transaction hash to command Tenant
   * shutdown.
   * 
   * @param nftId DEED NFT id in the blockchain
   * @param managerAddress DEED Provisioning Manager wallet address
   * @param transactionHash Ethereum Blockchain Deed Start command Transaction
   *          Hash
   * @throws UnauthorizedOperationException when the wallet isn't the DEED
   *           manager
   * @throws ObjectNotFoundException when NFT with selected identifier doesn't
   *           exists
   */
  public void stopTenant(String managerAddress,
                         String transactionHash,
                         long nftId) throws UnauthorizedOperationException, ObjectNotFoundException {
    if (!isDeedManager(managerAddress, nftId)) {
      throw new UnauthorizedOperationException(getUnauthorizedMessage(managerAddress, nftId));
    }
    DeedTenant deedTenant = deedTenantManagerRepository.findById(nftId)
                                                       .orElse(new DeedTenant());
    deedTenant.setNftId(nftId);
    deedTenant.setManagerAddress(managerAddress.toLowerCase());
    deedTenant.setStartupTransactionHash(null);
    deedTenant.setShutdownTransactionHash(transactionHash);
    deedTenant.setDate(LocalDateTime.now(ZoneOffset.UTC));
    setDeedNftProperties(deedTenant);

    deedTenantManagerRepository.save(deedTenant);
    listenerService.publishEvent(DEED_EVENT_TENANT_STOP, deedTenant);
  }

  /**
   * Checks if address is the provisioning manager of the DEED
   * 
   * @param nftId DEED NFT identifier
   * @param address Wallet or Contract Ethereum address
   * @return true if address is the provisioning manager of the DEED Tenant
   */
  public boolean isDeedManager(String address, long nftId) {
    return blockchainService.isDeedProvisioningManager(address, nftId);
  }

  /**
   * Checks if address is the DEED owner
   * 
   * @param nftId DEED NFT identifier
   * @param address Wallet or Contract Ethereum address
   * @return true if address is the owner of the DEED Tenant
   */
  public boolean isDeedOwner(String address, long nftId) {
    return blockchainService.isDeedOwner(address, nftId);
  }

  /**
   * Check if Deed Tenant is started or commanded to be started
   * 
   * @param nftId DEED NFT identifier
   * @return true if the Tenant Provisioning status is START_CONFIRMED or START_IN_PROGRESS
   */
  public boolean isTenantCommandStop(long nftId) {
    DeedTenant deedTenant = deedTenantManagerRepository.findById(nftId).orElse(null);
    return deedTenant == null || deedTenant.getTenantProvisioningStatus() == null
        || deedTenant.getTenantProvisioningStatus() == TenantProvisioningStatus.STOP_CONFIRMED;
  }

  private void setDeedNftProperties(DeedTenant deedTenant) throws ObjectNotFoundException {
    if (deedTenant.getCardType() < 0 || deedTenant.getCityIndex() < 0) {
      short cardType = blockchainService.getDeedCardType(deedTenant.getNftId());
      deedTenant.setCardType(cardType);
      short cityIndex = blockchainService.getDeedCityIndex(deedTenant.getNftId());
      deedTenant.setCityIndex(cityIndex);
    }
  }

  private String getUnauthorizedMessage(String managerAddress, long nftId) {
    return "User with address " + managerAddress + " isn't the manager of deed " + nftId;
  }

}
