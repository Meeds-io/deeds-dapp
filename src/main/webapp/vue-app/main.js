import './initComponents';
import i18nMessages from '../json/i18nMessages.json';
import * as ethUtils from './js/ethUtils.js';
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
    erc20ApproveABI: [
      'event Transfer(address indexed from, address indexed to, uint256 value)',
      'event Approval(address indexed owner, address indexed spender, uint256 value)',
      'function allowance(address owner, address spender) external view returns (uint256)',
      'function approve(address spender, uint256 amount) external returns (bool)',
      'function balanceOf(address owner) view returns (uint256)',
    ],
    routerABI: [
      'function getAmountsOut(uint amountIn, address[] memory path) public view returns(uint[] memory amounts)',
      'function swapExactETHForTokens(uint amountOutMin, address[] calldata path, address to, uint deadline) external payable returns (uint[] memory amounts)',
      'function swapExactTokensForETH(uint amountIn, uint amountOutMin, address[] calldata path, address to, uint deadline) external payable returns (uint[] memory amounts)',
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
    gasLimit: 250000,
    // Cuurent Gas Price 
    gasPrice: 0,
    gasPriceGwei: 0,
    transactionGas: 0,
    exchangeRate: 1,
    meedsPrice: 0,
    ethPrice: 0,
    // User balances
    etherBalance: 0,
    meedsBalance: 0,
    meedsAllowance: 0,
    xMeedsBalance: 0,
    selectedFiatCurrency,
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
          this.commit('setProvider');
        })
        .finally(() => this.commit('loaded'));
    },
    setNetworkId(state) {
      ethUtils.getSelectedChainId()
        .then(networkId => {
          state.networkId = new BigNumber(networkId).toNumber();
          if (state.managedNetworkIds.indexOf(state.networkId) >= 0) {
            state.validNetwork = true;
            if (state.networkId === 1) {
              // Mainnet
              state.etherscanBaseLink = 'https://etherscan.io/';
              state.routerAddress = '0xd9e1cE17f2641f24aE83637ab66a2cca9C378B9F';
              state.wethAddress = '0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2';
              state.meedAddress = '0x8503a7b00b4b52692cc6c14e5b96f142e30547b7';
              state.pairAddress = '0x960bd61d0b960b107ff5309a2dcced4705567070';
            } else if (state.networkId === 5) {
              // Goerli
              state.etherscanBaseLink = 'https://goerli.etherscan.io/';
              state.routerAddress = '0x1b02da8cb0d097eb8d57a175b88c7d8b47997506';
              state.wethAddress = '0xb4fbf271143f4fbf7b91a5ded31805e42b2208d6';
              state.meedAddress = '0x62aae5c3648617e6f6542d3a457eca3a00da7e03';
              state.pairAddress = '0x157144DBc40469c3d2dbB2EAA58b7b40E0b0249c';
              state.nftAddress = '0x1d8ecd8698ce319a79d328cc86a2b0b6a28d3ea3';
              state.nftAddress = '0x1d8ecd8698ce319a79d328cc86a2b0b6a28d3ea3';
              state.xMeedAddress = '0xfdf3056186f8441c2299fdcf114d9925140f151b';
            }
            this.commit('setAddress');
          } else {
            state.address = null;
            this.commit('loaded');
          }
        });
    },
    selectLanguage(state, language) {
      state.language = language;
      i18n.locale = language.indexOf('fr') === 0 ? 'fr' : 'en';
      localStorage.setItem('deeds-selectedLanguage', state.language);
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
    },
    setMeedsAllowance(state, meedsAllowance) {
      state.meedsAllowance = meedsAllowance;
    },
    setXMeedsBalance(state, xMeedsBalance) {
      state.xMeedsBalance = xMeedsBalance;
    },
    loadBalances(state) {
      if (state.meedContract && state.address) {
        state.meedContract.allowance(state.address, state.routerAddress).then(balance => state.meedsAllowance = balance);
        state.meedContract.balanceOf(state.address).then(balance => state.meedsBalance = balance);
        state.provider.getBalance(state.address).then(balance => state.etherBalance = balance);
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
          state.erc20ApproveABI,
          state.provider
        );
        state.wethContract = new ethers.Contract(
          state.meedAddress,
          state.erc20ApproveABI,
          state.provider
        );

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

        this.commit('loadBalances');
        this.commit('loadGasPrice');
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