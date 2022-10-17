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

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.meeds.deeds.constant.ObjectNotFoundException;
import io.meeds.deeds.constant.UnauthorizedOperationException;
import io.meeds.deeds.model.DeedTenant;
import io.meeds.deeds.storage.DeedTenantManagerRepository;

@Component
public class TenantService {

  @Autowired(required = false)
  private DeedTenantManagerRepository deedTenantManagerRepository;

  @Autowired
  private BlockchainService           blockchainService;

  @Autowired
  private ListenerService             listenerService;

  /**
   * Retrieves Deed Tenant Last status by NFT Identifier
   * 
   * @param managerAddress DEED Provisioning Manager wallet address
   * @param nftId Deed NFT Id
   * @return Last Command Status (START/STOP)
   * @throws UnauthorizedOperationException when the wallet isn't the DEED
   *           manager who
   */
  public String getLastTenantCommand(String managerAddress, long nftId) throws UnauthorizedOperationException {
    DeedTenant deedTenant = deedTenantManagerRepository.findById(nftId).orElse(null);
    if (deedTenant != null) {
      if (!StringUtils.equals(managerAddress, deedTenant.getManagerAddress())) {
        throw new UnauthorizedOperationException(getUnauthorizedMessage(managerAddress, nftId));
      }
      if (StringUtils.isNotBlank(deedTenant.getStartupTransactionHash())) {
        return "START";
      } else if (StringUtils.isNotBlank(deedTenant.getShutdownTransactionHash())) {
        return "STOP";
      }
    }
    return "";
  }

  /**
   * Retrieves Deed Tenant Start date
   * 
   * @param nftId Deed NFT Id
   * @return {@link LocalDateTime} if started, else null
   */
  public LocalDateTime getTenantStartDate(long nftId) {
    DeedTenant deedTenant = deedTenantManagerRepository.findById(nftId).orElse(null);
    return deedTenant == null ? null : deedTenant.getDate();
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
    deedTenant.setDate(LocalDateTime.now(ZoneOffset.UTC));
    setDeedNftProperties(deedTenant);

    deedTenantManagerRepository.save(deedTenant);
    listenerService.publishEvent("deed.event.tenantEmailUpdated", deedTenant);
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
    listenerService.publishEvent("deed.event.tenantStart", deedTenant);
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
    listenerService.publishEvent("deed.event.tenantStop", deedTenant);
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
