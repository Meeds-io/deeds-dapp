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
    if (resp?.ok) {
      return resp.json();
    } else {
      throw new Error(`Error getting hub reports with params ${params}`);
    }
  });
}

export function getReport(id) {
  return fetch(`${window.parentAppLocation}/api/hub/reports/${id}`, {
    method: 'GET',
  }).then((resp) => {
    if (resp?.ok) {
      return resp.json();
    } else {
      throw new Error(`Error getting hub report with id ${id}`);
    }
  });
}

export function getReward(id) {
  return fetch(`${window.parentAppLocation}/api/uem/rewards/${id}`, {
    method: 'GET',
  }).then((resp) => {
    if (resp?.ok) {
      return resp.json();
    } else {
      throw new Error(`Error getting UEM reward with id ${id}`);
    }
  });
}

export function getClaimableRewards(address) {
  return fetch(`${window.parentAppLocation}/api/uem/rewards/claimable/${address}`, {
    method: 'GET',
  }).then((resp) => {
    if (resp?.ok) {
      return resp.json();
    } else {
      throw new Error(`Error getting UEM claimable rewards for address ${address}`);
    }
  });
}
