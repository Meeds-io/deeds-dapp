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
import './initComponents-dapp';
import * as utils from './js/utils.js';
import * as ethUtils from './js/ethUtils.js';
import * as tokenUtils from './js/tokenUtils.js';
import * as exchange from './js/exchange.js';
import * as authentication from './js/authentication.js';
import * as authorizationCodeService from './js/authorizationCodeService.js';
import * as userProfileService from './js/userProfileService.js';
import * as tenantManagement from './js/tenantManagement.js';
import * as deedMetadata from './js/deedMetadata.js';
import * as tokenMetricService from './js/tokenMetricService.js';
import * as assetMetricService from './js/assetMetricService.js';
import * as deedTenantOfferService from './js/deedTenantOfferService.js';
import * as deedTenantLeaseService from './js/deedTenantLeaseService.js';

window.Object.defineProperty(Vue.prototype, '$utils', {
  value: utils,
});

window.Object.defineProperty(Vue.prototype, '$ethUtils', {
  value: ethUtils,
});

window.Object.defineProperty(Vue.prototype, '$exchange', {
  value: exchange,
});

window.Object.defineProperty(Vue.prototype, '$authentication', {
  value: authentication,
});

window.Object.defineProperty(Vue.prototype, '$authorizationCodeService', {
  value: authorizationCodeService,
});

