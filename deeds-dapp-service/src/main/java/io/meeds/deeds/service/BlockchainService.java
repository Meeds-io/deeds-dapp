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

import java.math.BigDecimal;
import java.math.BigInteger;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.tuples.generated.Tuple4;
import org.web3j.tuples.generated.Tuple5;

import io.meeds.deeds.constant.ObjectNotFoundException;
import io.meeds.deeds.contract.Deed;
import io.meeds.deeds.contract.DeedTenantProvisioning;
import io.meeds.deeds.contract.ERC20;
import io.meeds.deeds.contract.MeedsToken;
import io.meeds.deeds.contract.TokenFactory;
import io.meeds.deeds.contract.XMeedsNFTRewarding;
import io.meeds.deeds.model.DeedCity;
import io.meeds.deeds.model.FundInfo;

@Component
public class BlockchainService {

  private DeedTenantProvisioning deedTenantProvisioning;

  private Deed                   deed;

  private TokenFactory           tokenFactory;

  private XMeedsNFTRewarding     xMeedsToken;

  private MeedsToken             ethereumToken;

  private MeedsToken             polygonToken;

  private ERC20                  sushiPairToken;

  public BlockchainService(
                           DeedTenantProvisioning deedTenantProvisioning,
                           Deed deed,
                           @Qualifier("ethereumMeedToken")
                           MeedsToken ethereumToken,
                           @Qualifier("polygonMeedToken")
                           MeedsToken polygonToken,
                           XMeedsNFTRewarding xMeedsToken,
                           TokenFactory tokenFactory,
                           @Qualifier("sushiPairToken")
                           ERC20 sushiPairToken) {
    this.deedTenantProvisioning = deedTenantProvisioning;
    this.deed = deed;
    this.ethereumToken = ethereumToken;
    this.polygonToken = polygonToken;
    this.xMeedsToken = xMeedsToken;
    this.tokenFactory = tokenFactory;
    this.sushiPairToken = sushiPairToken;
  }

  /**
   * Retrieves from blockchain whether an address is the provisioning manager of
   * the deed or not
   *
   * @param address Ethereum address to check
   * @param nftId Deed NFT identifier
   * @return true if is manager else false
   */
  public boolean isDeedProvisioningManager(String address, long nftId) {
    return blockchainCall(deedTenantProvisioning.isProvisioningManager(address, BigInteger.valueOf(nftId)));
  }

  /**
   * Retrieves from blockchain whether an address is the owner of the deed
   *
   * @param address Ethereum address to check
   * @param nftId Deed NFT identifier
   * @return true if is owner else false
   */
  public boolean isDeedOwner(String address, long nftId) {
    return blockchainCall(deed.balanceOf(address, BigInteger.valueOf(nftId))).longValue() > 0;
  }

  /**
   * Retrieves from Blockchain DEED card type:
   * - 0 : Common
   * - 1 : Uncommon
   * - 2 : Rare
   * - 3 : Legendary
   *
   * @param nftId Deed NFT identifier
   * @return card type index
   * @throws ObjectNotFoundException when NFT with selected identifier doesn't
   *           exists
   */
  public short getDeedCardType(long nftId) throws ObjectNotFoundException {
    try {
      return deed.cardType(BigInteger.valueOf(nftId)).send().shortValue();
    } catch (Exception e) {
      if (StringUtils.contains(e.getMessage(), "execution reverted")) {
        throw new ObjectNotFoundException(e.getMessage());
      } else {
        throw new IllegalStateException("Error retrieving information 'getDeedCardType' from Blockchain", e);
      }
    }
  }

  /**
   * Retrieves from Blockchain DEED city index:
   * - 0 : Tanit
   * - 1 : Reshef
   * - 2 : Ashtarte
   * - 3 : Melqart
   * - 4 : Eshmun
   * - 5 : Kushor
   * - 6 : Hammon
   *
   * @param nftId Deed NFT identifier
   * @return card city index
   * @throws ObjectNotFoundException when NFT with selected identifier doesn't
   *           exists
   */
  public short getDeedCityIndex(long nftId) throws ObjectNotFoundException {
    try {
      return deed.cityIndex(BigInteger.valueOf(nftId)).send().shortValue();
    } catch (Exception e) {
      if (StringUtils.contains(e.getMessage(), "execution reverted")) {
        throw new ObjectNotFoundException(e.getMessage());
      } else {
        throw new IllegalStateException("Error retrieving information 'getDeedCityIndex' from Blockchain", e);
      }
    }
  }

  /**
   * @return {@link BigDecimal} representing the total supply of Meeds Token
   *         which is retrieved from ethereum blockchain. The retrieved value is
   *         divided by number of decimals of the token (10^18)
   */
  public BigDecimal meedsTotalSupplyNoDecimals() {
    BigInteger totalSupply = meedsTotalSupply();
    return new BigDecimal(totalSupply).divide(BigDecimal.valueOf(10).pow(18));
  }

  /**
   * @return {@link BigInteger} representing the total supply of Meeds Token
   *         which is retrieved from ethereum blockchain.
   */
  public BigInteger meedsTotalSupply() {
    return blockchainCall(ethereumToken.totalSupply());
  }

  /**
   * @return {@link BigInteger} representing the total supply of xMeeds Token
   *         which is retrieved from ethereum blockchain.
   */
  public BigInteger xMeedsTotalSupply() {
    return blockchainCall(xMeedsToken.totalSupply());
  }

