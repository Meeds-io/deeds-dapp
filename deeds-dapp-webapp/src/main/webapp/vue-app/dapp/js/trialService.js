/*
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2024 Meeds Association contact@meeds.io
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
export function saveTrial(fullname, position, organization, motivation, email, code) {
  const formData = new FormData();
  formData.append('fullname', fullname);
  formData.append('position', position);
  formData.append('organization', organization);
  formData.append('motivation', motivation);
  formData.append('email', email);
  const params = new URLSearchParams(formData).toString();
  return fetch(`${window.parentAppLocation}/api/trials/contact`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
      'X-AUTHORIZATION': code,
    },
    body: params,
  }).then((resp) => {
    if (resp && resp.ok) {
      return resp.json();
    } else {
      throw new Error('Error saving trial');
    }
  });
}
  