window.Object.defineProperty(Vue.prototype, '$userProfileService', {
  value: userProfileService,
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

window.Object.defineProperty(Vue.prototype, '$deedTenantOfferService', {
  value: deedTenantOfferService,
});

window.Object.defineProperty(Vue.prototype, '$deedTenantLeaseService', {
  value: deedTenantLeaseService,
});

Vue.use(Vuex);
Vue.use(Vuetify);

const page = document.querySelector('[name=pageName]').value;

const buildNumber = document.getElementsByTagName('meta').version.getAttribute('content');
const themePreference = window.localStorage.getItem('meeds-preferred-theme-colors') || 'system';
const systemThemeDark = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)')?.matches || false;
const dark = (systemThemeDark && themePreference === 'system') || themePreference === 'dark';
const vuetify = new Vuetify({
  icons: {
    iconfont: 'fa',
  },
  theme: {
    dark,
    disable: true,
    themes: {
      light: {
        primary: '#3f8487',
        secondary: '#e25d5d',
        info: '#476a9c',
        error: '#bc4343',
        warning: '#ffb441',
        success: '#2eb58c',
      },
      dark: {
        primary: '#3f8487',
        secondary: '#e25d5d',
        info: '#476a9c',
        error: '#bc4343',
        warning: '#ffb441',
        success: '#2eb58c',
      },
    },
  },
});

function getLanguage() {
  return document.documentElement.lang || 'en';
}

const language = getLanguage();

const selectedFiatCurrency = localStorage.getItem('deeds-selectedFiatCurrency') || 'usd';
const isMetamaskInstalled = ethUtils.isMetamaskInstalled();
const isMetamaskConnected = ethUtils.isMetamaskConnected();

window.parentAppLocation = window.location.pathname.split('/')[1];
if (window.parentAppLocation.length && (window.parentAppLocation === 'dapp' || window.parentAppLocation === 'deeds-dapp')) {
  window.parentAppLocation = `/${window.parentAppLocation}`;
} else {
  window.parentAppLocation = '';
}

const i18n = new VueI18n({
  locale: language,
  messages: {},
});

const networkSettings = {
  1: {
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
    vestingAddress: '0x440701ca5817b5847438da2ec2ca3b9fdbf37dfa',
    univ2PairAddress: null,
    tenantProvisioningAddress: '0x49C0cF46C0Eb6FdF05A4E8C1FE344d510422E1F0',
    tenantRentingAddress: '0x427aa8F31013960E0E5e73977c1918e15d693BAa',
    // External links
    etherscanBaseLink: 'https://etherscan.io/',
    polygonscanBaseLink: 'https://polygonscan.com/',
    openSeaBaseLink: 'https://opensea.io/assets/ethereum/0x0143b71443650aa8efa76bd82f35c22ebd558090',
    openSeaCollectionLink: 'https://opensea.io/collection/meeds-dao',
    whitepaperLink: 'https://mirror.xyz/meedsdao.eth/EDh9QfsuuIDNS0yKcQDtGdXc25vfkpnnKpc3RYUTJgc',
    defaultDateFormat: {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    },
  },
};

// Goerli
networkSettings[5] = {
  sushiswapRouterAddress: '0x1b02dA8Cb0d097eB8D57A175b88c7D8b47997506',
  sushiswapPairAddress: '0x131Bd5b643Bc12EFb9A4F23512BbA5e1ef3F33bD',
  wethAddress: '0xB4FBF271143F4FBf7B91A5ded31805e42b2208d6',
  meedAddress: '0x4998a63C7494afc4Ea96E7Ea86E88c59271914c1',
  tokenFactoryAddress: '0x13142F102152aBa8AD81281E7eC1374577D662EC',
  xMeedAddress: '0xee5BBf589577266e5ddee2CfB4acFB945e844079',
  deedAddress: '0x01ab6ab1621b5853Ad6F959f6b7df6A369fbd346',
  tenantProvisioningAddress: '0x238758516d1521a4aE108966104Aa1C5cC088220',
  tenantRentingAddress: '0x1d26cB4Cae533a721c4dA576C9Bd7b702c5e2fd8',
  etherscanBaseLink: 'https://goerli.etherscan.io/',
  // Opensea links
  openSeaBaseLink: 'https://testnets.opensea.io/assets/goerli/0x01ab6ab1621b5853Ad6F959f6b7df6A369fbd346',
  openSeaCollectionLink: 'https://testnets.opensea.io/collection/meeds-dao-testnet',
  // TestNet Date Format
  defaultDateFormat: {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: 'numeric',
    minute: 'numeric',
  },
};

const blockchainAddressAndNetworkState = {
  metamaskOffline: false,
  appLoading: true,
  deedLoading: true,
  tokenLoading: true,
  lpLoading: true,
  loadingBalances: true,
  loadingXMeedsBalance: true,
  loadingMeedsBalance: true,
  ens: null,
  MONTH_IN_SECONDS: 2629800,
  DAY_IN_SECONDS: 86400,
  // Contracts objects
  sushiswapRouterContract: null,
  wethContract: null,
  meedContract: null,
  xMeedContract: null,
  deedContract: null,
  tokenFactoryContract: null,
  tenantProvisioningContract: null,
  provisioningListenersInstalled: false,
  tenantRentingContract: null,
  tenantRentingContractListenersInstalled: false,
  polygonMeedContract: null,
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
  maxMeedSupplyReached: null,
  noMeedSupplyForLPRemaining: null,
  remainingMeedSupply: null,
  xMeedsTotalSupply: null,
  meedsBalanceOfXMeeds: null,
  meedsPendingBalanceOfXMeeds: null,
  xMeedsBalance: null,
  pointsBalance: null,
  ownedNfts: [],
  noCityLeft: false,
  currentCity: null,
  currentCardTypes: null,
  currentCityMintable: null,
  lastCityMintingCompleteDate: null,
  rewardedFundsLength: null,
  rewardedFunds: null,
  rewardedPools: null,
  rewardedTotalFixedPercentage: null,
  rewardedTotalAllocationPoints: null,
  poolsChanged: 2,
  selectedOfferId: null,
  selectedStandaloneOfferId: null,
  selectedStandaloneDeedCardName: null,
};

const pageUriPerLanguages = {
  en: {
    pages: [
      '',
      'marketplace',
      'portfolio',
      'tour',
      'whitepaper',
      'tokenomics',
      'about-us',
      'deeds',
      'legals',
      'stake',
      'owners',
      'farm',
      'tenants',
      'hubs'
    ],
    uriPrefix: '',
  },
  fr: {
    pages: [
      'fr',
      'place-de-marche',
      'portefeuille',
      'visite-guidee',
      'livre-blanc',
      'tokenomics',
      'qui-sommes-nous',
      'deeds',
      'mentions-legales',
      'rejoindre-dao',
      'proprietaires',
      'farm',
      'locataires',
      'rejoindre-hubs'
    ],
    uriPrefix: 'fr/',
  },
};

const store = new Vuex.Store({
  state: {
    ...networkSettings[1],
    ...blockchainAddressAndNetworkState,
    pageUriPerLanguages,
    page,
    buildNumber,
    parentLocation: window.parentAppLocation,
    addComethLiquidityLink: 'https://swap.cometh.io/#/add/ETH/0x6acA77CF3BaB0C4E8210A09B57B07854a995289a',
    rentComethLiquidityLink: 'https://swap.cometh.io/#/stake/0x6acA77CF3BaB0C4E8210A09B57B07854a995289a/ETH/0x035A8a07Bbae988893499e5c0D5b281b7967b107',
    browseOffersVideoLink: 'https://res.cloudinary.com/dcooc6vig/video/upload/f_auto,q_auto/v1681397088/meedsdao-site/assets/video/rent%20from%20marketplace.mp4',
    beTenantVideoLink: 'https://res.cloudinary.com/dcooc6vig/video/upload/f_auto,q_auto/v1681397113/meedsdao-site/assets/video/manage%20your%20hubs.mp4',
    mintDeedVideoLink: 'https://res.cloudinary.com/dcooc6vig/video/upload/f_auto,q_auto/v1681397143/meedsdao-site/assets/video/Stake%20and%20Mint.mp4',
    rentDeedVideoLink: 'https://res.cloudinary.com/dcooc6vig/video/upload/f_auto,q_auto/v1681397162/meedsdao-site/assets/video/Manage%20your%20Deed.mp4',
    echartsLoaded: false,
    address: null,
    networkId: null,
    validNetwork: false,
    yearInMinutes: 365 * 24 * 60,
    scrollbarWidth: utils.getScrollbarWidth(),
    cities: ['TANIT', 'RESHEF', 'ASHTARTE', 'MELQART', 'ESHMUN', 'KUSHOR', 'HAMMON'],
    cardTypes: ['COMMON', 'UNCOMMON', 'RARE', 'LEGENDARY'],
    offerTypes: ['RENTING', 'SALE'],
    cardTypeInfos: {},
    // Contract variables
    comethPool: null,
    provider: null,
    polygonProvider: null,
    pollingInterval: 12000, // 12 seconds
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
      'event TenantStarted(address indexed manager, uint256 indexed nftId)',
      'event TenantStopped(address indexed manager, uint256 indexed nftId)',
      'function startTenant(uint256 _nftId) external',
      'function stopTenant(uint256 _nftId) external',
      'function delegatee(uint256 _nftId) public view returns(address)',
      'function tenantStatus(uint256 _nftId) public view returns(bool)',
      'function isProvisioningManager(address _address, uint256 _nftId) public view returns(bool)',
    ],
    tenantRentingABI: [
      `function createOffer((
          uint256 id,
          uint256 deedId,
          address creator,
          uint16 months,
          uint8 noticePeriod,
          uint256 price,
          uint256 allDurationPrice,
          uint256 offerStartDate,
          uint256 offerExpirationDate,
          uint8 offerExpirationDays,
          address authorizedTenant,
          uint8 ownerMintingPercentage
      ))`,
      `function updateOffer((
          uint256 id,
          uint256 deedId,
          address creator,
          uint16 months,
          uint8 noticePeriod,
          uint256 price,
          uint256 allDurationPrice,
          uint256 offerStartDate,
          uint256 offerExpirationDate,
          uint8 offerExpirationDays,
          address authorizedTenant,
          uint8 ownerMintingPercentage
      ))`,
      'event OfferCreated(uint256 indexed id, uint256 indexed deedId, address owner)',
      'event OfferUpdated(uint256 indexed id, uint256 indexed deedId, address owner)',
      'event OfferDeleted(uint256 indexed id, uint256 indexed deedId, address owner)',
      'event RentPaid(uint256 indexed id, uint256 indexed deedId, address tenant, address owner, uint16 paidMonths, bool firstRent)',
      'event LeaseEnded(uint256 indexed id, uint256 indexed deedId, address tenant, uint16 leaseRemainingMonths)',
      'event TenantEvicted(uint256 indexed id, uint256 indexed deedId, address tenant, address owner, uint16 paidMonths, uint16 leaseRemainingMonths)',
      'function deleteOffer(uint256 _id)',
      'function acquireRent(uint256 _id, uint8 _monthsToPay)',
      'function payRent(uint256 _id, address _deedOwner, uint8 _monthsToPay)',
      'function endLease(uint256 _id)',
      'function evictTenant(uint256 _id)',
      'function MONTH_IN_SECONDS() public view returns(uint256)',
      'function DAY_IN_SECONDS() public view returns(uint256)'
    ],
    ZERO_X_ADDRESS: '0x0000000000000000000000000000000000000000',
    ZERO_BN: ethers.BigNumber.from('0'),
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
    meedsMaxTotalSupply: new BigNumber('100000000000000000000000000'),
    now: Date.now(),
    selectedFiatCurrency,
    rewardedMeedPerMinute: new BigNumber('10000000000000000000'),
    isMobile: false,
    authenticated: false,
    dark,
    systemThemeDark,
    themePreference,
    blackThemeColor: dark && 'white' || 'black',
    whiteThemeColor: dark && 'dark-color' || 'white',
    openedDrawersCount: 0,
    marketplaceURL: `${window.parentAppLocation}/${language === 'fr' ? 'fr/place-de-marche' : 'marketplace'}`,
    portfolioURL: `${window.parentAppLocation}/${language === 'fr' ? 'fr/portefeuille' : 'portfolio'}`,
    tourURL: `${window.parentAppLocation}/${language === 'fr' ? 'fr/visite-guidee' : 'tour'}`,
    whitepaperURL: `${window.parentAppLocation}/${language === 'fr' ? 'fr/livre-blanc' : 'whitepaper'}`,
    tokenomicsURL: `${window.parentAppLocation}/${language === 'fr' ? 'fr/tokenomics' : 'tokenomics'}`,
    deedsURL: `${window.parentAppLocation}/${language === 'fr' ? 'fr/deeds' : 'deeds'}`,
    aboutUsURL: `${window.parentAppLocation}/${language === 'fr' ? 'fr/qui-sommes-nous' : 'about-us'}`,
    legalsURL: `${window.parentAppLocation}/${language === 'fr' ? 'fr/mentions-legales' : 'legals'}`,
    stakeURL: `${window.parentAppLocation}/${language === 'fr' ? 'fr/rejoindre-dao' : 'stake'}`,
    ownersURL: `${window.parentAppLocation}/${language === 'fr' ? 'fr/proprietaires' : 'owners'}`,
    farmURL: `${window.parentAppLocation}/${language === 'fr' ? 'fr/farm' : 'farm'}`,
    tenantsURL: `${window.parentAppLocation}/${language === 'fr' ? 'fr/locataires' : 'tenants'}`,
    hubsUrl: `${window.parentAppLocation}/${language === 'fr' ? 'fr/rejoindre-hubs' : 'hubs'}`,
  },
  mutations: {
    echartsLoaded(state) {
      state.echartsLoaded = true;
    },
    incrementOpenedDrawer(state) {
      state.openedDrawersCount++;
    },
    decrementOpenedDrawer(state) {
      state.openedDrawersCount--;
    },
    setStandaloneOfferId(state, value) {
      state.selectedOfferId = null;
      state.selectedStandaloneOfferId = value;
    },
    setStandaloneDeedCardName(state, value) {
      state.selectedStandaloneDeedCardName = value;
    },
    refreshAuthentication(state) {
      const authenticated = state.address && authentication.isAuthenticated(state.address);
      this.commit('setAuthenticated', authenticated);
      if (!authenticated && authentication.hasAuthenticatedLogin()) {
        authentication.logout();
      }
    },
    setAuthenticated(state, value) {
      state.authenticated = value;
    },
    setMobile(state, value) {
      state.isMobile = value;
    },
    setDark(state, value) {
      state.dark = value;
      vuetify.framework.theme.dark = state.dark;
      state.blackThemeColor = state.dark && 'white' || 'black';
      state.whiteThemeColor = state.dark && 'dark-color' || 'white';
    },
    setThemePreference(state, value) {
      const isSystemTheme = value === 'system';
      const isDark = isSystemTheme ? systemThemeDark : value === 'dark';
      this.commit('setDark', isDark);
      state.themePreference = value;
      if (isSystemTheme) {
        window.localStorage.removeItem('meeds-preferred-theme-colors');
      } else {
        window.localStorage.setItem('meeds-preferred-theme-colors', value);
      }
    },
    setMetamaskInstalled(state) {
      state.isMetamaskInstalled = ethUtils.isMetamaskInstalled();
    },
    setMetamaskConnected(state) {
      state.isMetamaskConnected = ethUtils.isMetamaskConnected();
    },
    setAddress(state, networkChanged) {
      if (state.validNetwork) {
        ethUtils.getSelectedAddress()
          .then(addresses => {
            const address = addresses && addresses.length && addresses[0] || null;
            const addressChanged = address !== state.address;
            if (addressChanged) {
              state.address = address;
              this.commit('refreshAuthentication');
            }
            if (addressChanged || networkChanged) {
              this.commit('initBlockchainState');
              this.commit('setProvider');
            }
          })
          .finally(() => this.commit('loaded'));
      } else {
        this.commit('loaded');
      }
    },
    initBlockchainState(state) {
      Object.keys(blockchainAddressAndNetworkState).forEach(key => {
        state[key] = blockchainAddressAndNetworkState[key];
      });
    },
    setNetworkId(state) {
      ethUtils.getSelectedChainId()
        .then(networkId => {
          const previousNetworkId = state.networkId;
          state.networkId = new BigNumber(networkId).toNumber();
          const settings = networkSettings[state.networkId];
          state.validNetwork = !!settings;
          if (state.validNetwork) {
            Object.keys(settings).forEach(key => {
              state[key] = settings[key];
            });
          } else {
            Object.keys(networkSettings[1]).forEach(key => {
              state[key] = networkSettings[1][key];
            });
          }

          if (state.validNetwork) {
            state.addSushiswapLiquidityLink = `https://sushi.com/earn/eth:${state.sushiswapPairAddress}/add`;
            const networkChanged = previousNetworkId && previousNetworkId !== state.networkId;
            this.commit('setAddress', networkChanged);
          } else {
            this.commit('loaded');
          }
        });
    },
    refreshURLs(state, language) {
      state.marketplaceURL = `${window.parentAppLocation}/${language === 'fr' ? 'fr/place-de-marche' : 'marketplace'}`;
      state.portfolioURL = `${window.parentAppLocation}/${language === 'fr' ? 'fr/portefeuille' : 'portfolio'}`;
      state.tourURL = `${window.parentAppLocation}/${language === 'fr' ? 'fr/visite-guidee' : 'tour'}`;
      state.whitepaperURL = `${window.parentAppLocation}/${language === 'fr' ? 'fr/livre-blanc' : 'whitepaper'}`;
      state.tokenomicsURL = `${window.parentAppLocation}/${language === 'fr' ? 'fr/tokenomics' : 'tokenomics'}`;
      state.deedsURL = `${window.parentAppLocation}/${language === 'fr' ? 'fr/deeds' : 'deeds'}`;
      state.aboutUsURL = `${window.parentAppLocation}/${language === 'fr' ? 'fr/qui-sommes-nous' : 'about-us'}`;
      state.legalsURL = `${window.parentAppLocation}/${language === 'fr' ? 'fr/mentions-legales' : 'legals'}`;
      state.stakeURL = `${window.parentAppLocation}/${language === 'fr' ? 'fr/rejoindre-dao' : 'stake'}`;
      state.ownersURL = `${window.parentAppLocation}/${language === 'fr' ? 'fr/proprietaires' : 'owners'}`;
      state.farmURL = `${window.parentAppLocation}/${language === 'fr' ? 'fr/farm' : 'farm'}`;
      state.tenantsURL = `${window.parentAppLocation}/${language === 'fr' ? 'fr/locataires' : 'tenants'}`;
      state.hubsUrl = `${window.parentAppLocation}/${language === 'fr' ? 'fr/rejoindre-hubs' : 'hubs'}`;
    },
    refreshDocumentHead() {
      fetch(window.location.href, {
        method: 'GET',
        credentials: 'include',
      })
        .then(resp => resp?.ok && resp.text())
        .then(text => window.document.head.innerHTML = text.substring(text.indexOf('<head>')+6, text.indexOf('</head>')));
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
    setPolygonProvider(state) {
      if (!state.polygonProvider) {
        state.polygonProvider = new ethers.providers.JsonRpcProvider({
          url: 'https://polygon-rpc.com/',
        });
        state.polygonMeedContract = new ethers.Contract(
          state.polygonMeedAddress,
          state.erc20ABI,
          state.polygonProvider
        );
      }
    },
    loadComethRewardPool(state) {
      if (state.comethPool) {
        return;
      }
      this.commit('setPolygonProvider');
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
      const promises = [];
      this.commit('setPolygonProvider');
      promises.push(
        state.polygonMeedContract.balanceOf(pool.address)
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
    loadOwnedNfts(state) {
      try {
        if (state.deedContract && state.xMeedContract) {
          state.ownedNfts = [];
          tokenUtils.getNftsOfWallet(state.deedContract, state.xMeedContract, state.address)
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
      if (state.address && state.provider && state.validNetwork) {
        state.loadingBalances = true;
        try {
          state.provider.getBalance(state.address)
            .then(balance => state.etherBalance = balance);
          this.commit('loadMeedsBalances');
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
        const provider = new ethers.providers.Web3Provider(window.ethereum);
        provider.pollingInterval = state.pollingInterval;

        provider.lookupAddress(state.address)
          .then(name => state.ens = name);

        if (state.sushiswapRouterContract) {
          state.sushiswapRouterContract.removeAllListeners();
        }
        state.sushiswapRouterContract = new ethers.Contract(
          state.sushiswapRouterAddress,
          state.routerABI,
          provider
        );

        if (state.meedContract) {
          state.meedContract.removeAllListeners();
        }
        state.meedContract = new ethers.Contract(
          state.meedAddress,
          state.erc20ABI,
          provider
        );

        if (state.wethContract) {
          state.wethContract.removeAllListeners();
        }
        state.wethContract = new ethers.Contract(
          state.meedAddress,
          state.erc20ABI,
          provider
        );

        if (state.xMeedContract) {
          state.xMeedContract.removeAllListeners();
        }
        if (state.xMeedAddress) {
          state.xMeedContract = new ethers.Contract(
            state.xMeedAddress,
            state.xMeedRewardingABI,
            provider
          );
        } else {
          state.xMeedContract = null;
        }

        if (state.deedContract) {
          state.deedContract.removeAllListeners();
        }
        if (state.deedAddress) {
          state.deedContract = new ethers.Contract(
            state.deedAddress,
            state.nftABI,
            provider
          );
        } else {
          state.deedContract = null;
        }

        if (state.tokenFactoryContract) {
          state.tokenFactoryContract.removeAllListeners();
        }
        if (state.tokenFactoryAddress) {
          state.tokenFactoryContract = new ethers.Contract(
            state.tokenFactoryAddress,
            state.tokenFactoryABI,
            provider
          );
        } else {
          state.tokenFactoryContract = null;
        }

        if (state.tenantProvisioningContract) {
          state.tenantProvisioningContract.removeAllListeners();
        }
        if (state.tenantProvisioningAddress) {
          state.tenantProvisioningContract = new ethers.Contract(
            state.tenantProvisioningAddress,
            state.tenantProvisioningABI,
            provider
          );
        } else {
          state.tenantProvisioningContract = null;
        }

        if (state.tenantRentingContract) {
          state.tenantRentingContract.removeAllListeners();
        }
        if (state.tenantRentingAddress) {
          state.tenantRentingContract = new ethers.Contract(
            state.tenantRentingAddress,
            state.tenantRentingABI,
            provider
          );
          if (state.networkId === 5) {
            // eslint-disable-next-line new-cap
            state.tenantRentingContract.MONTH_IN_SECONDS()
              .then(data => state.MONTH_IN_SECONDS = data && data.toNumber());
            // eslint-disable-next-line new-cap
            state.tenantRentingContract.DAY_IN_SECONDS()
              .then(data => state.DAY_IN_SECONDS = data && data.toNumber());
          }
        } else {
          state.tenantRentingContract = null;
        }

        // eslint-disable-next-line new-cap
        const transferFilter = state.meedContract.filters.Transfer();
        state.meedContract.on(transferFilter, (from, to) => {
          const address = state.address?.toUpperCase();
          if (from.toUpperCase() === address || to.toUpperCase() === address) {
            this.commit('loadBalances');
          }
        });

        // eslint-disable-next-line new-cap
        const approveFilter = state.meedContract.filters.Approval();
        state.meedContract.on(approveFilter, (from, to, amount) => {
          const address = state.address?.toUpperCase();
          if (from.toUpperCase() === address || to.toUpperCase() === address) {
            this.commit('loadBalances');
            document.dispatchEvent(new CustomEvent('dapp-meeds-approved', {detail: {
              from,
              to,
              amount,
            }}));
          }
        });

        if (state.xMeedContract) {
          // eslint-disable-next-line new-cap
          const redeemFilter = state.xMeedContract.filters.Redeemed();
          state.xMeedContract.on(redeemFilter, (address) => {
            if (address.toUpperCase() === state.address?.toUpperCase()) {
              this.commit('loadPointsBalance');
              this.commit('loadOwnedNfts');
            }
          });
        }

        state.provider = provider;

        this.commit('loadBalances');
        this.commit('loadGasPrice');
        if (state.provisioningListenersToInstall) {
          this.commit('installProvisioningListeners');
        }
      } else {
        this.commit('loaded');
      }
    },
    installRentingListeners(state) {
      if (state.tenantRentingContract && !state.tenantRentingContractListenersInstalled) {
        state.tenantRentingContractListenersInstalled = true;
        state.tenantRentingContract.on(
          // eslint-disable-next-line new-cap
          state.tenantRentingContract.filters.OfferCreated(),
          (id, deedId, owner) => {
            const address = state.address?.toUpperCase();
            if (owner.toUpperCase() === address || state.selectedStandaloneOfferId) {
              document.dispatchEvent(new CustomEvent('deed-offer-created', {detail: {
                offerId: id,
                nftId: deedId,
                creator: owner,
              }}));
            }
          }
        );
        state.tenantRentingContract.on(
          // eslint-disable-next-line new-cap
          state.tenantRentingContract.filters.OfferUpdated(),
          (id, deedId, owner) => {
            const address = state.address?.toUpperCase();
            if (owner.toUpperCase() === address || state.selectedStandaloneOfferId) {
              document.dispatchEvent(new CustomEvent('deed-offer-updated', {detail: {
                offerId: id,
                nftId: deedId,
                creator: owner,
              }}));
            }
          }
        );
        state.tenantRentingContract.on(
          // eslint-disable-next-line new-cap
          state.tenantRentingContract.filters.OfferDeleted(),
          (id, deedId, owner) => {
            if (state.address) { // When an offer is confirmed as deleted, trigger this to all
              document.dispatchEvent(new CustomEvent('deed-offer-deleted', {detail: {
                offerId: id,
                nftId: deedId,
                creator: owner,
              }}));
            }
          }
        );

        state.tenantRentingContract.on(
          // eslint-disable-next-line new-cap
          state.tenantRentingContract.filters.RentPaid(),
          (id, deedId, tenant, owner, firstRent) => {
            const address = state.address?.toUpperCase();
            if (owner.toUpperCase() === address || tenant.toUpperCase() === address) {
              document.dispatchEvent(new CustomEvent('deed-lease-paid', {detail: {
                leaseId: id,
                nftId: deedId,
                manager: tenant,
                owner: owner,
                firstRent: firstRent,
              }}));
            }
          }
        );
        state.tenantRentingContract.on(
          // eslint-disable-next-line new-cap
          state.tenantRentingContract.filters.LeaseEnded(),
          (id, deedId, tenant, leaseRemainingMonths) => {
            const address = state.address?.toUpperCase();
            if (tenant.toUpperCase() === address) {
              document.dispatchEvent(new CustomEvent('deed-lease-ended', {detail: {
                leaseId: id,
                nftId: deedId,
                manager: tenant,
                leaseRemainingMonths: leaseRemainingMonths,
              }}));
            }
          }
        );

        state.tenantRentingContract.on(
          // eslint-disable-next-line new-cap
          state.tenantRentingContract.filters.TenantEvicted(),
          (id, deedId, tenant, owner, leaseRemainingMonths) => {
            const address = state.address?.toUpperCase();
            if (owner.toUpperCase() === address || tenant.toUpperCase() === address) {
              document.dispatchEvent(new CustomEvent('deed-lease-tenant-evicted', {detail: {
                leaseId: id,
                nftId: deedId,
                manager: tenant,
                owner: owner,
                leaseRemainingMonths: leaseRemainingMonths,
              }}));
            }
          }
        );
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
        this.commit('loadOfflineData');
      }
    },
    loadOfflineData(state) {
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
          state.deedLoading = false;
          state.tokenLoading = false;
          state.lpLoading = false;
          state.appLoading = false;
          state.loadingBalances = false;
          state.loadingXMeedsBalance = false;
          state.loadingMeedsBalance = false;
        });
    },
    loaded(state) {
      state.appLoading = false;
      if (!state.address || !state.validNetwork) {
        this.commit('setMetamaskOffline');
      }
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
    loadCardInfo(state, cardType) {
      if (!state.cardTypeInfos[cardType]) {
        Vue.set(state.cardTypeInfos, cardType, {loading: true});
        const cityIndex = parseInt(cardType / 4);
        const cardTypeIndex = parseInt(cardType % 4);
        deedMetadata.getCardInfo(cityIndex, cardTypeIndex)
          .then(cardInfo => {
            Vue.set(state.cardTypeInfos, cardType, cardInfo);
            return state.xMeedContract && tokenUtils.getCityCardType(state.xMeedContract, cardType);
          })
          .then(card => {
            if (card) {
              const cardInfo = state.cardTypeInfos[cardType];
              Object.assign(cardInfo, card, cardInfo);
              Vue.set(state.cardTypeInfos, cardType, cardInfo);
            }
          });
      }
    },
    installProvisioningListeners(state) {
      if (!state.provisioningListenersInstalled && state.address && state.tenantProvisioningContract) {
        state.provisioningListenersInstalled = true;

        // eslint-disable-next-line new-cap
        state.tenantProvisioningContract.on(state.tenantProvisioningContract.filters.TenantStarted(), (address, nftId)  => {
          if (address.toUpperCase() === state.address?.toUpperCase()) {
            document.dispatchEvent(new CustomEvent('nft-tenant-provisioning-changed', {detail: {
              nftId: nftId.toNumber(),
              command: 'start',
            }}));
          }
        });

        // eslint-disable-next-line new-cap
        state.tenantProvisioningContract.on(state.tenantProvisioningContract.filters.TenantStopped(), (address, nftId)  => {
          if (address.toUpperCase() === state.address?.toUpperCase()) {
            document.dispatchEvent(new CustomEvent('nft-tenant-provisioning-changed', {detail: {
              nftId: nftId.toNumber(),
              command: 'stop',
            }}));
          }
        });
      } else {
        state.provisioningListenersToInstall = true;
      }
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

    if (!store.appLoading) {
      window.ethereum.on('connect', () => store.commit('refreshMetamaskState'));
      window.ethereum.on('disconnect', () => store.commit('refreshMetamaskState'));
    }
  } else {
    store.commit('loaded');

    // If the event is not dispatched by the end of the timeout,
    // the user probably doesn't have MetaMask installed.
    setTimeout(initialize, 3000); // 3 seconds
  }
}

initialize();

window.ethereum?.on('accountsChanged', () => {
  if (authentication.hasAuthenticatedLogin()) {
    authentication.logout().finally(() => window.location.reload());
  } else {
    initialize();
  }
});
window.ethereum?.on('chainChanged', initialize);

window.addEventListener('ethereum#initialized', initialize, {
  once: true,
});

let app = null;

function initializeVueApp(language) {
  fetch(`${window.parentAppLocation}/static/i18n/messages_${language}.properties?_=${buildNumber}`)
    .then(resp => resp && resp.ok && resp.text())
    .then(i18nMessages => {
      const data = i18nMessages
        .split('\n')
        .filter(Boolean)
        .reduce((obj, line) => {
          const pair = line.split(/=(.*)/s);
          if (pair.length > 1) {
            try {
              obj[pair[0]] = decodeURIComponent(pair[1]
                ?.replace( /\\u00([a-fA-F0-9]{2})/g, '%$1')
                ?.replace( /\\[uU]([a-fA-F0-9]{4})/g, (g, m1) => String.fromCharCode(parseInt(m1, 16)))
                ?.replace(/\\n/g, '\n'));
            } catch (e) {
              obj[pair[0]] = pair[1]
                ?.replace( /\\[uU]([a-fA-F0-9]{4})/g, (g, m1) => String.fromCharCode(parseInt(m1, 16)))
                ?.replace(/\\n/g, '\n');
            }
          }
          return obj;
        }, {});

      i18n.mergeLocaleMessage(language, data);
      if (!app) {
        app = new Vue({
          el: '#deedsApp',
          template: '<deeds-site id="deedsApp" />',
          store,
          i18n,
          vuetify,
        });
      }

      document.cookie = `preferred-language=${language}; path= /`;
    });
}

initializeVueApp(language);