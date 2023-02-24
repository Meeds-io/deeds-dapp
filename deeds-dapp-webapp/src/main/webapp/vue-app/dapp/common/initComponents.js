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
import Drawer from './components/Drawer.vue';
import ConfirmDialog from './components/ConfirmDialog.vue';
import NumberFormat from './components/NumberFormat.vue';
import ContractAddress from './components/ContractAddress.vue';
import TabLink from './components/TabLink.vue';
import MetamaskButton from './components/MetamaskButton.vue';
import CardCarousel from './components/CardCarousel.vue';
import TokenAssetTemplate from './components/TokenAssetTemplate.vue';
import EmailField from './components/EmailField.vue';
import LoginButton from './components/LoginButton.vue';
import DateFormat from './components/DateFormat.vue';
import ExtendedTextarea from './components/ExtendedTextarea.vue';
import ButtonGroupItem from './components/ButtonGroupItem.vue';
import DynamicHtml from './components/DynamicHtml.vue';

const components = {
  'deeds-drawer': Drawer,
  'deeds-confirm-dialog': ConfirmDialog,
  'deeds-number-format': NumberFormat,
  'deeds-contract-address': ContractAddress,
  'deeds-metamask-button': MetamaskButton,
  'deeds-tab-link': TabLink,
  'deeds-card-caroussel': CardCarousel,
  'deeds-token-asset-template': TokenAssetTemplate,
  'deeds-email-field': EmailField,
  'deeds-login-button': LoginButton,
  'deeds-date-format': DateFormat,
  'deeds-extended-textarea': ExtendedTextarea,
  'deeds-button-group-item': ButtonGroupItem,
  'deeds-dynamic-html': DynamicHtml,
};

for (const key in components) {
  Vue.component(key, components[key]);
}