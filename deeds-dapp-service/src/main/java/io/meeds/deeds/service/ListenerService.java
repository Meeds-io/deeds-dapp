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
package io.meeds.deeds.service;

import java.util.*;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.lettuce.core.RedisClient;
import io.lettuce.core.pubsub.RedisPubSubListener;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands;
import io.meeds.deeds.listener.EventListener;
import lombok.*;

@Component
public class ListenerService implements ApplicationContextAware {

  private static final Logger       LOG           = LoggerFactory.getLogger(ListenerService.class);

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  static {
    // Workaround when Jackson is defined in shared library with different
    // version and without artifact jackson-datatype-jsr310
    OBJECT_MAPPER.registerModule(new JavaTimeModule());
  }

  @Value("${meeds.redis.listenerService.channel:channel}")
  private String                              channelName;

  @Autowired(required = false)
  private RedisClient                         redisClient;

  private List<EventListener<?>>              eventListeners = new ArrayList<>();

  private Map<String, List<EventListener<?>>> listeners      = new HashMap<>();

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    @SuppressWarnings("rawtypes")
    Map<String, EventListener> eventListenerBeans = applicationContext.getBeansOfType((EventListener.class));
    for (EventListener<?> eventListener : eventListenerBeans.values()) {
      eventListeners.add(eventListener);
    }
  }

  @PostConstruct
  public void init() {
    initSubscription();
    initListeners();
  }

  public void addListener(EventListener<?> listener) {
    List<String> supportedEvents = listener.getSupportedEvents();
    if (!CollectionUtils.isEmpty(supportedEvents)) {
      supportedEvents.forEach(eventName -> addListener(eventName, listener));
    }
  }

  public void addListener(String eventName, EventListener<?> listener) {
    listeners.computeIfAbsent(eventName, key -> new ArrayList<>())
             .add(listener);
  }

  public void removeListsner(String listenerName) {
    listeners.forEach((key, eventListeners) -> { // NOSONAR
      eventListeners.removeIf(eventListener -> StringUtils.equals(eventListener.getName(), listenerName));
    });
  }

  public void removeListsner(String eventName, String listenerName) {
    listeners.computeIfPresent(eventName, (key, list) -> {
      list.removeIf(eventListener -> StringUtils.equals(eventListener.getName(), listenerName));
      return list;
    });
  }

  public void publishEvent(String eventName, Object data) {
    Event event = new Event(eventName, data, data.getClass().getName());
    // Trigger Event Remotely
    try {
      StatefulRedisPubSubConnection<String, String> connection = redisClient.connectPubSub();
      RedisPubSubAsyncCommands<String, String> async = connection.async();
      String eventJsonString = serializeObjectToJson(event);
      async.publish(channelName, eventJsonString);
    } catch (Exception e) {
      LOG.warn("Redis connection failure, event {} will not be triggered remotely, try to trigger it locally only",
               event.getEventName(),
               e);

      // Trigger events locally
      publishEventLocally(event);
    }
  }

  protected void publishEventLocally(Event event) {
    List<EventListener<?>> listenerList = listeners.get(event.getEventName());
    if (!CollectionUtils.isEmpty(listenerList)) {
      listenerList.forEach(listener -> listener.handleEvent(event.getEventName(), event.getData()));
    }
  }

  protected void triggerEvent(Event event) {
    String eventName = event.getEventName();
    Object data = event.getData();

    List<EventListener<?>> listenerList = listeners.get(eventName);
    if (!CollectionUtils.isEmpty(listenerList)) {
      listenerList.forEach(listener -> listener.handleEvent(eventName, data));
    }
  }

  private void initListeners() {
    // Reorder Listeners by Event Name and use a new Map
    // In order to allow programatically add/remove listeners
    if (!CollectionUtils.isEmpty(eventListeners)) {
      eventListeners.forEach(this::addListener);
    }
  }

  private void initSubscription() {
    try {
      // Add Redis Pub/Sub Listener
      StatefulRedisPubSubConnection<String, String> connection = redisClient.connectPubSub();
      connection.addListener(new Listener());

      // Subscribe to channel
      RedisPubSubAsyncCommands<String, String> async = connection.async();
      async.subscribe(channelName);
    } catch (Exception e) {
      LOG.warn("Redis connection failure", e);
    }
  }

  private String serializeObjectToJson(Event event) {
    try {
      return OBJECT_MAPPER.writeValueAsString(event);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("An error occurred while parsing POJO object to JSON:" + event, e);
    }
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Event {

    private String eventName;

    private Object data;

    private String dataClassName;

  }

  public class Listener implements RedisPubSubListener<String, String> {

    @Override
    public void message(String channel, String message) {
      try {
        Event event = OBJECT_MAPPER.readValue(message, Event.class);
        triggerEvent(event);
      } catch (Exception e) {
        throw new IllegalStateException("An error occurred while parsing JSON to POJO object:" + message, e);
      }
    }

    @Override
    public void message(String pattern, String channel, String message) {
      try {
        Event event = OBJECT_MAPPER.readValue(message, Event.class);
        triggerEvent(event);
      } catch (Exception e) {
        throw new IllegalStateException("An error occurred while parsing JSON to POJO object:" + message, e);
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

}
