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
    language,
    isMetamaskInstalled,
    isMetamaskConnected,
    pairHistoryData: null,
    currencyExchangeRate: null,
    etherBalance: 0,
    meedsBalance: 0,
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
        })
        .then(() => this.commit('loaded'));
    },
    setNetworkId(state) {
      ethUtils.getSelectedChainId().then(networkId => state.networkId = networkId);
    },
    selectLanguage(state, language) {
      state.language = language;
      i18n.locale = language.indexOf('fr') === 0 ? 'fr' : 'en';
      localStorage.setItem('deeds-selectedLanguage', state.language);
    },
    setEtherBalance(state, etherBalance) {
      state.etherBalance = etherBalance;
    },
    setMeedsBalance(state, meedsBalance) {
      state.meedsBalance = meedsBalance;
    },
    setXMeedsBalance(state, xMeedsBalance) {
      state.xMeedsBalance = xMeedsBalance;
    },
    loadPairHistoryData(state) {
      exchange.retrieveMeedsData().then(result => state.pairHistoryData = result || {});
    },
    loadCurrencyExchangeRate(state) {
      exchange.retrieveCurrencyExchangeRate().then(result => state.currencyExchangeRate = result || {});
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
      this.commit('setAddress');
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