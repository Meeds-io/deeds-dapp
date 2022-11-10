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
package io.meeds.deeds.redis;

import static io.meeds.deeds.redis.model.EventSerialization.OBJECT_MAPPER;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.lettuce.core.pubsub.RedisPubSubListener;
import io.meeds.deeds.redis.model.Event;
import io.meeds.deeds.service.ListenerService;

public class Listener implements RedisPubSubListener<String, String> {

  private static final Logger LOG = LoggerFactory.getLogger(Listener.class);

  private ListenerService     listenerService;

  public Listener(ListenerService listenerService) {
    this.listenerService = listenerService;
  }

  @Override
  public void message(String channel, String message) {
    Event event;
    try {
      event = OBJECT_MAPPER.readValue(message, Event.class);
    } catch (Exception e) {
      throw new IllegalStateException("An error occurred while parsing JSON to POJO object:" + message, e);
    }
    try {
      listenerService.triggerEvent(event);
    } catch (Exception e) {
      throw new IllegalStateException("An error occurred while triggering event: " + message, e);
    }
  }

  @Override
  public void message(String pattern, String channel, String message) {
    Event event;
    try {
      event = OBJECT_MAPPER.readValue(message, Event.class);
    } catch (Exception e) {
      throw new IllegalStateException("An error occurred while parsing JSON to POJO object:" + message, e);
    }
    try {
      listenerService.triggerEvent(event);
    } catch (Exception e) {
      throw new IllegalStateException("An error occurred while triggering event: " + message, e);
    }
  }

  @Override
  public void subscribed(String channel, long count) {
    LOG.info("ListenerService subscribed to channel '{}', count = {}", channel, count);
  }

  @Override
  public void psubscribed(String pattern, long count) {
    LOG.info("ListenerService psubscribed to pattern '{}', count = {}", pattern, count);
  }

  @Override
  public void unsubscribed(String channel, long count) {
    LOG.info("ListenerService unsubscribed to channel '{}', count = {}", channel, count);
  }

  @Override
  public void punsubscribed(String pattern, long count) {
    LOG.info("ListenerService punsubscribed to pattern '{}', count = {}", pattern, count);
  }
}
