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
const dark = false;
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
const language = localStorage.getItem('deeds-selectedLanguage') || (navigator.language.indexOf('fr') === 0 ? 'fr' : 'en');

const i18n = new VueI18n({
  locale: language,
  messages: {},
});

const pathParts = window.location.pathname.split('/');
window.parentAppLocation = pathParts[1];
const page = pathParts.length > 2 && pathParts[2] || 'home';

const store = new Vuex.Store({
  state: {
    buildNumber,
    page,
    pageState: null,
    parentLocation: window.parentAppLocation,
    whitepaperLink: 'https://mirror.xyz/meedsdao.eth/EDh9QfsuuIDNS0yKcQDtGdXc25vfkpnnKpc3RYUTJgc',
    introductiveVideoLink: 'https://res.cloudinary.com/dcooc6vig/video/upload/v1678356211/meedsdao-site/assets/video/Video_test_uic8tk.mp4',
    contributionProgramsVideoLink: 'https://res.cloudinary.com/dcooc6vig/video/upload/v1678449277/meedsdao-site/assets/video/Contributions_Programs_u49wx7.mp4',
    rewardsVideoLink: 'https://res.cloudinary.com/dcooc6vig/video/upload/v1678450208/meedsdao-site/assets/video/Rewards_ws45i1.mp4',
    overviewVideoLink: 'https://res.cloudinary.com/dcooc6vig/video/upload/v1678449271/meedsdao-site/assets/video/Overview_hw2qom.mp4',
    perksVideoLink: 'https://res.cloudinary.com/dcooc6vig/video/upload/v1678449264/meedsdao-site/assets/video/Perks_at4eyy.mp4',
    teamworkVideoLink: 'https://res.cloudinary.com/dcooc6vig/video/upload/v1678449329/meedsdao-site/assets/video/Teamwork_cmr4ns.mp4',
    language,
    isMobile: false,
    staticPage: true,
    whiteThemeColor: 'white',
    blackThemeColor: 'black',
    dark,
  },
  mutations: {
    setPageState(state, value) {
      state.pageState = value;
    },
    setMobile(state, value) {
      state.isMobile = value;
    },
    selectLanguage(state, lang) {
      state.language = lang;
      i18n.locale = lang.indexOf('fr') === 0 ? 'fr' : 'en';
      localStorage.setItem('deeds-selectedLanguage', state.language);
      initializeVueApp(lang);
    },
  }
});

let app = null;
function initializeVueApp(lang) {
  fetch(`/${window.parentAppLocation}/static/i18n/messages_${lang}.properties?_=${buildNumber}`)
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
                ?.replace(/\\n/g, '\n'));
            } catch (e) {
              obj[pair[0]] = pair[1]
                ?.replace( /\\[uU]([a-fA-F0-9]{4})/g, (g, m1) => String.fromCharCode(parseInt(m1, 16)))
                ?.replace(/\\n/g, '\n');
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
    });
}

initializeVueApp(language);