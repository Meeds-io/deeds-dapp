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
const Marketplace = { template: '<deeds-marketplace />' };
const Tenants = { template: '<deeds-tenants />' };
const Owners = { template: '<deeds-owners />' };
const Stake = { template: '<deeds-stake />' };
const Deeds = { template: '<deeds-deeds />' };
const Farm = { template: '<deeds-farm />' };
const Overview = { template: '<deeds-overview />' };
const Tokenomics = { template: '<deeds-tokenomics />' };
const Hubs = { template: '<deeds-hubs />' };
const StaticPageContent = { template: '<deeds-static-page-content />' };

export default {
  '/': StaticPageContent,
  '/default': StaticPageContent,
  '/place-de-marche': Marketplace,
  '/rejoindre-hubs': Hubs,
  '/locataires': Tenants,
  '/proprietaires': Owners,
  '/portefeuille': Overview,
  '/tokenomics': Tokenomics,
  '/rejoindre-dao': Stake,
  '/deeds': Deeds,
  '/farm': Farm,
  '/marketplace': Marketplace,
  '/tenants': Tenants,
  '/owners': Owners,
  '/portfolio': Overview,
  '/stake': Stake,
  '/join-hubs': Hubs };

