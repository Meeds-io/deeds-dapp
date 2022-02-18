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
export function changeStatus(networkId, deedAddress, nftId, status) {
  return fetch(`/deeds-dapp/api/tenant/${networkId}/${deedAddress}/${nftId}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
      'X-XSRF-TOKEN': getCookie('XSRF-TOKEN'),
    },
    credentials: 'include',
    body: `status=${status.toUpperCase()}`,
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error(`Error changing tenant status to ${status}`);
    }
  });
}

export function getStatus(networkId, deedAddress, nftId) {
  return fetch(`/deeds-dapp/api/tenant/${networkId}/${deedAddress}/${nftId}`, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (resp && resp.ok) {
      return resp.text();
    } else {
      throw new Error('Error getting tenant status');
    }
  });
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