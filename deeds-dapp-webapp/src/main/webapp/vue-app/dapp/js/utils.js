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
