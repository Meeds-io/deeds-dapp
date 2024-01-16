/*
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
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
import StaticPageContent from './components/StaticPageContent.vue';
import WhyMeeds from './components/page/WhyMeeds.vue';
import Whitepaper from './components/page/Whitepaper.vue';
import AboutUs from './components/page/AboutUs.vue';
import Legals from './components/page/Legals.vue';
import ProductTour from './components/page/productTour.vue';

const components = {
  'deeds-static-page-content': StaticPageContent,
  'deeds-why-meeds': WhyMeeds,
  'deeds-whitepaper': Whitepaper,
  'deeds-about-us': AboutUs,
  'deeds-legals': Legals,
  'deeds-product-tour': ProductTour,
};

for (const key in components) {
  Vue.component(key, components[key]);
}