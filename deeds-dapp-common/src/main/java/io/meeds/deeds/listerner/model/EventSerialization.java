/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 - 2022 Meeds Association contact@meeds.io
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
package io.meeds.deeds.listerner.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class EventSerialization extends StdDeserializer<Event> {

  public static final ObjectMapper OBJECT_MAPPER;

  private static final long        serialVersionUID = -5369587672932623714L;

  private static final Logger      LOG              = LoggerFactory.getLogger(EventSerialization.class);

  static {
    // Workaround when Jackson is defined in shared library with different
    // version and without artifact jackson-datatype-jsr310
    OBJECT_MAPPER = JsonMapper.builder()
                              .configure(JsonReadFeature.ALLOW_MISSING_VALUES, true)
                              .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                              .build();
    OBJECT_MAPPER.registerModule(new JavaTimeModule());
  }

  public EventSerialization() {
    this(null);
  }

  public EventSerialization(Class<?> vc) {
    super(vc);
  }

  @Override
  public Event deserialize(JsonParser p, DeserializationContext ctxt) {
    try {
      JsonNode node = p.getCodec().readTree(p);
      String eventName = node.get("eventName").asText();
      String dataClassName = node.get("dataClassName").asText();
      String dataJson = node.get("data").toString();
      Object data = OBJECT_MAPPER.readValue(dataJson, Class.forName(dataClassName));
      return new Event(eventName, data, dataClassName);
    } catch (Exception e) {
      LOG.debug("Error reading value of event", e);
      return null;
    }
  }

}
