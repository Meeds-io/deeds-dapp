/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
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
package io.meeds.deeds.common.service;

import static io.meeds.deeds.common.constant.CommonConstants.DEED_EVENT_TENANT_EMAIL_UPDATED;
import static io.meeds.deeds.common.constant.CommonConstants.TENANT_COMMAND_START_EVENT;
import static io.meeds.deeds.common.constant.CommonConstants.TENANT_COMMAND_STOP_EVENT;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.meeds.deeds.common.constant.TenantProvisioningStatus;
import io.meeds.deeds.common.constant.UnauthorizedOperationException;
import io.meeds.deeds.common.elasticsearch.model.DeedTenant;
import io.meeds.deeds.common.elasticsearch.storage.DeedTenantManagerRepository;
import io.meeds.wom.api.constant.ObjectNotFoundException;

@Component
public class TenantService {

  private static final Logger         LOG = LoggerFactory.getLogger(TenantService.class);

  @Autowired
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
   * @param nftId DEED NFT id in the blockchain
   * @return Deed Card Type
   * @throws ObjectNotFoundException when NFT with selected identifier doesn't
   *           exists
   */
  public short getCardType(long nftId) throws ObjectNotFoundException {
    DeedTenant deedTenant = getDeedTenantOrImport(nftId);
    return deedTenant.getCardType();
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
   * Retrieve list of {@link DeedTenant} of NFT owner
   * 
   * @param ownerAddress Deed Owner Address
   * @return {@link List} of associated Deed provisioning information
   */
  public List<DeedTenant> getDeedTenants(String ownerAddress) {
    return deedTenantManagerRepository.findByOwnerAddress(StringUtils.lowerCase(ownerAddress));
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
    DeedTenant deedTenant = deedTenantManagerRepository.findById(nftId).orElse(null);
    if (deedTenant == null) {
      deedTenant = buildDeedTenantFromBlockchain(nftId);
    } else {
      setDeedNftProperties(deedTenant);
    }

    deedTenant.setManagerAddress(managerAddress.toLowerCase());
    deedTenant.setManagerEmail(email);
    if (deedTenant.getDate() == null) {
      deedTenant.setDate(LocalDateTime.now(ZoneOffset.UTC));
    }

    deedTenant = saveDeedTenant(deedTenant);
    listenerService.publishEvent(DEED_EVENT_TENANT_EMAIL_UPDATED, deedTenant);
  }

  public DeedTenant getDeedTenantOrImport(String managerAddress, // NOSONAR
                                          Long nftId,
                                          boolean refreshFromBlockchain) throws ObjectNotFoundException {
    DeedTenant deedTenant = getDeedTenantOrImport(managerAddress, nftId);
    if (refreshFromBlockchain && deedTenant != null) {
      boolean isPending = deedTenant.getTenantProvisioningStatus() != null
                          && deedTenant.getTenantProvisioningStatus().isPending();
      if (isPending) {
        boolean started = blockchainService.isDeedStarted(deedTenant.getNftId());
        if (started && deedTenant.getTenantProvisioningStatus().isStart()) {
          deedTenant.setTenantProvisioningStatus(TenantProvisioningStatus.START_CONFIRMED);
        } else if (!started && deedTenant.getTenantProvisioningStatus().isStop()) {
          deedTenant.setTenantProvisioningStatus(TenantProvisioningStatus.STOP_CONFIRMED);
        }
      } else {
        // Mark Tenant as in progress to be checked again on DB
        if (blockchainService.isDeedStarted(deedTenant.getNftId())) {
          if (StringUtils.isBlank(deedTenant.getStartupTransactionHash())) {
            deedTenant.setTenantProvisioningStatus(TenantProvisioningStatus.START_CONFIRMED);
          } else {
            deedTenant.setTenantProvisioningStatus(TenantProvisioningStatus.START_IN_PROGRESS);
          }
          saveDeedTenant(deedTenant);

          // Return effective status
          deedTenant.setTenantProvisioningStatus(TenantProvisioningStatus.START_CONFIRMED);
        } else {
          if (StringUtils.isBlank(deedTenant.getShutdownTransactionHash())) {
            deedTenant.setTenantProvisioningStatus(TenantProvisioningStatus.STOP_CONFIRMED);
          } else {
            deedTenant.setTenantProvisioningStatus(TenantProvisioningStatus.STOP_IN_PROGRESS);
          }
          saveDeedTenant(deedTenant);

          // Return effective status
          deedTenant.setTenantProvisioningStatus(TenantProvisioningStatus.STOP_CONFIRMED);
        }
      }
    }
    return deedTenant;
  }

  public DeedTenant getDeedTenantOrImport(String managerAddress, Long nftId) throws ObjectNotFoundException { // NOSONAR
    DeedTenant deedTenant = deedTenantManagerRepository.findById(nftId).orElse(null);
    boolean changed = deedTenant == null;
    if (deedTenant == null) {
      deedTenant = buildDeedTenantFromBlockchain(nftId);
      if (StringUtils.isBlank(deedTenant.getManagerAddress())
          && isDeedManager(managerAddress, nftId)) {
        deedTenant.setManagerAddress(managerAddress.toLowerCase());
      }
      if (StringUtils.isBlank(deedTenant.getOwnerAddress())
          && isDeedOwner(managerAddress, nftId)) {
        deedTenant.setOwnerAddress(managerAddress.toLowerCase());
      }
    } else if (StringUtils.isNotBlank(managerAddress)
               && !isProvisioningManager(managerAddress, deedTenant)) {
      if (isDeedOwner(managerAddress, nftId)) {
        deedTenant.setOwnerAddress(managerAddress.toLowerCase());
        changed = true;
      } else if (StringUtils.isBlank(deedTenant.getOwnerAddress())) {
        String deedOwner = blockchainService.getDeedOwner(nftId);
        if (StringUtils.isNotBlank(deedOwner)) {
          deedTenant.setOwnerAddress(deedOwner);
          changed = true;
        }
      }
      if (isDeedManager(managerAddress, nftId)) {
        deedTenant.setManagerAddress(managerAddress.toLowerCase());
        changed = true;
      } else if (StringUtils.isBlank(deedTenant.getManagerAddress())) {
        String deedManager = blockchainService.getDeedManager(nftId);
        if (StringUtils.isNotBlank(deedManager)) {
          deedTenant.setManagerAddress(deedManager);
          changed = true;
        }
      }
    }
    if (changed) {
      return saveDeedTenant(deedTenant);
    } else {
      return deedTenant;
    }
  }

  public DeedTenant getDeedTenantOrImport(Long nftId) throws ObjectNotFoundException {
    DeedTenant deedTenant = deedTenantManagerRepository.findById(nftId).orElse(null);
    if (deedTenant == null) {
      deedTenant = buildDeedTenantFromBlockchain(nftId);
      deedTenant = saveDeedTenant(deedTenant);
    }
    return deedTenant;
  }

  /**
   * Changes the DeedTenant Manager when an offer was acquired.
   * 
   * @param nftId Deed NFT blockchain identifier
   * @param newManager New Manager Address
   * @throws ObjectNotFoundException when Deed doesn't exist
   * @throws UnauthorizedOperationException when the address isn't the manager
   *           on blockchain
   */
  public void markDeedAsAcquired(long nftId, String newManager) throws ObjectNotFoundException, UnauthorizedOperationException {
    LOG.debug("Mark Tenant of {} as aqcuired by new Provisioning Manager: {}", nftId, newManager);
    DeedTenant deedTenant = getDeedTenantOrImport(newManager, nftId);
    if (!StringUtils.equalsIgnoreCase(deedTenant.getManagerAddress(), newManager)) {
      throw new UnauthorizedOperationException(newManager + " address isn't the provisioning manager of Tenant in blockchain");
    }
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
                                                       .orElse(null);
    if (deedTenant == null) {
      deedTenant = buildDeedTenantFromBlockchain(nftId);
      deedTenant.setTenantProvisioningStatus(TenantProvisioningStatus.START_IN_PROGRESS);
    } else {
      setDeedNftProperties(deedTenant);
    }

    deedTenant.setManagerAddress(managerAddress.toLowerCase());
    deedTenant.setManagerEmail(email);
    deedTenant.setDate(LocalDateTime.now(ZoneOffset.UTC));
    deedTenant.setStartupTransactionHash(transactionHash);
    deedTenant.setShutdownTransactionHash(null);
    deedTenant = saveDeedTenant(deedTenant);

    listenerService.publishEvent(TENANT_COMMAND_START_EVENT, deedTenant);
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
    DeedTenant deedTenant = deedTenantManagerRepository.findById(nftId).orElse(null);
    if (deedTenant == null) {
      deedTenant = buildDeedTenantFromBlockchain(nftId);
      deedTenant.setTenantProvisioningStatus(TenantProvisioningStatus.STOP_IN_PROGRESS);
    } else {
      setDeedNftProperties(deedTenant);
    }

    deedTenant.setManagerAddress(managerAddress.toLowerCase());
    deedTenant.setStartupTransactionHash(null);
    deedTenant.setShutdownTransactionHash(transactionHash);
    deedTenant.setDate(LocalDateTime.now(ZoneOffset.UTC));

    saveDeedTenant(deedTenant);
    listenerService.publishEvent(TENANT_COMMAND_STOP_EVENT, deedTenant);
  }

  /**
   * Retrieve Deed Tenant information from blockchain
   * 
   * @param nftId DEED NFT id in the blockchain
   * @return {@link DeedTenant}
   * @throws ObjectNotFoundException when Deed NFT id is not recognized on
   *           blockchain
   */
  public DeedTenant buildDeedTenantFromBlockchain(long nftId) throws ObjectNotFoundException {
    DeedTenant deedTenant = new DeedTenant();
    deedTenant.setNftId(nftId);
    setDeedNftProperties(deedTenant);
    deedTenant.setManagerAddress(StringUtils.lowerCase(blockchainService.getDeedManager(nftId)));
    deedTenant.setOwnerAddress(StringUtils.lowerCase(blockchainService.getDeedOwner(nftId)));
    if (blockchainService.isDeedStarted(deedTenant.getNftId())) {
      deedTenant.setTenantProvisioningStatus(TenantProvisioningStatus.START_CONFIRMED);
    } else {
      deedTenant.setTenantProvisioningStatus(TenantProvisioningStatus.STOP_CONFIRMED);
    }
    return deedTenant;
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
   * @return true if the Tenant Provisioning status is START_CONFIRMED or
   *         START_IN_PROGRESS
   */
  public boolean isTenantCommandStop(long nftId) {
    DeedTenant deedTenant = deedTenantManagerRepository.findById(nftId).orElse(null);
    return deedTenant == null || deedTenant.getTenantProvisioningStatus() == null
           || deedTenant.getTenantProvisioningStatus() == TenantProvisioningStatus.STOP_CONFIRMED;
  }

  /**
   * @param networkId Blockchain Network identifier to check
   * @return true if Network is valid else return false
   */
  public boolean isBlockchainNetworkValid(long networkId) {
    return networkId == getBlockchainNetworkId();
  }

  /**
   * @return Blockchain network identifier
   */
  public long getBlockchainNetworkId() {
    try {
      return blockchainService.getNetworkId();
    } catch (Exception e) {
      LOG.warn("Error getting network id: {}", e.getMessage());
      return 1l; // Default Etheureum Mainnet
    }
  }

  /**
   * Stores Tenant information
   * 
   * @param deedTenant {@link DeedTenant}
   * @return {@link DeedTenant}
   */
  public DeedTenant saveDeedTenant(DeedTenant deedTenant) {
    return deedTenantManagerRepository.save(deedTenant);
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

  private boolean isProvisioningManager(String managerAddress, DeedTenant deedTenant) {
    return StringUtils.equalsIgnoreCase(deedTenant.getManagerAddress(), managerAddress)
           || StringUtils.equalsIgnoreCase(deedTenant.getOwnerAddress(), managerAddress);
  }

}
