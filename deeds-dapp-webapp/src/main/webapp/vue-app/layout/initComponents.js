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
import Site from './components/Site.vue';
import SiteContent from './components/SiteContent.vue';
// Topbar components
import Topbar from './components/Topbar.vue';
import TopbarAddressSelector from './components/topbar/AddressSelector.vue';
import TopbarFiatCurrencySelector from './components/topbar/FiatCurrencySelector.vue';
import TopbarLanguageSelector from './components/topbar/LanguageSelector.vue';
import TopbarGasPrice from './components/topbar/GasPrice.vue';
import TopbarMetamaskButton from './components/topbar/MetamaskButton.vue';
// Metamask connection button components
import Metamask from './components/Metamask.vue';
// Navbar components
import Navbar from './components/Navbar.vue';
// Page selector components
import Page from './components/Page.vue';
import Notifications from './components/Notifications.vue';

const components = {
  'deeds-site': Site,
  'deeds-site-content': SiteContent,
  'deeds-topbar': Topbar,
  'deeds-topbar-address-selector': TopbarAddressSelector,
  'deeds-topbar-fiat-currency-selector': TopbarFiatCurrencySelector,
  'deeds-topbar-language-selector': TopbarLanguageSelector,
  'deeds-topbar-gas-price': TopbarGasPrice,
  'deeds-topbar-metamask-button': TopbarMetamaskButton,
  'deeds-metamask': Metamask,
  'deeds-navbar': Navbar,
  'deeds-page': Page,
  'deeds-notifications': Notifications,
};

for (const key in components) {
  Vue.component(key, components[key]);
}