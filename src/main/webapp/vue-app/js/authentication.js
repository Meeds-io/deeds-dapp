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
export function login(address, message, signedMessage) {
  if (hasAuthenticatedLogin()) {
    return logout().then(() => login(address, signedMessage));
  }
  const formData = new FormData();
  formData.append('username', address);
  formData.append('password', signedMessage);
  formData.append('message', message);
  const params = new URLSearchParams(formData).toString();

  return fetch('/deeds-dapp/login', {
    method: 'POST',
    redirect: 'manual',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
      'X-XSRF-TOKEN': getCookie('XSRF-TOKEN'),
    },
    credentials: 'include',
    body: params,
  }).then(resp => {
    if (resp && resp.ok) {
      document.querySelector('[name=login]').value = address;
    } else {
      throw new Error('Authentication Failed');
    }
  });
}

export function logout() {
  return fetch('/deeds-dapp/logout', {
    method: 'POST',
    redirect: 'manual',
    credentials: 'include',
    headers: {
      'X-XSRF-TOKEN': getCookie('XSRF-TOKEN'),
    },
  }).then(resp => {
    if (resp && resp.ok) {
      return resp.text();
    } else {
      throw new Error('Logout failed');
    }
  }).then(token => {
    // Remove logged in address
    document.querySelector('[name=login]').removeAttribute('value');
    // Refresh login Message
    document.querySelector('[name=loginMessage]').value = token || '';
  });
}

export function isAuthenticated(address) {
  const userLogin = getAuthenticatedLogin();
  return userLogin && address && address.toUpperCase() === userLogin.toUpperCase() || false;
}

export function hasAuthenticatedLogin() {
  const userLogin = getAuthenticatedLogin();
  return userLogin && userLogin.indexOf('0x') === 0;
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