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
package io.meeds.deeds.utils;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.meeds.deeds.constant.WomParsingException;

public class JsonUtils {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  static {
    // Workaround when Jackson is defined in shared library with different
    // version and without artifact jackson-datatype-jsr310
    OBJECT_MAPPER.registerModule(new JavaTimeModule());
  }

  private JsonUtils() {
    // Utils class
  }

  public static final <T> T fromJsonString(String value, Class<T> resultClass) throws WomParsingException {
    if (StringUtils.isBlank(value)) {
      return null;
    }
    try {
      return OBJECT_MAPPER.readValue(value, resultClass);
    } catch (Exception e) {
      throw new WomParsingException("wom.unableToParseObject", e);
    }
  }

  public static final String toJsonString(Object object) throws WomParsingException {
    try {
      return OBJECT_MAPPER.writeValueAsString(object);
    } catch (Exception e) {
      throw new WomParsingException("wom.unableToParseObject", e);
    }
  }

}
