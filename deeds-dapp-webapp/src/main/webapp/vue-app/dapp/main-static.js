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
  return document?.documentElement?.lang || 'en';
}

const language = getLanguage();

const i18n = new VueI18n({
  locale: language,
  messages: {},
});

const page = document.querySelector('[name=pageName]').value;

const pathParts = window.location.pathname.split('/');
window.parentAppLocation = pathParts[1] || '';

if (window.parentAppLocation.length && (window.parentAppLocation === 'dapp' || window.parentAppLocation === 'deeds-dapp')) {
  window.parentAppLocation = `/${window.parentAppLocation}`;
} else {
  window.parentAppLocation = '';
}

const pageUriPerLanguages = {
  en: {
    pages: [
      '',
      'marketplace',
      'portfolio',
      'tour',
      'whitepaper',
      'tokenomics',
      'about-us',
      'mint',
      'legals',
      'stake',
      'owners',
      'farm',
      'tenants',
      'why-meeds',
      'buy'
    ],
    uriPrefix: '',
  },
  fr: {
    pages: [
      'fr',
      'place-de-marche',
      'portefeuille',
      'visite-guidee',
      'livre-blanc',
      'tokenomics',
      'qui-sommes-nous',
      'mint',
      'mentions-legales',
      'rejoindre-dao',
      'proprietaires',
      'farm',
      'locataires',
      'pourquoi-meeds',
      'acheter'
    ],
    uriPrefix: 'fr/',
  },
};

const store = new Vuex.Store({
  state: {
    buildNumber,
    page,
    pageState: null,
    pageUriPerLanguages,
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
    homeUrl: `${window.parentAppLocation}/${pageUriPerLanguages[language].pages[0]}`,
    marketplaceURL: `${window.parentAppLocation}/${pageUriPerLanguages[language].uriPrefix}${pageUriPerLanguages[language].pages[1]}`,
    portfolioURL: `${window.parentAppLocation}/${pageUriPerLanguages[language].uriPrefix}${pageUriPerLanguages[language].pages[2]}`,
    tourURL: `${window.parentAppLocation}/${pageUriPerLanguages[language].uriPrefix}${pageUriPerLanguages[language].pages[3]}`,
    whitepaperURL: `${window.parentAppLocation}/${pageUriPerLanguages[language].uriPrefix}${pageUriPerLanguages[language].pages[4]}`,
    tokenomicsURL: `${window.parentAppLocation}/${pageUriPerLanguages[language].uriPrefix}${pageUriPerLanguages[language].pages[5]}`,
    aboutUsURL: `${window.parentAppLocation}/${pageUriPerLanguages[language].uriPrefix}${pageUriPerLanguages[language].pages[6]}`,
    mintUrl: `${window.parentAppLocation}/${pageUriPerLanguages[language].uriPrefix}${pageUriPerLanguages[language].pages[7]}`,
    legalsURL: `${window.parentAppLocation}/${pageUriPerLanguages[language].uriPrefix}${pageUriPerLanguages[language].pages[8]}`,
    stakeURL: `${window.parentAppLocation}/${pageUriPerLanguages[language].uriPrefix}${pageUriPerLanguages[language].pages[9]}`,
    ownersURL: `${window.parentAppLocation}/${pageUriPerLanguages[language].uriPrefix}${pageUriPerLanguages[language].pages[10]}`,
    farmURL: `${window.parentAppLocation}/${pageUriPerLanguages[language].uriPrefix}${pageUriPerLanguages[language].pages[11]}`,
    tenantsURL: `${window.parentAppLocation}/${pageUriPerLanguages[language].uriPrefix}${pageUriPerLanguages[language].pages[12]}`,
    whyMeedsUrl: `${window.parentAppLocation}/${pageUriPerLanguages[language].uriPrefix}${pageUriPerLanguages[language].pages[13]}`,
    buyUrl: `${window.parentAppLocation}/${pageUriPerLanguages[language].uriPrefix}${pageUriPerLanguages[language].pages[14]}`,
  },
  mutations: {
    setPageState(state, value) {
      state.pageState = value;
    },
    setMobile(state, value) {
      state.isMobile = value;
    },
    refreshDocumentHead() {
      fetch(window.location.href, {
        method: 'GET',
        credentials: 'include',
      })
        .then(resp => resp?.ok && resp.text())
        .then(text => window.document.head.innerHTML = text.substring(text.indexOf('<head>') + '<head>'.length, text.indexOf('</head>')));
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
