import './initComponents';
import i18nMessages from '../json/i18nMessages.json';
import * as ethUtils from './js/ethUtils.js';
import * as tokenUtils from './js/tokenUtils.js';
import * as exchange from './js/exchange.js';

window.Object.defineProperty(Vue.prototype, '$ethUtils', {
  value: ethUtils,
});

window.Object.defineProperty(Vue.prototype, '$exchange', {
  value: exchange,
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

const store = new Vuex.Store({
  state: {
    appLoading: true,
    address: null,
    networkId: null,
    validNetwork: false,
    etherscanBaseLink: null,
    managedNetworkIds: [1, 5],
    provider: null,
    erc20ABI: [
      'event Transfer(address indexed from, address indexed to, uint256 value)',
      'event Approval(address indexed owner, address indexed spender, uint256 value)',
      'function allowance(address owner, address spender) external view returns (uint256)',
      'function approve(address spender, uint256 amount) external returns (bool)',
      'function balanceOf(address owner) view returns (uint256)',
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
      'function earned(address account) public view returns (uint256)',
      'function currentCityIndex() public view returns (uint8)',
      'function cityInfo(uint256 index) public view returns (string name, uint32 population, uint32 maxPopulation, uint256 availability)',
      'function cardTypeInfo(uint256 index) public view returns (string name, string uri, uint8 cityIndex, uint8 cardType, uint32 supply, uint32 maxSupply, uint256 amount)',
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
    // Contracts addresses
    routerAddress: null,
    wethAddress: null,
    meedAddress: null,
    pairAddress: null,
    // Contracts objects
    routerContract: null,
    wethContract: null,
    meedContract: null,
    xMeedContract: null,
    nftContract: null,
    // User preferred language
    language,
    // Metamask status
    isMetamaskInstalled,
    isMetamaskConnected,
    // Meed/ETH pair historical data
    pairHistoryData: null,
    // Euro/USD historical exchange rate data
    currencyExchangeRate: null,
    // Default Gas limit for sent transactions
    gasLimit: 500000,
    // Cuurent Gas Price 
    gasPrice: 0,
    gasPriceGwei: 0,
    transactionGas: 0,
    exchangeRate: 1,
    meedsPrice: 0,
    ethPrice: 0,
    // User balances
    etherBalance: null,
    meedsBalance: null,
    meedsBalanceNoDecimals: null,
    meedsRouteAllowance: null,
    meedsStakeAllowance: null,
    meedsTotalSupply: null,
    xMeedsTotalSupply: null,
    meedsBalanceOfXMeeds: null,
    meedsBalanceOfXMeedsNoDecimals: null,
    xMeedsBalance: null,
    xMeedsBalanceNoDecimals: null,
    pointsBalance: null,
    pointsBalanceNoDecimals: null,
    ownedNfts: null,
    selectedFiatCurrency,
    currentCity: null,
    currentCardTypes: null,
    currentCityMintable: null,
    lastCityMintingCompleteDate: null,
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
              state.validNetwork = false; // TODO coming soon

              // Mainnet
              state.etherscanBaseLink = 'https://etherscan.io/';
              state.routerAddress = '0xd9e1cE17f2641f24aE83637ab66a2cca9C378B9F';
              state.pairAddress = '0x960bd61d0b960b107ff5309a2dcced4705567070';
              state.wethAddress = '0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2';
              state.meedAddress = '0x8503a7b00b4b52692cc6c14e5b96f142e30547b7';
              // TODO replace with real addresses
              state.nftAddress = null;
              state.xMeedAddress = null;
            } else if (state.networkId === 5) {
              state.validNetwork = true;

              // Goërli
              state.etherscanBaseLink = 'https://goerli.etherscan.io/';
              state.routerAddress = '0x1b02da8cb0d097eb8d57a175b88c7d8b47997506';
              state.pairAddress = '0x157144DBc40469c3d2dbB2EAA58b7b40E0b0249c';
              state.wethAddress = '0xb4fbf271143f4fbf7b91a5ded31805e42b2208d6';
              state.meedAddress = '0x62aae5c3648617e6f6542d3a457eca3a00da7e03';
              state.nftAddress = '0xb26cCD76748Fa79bF242Cfaca6687184CaF48093';
              state.xMeedAddress = '0x36e19bB29573F5Dc5d6A124444e81646A59b6702';
            }
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
    setMeedsPrice(state, meedsPrice) {
      state.meedsPrice = meedsPrice;
    },
    setEthPrice(state, ethPrice) {
      state.ethPrice = ethPrice;
    },
    setExchangeRate(state, exchangeRate) {
      state.exchangeRate = exchangeRate;
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
    loadPointsBalance(state) {
      tokenUtils.getPointsBalance(state.xMeedContract, state.address)
        .then(balance => {
          state.pointsBalance = balance;
          state.pointsBalanceNoDecimals = ethUtils.computeTokenBalanceNoDecimals(state.pointsBalance, 3, state.language);
        });
    },
    loadCurrentCity(state) {
      tokenUtils.getCurrentCity(state.xMeedContract)
        .then(currentCity => {
          state.currentCity = currentCity;
          return tokenUtils.isCurrentCityMintable(state.xMeedContract);
        })
        .then(currentCityMintable => {
          state.currentCityMintable = currentCityMintable;
          if (!currentCityMintable) {
            return tokenUtils.getLastCityMintingCompleteDate(state.xMeedContract)
              .then(lastCityMintingCompleteDate => state.lastCityMintingCompleteDate = lastCityMintingCompleteDate);
          }
        })
        .then(() => state.currentCity && tokenUtils.getCityCardTypes(state.xMeedContract, state.currentCity.id))
        .then(currentCardTypes => state.currentCardTypes = currentCardTypes);
    },
    loadOwnedNfts(state) {
      if (state.nftContract && state.xMeedContract) {
        tokenUtils.getNftsOfWallet(state.nftContract, state.xMeedContract, state.address)
          .then(nfts => state.ownedNfts = nfts);
      }
    },
    loadBalances(state) {
      if (state.address && state.provider) {
        state.provider.getBalance(state.address).then(balance => state.etherBalance = balance);
        if (state.meedContract) {
          state.meedContract.balanceOf(state.address).then(balance => this.commit('setMeedsBalance', balance));
          state.meedContract.balanceOf(state.xMeedAddress).then(balance => state.meedsBalanceOfXMeeds = balance);
          state.meedContract.allowance(state.address, state.routerAddress).then(balance => state.meedsRouteAllowance = balance);
          state.meedContract.allowance(state.address, state.xMeedAddress).then(balance => state.meedsStakeAllowance = balance);
          state.meedContract.totalSupply().then(totalSupply => state.meedsTotalSupply = totalSupply);
        }
        if (state.xMeedContract) {
          state.xMeedContract.balanceOf(state.address).then(balance => this.commit('setXMeedsBalance', balance));
          state.xMeedContract.totalSupply().then(totalSupply => state.xMeedsTotalSupply = totalSupply);
        }
      }
    },
    setProvider(state) {
      if (state.address) {
        state.provider = new ethers.providers.Web3Provider(window.ethereum);
        state.routerContract = new ethers.Contract(
          state.routerAddress,
          state.routerABI,
          state.provider,
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
        if (state.nftAddress) {
          state.nftContract = new ethers.Contract(
            state.nftAddress,
            state.nftABI,
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
              this.commit('loadPointsBalance');
              this.commit('loadCurrentCity');
            }
          });
        }

        this.commit('loadBalances');
        this.commit('loadGasPrice');
        this.commit('loadOwnedNfts');
        this.commit('loadPointsBalance');
        this.commit('loadCurrentCity');
      }
    },
    loadGasPrice(state) {
      if (state.provider) {
        state.provider.getGasPrice().then(gasPrice => {
          state.gasPriceGwei = gasPrice && ethers.utils.formatUnits(gasPrice, 'gwei') || 0;
          state.gasPrice = gasPrice && gasPrice.toNumber() || 0;
          state.transactionGas = gasPrice.mul(state.gasLimit);
        });
      }
    },
    loadPairHistoryData(state) {
      exchange.retrieveMeedsData().then(result => {
        state.pairHistoryData = result || {};
        const today = new Date().toISOString().substring(0, 10);
        const todayStats = state.pairHistoryData[today];
        if (todayStats) {
          if (todayStats.meedsPrice) {
            this.commit('setMeedsPrice', new BigNumber(todayStats.meedsPrice));
          }
          if (todayStats.ethPrice) {
            this.commit('setEthPrice', new BigNumber(todayStats.ethPrice));
          }
        }
      });
    },
    loadCurrencyExchangeRate(state) {
      exchange.retrieveCurrencyExchangeRate().then(result => {
        state.currencyExchangeRate = result || {};
        const todayDate = new Date().toISOString().substring(0, 10);
        if (state.currencyExchangeRate && state.currencyExchangeRate[todayDate]) {
          this.commit('setExchangeRate', state.currencyExchangeRate[todayDate]);
        } else {
          this.commit('setExchangeRate', 1);
        }
      });
    },
    loaded(state) {
      state.appLoading = false;
    },
    selectFiatCurrency(state, fiatCurrency) {
      state.selectedFiatCurrency = fiatCurrency;
      localStorage.setItem('deeds-selectedFiatCurrency', state.selectedFiatCurrency);
    },
    refreshMetamaskState() {
      this.commit('setMetamaskInstalled');
      this.commit('setMetamaskConnected');
      this.commit('setNetworkId');
    },
  }
});

if (isMetamaskInstalled) {
  if (window.ethereum._metamask && window.ethereum._metamask.isUnlocked) {
    window.ethereum._metamask.isUnlocked().then(unlocked => {
      if (unlocked) {
        store.commit('refreshMetamaskState');
      } else {
        store.commit('loaded');
      }
    });
  }

  window.ethereum.on('connect', () => store.commit('refreshMetamaskState'));
  window.ethereum.on('disconnect', () => store.commit('refreshMetamaskState'));
  window.ethereum.on('accountsChanged', () => window.location.reload());
  window.ethereum.on('chainChanged', () => window.location.reload());

  window.setInterval(() => store.commit('loadGasPrice'), 30000);
} else {
  store.commit('loaded');
}

new Vue({
  el: '#deedsApp',
  template: '<deeds-site id="deedsApp" />',
  created() {
    store.commit('loadPairHistoryData');
    store.commit('loadCurrencyExchangeRate');
  },
  store,
  i18n,
  vuetify: new Vuetify({
    dark: true,
    silent: true,
    iconfont: 'mdi',
  }),
});