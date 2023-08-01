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

export function getLeases(paramsObj, networkId) {
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
  formData.append('networkId', networkId || 0);
  const params = new URLSearchParams(formData).toString();
  return fetch(`${window.parentAppLocation}/api/leases?${params}`, {
    method: 'GET',
    headers: {
      'X-XSRF-TOKEN': getCookie('XSRF-TOKEN'),
    },
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error(`Error getting leases for nft with params ${JSON.stringify(paramsObj)}`);
    } else {
      return resp.json();
    }
  });
}

export function getLease(leaseId, refreshFromBlockchain) {
  const headers = {
    'X-XSRF-TOKEN': getCookie('XSRF-TOKEN'),
  };
  if (refreshFromBlockchain) {
    headers['X-REFRESH'] = 'true';
  }
  return fetch(`${window.parentAppLocation}/api/leases/${leaseId}`, {
    method: 'GET',
    headers,
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error(`Error getting lease with id ${leaseId}`);
    } else {
      return resp.json();
    }
  });
}

export function createLease(offerId, transactionHash, code) {
  const formData = new FormData();
  formData.append('offerId', offerId);
  formData.append('transactionHash', transactionHash);
  const params = new URLSearchParams(formData).toString();
  return fetch(`${window.parentAppLocation}/api/leases/`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
      'X-XSRF-TOKEN': getCookie('XSRF-TOKEN'),
      'X-AUTHORIZATION': code,
    },
    credentials: 'include',
    body: params,
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Error creating deed lease');
    } else {
      return resp.json();
    }
  });
}

export function payRent(leaseId, ownerAddress, paidMonths, transactionHash) {
  const formData = new FormData();
  formData.append('ownerAddress', ownerAddress);
  formData.append('paidMonths', paidMonths);
  formData.append('transactionHash', transactionHash);
  const params = new URLSearchParams(formData).toString();
  return fetch(`${window.parentAppLocation}/api/leases/${leaseId}`, {
    method: 'PATCH',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
      'X-XSRF-TOKEN': getCookie('XSRF-TOKEN'),
    },
    credentials: 'include',
    body: params,
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Error indicating a lease payment');
    } else {
      return resp.json();
    }
  });
}

export function endLease(leaseId, transactionHash) {
  const formData = new FormData();
  formData.append('transactionHash', transactionHash);
  const params = new URLSearchParams(formData).toString();
  return fetch(`${window.parentAppLocation}/api/leases/${leaseId}`, {
    method: 'DELETE',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
      'X-XSRF-TOKEN': getCookie('XSRF-TOKEN'),
    },
    credentials: 'include',
    body: params,
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Error indicating a lease end');
    } else {
      return resp.json();
    }
  });
}
