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
import './initComponents.js';

Vue.use(Vuex);
Vue.use(Vuetify);

Vue.prototype.parentLocation = window.parentAppLocation = window.location.pathname.split('/')[1];

const lang = localStorage.getItem('deeds-selectedLanguage') || (navigator.language.indexOf('fr') === 0 ? 'fr' : 'en');
const i18n = new VueI18n({
  locale: lang,
  messages: {},
});
const store = new Vuex.Store({
  state: {
    language: lang,
    parentLocation: window.parentAppLocation,
  },
  mutations: {
    selectLanguage(state, language) {
      state.language = language;
      i18n.locale = language.indexOf('fr') === 0 ? 'fr' : 'en';
      localStorage.setItem('deeds-selectedLanguage', state.language);
      getI18N(language);
    },
  }
});

const buildNumber = document.getElementsByTagName('meta').version.getAttribute('content');

function mergeI18N(i18nMessages, language) {
  const data = i18nMessages
    .split('\n')
    .filter(Boolean)
    .reduce((obj, line) => {
      const pair = line.split(/=(.*)/s);
      obj[pair[0]] = pair[1].replace( /\\u([a-fA-F0-9]{4})/g, (g, m1) => String.fromCharCode(parseInt(m1, 16)));
      return obj;
    }, {});
  i18n.mergeLocaleMessage(language, data);
  i18n.locale = language.indexOf('fr') === 0 ? 'fr' : 'en';
}

function getI18N(language) {
  return Promise.all([
    fetch(`/${window.parentAppLocation}/static/i18n/site_${language}.properties?_=${buildNumber}`)
      .then(resp => resp && resp.ok && resp.text())
      .then(i18nMessages => mergeI18N(i18nMessages, language)),
    fetch(`/${window.parentAppLocation}/static/i18n/messages_${language}.properties?_=${buildNumber}`)
      .then(resp => resp && resp.ok && resp.text())
      .then(i18nMessages => mergeI18N(i18nMessages, language)),
  ]);
}

getI18N(lang)
  .then(() => {
    new Vue({
      el: '#staticContent',
      template: '<deeds-static-page id="staticContent" />',
      i18n,
      store,
      vuetify: new Vuetify({
        icons: {
          iconfont: 'fa',
        },
        theme: {
          dark: false,
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
      }),
    });
  });