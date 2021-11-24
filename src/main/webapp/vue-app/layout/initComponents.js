import Site from './components/Site.vue';
// Topbar components
import Topbar from './components/Topbar.vue';
import TopbarAddressSelector from './components/topbar/AddressSelector.vue';
import TopbarFiatCurrencySelector from './components/topbar/FiatCurrencySelector.vue';
import TopbarLanguageSelector from './components/topbar/LanguageSelector.vue';
// Metamask connection button components
import Metamask from './components/Metamask.vue';
// Navbar components
import Navbar from './components/Navbar.vue';
// Page selector components
import Page from './components/Page.vue';
import Notifications from './components/Notifications.vue';

const components = {
  'deeds-site': Site,
  'deeds-topbar': Topbar,
  'deeds-topbar-address-selector': TopbarAddressSelector,
  'deeds-topbar-fiat-currency-selector': TopbarFiatCurrencySelector,
  'deeds-topbar-language-selector': TopbarLanguageSelector,
  'deeds-metamask': Metamask,
  'deeds-navbar': Navbar,
  'deeds-page': Page,
  'deeds-notifications': Notifications,
};

for (const key in components) {
  Vue.component(key, components[key]);
}