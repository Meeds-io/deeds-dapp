/**
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

export function getReports(paramsObj) {
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
  return fetch(`${window.parentAppLocation}/api/hub/reports?${params}`, {
    method: 'GET',
  }).then((resp) => {
    if (resp?.status === 200) {
      return resp.json();
    } else if (resp?.status === 404) {
      return null;
    } else {
      return handleResponseError(resp);
    }
  });
}

export function getReport(hash) {
  return fetch(`${window.parentAppLocation}/api/hub/reports/${hash}`, {
    method: 'GET',
  }).then((resp) => {
    if (resp?.status === 200) {
      return resp.json();
    } else if (resp?.status === 404) {
      return null;
    } else {
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