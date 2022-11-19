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
import {getCookie} from './authentication';

export function getOffers(paramsObj, networkId) {
  const formData = new FormData();
  if (paramsObj) {
    Object.keys(paramsObj).forEach(key => {
      const value = paramsObj[key];
      if (window.Array && Array.isArray && Array.isArray(value)) {
        value.forEach(val => formData.append(key, val));
      } else {
        formData.append(key, value);
      }
    });
  }
  formData.append('networkId', networkId);
  const params = new URLSearchParams(formData).toString();
  return fetch(`/${window.parentAppLocation}/api/offers?${params}`, {
    method: 'GET',
    headers: {
      'X-XSRF-TOKEN': getCookie('XSRF-TOKEN'),
    },
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error(`Error getting offers for nft with params ${paramsObj}`);
    } else {
      return resp.json();
    }
  });
}

export function getOffer(offerId) {
  return fetch(`/${window.parentAppLocation}/api/offers/${offerId}`, {
    method: 'GET',
    headers: {
      'X-XSRF-TOKEN': getCookie('XSRF-TOKEN'),
    },
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error(`Error getting offer with id ${offerId}`);
    } else {
      return resp.json();
    }
  });
}

export function createOffer(offer, code) {
  return fetch(`/${window.parentAppLocation}/api/offers/`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'X-XSRF-TOKEN': getCookie('XSRF-TOKEN'),
      'X-AUTHORIZATION': code,
    },
    credentials: 'include',
    body: JSON.stringify(offer),
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Error creating deed offer');
    } else {
      return resp.json();
    }
  });
}

export function rentOffer() {
  return Promise.resolve();
}

export function updateOffer(offerId, offer) {
  return fetch(`/${window.parentAppLocation}/api/offers/${offerId}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      'X-XSRF-TOKEN': getCookie('XSRF-TOKEN'),
    },
    credentials: 'include',
    body: JSON.stringify(offer),
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error(`Error updating deed offer with id ${offerId}`);
    } else {
      return resp.json();
    }
  });
}

export function deleteOffer(offerId) {
  return fetch(`/${window.parentAppLocation}/api/offers/${offerId}`, {
    method: 'DELETE',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
      'X-XSRF-TOKEN': getCookie('XSRF-TOKEN'),
    },
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error(`Error deleting deed offer with id ${offerId}`);
    }
  });
}
