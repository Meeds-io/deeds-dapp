/*
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2020 - 2022 Meeds Association contact@meeds.io
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
import './initComponents';
import * as ethUtils from './js/ethUtils.js';
import * as tokenUtils from './js/tokenUtils.js';
import * as exchange from './js/exchange.js';
import * as authentication from './js/authentication.js';
import * as tenantManagement from './js/tenantManagement.js';
import * as deedMetadata from './js/deedMetadata.js';
import * as tokenMetricService from './js/tokenMetricService.js';
import * as assetMetricService from './js/assetMetricService.js';

window.Object.defineProperty(Vue.prototype, '$ethUtils', {
  value: ethUtils,
});

window.Object.defineProperty(Vue.prototype, '$exchange', {
  value: exchange,
});

window.Object.defineProperty(Vue.prototype, '$authentication', {
  value: authentication,
});

window.Object.defineProperty(Vue.prototype, '$tenantManagement', {
  value: tenantManagement,
});

window.Object.defineProperty(Vue.prototype, '$deedMetadata', {
  value: deedMetadata,
});

window.Object.defineProperty(Vue.prototype, '$tokenMetricService', {
  value: tokenMetricService,
});

Vue.use(Vuex);
Vue.use(Vuetify);

const language = localStorage.getItem('deeds-selectedLanguage') || (navigator.language.indexOf('fr') === 0 ? 'fr' : 'en');

const selectedFiatCurrency = localStorage.getItem('deeds-selectedFiatCurrency') || 'usd';
const isMetamaskInstalled = ethUtils.isMetamaskInstalled();
const isMetamaskConnected = ethUtils.isMetamaskConnected();

window.parentAppLocation = window.location.pathname.split('/')[1];

const i18n = new VueI18n({
  locale: language,
  messages: {},
});

const store = new Vuex.Store({
  state: {
    parentLocation: window.parentAppLocation,
    appLoading: true,
    deedLoading: true,
    tokenLoading: true,
    lpLoading: true,
    ens: null,
    address: null,
    networkId: null,
    validNetwork: false,
    yearInMinutes: 365 * 24 * 60,
    // Contracts addresses
    sushiswapRouterAddress: '0xd9e1cE17f2641f24aE83637ab66a2cca9C378B9F',
    sushiswapPairAddress: '0x960bd61d0b960b107ff5309a2dcced4705567070',
    wethAddress: '0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2',
    meedAddress: '0x8503a7b00b4b52692cc6c14e5b96f142e30547b7',
    tokenFactoryAddress: '0x1B37D04759aD542640Cc44Ff849a373040386050',
    xMeedAddress: '0x44d6d6ab50401dd846336e9c706a492f06e1bcd4',
    deedAddress: '0x0143b71443650aa8efa76bd82f35c22ebd558090',
    polygonMeedAddress: '0x6aca77cf3bab0c4e8210a09b57b07854a995289a',
    polygonWethAddress: '0x0d500B1d8E8eF31E21C99d1Db9A6444d3ADf1270',
    comethPairAddress: '0xb82F8457fcf644803f4D74F677905F1d410Cd395',
    comethRouterAddress: '0x93bcdc45f7e62f89a8e901dc4a0e2c6c427d9f25',
    comethTokenFactoryAddress: '0x035a8a07bbae988893499e5c0d5b281b7967b107',
    comethPool: null,
    vestingAddress: '0x440701ca5817b5847438da2ec2ca3b9fdbf37dfa',
    univ2PairAddress: null,
    tenantProvisioningAddress: null,
    // External links
    etherscanBaseLink: 'https://etherscan.io/',
    polygonscanBaseLink: 'https://polygonscan.com/',
    openSeaBaseLink: 'https://testnets.opensea.io/assets/rinkeby/0x0143b71443650aa8efa76bd82f35c22ebd558090',
    openSeaCollectionLink: 'https://opensea.io/collection/meeds-dao',
    whitepaperLink: 'https://mirror.xyz/meedsdao.eth/EDh9QfsuuIDNS0yKcQDtGdXc25vfkpnnKpc3RYUTJgc',
    // Contract variables
    provider: null,
    polygonProvider: new ethers.providers.JsonRpcProvider({
      url: 'https://polygon-rpc.com/',
    }),
    erc20ABI: [
      'event Transfer(address indexed from, address indexed to, uint256 value)',
      'event Approval(address indexed owner, address indexed spender, uint256 value)',
      'function allowance(address owner, address spender) external view returns (uint256)',
      'function approve(address spender, uint256 amount) external returns (bool)',
      'function balanceOf(address owner) view returns (uint256)',
      'function totalSupply() public view returns (uint256)',
      'function symbol() public view returns (string)',
    ],
    polygonTokenFactoryABI: [
      'function balanceOf(address owner) view returns (uint256)',
      'function earned(address owner) view returns (uint256[] memory earns)',
      'function getRewardsForDuration() view returns (uint256[] memory rewards)',
      'function rewardsDuration() public view returns (uint256)',
      'function totalSupply() public view returns (uint256)',
    ],
    routerABI: [
      'function getAmountsOut(uint amountIn, address[] memory path) public view returns(uint[] memory amounts)',
      'function swapExactETHForTokens(uint amountOutMin, address[] calldata path, address to, uint deadline) external payable returns (uint[] memory amounts)',
      'function swapExactTokensForETH(uint amountIn, uint amountOutMin, address[] calldata path, address to, uint deadline) external payable returns (uint[] memory amounts)',
    ],
    xMeedRewardingABI: [
      'event Redeemed(address indexed user, string city, string cardType, uint256 id)',
      'event Staked(address indexed user, uint256 amount)',
      'event Withdrawn(address indexed user, uint256 amount)',
      'function transfer(address recipient, uint256 amount) public returns (bool)',
      'function transferFrom(address sender, address recipient, uint256 amount) public returns (bool)',
      'function allowance(address owner, address spender) external view returns (uint256)',
      'function approve(address spender, uint256 amount) external returns (bool)',
      'function balanceOf(address owner) view returns (uint256)',
      'function totalSupply() public view returns (uint256)',
      'function stake(uint256 amount) public',
      'function withdraw(uint256 amount) public',
      'function exit() external',
      'function isCurrentCityMintable() public view returns (bool)',
      'function cityMintingStartDate() public view returns (uint256)',
      'function redeem(uint8 cardTypeId) public returns (uint256 tokenId)',
      'function startRewardsTime() public view returns (uint256)',
      'function earned(address account) public view returns (uint256)',
      'function currentCityIndex() public view returns (uint8)',
      'function cityInfo(uint256 index) public view returns (string name, uint32 population, uint32 maxPopulation, uint256 availability)',
      'function cardTypeInfo(uint256 index) public view returns (string name, uint8 cityIndex, uint8 cardType, uint32 supply, uint32 maxSupply, uint256 amount)',
      'function lastCityMintingCompleteDate() public view returns (uint256)',
    ],
    nftABI: [
      'event StartedUsingNFT(address indexed account, uint256 indexed id, address indexed strategy)',
      'event EndedUsingNFT(address indexed account, uint256 indexed id, address indexed strategy)',
      'function totalSupply() public view returns (uint256)',
      'function totalSupply(uint256 _id) public view returns (uint256)',
      'function uri(uint256 _id) public view returns (string)',
      'function maxSupply(uint256 _id) public view returns (uint256)',
      'function cityIndex(uint256 _id) public view returns (uint256)',
      'function cardType(uint256 _id) public view returns (uint256)',
      'function balanceOf(address _owner, uint256 _id) public view returns (uint256)',
      'function balanceOfBatch(address[] _owners, uint256[] _ids) public view returns (uint256[])',
      'function nftsOf(address account) public view returns (uint256[])',
    ],
    tokenFactoryABI: [
      'event Harvest(address indexed user, address indexed fundAddress, uint256 amount)',
      'function startRewardsTime() public view returns (uint256)',
      'function meedPerMinute() public view returns (uint256)',
      'function totalAllocationPoints() public view returns (uint256)',
      'function totalFixedPercentage() public view returns (uint256)',
      'function fundsLength() public view returns (uint256)',
      'function fundAddresses(uint256 _index) public view returns (address)',
      'function fundInfos(address _fundAddress) public view returns (uint256 fixedPercentage, uint256 allocationPoint, uint256 lastRewardTime, uint256 accMeedPerShare, bool isLPToken)',
      'function userLpInfos(address _fundAddress, address _userAddress) public view returns (uint256 amount, uint256 rewardDebt)',
      'function deposit(address _fundAddress, uint256 _amount) public',
      'function withdraw(address _fundAddress, uint256 _amount) public',
      'function harvest(address _fundAddress) public',
      'function pendingRewardBalanceOf(address _fundAddress) public view returns (uint256)',
      'function pendingRewardBalanceOf(address _fundAddress, address _userAddress) public view returns (uint256)',
    ],
    tenantProvisioningABI: [
      'function startTenant(uint256 _nftId) external',
      'function stopTenant(uint256 _nftId) external',
      'function delegatee(uint256 _nftId) public view returns(address)',
      'function tenantStatus(uint256 _nftId) public view returns(bool)',
      'function isProvisioningManager(address _address, uint256 _nftId) public view returns(bool)',
    ],
    // Contracts objects
    sushiswapRouterContract: null,
    wethContract: null,
    meedContract: null,
    xMeedContract: null,
    deedContract: null,
    tokenFactoryContract: null,
    tenantProvisioningContract: null,
    polygonMeedContract: null,
    // User preferred language
    language,
    // Metamask status
    metamaskOffline: false,
    isMetamaskInstalled,
    isMetamaskConnected,
    // Meed/ETH pair historical data
    pairHistoryData: null,
    // Default Gas limit for sent transactions
    approvalGasLimit: 60000,
    stakeGasLimit: 200000,
    unstakeGasLimit: 200000,
    redeemGasLimit: 300000,
    harvestGasLimit: 250000,
    depositGasLimit: 250000,
    withdrawGasLimit: 250000,
    tradeGasLimit: 170000,
    startTenantGasLimit: 150000,
    stopTenantGasLimit: 150000,
    maxGasLimit: 300000,
    // Cuurent Gas Price 
    gasPrice: 0,
    gasPriceGwei: 0,
    transactionGas: 0,
    meedPrice: 0,
    // User balances
    etherBalance: null,
    meedsBalance: null,
    polygonMeedsBalance: null,
    meedsRouteAllowance: null,
    meedsStakeAllowance: null,
    meedsTotalSupply: null,
    meedsMaxTotalSupply: new BigNumber('100000000000000000000000000'),
    maxMeedSupplyReached: null,
    noMeedSupplyForLPRemaining: null,
    remainingMeedSupply: null,
    xMeedsTotalSupply: null,
    meedsBalanceOfXMeeds: null,
    meedsPendingBalanceOfXMeeds: null,
    now: Date.now(),
    xMeedsBalance: null,
    pointsBalance: null,
    ownedNfts: null,
    selectedFiatCurrency,
    noCityLeft: false,
    currentCity: null,
    currentCardTypes: null,
    currentCityMintable: null,
    lastCityMintingCompleteDate: null,
    rewardedMeedPerMinute: new BigNumber('10000000000000000000'),
    rewardedFundsLength: null,
    rewardedFunds: null,
    rewardedPools: null,
    rewardedTotalFixedPercentage: null,
    rewardedTotalAllocationPoints: null,
    addSushiswapLiquidityLink: 'https://app.sushi.com/add/ETH/0x8503a7b00b4b52692cc6c14e5b96f142e30547b7?chainId=1',
    addUniswapLiquidityLink: null,
    addComethLiquidityLink: 'https://swap.cometh.io/#/add/ETH/0x6acA77CF3BaB0C4E8210A09B57B07854a995289a',
    rentComethLiquidityLink: 'https://swap.cometh.io/#/stake/0x6acA77CF3BaB0C4E8210A09B57B07854a995289a/ETH/0x035A8a07Bbae988893499e5c0D5b281b7967b107',
    isMobile: false,
    poolsChanged: 2,
  },
  mutations: {
    setMobile(state, value) {
      state.isMobile = value;
    },
    setMetamaskInstalled(state) {
      state.isMetamaskInstalled = ethUtils.isMetamaskInstalled();
    },
    setMetamaskConnected(state) {
      state.isMetamaskConnected = ethUtils.isMetamaskConnected();
    },
    setAddress(state) {
      ethUtils.getSelectedAddress()
        .then(addresses => {
          state.address = addresses && addresses.length && addresses[0] || null;
          this.commit('setProvider');
        })
        .finally(() => this.commit('loaded'));
    },
    setNetworkId(state) {
      ethUtils.getSelectedChainId()
        .then(networkId => {
          state.networkId = new BigNumber(networkId).toNumber();
          if (state.networkId === 1) {
            // Mainnet
          } else if (state.networkId === 5) {
            // Goerli
            state.etherscanBaseLink = 'https://goerli.etherscan.io/';
            state.sushiswapRouterAddress = '0x1b02dA8Cb0d097eB8D57A175b88c7D8b47997506';
            state.sushiswapPairAddress = '0x131Bd5b643Bc12EFb9A4F23512BbA5e1ef3F33bD';
            state.wethAddress = '0xB4FBF271143F4FBf7B91A5ded31805e42b2208d6';
            state.meedAddress = '0x4998a63C7494afc4Ea96E7Ea86E88c59271914c1';
            state.tokenFactoryAddress = '0x13142F102152aBa8AD81281E7eC1374577D662EC';
            state.xMeedAddress = '0xee5BBf589577266e5ddee2CfB4acFB945e844079';
            state.deedAddress = '0x01ab6ab1621b5853Ad6F959f6b7df6A369fbd346';
            state.tenantProvisioningAddress = '0x801FD0FB6f70FAF985410FCbc97FC72D0CC76d8C';

            // Opensea links
            state.openSeaBaseLink = `https://testnets.opensea.io/assets/goerli/${state.deedAddress}`;
            state.openSeaCollectionLink = `https://testnets.opensea.io/collection/${state.deedAddress}`;
          } else {
            this.commit('setMetamaskOffline');
            return;
          }
          state.validNetwork = true;
          state.addSushiswapLiquidityLink = `https://app.sushi.com/add/ETH/${state.meedAddress}?chainId=${state.networkId}`;
          this.commit('setAddress');
        });
    },
    selectLanguage(state, language) {
      state.language = language;
      i18n.locale = language.indexOf('fr') === 0 ? 'fr' : 'en';
      localStorage.setItem('deeds-selectedLanguage', state.language);
      initializeVueApp(language);
    },
    setEtherBalance(state, etherBalance) {
      state.etherBalance = etherBalance;
    },
    setMeedPrice(state, meedPrice) {
      state.meedPrice = meedPrice;
    },
    setMeedsBalance(state, meedsBalance) {
      state.meedsBalance = meedsBalance;
    },
    setPolygonMeedsBalance(state, balance) {
      state.polygonMeedsBalance = balance;
    },
    setMeedsRouteAllowance(state, meedsRouteAllowance) {
      state.meedsRouteAllowance = meedsRouteAllowance;
    },
    setXMeedsBalance(state, xMeedsBalance) {
      state.xMeedsBalance = xMeedsBalance;
      state.loadingXMeedsBalance = false;
    },
    loadPointsPeriodically() {
      window.setInterval(() => this.commit('loadPointsBalance'), 20000);
    },
    loadPointsBalance(state) {
      if (state.xMeedContract) {
        tokenUtils.getPointsBalance(state.xMeedContract, state.address)
          .then(balance => {
            state.pointsBalance = balance;
          });
      } else {
        state.pointsBalance = 0;
      }
    },
    loadCurrentCity(state, reloadOnNotMintable) {
      if (state.xMeedContract) {
        tokenUtils.getCurrentCity(state.xMeedContract)
          .then(currentCity => {
            state.currentCity = currentCity;
            state.noCityLeft = !currentCity;
            if (currentCity) {
              return tokenUtils.isCurrentCityMintable(state.xMeedContract);
            } else {
              return false;
            }
          })
          .then(currentCityMintable => {
            state.currentCityMintable = currentCityMintable;
            if (!currentCityMintable && state.currentCity) {
              if (reloadOnNotMintable) {
                throw new Error('City not mintable yet');
              } else {
                return tokenUtils.getLastCityMintingCompleteDate(state.xMeedContract)
                  .then(lastCityMintingCompleteDate => state.lastCityMintingCompleteDate = lastCityMintingCompleteDate);
              }
            }
          })
          .then(() => state.currentCity && tokenUtils.getCityCardTypes(state.xMeedContract, state.currentCity.id))
          .then(currentCardTypes => state.currentCardTypes = currentCardTypes || [])
          .catch(error => console.error('Error while loading current city', error))
          .finally(() => {
            if (reloadOnNotMintable && !state.currentCityMintable) {
              window.setTimeout(() => this.commit('loadCurrentCity', true), 1000);
            }
          });
      } else {
        state.currentCityMintable = false;
        state.currentCardTypes = [];
      }
    },
    loadComethRewardPool(state) {
      if (state.comethPool) {
        return;
      }
      const pool = {
        address: state.comethPairAddress,
        contract: new ethers.Contract(
          state.comethPairAddress,
          state.erc20ABI,
          state.polygonProvider
        ),
        symbol: 'UNI-V2',
        userInfo: {},
      };
      const tokenFactoryContract = new ethers.Contract(
        state.comethTokenFactoryAddress,
        state.polygonTokenFactoryABI,
        state.polygonProvider
      );
      const polygonMeedContract = new ethers.Contract(
        state.polygonMeedAddress,
        state.erc20ABI,
        state.polygonProvider
      );
      const promises = [];
      promises.push(
        polygonMeedContract.balanceOf(pool.address)
          .then(balance => pool.meedsBalance = balance)
      );
      promises.push(
        tokenFactoryContract.totalSupply()
          .then(balance => pool.lpBalanceOfTokenFactory = balance)
      );
      promises.push(
        pool.contract.totalSupply()
          .then(totalSupply => pool.totalSupply = totalSupply)
      );
      promises.push(
        tokenFactoryContract.rewardsDuration()
          .then(rewardsDuration => pool.rewardsDuration = rewardsDuration)
      );
      promises.push(
        tokenFactoryContract.getRewardsForDuration()
          .then(rewardsForDuration => pool.rewardsForDuration = rewardsForDuration && rewardsForDuration.length > 1 && rewardsForDuration[1] || new BigNumber(0))
      );
      if (state.address) {
        promises.push(
          tokenFactoryContract.balanceOf(state.address)
            .then(balance => pool.userInfo.amount = balance)
        );
        promises.push(
          pool.contract.balanceOf(state.address)
            .then(balance => pool.userInfo.lpBalance = balance)
        );
        promises.push(
          tokenFactoryContract.earned(state.address)
            .then(balances => pool.userInfo.meedsPendingUserReward = balances && balances.length > 1 && balances[1] || 0)
        );
      }
      Promise.all(promises)
        .then(() => {
          pool.yearlyRewardedMeeds = pool.rewardsForDuration.mul(state.yearInMinutes).mul(60).div(pool.rewardsDuration);
          pool.stakedEquivalentMeedsBalanceOfPool = pool.meedsBalance.mul(pool.lpBalanceOfTokenFactory).mul(2).div(pool.totalSupply);
          pool.apy = new BigNumber(pool.yearlyRewardedMeeds.toString()).dividedBy(pool.stakedEquivalentMeedsBalanceOfPool.toString()).multipliedBy(100);
        })
        .finally(() => state.comethPool = pool);
    },
    async loadOwnedNfts(state) {
      try {
        if (state.deedContract && state.xMeedContract) {
          await tokenUtils.getNftsOfWallet(state.deedContract, state.xMeedContract, state.address)
            .then(nfts => state.ownedNfts = nfts);
        } else {
          state.ownedNfts = [];
        }
      } finally {
        this.commit('deedLoaded');
      }
    },
    loadRewardedFunds(state, ignoreWhenLoaded, excludePools) {
      if (ignoreWhenLoaded && state.rewardedFunds) {
        // Avoid reloading only when necessary
        return;
      }
      if (state.tokenFactoryContract && !state.rewardedFundsLength) {
        state.tokenFactoryContract.fundsLength()
          .then(length => {
            state.rewardedFundsLength = length.toNumber();
            const promises = [];
            for (let i = 0; i < length; i++) {
              promises.push(
                state.tokenFactoryContract.fundAddresses(i)
                  .then(fundAddress => {
                    return state.tokenFactoryContract.fundInfos(fundAddress)
                      .then(fundInfo => {
                        const fund = {};
                        Object.keys(fundInfo).forEach(key => {
                          if (Number.isNaN(Number(key))) {
                            fund[key] = fundInfo[key];
                          }
                        });
                        return Object.assign({
                          index: i,
                          address: fundAddress,
                        }, fund || {});
                      });
                  })
              );
            }
            return Promise.all(promises);
          })
          .then(fundInfos => {
            state.rewardedFunds = fundInfos || [];
            const rewardedPools = state.rewardedFunds.filter(fund => fund.isLPToken);
            rewardedPools.forEach(pool => {
              pool.refresh = 0;
              pool.loading = true;
            });
            if (!excludePools) {
              state.rewardedPools = rewardedPools;
              return this.commit('loadLPTokenAssets', excludePools);
            }
          });
        state.tokenFactoryContract.totalAllocationPoints().then(value => state.rewardedTotalAllocationPoints = value);
        state.tokenFactoryContract.totalFixedPercentage().then(value => state.rewardedTotalFixedPercentage = value);
      }
    },
    loadLPTokenAssets(state) {
      state.rewardedPools.forEach(pool => {
        pool.loadingUserInfo = true;
        pool.contract = new ethers.Contract(
          pool.address,
          state.erc20ABI,
          state.provider
        );
        pool.contract.symbol()
          .then(symbol => pool.symbol = symbol);
        this.commit('loadLPTokenAsset', pool);
      });
      this.commit('lpLoaded');
    },
    loadLPTokenAsset(state, pool) {
      this.commit('refreshLPUserInfo', pool);
      this.commit('loadLPApy', pool);
    },
    refreshLPUserInfo(state, pool) {
      pool.loadingUserInfo = true;
      state.tokenFactoryContract.userLpInfos(pool.address, state.address)
        .then(userInfo => {
          const user = {};
          Object.keys(userInfo).forEach(key => {
            if (Number.isNaN(Number(key))) {
              user[key] = userInfo[key];
            }
          });
          return pool.contract.balanceOf(state.address)
            .then(balance => user.lpBalance = balance)
            .finally(() => pool.userInfo = user);
        })
        .finally(() => {
          pool.loadingUserInfo = false;
          state.poolsChanged++;
        });
    },
    loadLPApy(state, pool) {
      const promises = [];
      promises.push(
        state.meedContract.balanceOf(pool.address)
          .then(balance => pool.meedsBalance = balance)
      );
      promises.push(
        pool.contract.balanceOf(state.tokenFactoryAddress)
          .then(balance => pool.lpBalanceOfTokenFactory = balance)
      );
      promises.push(
        pool.contract.totalSupply()
          .then(totalSupply => pool.totalSupply = totalSupply)
      );
      Promise.all(promises)
        .then(() => this.commit('computeLPApy', pool))
        .finally(() => {
          pool.loading = false;
          // Force reloading list of pools after updated
          const index = state.rewardedPools.findIndex(tmpPool => tmpPool === pool);
          state.rewardedPools.splice(index, 1, pool);
        });
    },
    computeLPApy(state, pool) {
      pool.stakedEquivalentMeedsBalanceOfPool = !pool.totalSupply.isZero()
        && pool.meedsBalance.mul(pool.lpBalanceOfTokenFactory).mul(2).div(pool.totalSupply) || new BigNumber(0);
      if (pool.fixedPercentage && !pool.fixedPercentage.isZero()) {
        pool.yearlyRewardedMeeds = state.rewardedMeedPerMinute
          .multipliedBy(state.yearInMinutes)
          .multipliedBy(pool.fixedPercentage.toString())
          .dividedBy(100);
      } else if (pool.allocationPoint && !pool.allocationPoint.isZero()) {
        pool.yearlyRewardedMeeds = state.rewardedMeedPerMinute
          .multipliedBy(state.yearInMinutes)
          .multipliedBy(pool.allocationPoint.toString())
          .dividedBy(state.rewardedTotalAllocationPoints.toString())
          .dividedBy(100)
          .multipliedBy(100 - state.rewardedTotalFixedPercentage.toNumber());
      } else {
        pool.yearlyRewardedMeeds = new BigNumber(0);
      }

      if (state.noMeedSupplyForLPRemaining
          || pool.stakedEquivalentMeedsBalanceOfPool.isZero()
          || pool.yearlyRewardedMeeds.isZero()) {
        pool.apy = 0;
      } else {
        pool.apy = pool.yearlyRewardedMeeds.dividedBy(pool.stakedEquivalentMeedsBalanceOfPool.toString()).multipliedBy(100);
      }
      pool.refresh++;
    },
    async loadMeedsBalances(state) {
      try {
        if (state.xMeedContract) {
          state.loadingMeedsBalance = true;
          await state.meedContract.balanceOf(state.address)
            .then(balance => this.commit('setMeedsBalance', balance))
            .finally(() => state.loadingMeedsBalance = false);
        } else {
          this.commit('setXMeedsBalance', 0);
          state.loadingMeedsBalance = false;
        }
      } finally {
        this.commit('tokenLoaded');
      }
    },
    async loadPolygonBalances(state) {
      try {
        if (state.polygonMeedContract) {
          state.loadingPolygonMeedsBalance = true;
          await state.polygonMeedContract.balanceOf(state.address)
            .then(balance => this.commit('setPolygonMeedsBalance', balance))
            .finally(() => state.loadingPolygonMeedsBalance = false);
        } else {
          state.loadingPolygonMeedsBalance = false;
        }
      } finally {
        this.commit('tokenLoaded');
      }
    },
    async loadXMeedsBalances(state) {
      try {
        if (state.xMeedContract) {
          state.loadingXMeedsBalance = true;
          await state.xMeedContract.balanceOf(state.address)
            .then(balance => this.commit('setXMeedsBalance', balance))
            .finally(() => state.loadingXMeedsBalance = false);
        } else {
          this.commit('setXMeedsBalance', 0);
          state.loadingXMeedsBalance = false;
        }
      } finally {
        this.commit('tokenLoaded');
      }
    },
    async loadBalances(state) {
      if (state.address && state.provider) {
        state.loadingBalances = true;
        try {
          state.provider.getBalance(state.address).then(balance => state.etherBalance = balance);
          this.commit('loadMeedsBalances');
          this.commit('loadPolygonBalances');
          if (state.meedContract) {
            if (state.xMeedAddress) {
              await state.meedContract.balanceOf(state.xMeedAddress).then(balance => state.meedsBalanceOfXMeeds = balance);
              await state.meedContract.allowance(state.address, state.xMeedAddress).then(balance => state.meedsStakeAllowance = balance);
            } else {
              state.meedsBalanceOfXMeeds = 0;
              state.meedsStakeAllowance = 0;
            }
            await state.meedContract.allowance(state.address, state.sushiswapRouterAddress).then(balance => state.meedsRouteAllowance = balance);
            await state.meedContract.totalSupply().then(totalSupply => {
              state.meedsTotalSupply = totalSupply;
              state.maxMeedSupplyReached = totalSupply.gte(state.meedsMaxTotalSupply.toFixed(0));
              if (state.maxMeedSupplyReached) {
                state.meedContract.balanceOf(state.tokenFactoryAddress).then(balance => {
                  state.noMeedSupplyForLPRemaining = !balance || balance.isZero();
                });
              }
            });
          }
          this.commit('loadXMeedsBalances');
          if (state.xMeedContract) {
            await state.xMeedContract.totalSupply().then(totalSupply => state.xMeedsTotalSupply = totalSupply);
            if (state.tokenFactoryContract) {
              await state.tokenFactoryContract['pendingRewardBalanceOf(address)'](state.xMeedAddress)
                .then(balance => state.meedsPendingBalanceOfXMeeds = balance)
                .catch(() => state.meedsPendingBalanceOfXMeeds = 0);
            }
          } else {
            state.xMeedsTotalSupply = 0;
            state.meedsPendingBalanceOfXMeeds = 0;
          }
        } finally {
          state.loadingBalances = false;
          this.commit('tokenLoaded');
        }
      }
    },
    setProvider(state) {
      if (state.address && state.validNetwork) {
        state.provider = new ethers.providers.Web3Provider(window.ethereum);
        state.sushiswapRouterContract = new ethers.Contract(
          state.sushiswapRouterAddress,
          state.routerABI,
          state.provider
        );
        state.meedContract = new ethers.Contract(
          state.meedAddress,
          state.erc20ABI,
          state.provider
        );
        state.polygonMeedContract = new ethers.Contract(
          state.polygonMeedAddress,
          state.erc20ABI,
          state.polygonProvider
        );
        state.wethContract = new ethers.Contract(
          state.meedAddress,
          state.erc20ABI,
          state.provider
        );
        if (state.xMeedAddress) {
          state.xMeedContract = new ethers.Contract(
            state.xMeedAddress,
            state.xMeedRewardingABI,
            state.provider
          );
        }
        if (state.deedAddress) {
          state.deedContract = new ethers.Contract(
            state.deedAddress,
            state.nftABI,
            state.provider
          );
        }
        if (state.tokenFactoryAddress) {
          state.tokenFactoryContract = new ethers.Contract(
            state.tokenFactoryAddress,
            state.tokenFactoryABI,
            state.provider
          );
        }
        if (state.tenantProvisioningAddress) {
          state.tenantProvisioningContract = new ethers.Contract(
            state.tenantProvisioningAddress,
            state.tenantProvisioningABI,
            state.provider
          );
        }

        // eslint-disable-next-line new-cap
        const transferFilter = state.meedContract.filters.Transfer();
        state.meedContract.on(transferFilter, (from, to) => {
          const address = state.address.toUpperCase();
          if (from.toUpperCase() === address || to.toUpperCase() === address) {
            this.commit('loadBalances');
          }
        });

        // eslint-disable-next-line new-cap
        const approveFilter = state.meedContract.filters.Approval();
        state.meedContract.on(approveFilter, (from, to) => {
          const address = state.address.toUpperCase();
          if (from.toUpperCase() === address || to.toUpperCase() === address) {
            this.commit('loadBalances');
          }
        });

        if (state.xMeedContract) {
          // eslint-disable-next-line new-cap
          const redeemFilter = state.xMeedContract.filters.Redeemed();
          state.xMeedContract.on(redeemFilter, (address) => {
            if (address.toUpperCase() === state.address.toUpperCase()) {
              this.commit('loadOwnedNfts');
              this.commit('loadPointsBalance');
            }
            this.commit('loadCurrentCity');
          });
        }

        this.commit('loadBalances');
        this.commit('loadGasPrice');
        this.commit('loadOwnedNfts');
        this.commit('loadCurrentCity');
        this.commit('loadPointsBalance');
        this.commit('loadPointsPeriodically');
        state.provider.lookupAddress(state.address)
          .then(name => state.ens = name);
      } else {
        this.commit('setMetamaskOffline');
      }
    },
    loadGasPrice(state) {
      if (state.provider) {
        state.provider.getGasPrice().then(gasPrice => {
          state.gasPriceGwei = gasPrice && ethers.utils.formatUnits(gasPrice, 'gwei') || 0;
          state.gasPrice = gasPrice && gasPrice.toNumber() || 0;
          state.transactionGas = gasPrice.mul(state.maxGasLimit);
        });
      }
    },
    loadMeedPrice(state) {
      const today = new Date().toISOString().substring(0, 10);
      exchange.getMeedsExchange(today, state.selectedFiatCurrency)
        .then(result => {
          if (result && result.length && result[0].currencyPrice) {
            this.commit('setMeedPrice', new BigNumber(result[0].currencyPrice));
          }
        });
    },
    setMetamaskOffline(state) {
      if (!state.metamaskOffline) {
        state.metamaskOffline = true;
        state.polygonMeedContract = new ethers.Contract(
          state.polygonMeedAddress,
          state.erc20ABI,
          state.polygonProvider
        );
        assetMetricService.getMetrics()
          .then(metrics => {
            if (!metrics) {
              return;
            }
            state.rewardedTotalAllocationPoints = ethers.BigNumber.from(metrics.totalAllocationPoints);
            state.rewardedTotalFixedPercentage = ethers.BigNumber.from(metrics.totalFixedPercentage);
            const xMeedFund = metrics.pools.find(pool => !pool.isLPToken);
            if (xMeedFund) {
              state.xMeedsTotalSupply = ethers.BigNumber.from(xMeedFund.totalSupply);
              state.meedsPendingBalanceOfXMeeds = ethers.BigNumber.from(xMeedFund.xmeedPendingReward);
              state.meedsBalanceOfXMeeds = ethers.BigNumber.from(xMeedFund.meedsBalance);
              state.xMeedAddress = xMeedFund.address;
            }
            state.rewardedFunds = metrics.pools.map((fundInfo, i) => {
              const fund = {};
              Object.keys(fundInfo).forEach(key => {
                if (Number.isNaN(Number(key))) {
                  if (fundInfo[key] && fundInfo[key] !== true && String(fundInfo[key]).indexOf('0x') < 0 && Number.isInteger(Number(fundInfo[key]))) {
                    fund[key] = ethers.BigNumber.from(fundInfo[key]);
                  } else {
                    fund[key] = fundInfo[key];
                  }
                }
              });
              fund.index = i;
              return fund;
            });
            const rewardedPools = state.rewardedFunds.filter(fund => fund.isLPToken);
            rewardedPools.forEach(pool => this.commit('computeLPApy', pool));
            state.rewardedPools = rewardedPools;

            Object.keys(metrics.currentCity).forEach(key => {
              if (Number.isNaN(Number(key))) {
                if (metrics.currentCity[key] && metrics.currentCity[key] !== true && String(metrics.currentCity[key]).indexOf('0x') < 0 && Number.isInteger(Number(metrics.currentCity[key]))) {
                  metrics.currentCity[key] = ethers.BigNumber.from(metrics.currentCity[key]);
                }
              }
            });
            state.currentCity = metrics.currentCity;
          })
          .finally(() => {
            state.appLoading = false;
            state.deedLoading = false;
            state.tokenLoading = false;
            state.lpLoading = false;
          });
      }
    },
    loaded(state) {
      state.appLoading = false;
    },
    deedLoaded(state) {
      state.deedLoading = false;
    },
    tokenLoaded(state) {
      state.tokenLoading = state.loadingBalances
        || state.loadingXMeedsBalance
        || state.loadingMeedsBalance
        || state.loadingPolygonMeedsBalance;
    },
    lpLoaded(state) {
      state.lpLoading = false;
    },
    endTimer(state) {
      state.refreshNowIntervalUsage--;
      if (state.refreshNowInterval && state.refreshNowIntervalUsage <= 0) {
        window.clearInterval(state.refreshNowInterval);
        state.refreshNowInterval = null;
      }
    },
    startTimer(state) {
      if (!state.refreshNowInterval) {
        state.refreshNowInterval = window.setInterval(() => {
          state.now = Date.now();
        }, 1000);
        state.refreshNowIntervalUsage = 1;
      } else {
        state.refreshNowIntervalUsage++;
      }
    },
    selectFiatCurrency(state, fiatCurrency) {
      state.selectedFiatCurrency = fiatCurrency;
      localStorage.setItem('deeds-selectedFiatCurrency', state.selectedFiatCurrency);

      this.commit('loadMeedPrice');
    },
    refreshMetamaskState() {
      this.commit('setMetamaskInstalled');
      this.commit('setMetamaskConnected');
      this.commit('setNetworkId');
    },
  }
});


function initialize() {
  if (ethUtils.isMetamaskInstalled()) {
    store.commit('refreshMetamaskState');

    if (window.ethereum._metamask && window.ethereum._metamask.isUnlocked) {
      window.ethereum._metamask.isUnlocked()
        .then(unlocked => {
          if (!unlocked) {
            store.commit('loaded');
          }
        });
    }

    window.ethereum.on('connect', () => store.commit('refreshMetamaskState'));
    window.ethereum.on('disconnect', () => store.commit('refreshMetamaskState'));
    window.ethereum.on('accountsChanged', () => {
      if (authentication.hasAuthenticatedLogin()) {
        authentication.logout().finally(() => window.location.reload());
      } else {
        window.location.reload();
      }
    });
    window.ethereum.on('chainChanged', () => window.location.reload());
  
    window.setInterval(() => store.commit('loadGasPrice'), 12000);
  } else {
    store.commit('setMetamaskOffline');

    window.addEventListener('ethereum#initialized', initialize, {
      once: true,
    });

    // If the event is not dispatched by the end of the timeout,
    // the user probably doesn't have MetaMask installed.
    setTimeout(initialize, 3000); // 3 seconds
  }
}

initialize();

const buildNumber = document.getElementsByTagName('meta').version.getAttribute('content');
let app = null;

function initializeVueApp(language) {
  fetch(`/${window.parentAppLocation}/static/i18n/messages_${language}.properties?_=${buildNumber}`)
    .then(resp => resp && resp.ok && resp.text())
    .then(i18nMessages => {
      const data = i18nMessages
        .split('\n')
        .filter(Boolean)
        .reduce((obj, line) => {
          const pair = line.split(/=(.*)/s);
          obj[pair[0]] = pair[1].replace( /\\u([a-fA-F0-9]{4})/g, (g, m1) => String.fromCharCode(parseInt(m1, 16)));
          return obj;
        }, {});

      i18n.mergeLocaleMessage(language, data);
      if (!app) {
        app = new Vue({
          el: '#deedsApp',
          template: '<deeds-site id="deedsApp" />',
          store,
          i18n,
          vuetify: new Vuetify({
            dark: true,
            silent: true,
            iconfont: 'mdi',
            theme: { disable: true },
          }),
        });
      }
    });
}

initializeVueApp(language);