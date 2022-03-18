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
import java.util.Map.Entry;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.lettuce.core.RedisClient;
import io.lettuce.core.pubsub.RedisPubSubListener;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands;
import io.meeds.deeds.listener.EventListener;
import io.meeds.deeds.redis.RedisConfigurationProperties;
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

  @Autowired(required = false)
  private RedisConfigurationProperties        redisProperties;

  @Autowired(required = false)
  private RedisClient                         redisClient;

  private ApplicationContext                  applicationContext;

  private List<EventListener<?>>              eventListeners = new ArrayList<>();

  private Map<String, List<EventListener<?>>> listeners;

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  @PostConstruct
  public void init() {
    initSubscription();
  }

  public void addListener(EventListener<?> listener) {
    List<String> supportedEvents = listener.getSupportedEvents();
    if (!CollectionUtils.isEmpty(supportedEvents)) {
      supportedEvents.forEach(eventName -> addListener(eventName, listener));
    }
  }

  public void addListener(String eventName, EventListener<?> listener) {
    getListeners().computeIfAbsent(eventName, key -> new ArrayList<>())
                  .add(listener);
  }

  public void removeListsner(String listenerName) {
    Iterator<Entry<String, List<EventListener<?>>>> iterator = getListeners().entrySet().iterator();
    iterator.forEachRemaining(entry -> {
      List<EventListener<?>> list = entry.getValue();
      list.removeIf(eventListener -> StringUtils.equals(eventListener.getName(), listenerName));
      if (list.isEmpty()) {
        iterator.remove();
      }
    });
  }

  public void removeListsner(String eventName, String listenerName) {
    Iterator<Entry<String, List<EventListener<?>>>> iterator = getListeners().entrySet().iterator();
    iterator.forEachRemaining(entry -> {
      if (!StringUtils.equals(entry.getKey(), eventName)) {
        return;
      }
      List<EventListener<?>> list = entry.getValue();
      list.removeIf(eventListener -> StringUtils.equals(eventListener.getName(), listenerName));
      if (list.isEmpty()) {
        iterator.remove();
      }
    });
  }

  public void publishEvent(String eventName, Object data) {
    Event event = new Event(eventName, data, data == null ? null : data.getClass().getName());

    // Trigger Event Remotely
    try {
      StatefulRedisPubSubConnection<String, String> connection = redisClient.connectPubSub();
      RedisPubSubAsyncCommands<String, String> async = connection.async();
      String eventJsonString = serializeObjectToJson(event);
      async.publish(getChannelName(), eventJsonString);
    } catch (Exception e) {
      LOG.warn("Redis connection failure, event {} will not be triggered remotely, try to trigger it locally only",
               event.getEventName(),
               e);

      // Trigger events locally
      publishEventLocally(event);
    }
  }

  @SuppressWarnings("deprecation")
  protected void publishEventLocally(Event event) {
    List<EventListener<?>> listenerList = getListeners().get(event.getEventName());
    if (!CollectionUtils.isEmpty(listenerList)) {
      listenerList.forEach(listener -> listener.handleEvent(event.getEventName(), event.getData()));
    }
  }

  @SuppressWarnings("deprecation")
  protected void triggerEvent(Event event) {
    String eventName = event.getEventName();
    Object data = event.getData();

    List<EventListener<?>> listenerList = getListeners().get(eventName);
    if (!CollectionUtils.isEmpty(listenerList)) {
      listenerList.forEach(listener -> listener.handleEvent(eventName, data));
    }
  }

  protected void initSubscription() {
    try {
      // Add Redis Pub/Sub Listener
      StatefulRedisPubSubConnection<String, String> connection = redisClient.connectPubSub();
      connection.addListener(new Listener());

      // Subscribe to channel
      RedisPubSubAsyncCommands<String, String> async = connection.async();
      async.subscribe(getChannelName());
    } catch (Exception e) {
      LOG.warn("Redis connection failure", e);
    }
  }

  protected Map<String, List<EventListener<?>>> getListeners() {// NOSONAR
    if (listeners == null) {
      initListeners();
    }
    return listeners;
  }

  @SuppressWarnings("rawtypes")
  protected synchronized void initListeners() {
    if (listeners == null) {
      listeners = new HashMap<>();
    }
    Map<String, EventListener> eventListenerBeans = applicationContext.getBeansOfType(EventListener.class);
    for (EventListener<?> eventListener : eventListenerBeans.values()) {
      eventListeners.add(eventListener);
    }
    // Reorder Listeners by Event Name and use a new Map
    // In order to allow programatically add/remove listeners
    if (!CollectionUtils.isEmpty(eventListeners)) {
      eventListeners.forEach(this::addListener);
    }
  }

  private String serializeObjectToJson(Event event) {
    try {
      return OBJECT_MAPPER.writeValueAsString(event);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("An error occurred while parsing POJO object to JSON:" + event, e);
    }
  }

  private String getChannelName() {
    return redisProperties.getChannelName();
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonDeserialize(using = EventDeserializer.class)
  public static class Event {

    private String eventName;

    private Object data;

    private String dataClassName;

  }

  public static class EventDeserializer extends StdDeserializer<Event> {

    private static final long serialVersionUID = -5369587672932623714L;

    public EventDeserializer() {
      this(null);
    }

    public EventDeserializer(Class<?> vc) {
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