  /**
   * @return {@link BigInteger} representing the total supply of xMeeds Token
   *         which is retrieved from ethereum blockchain.
   */
  public BigInteger sushiPairTotalSupply() {
    return blockchainCall(sushiPairToken.totalSupply());
  }

  /**
   * @param address Fund Address to get its rewarding information
   * @return {@link FundInfo} with rewarding parameters retrieved from Token
   *         Factory
   */
  public FundInfo getFundInfo(String address) {
    Tuple5<BigInteger, BigInteger, BigInteger, BigInteger, Boolean> fundInfo = blockchainCall(tokenFactory.fundInfos(address));
    return fundInfo == null ? null
                            : new FundInfo(address,
                                           fundInfo.component1(),
                                           fundInfo.component2(),
                                           fundInfo.component3(),
                                           fundInfo.component4(),
                                           fundInfo.component5());
  }

  /**
   * @return {@link DeedCity} representing current minting city
   */
  public DeedCity getCurrentCity() {
    BigInteger currentCityIndex = blockchainCall(xMeedsToken.currentCityIndex());
    Tuple4<String, BigInteger, BigInteger, BigInteger> cityInfo = blockchainCall(xMeedsToken.cityInfo(currentCityIndex));
    return cityInfo == null ? null
                            : new DeedCity(currentCityIndex,
                                           cityInfo.component1(),
                                           cityInfo.component2(),
                                           cityInfo.component3(),
                                           cityInfo.component4());
  }

  /**
   * @return {@link FundInfo} of xMeed Token with rewarding parameters retrieved
   *         from Token Factory
   */
  public FundInfo getXMeedFundInfo() {
    FundInfo fundInfo = getFundInfo(xMeedsToken.getContractAddress());
    fundInfo.setTotalSupply(xMeedsTotalSupply());
    fundInfo.setXMeedPendingReward(pendingRewardBalanceOf(xMeedsToken.getContractAddress()));
    fundInfo.setMeedsBalance(meedBalanceOf(xMeedsToken.getContractAddress()));
    return fundInfo;
  }

  /**
   * @return {@link FundInfo} of Sushi Pair Token with rewarding parameters
   *         retrieved from Token Factory
   */
  public FundInfo getSushiPairFundInfo() {
    FundInfo fundInfo = getFundInfo(sushiPairToken.getContractAddress());
    fundInfo.setSymbol(sushiPairSymbol());
    fundInfo.setLpBalanceOfTokenFactory(stakedSushiPair());
    fundInfo.setTotalSupply(sushiPairTotalSupply());
    fundInfo.setMeedsBalance(meedBalanceOf(sushiPairToken.getContractAddress()));
    return fundInfo;
  }

  /**
   * @param address Address to get its pending rewardings not minted yet
   * @return {@link BigInteger} for Meed Token value with decimals
   */
  public BigInteger pendingRewardBalanceOf(String address) {
    return blockchainCall(tokenFactory.pendingRewardBalanceOf(address));
  }

  /**
   * @return {@link BigInteger} for total allocation points configured in token
   *         Factory
   */
  public BigInteger totalAllocationPoints() {
    return blockchainCall(tokenFactory.totalAllocationPoints());
  }

  /**
   * @return {@link BigInteger} for total fixed percentages configured in token
   *         Factory
   */
  public BigInteger totalFixedPercentage() {
    return blockchainCall(tokenFactory.totalFixedPercentage());
  }

  /**
   * @param address Ethereum address
   * @return {@link BigDecimal} representing the balance of address which is
   *         retrieved from ethereum blockchain. The retrieved value is divided
   *         by number of decimals of the token (10^18)
   */
  public BigDecimal meedBalanceOfNoDecimals(String address) {
    BigInteger balance = meedBalanceOf(address);
    return new BigDecimal(balance).divide(BigDecimal.valueOf(10).pow(18));
  }

  /**
   * @param address Ethereum address
   * @return {@link BigInteger} representing the balance of address which is
   *         retrieved from ethereum blockchain.
   */
  public BigInteger meedBalanceOf(String address) {
    return blockchainCall(ethereumToken.balanceOf(address));
  }

  /**
   * @return Sushi Swap Pair token symbol
   */
  public String sushiPairSymbol() {
    return blockchainCall(sushiPairToken.symbol());
  }

  /**
   * @return total staked SLP amount in TokenFactory
   */
  public BigInteger stakedSushiPair() {
    return blockchainCall(sushiPairToken.balanceOf(tokenFactory.getContractAddress()));
  }

  /**
   * @param address Ethereum address
   * @return {@link BigDecimal} representing the balance of address which is
   *         retrieved from polygon blockchain. The retrieved value is divided
   *         by number of decimals of the token (10^18)
   */
  public BigDecimal meedBalanceOfOnPolygon(String address) {
    BigInteger balance = blockchainCall(polygonToken.balanceOf(address));
    return new BigDecimal(balance).divide(BigDecimal.valueOf(10).pow(18));
  }

  private <T> T blockchainCall(RemoteFunctionCall<T> remoteCall) {
    try {
      return remoteCall.send();
    } catch (Exception e) {
      throw new IllegalStateException("Error calling blockchain", e);
    }
  }
}
