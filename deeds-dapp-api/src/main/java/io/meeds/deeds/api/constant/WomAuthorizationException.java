/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.deeds.api.constant;

import io.meeds.deeds.api.model.WomErrorMessage;

public class WomAuthorizationException extends WomException {

  private static final long serialVersionUID = 4637971005730642774L;

  public WomAuthorizationException(String message) {
    super(message);
  }

  public WomAuthorizationException(String message, Exception e) {
    super(message, e);
  }

  public WomAuthorizationException(String message, boolean shouldRetry) {
    super(message, shouldRetry);
  }

  public WomAuthorizationException(String message, boolean shouldRetry, int code) {
    super(message, shouldRetry, code);
  }

  public WomAuthorizationException(WomErrorMessage errorMessage) {
    super(errorMessage);
  }

  public WomAuthorizationException(WomErrorMessage errorMessage, Exception e) {
    super(errorMessage, e);
  }
}
