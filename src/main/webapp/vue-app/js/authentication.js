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
export function login(address, signedMessage) {
  if (hasAuthenticatedLogin()) {
    return logout().then(() => login(address, signedMessage));
  }
  return fetch('/dapp/login', {
    method: 'POST',
    redirect: 'manual',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
      'X-XSRF-TOKEN': getCookie('XSRF-TOKEN'),
    },
    credentials: 'include',
    body: `username=${address}&password=${signedMessage}`,
  }).then(resp => {
    if (resp && resp.ok) {
      document.querySelector('[name=login]').value = address;
    } else {
      throw new Error('Authentication Failed');
    }
  });
}

export function logout() {
  return fetch('/dapp/logout', {
    method: 'POST',
    redirect: 'manual',
    credentials: 'include',
    headers: {
      'X-XSRF-TOKEN': getCookie('XSRF-TOKEN'),
    },
  }).then(resp => {
    if (resp && resp.ok) {
      // Remove logged in address
      document.querySelector('[name=login]').removeAttribute('value');
    } else {
      throw new Error('Logout failed');
    }
  });
}

export function isAuthenticated(address) {
  const login = getAuthenticatedLogin();
  return login && address && address.toUpperCase() === login.toUpperCase();
}

export function hasAuthenticatedLogin() {
  const login = getAuthenticatedLogin();
  return login && login.length;
}

export function getAuthenticatedLogin() {
  return document.querySelector('[name=login]') && document.querySelector('[name=login]').value;
}

export function getCookie(cname) {
  const name = `${cname  }=`;
  const decodedCookie = decodeURIComponent(document.cookie);
  const ca = decodedCookie.split(';');
  for (let i = 0; i <ca.length; i++) {
    let c = ca[i];
    while (c.charAt(0) === ' ') {
      c = c.substring(1);
    }
    if (c.indexOf(name) === 0) {
      return c.substring(name.length, c.length);
    }
  }
  return '';
}