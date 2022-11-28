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

export function startTenant(nftId, email, transactionHash) {
  const formData = new FormData();
  if (email) {
    formData.append('email', email);
  }
  formData.append('transactionHash', transactionHash);
  const params = new URLSearchParams(formData).toString();

  return fetch(`/${window.parentAppLocation}/api/tenants/${nftId}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
      'X-XSRF-TOKEN': getCookie('XSRF-TOKEN'),
    },
    credentials: 'include',
    body: params,
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error(`Error changing tenant status for nft with id ${nftId}`);
    }
  });
}

export function stopTenant(nftId, transactionHash) {
  const formData = new FormData();
  formData.append('transactionHash', transactionHash);
  const params = new URLSearchParams(formData).toString();

  return fetch(`/${window.parentAppLocation}/api/tenants/${nftId}`, {
    method: 'DELETE',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
      'X-XSRF-TOKEN': getCookie('XSRF-TOKEN'),
    },
    credentials: 'include',
    body: params,
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error(`Error changing tenant status for nft with id ${nftId}`);
    }
  });
}

export function getTenantStartDate(nftId) {
  return fetch(`/${window.parentAppLocation}/api/tenants/${nftId}/startDate`, {
    method: 'GET',
    headers: {
      'X-XSRF-TOKEN': getCookie('XSRF-TOKEN'),
    },
    credentials: 'include',
  }).then(resp => {
    if (resp && resp.ok) {
      return resp.text();
    } else {
      throw new Error(`Error getting Deed Tenant #${nftId} Start Date`);
    }
  });
}

export function getTenantInfo(nftId) {
  return fetch(`/${window.parentAppLocation}/api/tenants/${nftId}`, {
    method: 'GET',
    headers: {
      'X-XSRF-TOKEN': getCookie('XSRF-TOKEN'),
    },
    credentials: 'include',
  }).then(resp => {
    if (resp && resp.ok) {
      return resp.json();
    } else {
      throw new Error(`Error getting Deed Tenant #${nftId} information`);
    }
  });
}

export function getTenants() {
  return fetch(`/${window.parentAppLocation}/api/tenants`, {
    method: 'GET',
    headers: {
      'X-XSRF-TOKEN': getCookie('XSRF-TOKEN'),
    },
    credentials: 'include',
  }).then(resp => {
    if (resp && resp.ok) {
      return resp.json();
    } else {
      throw new Error('Error getting Deed Tenants Provisioning information');
    }
  });
}
