/*
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2020 - 2021 Meeds Association contact@meeds.io
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
import i18nMessages from './json/i18nMessages.json';
import * as ethUtils from './js/ethUtils.js';
import * as tokenUtils from './js/tokenUtils.js';
import * as exchange from './js/exchange.js';
import * as authentication from './js/authentication.js';
import * as tenantManagement from './js/tenantManagement.js';
import * as deedMetadata from './js/deedMetadata.js';

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

Vue.use(Vuex);
Vue.use(Vuetify);

const language = localStorage.getItem('deeds-selectedLanguage') || (navigator.language.indexOf('fr') === 0 ? 'fr' : 'en');
const i18n = new VueI18n({
  locale: language,
  fallbackLocale: 'en',
  messages: i18nMessages,
});
const selectedFiatCurrency = localStorage.getItem('deeds-selectedFiatCurrency') || 'usd';
const isMetamaskInstalled = ethUtils.isMetamaskInstalled();
const isMetamaskConnected = ethUtils.isMetamaskConnected();

window.parentAppLocation = window.location.pathname.split('/')[1];

const store = new Vuex.Store({
  state: {
    parentLocation: window.parentAppLocation,
    appLoading: true,
    ens: null,
    address: null,
    networkId: null,
    validNetwork: false,
    etherscanBaseLink: null,
    openSeaBaseLink: null,
    openSeaCollectionLink: null,
    managedNetworkIds: [1, 4],
    provider: null,
    erc20ABI: [
      'event Transfer(address indexed from, address indexed to, uint256 value)',
      'event Approval(address indexed owner, address indexed spender, uint256 value)',
      'function allowance(address owner, address spender) external view returns (uint256)',
      'function approve(address spender, uint256 amount) external returns (bool)',
      'function balanceOf(address owner) view returns (uint256)',
      'function totalSupply() public view returns (uint256)',
      'function symbol() public view returns (string)',
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
    // Contracts addresses
    sushiswapRouterAddress: null,
    wethAddress: null,
    meedAddress: null,
    sushiswapPairAddress: null,
    univ2PairAddress: null,
    xMeedAddress: null,
    deedAddress: null,
    tokenFactoryAddress: null,
    tenantProvisioningAddress: null,
    // Contracts objects
    sushiswapRouterContract: null,
    wethContract: null,
    meedContract: null,
    xMeedContract: null,
    deedContract: null,
    tokenFactoryContract: null,
    tenantProvisioningContract: null,
    // User preferred language
    language,
    // Metamask status
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
    loadingMeedsBalance: true,
    meedsBalance: null,
    meedsBalanceNoDecimals: null,
    meedsRouteAllowance: null,
    meedsStakeAllowance: null,
    meedsTotalSupply: null,
    maxMeedSupplyReached: null,
    noMeedSupplyForLPRemaining: null,
    remainingMeedSupply: null,
    xMeedsTotalSupply: null,
    meedsBalanceOfXMeeds: null,
    meedsPendingBalanceOfXMeeds: null,
    farmingStartTime: 1648425600000, // Mar 28 2022 00:00:00 GMT
    stakingStartTime: 1648425600000, // Mar 28 2022 00:00:00 GMT
    deedGenesisStartTime: 1651363200000, // May 01 2022 00:00:00 GMT
    pointsRewardsStartTime: 1648425600000, // Mar 28 2022 00:00:00 GMT
    now: Date.now(),
    xMeedsBalance: null,
    loadingXMeedsBalance: true,
    xMeedsBalanceNoDecimals: null,
    pointsBalance: null,
    pointsBalanceNoDecimals: null,
    ownedNfts: null,
    selectedFiatCurrency,
    noCityLeft: false,
    currentCity: null,
    currentCardTypes: null,
    currentCityMintable: null,
    lastCityMintingCompleteDate: null,
    rewardedMeedPerMinute: null,
    rewardedFundsLength: null,
    rewardedFunds: null,
    rewardedTotalFixedPercentage: null,
    rewardedTotalAllocationPoints: null,
    addSushiswapLiquidityLink: null,
    addUniswapLiquidityLink: null,
  },
  mutations: {
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
          if (state.address && state.validNetwork) {
            this.commit('setProvider');
          }
        })
        .finally(() => this.commit('loaded'));
    },
    setNetworkId(state) {
      ethUtils.getSelectedChainId()
        .then(networkId => {
          state.networkId = new BigNumber(networkId).toNumber();
          if (state.managedNetworkIds.indexOf(state.networkId) >= 0) {
            if (state.networkId === 1) {
              state.validNetwork = true;

              // Mainnet
              state.etherscanBaseLink = 'https://etherscan.io/';
              state.sushiswapRouterAddress = '0xd9e1cE17f2641f24aE83637ab66a2cca9C378B9F';
              state.sushiswapPairAddress = '0x960bd61d0b960b107ff5309a2dcced4705567070';
              state.wethAddress = '0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2';
              state.meedAddress = '0x8503a7b00b4b52692cc6c14e5b96f142e30547b7';
              state.tokenFactoryAddress = '0x1B37D04759aD542640Cc44Ff849a373040386050';

              state.xMeedAddress = '0x44d6d6ab50401dd846336e9c706a492f06e1bcd4';
              state.deedAddress = '0x0143b71443650aa8efa76bd82f35c22ebd558090';
              state.tenantProvisioningAddress = null;

              // TODO replace with real addresses
              // state.univ2PairAddress = '0x1ba26c3a4ba059660149a43f69c49230f134dbc2';

              state.addSushiswapLiquidityLink = `https://app.sushi.com/add/ETH/${state.meedAddress}`;
              // state.addUniswapLiquidityLink = `https://app.uniswap.org/#/add/v2/ETH/${state.meedAddress}`;

              state.openSeaCollectionLink = 'https://opensea.io/collection/meeds-dao';
              state.openSeaBaseLink = `https://opensea.io/assets/${state.deedAddress}`;
            } else if (state.networkId === 4) {
              state.validNetwork = true;

              // Rinkeby
              state.etherscanBaseLink = 'https://rinkeby.etherscan.io/';
              state.sushiswapRouterAddress = '0x1b02dA8Cb0d097eB8D57A175b88c7D8b47997506';
              state.sushiswapPairAddress = '0xccbaed81a22663c18602ec23f790d4ecee843b46';
              state.wethAddress = '0xc778417e063141139fce010982780140aa0cd5ab';
              state.meedAddress = '0xe25aD27222D84662D7484363B4c25da123A1cB20';
              state.tokenFactoryAddress = '0xab87e14c13C37039f14e754beFDB77f679E2C8C0';

              state.xMeedAddress = '0x664d1d851ea235f03Df14BfDa0B9f185fb0F94E3';
              state.deedAddress = '0xDa91F50491AB8b3C82b43623fC317d9fF5207D9c';
              state.tenantProvisioningAddress = '0x8b5B554985c4393eF30FDf33146A22a99A984C69';

              state.univ2PairAddress = '0x24c6839a9db67c28ae9f493e4034d6ce82c571d6';

              state.addSushiswapLiquidityLink = `https://app.sushi.com/add/ETH/${state.meedAddress}`;
              state.addUniswapLiquidityLink = `https://app.uniswap.org/#/add/v2/ETH/${state.meedAddress}`;

              state.openSeaBaseLink = `https://testnets.opensea.io/assets/rinkeby/${state.deedAddress}`;
              state.openSeaCollectionLink = 'https://testnets.opensea.io/collection/meeds-deed-token-znroiz3lxb';

              state.farmingStartTime = 0;
              state.stakingStartTime = 0;
              state.pointsRewardsStartTime = 0;
              state.deedGenesisStartTime = 0;
            }
            state.addComethLiquidityLink = 'https://swap.cometh.io/#/add/ETH/0x6acA77CF3BaB0C4E8210A09B57B07854a995289a';
            state.rentComethLiquidityLink = 'https://swap.cometh.io/#/stake/0x6acA77CF3BaB0C4E8210A09B57B07854a995289a/ETH/0x035A8a07Bbae988893499e5c0D5b281b7967b107';
          }
          this.commit('setAddress');
        });
    },
    selectLanguage(state, language) {
      state.language = language;
      i18n.locale = language.indexOf('fr') === 0 ? 'fr' : 'en';
      localStorage.setItem('deeds-selectedLanguage', state.language);
      state.meedsBalanceNoDecimals = ethUtils.computeTokenBalanceNoDecimals(state.meedsBalance, 3, state.language);
      state.xMeedsBalanceNoDecimals = ethUtils.computeTokenBalanceNoDecimals(state.xMeedsBalance, 3, state.language);
      state.pointsBalanceNoDecimals = ethUtils.computeTokenBalanceNoDecimals(state.pointsBalance, 3, state.language);
    },
    setEtherBalance(state, etherBalance) {
      state.etherBalance = etherBalance;
    },
    setMeedPrice(state, meedPrice) {
      state.meedPrice = meedPrice;
    },
    setMeedsBalance(state, meedsBalance) {
      state.meedsBalance = meedsBalance;
      state.meedsBalanceNoDecimals = ethUtils.computeTokenBalanceNoDecimals(state.meedsBalance, 3, state.language);
    },
    setMeedsRouteAllowance(state, meedsRouteAllowance) {
      state.meedsRouteAllowance = meedsRouteAllowance;
    },
    setXMeedsBalance(state, xMeedsBalance) {
      state.xMeedsBalance = xMeedsBalance;
      state.xMeedsBalanceNoDecimals = ethUtils.computeTokenBalanceNoDecimals(state.xMeedsBalance, 3, state.language);
    },
    loadPointsPeriodically() {
      window.setInterval(() => this.commit('loadPointsBalance'), 20000);
    },
    loadPointsBalance(state) {
      if (state.xMeedContract) {
        tokenUtils.getPointsBalance(state.xMeedContract, state.address)
          .then(balance => {
            state.pointsBalance = balance;
            state.pointsBalanceNoDecimals = ethUtils.computeTokenBalanceNoDecimals(state.pointsBalance, 3, state.language);
          });
      } else {
        state.pointsBalance = 0;
        state.pointsBalanceNoDecimals = 0;
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
    loadOwnedNfts(state) {
      if (state.deedContract && state.xMeedContract) {
        tokenUtils.getNftsOfWallet(state.deedContract, state.xMeedContract, state.address)
          .then(nfts => state.ownedNfts = nfts);
      } else {
        state.ownedNfts = [];
      }
    },
    loadRewardedFunds(state) {
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
          .then(fundInfos => state.rewardedFunds = fundInfos || []);
        state.tokenFactoryContract.totalAllocationPoints().then(value => state.rewardedTotalAllocationPoints = value);
        state.tokenFactoryContract.totalFixedPercentage().then(value => state.rewardedTotalFixedPercentage = value);
      }
    },
    loadMeedsBalances(state) {
      state.loadingMeedsBalance = true;
      state.meedContract.balanceOf(state.address)
        .then(balance => this.commit('setMeedsBalance', balance))
        .finally(() => state.loadingMeedsBalance = false);
    },
    loadXMeedsBalances(state) {
      if (state.xMeedContract) {
        state.loadingXMeedsBalance = true;
        state.xMeedContract.balanceOf(state.address)
          .then(balance => this.commit('setXMeedsBalance', balance))
          .finally(() => state.loadingXMeedsBalance = false);
      } else {
        this.commit('setXMeedsBalance', 0);
        state.loadingXMeedsBalance = false;
      }
    },
    loadBalances(state) {
      if (state.address && state.provider) {
        state.provider.getBalance(state.address).then(balance => state.etherBalance = balance);
        if (state.meedContract) {
          this.commit('loadMeedsBalances');
          if (state.xMeedAddress) {
            state.meedContract.balanceOf(state.xMeedAddress).then(balance => state.meedsBalanceOfXMeeds = balance);
            state.meedContract.allowance(state.address, state.xMeedAddress).then(balance => state.meedsStakeAllowance = balance);
          } else {
            state.meedsBalanceOfXMeeds = 0;
            state.meedsStakeAllowance = 0;
          }
          state.meedContract.allowance(state.address, state.sushiswapRouterAddress).then(balance => state.meedsRouteAllowance = balance);
          state.meedContract.totalSupply().then(totalSupply => {
            state.meedsTotalSupply = totalSupply;
            state.maxMeedSupplyReached = totalSupply.gte('100000000000000000000000000');
            if (totalSupply.gte('100000000000000000000000000')) {
              state.meedContract.balanceOf(state.tokenFactoryAddress).then(balance => {
                state.noMeedSupplyForLPRemaining = !balance || balance.isZero();
              });
            }
          });
        }
        this.commit('loadXMeedsBalances');
        if (state.xMeedContract) {
          state.xMeedContract.totalSupply().then(totalSupply => state.xMeedsTotalSupply = totalSupply);
          if (state.tokenFactoryContract) {
            state.tokenFactoryContract['pendingRewardBalanceOf(address)'](state.xMeedAddress)
              .then(balance => state.meedsPendingBalanceOfXMeeds = balance)
              .catch(() => state.meedsPendingBalanceOfXMeeds = 0);
          }
        } else {
          state.xMeedsTotalSupply = 0;
          state.meedsPendingBalanceOfXMeeds = 0;
        }
      }
    },
    setProvider(state) {
      if (state.address) {
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

        if (state.xMeedContract && (state.deedGenesisStartTime - Date.now() < 10000)) {
          state.xMeedContract.startRewardsTime().then(timestamp => {
            state.pointsRewardsStartTime = timestamp * 1000;
          });
        }
        if (state.tokenFactoryContract) {
          state.tokenFactoryContract.meedPerMinute().then(balance => state.rewardedMeedPerMinute = balance);
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
    loaded(state) {
      state.appLoading = false;
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
      window.ethereum._metamask.isUnlocked().then(unlocked => {
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
    store.commit('loaded');

    window.addEventListener('ethereum#initialized', initialize, {
      once: true,
    });

    // If the event is not dispatched by the end of the timeout,
    // the user probably doesn't have MetaMask installed.
    setTimeout(initialize, 3000); // 3 seconds
  }
}

initialize();

new Vue({
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