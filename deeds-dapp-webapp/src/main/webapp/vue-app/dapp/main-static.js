/*
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
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
import './initComponents-static';

Vue.use(Vuex);
Vue.use(Vuetify);

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
  const lang = document.documentElement.lang;
  return lang || localStorage.getItem('deeds-selectedLanguage') || (navigator.language.indexOf('fr') === 0 ? 'fr' : 'en');
}

const language = getLanguage();

const i18n = new VueI18n({
  locale: language,
  messages: {},
});

const pathParts = window.location.pathname.split('/');
window.parentAppLocation = pathParts[1] || '';
let page;
if (window.parentAppLocation.length && (window.parentAppLocation === 'dapp' || window.parentAppLocation === 'deeds-dapp')) {
  window.parentAppLocation = `/${window.parentAppLocation}`;
  page = pathParts.length > 2 && pathParts[2] || 'home';
} else {
  window.parentAppLocation = '';
  page = pathParts.length > 1 && pathParts[1] || 'home';
}

const store = new Vuex.Store({
  state: {
    buildNumber,
    page,
    pageState: null,
    parentLocation: window.parentAppLocation,
    whitepaperLink: 'https://mirror.xyz/meedsdao.eth/EDh9QfsuuIDNS0yKcQDtGdXc25vfkpnnKpc3RYUTJgc',
    introductiveVideoLink: 'https://res.cloudinary.com/dcooc6vig/video/upload/f_auto,q_auto/v1678984879/meedsdao-site/assets/video/meeds_intro.mp4',
    contributionProgramsVideoLink: 'https://res.cloudinary.com/dcooc6vig/video/upload/f_auto,q_auto/v1678985745/meedsdao-site/assets/video/contributions.mp4',
    rewardsVideoLink: 'https://res.cloudinary.com/dcooc6vig/video/upload/f_auto,q_auto/v1678985713/meedsdao-site/assets/video/rewards.mp4',
    overviewVideoLink: 'https://res.cloudinary.com/dcooc6vig/video/upload/f_auto,q_auto/v1678985770/meedsdao-site/assets/video/overview.mp4',
    perksVideoLink: 'https://res.cloudinary.com/dcooc6vig/video/upload/f_auto,q_auto/v1678985800/meedsdao-site/assets/video/perks.mp4',
    teamworkVideoLink: 'https://res.cloudinary.com/dcooc6vig/video/upload/f_auto,q_auto/v1678985550/meedsdao-site/assets/video/teamwork.mp4',
    language,
    isMobile: false,
    staticPage: true,
    systemThemeDark,
    themePreference,
    dark,
    blackThemeColor: dark && 'white' || 'black',
    whiteThemeColor: dark && 'dark-color' || 'white',
    marketplaceLabel: `${language === 'fr' ? 'place-de-marche' : 'marketplace'}`,
    marketplaceURL: `${window.parentAppLocation}/${language === 'fr' ? 'place-de-marche' : 'marketplace'}`,
    portfolioURL: `${window.parentAppLocation}/${language === 'fr' ? 'portefeuille' : 'portfolio'}`,
    tourURL: `${window.parentAppLocation}/${language === 'fr' ? 'visite-guidee' : 'tour'}`,
    whitepaperURL: `${window.parentAppLocation}/${language === 'fr' ? 'livre-blanc' : 'whitepaper'}`,
    tokenomicsURL: `${window.parentAppLocation}/${language === 'fr' ? 'tokenomics-fr' : 'tokenomics'}`,
    deedsURL: `${window.parentAppLocation}/${language === 'fr' ? 'deeds-fr' : 'deeds'}`,
    aboutUsURL: `${window.parentAppLocation}/${language === 'fr' ? 'qui-sommes-nous' : 'about-us'}`,
    legalsURL: `${window.parentAppLocation}/${language === 'fr' ? 'mentions-legales' : 'legals'}`,
    stakeURL: `${window.parentAppLocation}/${language === 'fr' ? 'rejoindre-dao' : 'stake'}`,
    ownersURL: `${window.parentAppLocation}/${language === 'fr' ? 'proprietaires' : 'owners'}`,
    farmURL: `${window.parentAppLocation}/${language === 'fr' ? 'farm-fr' : 'farm'}`,
    tenantsURL: `${window.parentAppLocation}/${language === 'fr' ? 'locataires' : 'tenants'}`,
  },
  mutations: {
    setPageState(state, value) {
      state.pageState = value;
    },
    setMobile(state, value) {
      state.isMobile = value;
    },
    refreshURLs(state, language) {
      state.marketplaceURL = `${window.parentAppLocation}/${language === 'fr' ? 'place-de-marche' : 'marketplace'}`;
      state.portfolioURL = `${window.parentAppLocation}/${language === 'fr' ? 'portefeuille' : 'portfolio'}`;
      state.tourURL = `${window.parentAppLocation}/${language === 'fr' ? 'visite-guidee' : 'tour'}`;
      state.whitepaperURL = `${window.parentAppLocation}/${language === 'fr' ? 'livre-blanc' : 'whitepaper'}`;
      state.tokenomicsURL = `${window.parentAppLocation}/${language === 'fr' ? 'tokenomics-fr' : 'tokenomics'}`;
      state.deedsURL = `${window.parentAppLocation}/${language === 'fr' ? 'deeds-fr' : 'deeds'}`;
      state.aboutUsURL = `${window.parentAppLocation}/${language === 'fr' ? 'qui-sommes-nous' : 'about-us'}`;
      state.legalsURL = `${window.parentAppLocation}/${language === 'fr' ? 'mentions-legales' : 'legals'}`;
      state.stakeURL = `${window.parentAppLocation}/${language === 'fr' ? 'rejoindre-dao' : 'stake'}`;
      state.ownersURL = `${window.parentAppLocation}/${language === 'fr' ? 'proprietaires' : 'owners'}`;
      state.farmURL = `${window.parentAppLocation}/${language === 'fr' ? 'farm-fr' : 'farm'}`;
      state.tenantsURL = `${window.parentAppLocation}/${language === 'fr' ? 'locataires' : 'tenants'}`;
    },
    refreshDocumentHead() {
      fetch(window.location.href, {
        method: 'GET',
        credentials: 'include',
      })
        .then(resp => resp?.ok && resp.text())
        .then(text => window.document.head.innerHTML = text);
    },
    selectLanguage(state, lang) {
      state.language = lang;
      i18n.locale = lang.indexOf('fr') === 0 ? 'fr' : 'en';
      localStorage.setItem('deeds-selectedLanguage', state.language);
      this.commit('refreshURLs', lang);
      this.commit('refreshDocumentHead');
      initializeVueApp(lang);
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
  }
});

let app = null;
function initializeVueApp(lang) {
  fetch(`${window.parentAppLocation}/static/i18n/messages_${lang}.properties?_=${buildNumber}`)
    .then(resp => resp && resp.ok && resp.text())
    .then(i18nMessages => {
      const data = i18nMessages && i18nMessages
        .split('\n')
        .filter(Boolean)
        .reduce((obj, line) => {
          const pair = line.split(/=(.*)/s);
          if (pair.length > 1) {
            try {
              obj[pair[0]] = decodeURIComponent(pair[1]
                ?.replace( /\\u00([a-fA-F0-9]{2})/g, '%$1')
                ?.replace( /\\[uU]([a-fA-F0-9]{4})/g, (g, m1) => String.fromCharCode(parseInt(m1, 16)))
                ?.replace(/\\n/g, '\n'))
                ?.replace(/\\/g,'');
            } catch (e) {
              obj[pair[0]] = pair[1]
                ?.replace( /\\[uU]([a-fA-F0-9]{4})/g, (g, m1) => String.fromCharCode(parseInt(m1, 16)))
                ?.replace(/\\n/g, '\n')
                ?.replace(/\\/g,'');
            }
          }
          return obj;
        }, {}) || {};

      i18n.mergeLocaleMessage(lang, data);
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
