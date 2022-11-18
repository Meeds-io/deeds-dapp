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
import DeedsMarketplace from './components/DeedsMarketplace.vue';
import DeedsMarketplaceIntroduction from './components/DeedsMarketplaceIntroduction.vue';
import DeedsMarketplaceIntroductionItem from './components/DeedsMarketplaceIntroductionItem.vue';
import DeedsMarketplaceDeeds from './components/DeedsMarketplaceDeeds.vue';
import DeedsMarketplaceDeedsEmpty from './components/DeedsMarketplaceDeedsEmpty.vue';
import DeedsMarketplaceDeedsList from './components/DeedsMarketplaceDeedsList.vue';
import DeedsMarketplaceDeedsSelector from './components/DeedsMarketplaceDeedsSelector.vue';
import DeedsMarketplaceOfferCard from './components/MarketplaceOfferCard.vue';
import DeedsMarketplaceOfferCardDetails from './components/MarketplaceOfferCardDetails.vue';
import DeedsMarketplaceOfferCardDetailsTopbar from './components/MarketplaceOfferCardDetailsTopbar.vue';
import DeedsMarketplaceOfferCardDetailsNav from './components/MarketplaceOfferCardDetailsNav.vue';
import DeedsMarketplaceOfferCardDetailsCharacteristics from './components/MarketplaceOfferCardDetailsCharacteristics.vue';
import DeedsMarketplaceOfferCardDetailsRental from './components/MarketplaceOfferCardDetailsRental.vue';
import DeedsMarketplaceOfferCardDetailsFooter from './components/MarketplaceOfferCardDetailsFooter.vue';
import DeedsMarketplaceOfferRentDrawer from './components/drawers/DeedOfferRentDrawer.vue';

const components = {
  'deeds-marketplace': DeedsMarketplace,
  'deeds-marketplace-introduction': DeedsMarketplaceIntroduction,
  'deeds-marketplace-introduction-item': DeedsMarketplaceIntroductionItem,
  'deeds-marketplace-deeds': DeedsMarketplaceDeeds,
  'deeds-marketplace-deeds-empty': DeedsMarketplaceDeedsEmpty,
  'deeds-marketplace-deeds-list': DeedsMarketplaceDeedsList,
  'deeds-marketplace-deeds-selector': DeedsMarketplaceDeedsSelector,
  'deeds-marketplace-offer-card': DeedsMarketplaceOfferCard,
  'deeds-marketplace-offer-card-details': DeedsMarketplaceOfferCardDetails,
  'deeds-marketplace-offer-card-details-topbar': DeedsMarketplaceOfferCardDetailsTopbar,
  'deeds-marketplace-offer-card-details-nav': DeedsMarketplaceOfferCardDetailsNav,
  'deeds-marketplace-offer-card-details-charcteristics': DeedsMarketplaceOfferCardDetailsCharacteristics,
  'deeds-marketplace-offer-card-details-rental': DeedsMarketplaceOfferCardDetailsRental,
  'deeds-marketplace-offer-card-details-footer': DeedsMarketplaceOfferCardDetailsFooter,
  'deeds-marketplace-offer-rent-drawer': DeedsMarketplaceOfferRentDrawer,
};

for (const key in components) {
  Vue.component(key, components[key]);
}