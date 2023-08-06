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
export function getHubs(paramsObj) {
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
  const params = new URLSearchParams(formData).toString();
  return fetch(`${window.parentAppLocation}/api/hubs?${params}`, {
    method: 'GET',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error(`Error getting hubs with params ${JSON.stringify(paramsObj)}`);
    } else {
      return resp.json();
    }
  });
}

export function getHub(address) {
  return fetch(`${window.parentAppLocation}/api/hubs/${address}`, {
    method: 'GET',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error(`Error getting hub with address ${address}`);
    } else {
      return resp.json();
    }
  });
}

export function getToken() {
  return fetch(`${window.parentAppLocation}/api/hubs/token`, {
    method: 'GET',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Error getting hub Token');
    } else {
      return resp.text();
    }
  });
}

export function disconnectFromWoM(request) {
  return fetch(`${window.parentAppLocation}/api/hubs`, {
    method: 'DELETE',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(request),
  }).then((resp) => {
    if (!resp?.ok) {
      return handleResponseError(resp);
    }
  });
}

export function getErrorKey(error) {
  try {
    return JSON.parse(error).messageKey.split(':')[0];
  } catch (e) {
    return String(error).split(':')[0];
  }
}

function handleResponseError(resp) {
  return resp.text()
    .then(error => {
      throw new Error(getErrorKey(error));
    });
}