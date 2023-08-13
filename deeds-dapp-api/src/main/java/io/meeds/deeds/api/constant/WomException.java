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
import lombok.Getter;

public class WomException extends Exception {

  private static final long serialVersionUID = -31643228664080493L;

  @Getter
  private final boolean     shouldRetry;

  @Getter
  private final int         code;

  public WomException(String message) {
    super(message);
    this.shouldRetry = false;
    this.code = 0;
  }

  public WomException(String message, Exception e) {
    super(message, e);
    this.shouldRetry = false;
    this.code = 0;
  }

  public WomException(String message, boolean shouldRetry) {
    super(message);
    this.shouldRetry = shouldRetry;
    this.code = 0;
  }

  public WomException(String message, boolean shouldRetry, int code) {
    super(message);
    this.shouldRetry = shouldRetry;
    this.code = code;
  }

  public WomException(WomErrorMessage errorMessage) {
    super(errorMessage.getMessageKey());
    this.shouldRetry = errorMessage.isShouldRetry();
    this.code = errorMessage.getCode();
  }

  public WomException(WomErrorMessage errorMessage, Exception e) {
    super(errorMessage.getMessageKey(), e);
    this.shouldRetry = errorMessage.isShouldRetry();
    this.code = errorMessage.getCode();
  }

  public WomErrorMessage getErrorCode() {
    return new WomErrorMessage(code, shouldRetry, getMessage());
  }

}
