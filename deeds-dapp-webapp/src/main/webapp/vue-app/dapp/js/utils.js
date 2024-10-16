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

export function getScrollbarWidth() {
  const outer = document.createElement('div');
  outer.style.visibility = 'hidden';
  outer.style.overflow = 'scroll';
  outer.style.msOverflowStyle = 'scrollbar';
  document.body.appendChild(outer);
  const inner = document.createElement('div');
  outer.appendChild(inner);
  const scrollbarWidth = (outer.offsetWidth - inner.offsetWidth);
  outer.parentNode.removeChild(outer);
  return scrollbarWidth;
}

export function getQueryParam(paramName) {
  const uri = window.location.search.substring(1);
  const params = new URLSearchParams(uri);
  return params.get(paramName);
}

export function copyToClipboard(text) {
  try {
    navigator.clipboard.writeText(text);
    document.dispatchEvent(new CustomEvent('copy-success'));
    return true;
  } catch (e) {
    document.dispatchEvent(new CustomEvent('copy-error'));
    return false;
  }
}

export function sortByName(tab, lang) {
  return tab.sort((obj1, obj2) => getHubName(obj1?.name, lang).localeCompare(getHubName(obj2?.name, lang)));
}

export function getHubName(namesByLang, lang) {
  return namesByLang?.[lang]?.toLowerCase?.() || namesByLang?.['en']?.toLowerCase?.() || '';
}

export function refreshHubUrl(hubAddress, reportId) {
  const link = hubAddress && reportId && `${window.location.pathname}?address=${hubAddress}&report=${reportId}`
      || (hubAddress && `${window.location.pathname}?address=${hubAddress}`)
      || window.location.pathname;
  if (!window.location.href !== link) {
    const fullLink = `${origin}${link}`;
    window.setTimeout(() => window.history.pushState({}, '', fullLink), 50);
  }
}

export function percentage(value, language, displaySign, fractions) {
  return new Intl.NumberFormat(language, {
    style: 'percent',
    signDisplay: displaySign && 'always' || 'never',
    roundingMode: 'halfCeil',
    minimumFractionDigits: 0,
    maximumFractionDigits: fractions === 0 ? 0 : (!fractions && 2 || Number(fractions)),
  }).format(value || 0);
}

export function numberFormatWithDigits(value, language, minimumFractionDigits, maximumFractionDigits) {
  return new Intl.NumberFormat(language, {
    style: 'decimal',
    roundingMode: 'halfCeil',
    minimumFractionDigits: minimumFractionDigits || 0,
    maximumFractionDigits: maximumFractionDigits || (value < 10 ? 2 :0),
  }).format(value || 0);
}

const TEXTAREA = document.createElement('textarea');

export function htmlToText(htmlContent) {
  if (!htmlContent) {
    return '';
  }
  let content = htmlContent.replace(/<[^>]+>/g, ' ').trim();
  TEXTAREA.innerHTML = content;
  content = TEXTAREA.value;
  return content.replace(/[\r|\n|\t]/g, ' ').replace(/ +(?= )/g,' ').trim();
}
