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

import static io.meeds.deeds.redis.model.EventSerialization.OBJECT_MAPPER;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands;
import io.meeds.deeds.listener.EventListener;
import io.meeds.deeds.model.DeedTenantEvent;
import io.meeds.deeds.redis.Listener;
import io.meeds.deeds.redis.RedisConfigurationProperties;
import io.meeds.deeds.redis.model.Event;
import io.meeds.deeds.storage.DeedTenantEventRepository;
import lombok.Getter;

@Component
public class ListenerService implements ApplicationContextAware {

  private static final Logger                             LOG               = LoggerFactory.getLogger(ListenerService.class);

  protected StatefulRedisPubSubConnection<String, String> subscriptionConnection;

  protected StatefulRedisPubSubConnection<String, String> publicationConnection;

  protected Listener                                      redisListener;

  @Autowired
  private RedisConfigurationProperties                    redisProperties;

  @Autowired(required = false)
  private RedisClient                                     redisClient;

  @Autowired
  private DeedTenantEventRepository                       deedTenantEventRepository;

  private ApplicationContext                              applicationContext;

  private List<EventListener<?>>                          eventListeners    = new ArrayList<>();

  private Map<String, List<EventListener<?>>>             listeners;

  @Getter
  private Instant                                         lastEventScanDate = Instant.now();

  public ListenerService() {
    redisListener = new Listener(this);
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  @PostConstruct
  public void init() {
    initSubscription();
    initElasticsearchEventScanning();
  }

  @PreDestroy
  public void destroy() {
    if (this.subscriptionConnection != null && this.subscriptionConnection.isOpen()) {
      this.subscriptionConnection.close();
    }
    if (this.publicationConnection != null && this.publicationConnection.isOpen()) {
      this.publicationConnection.close();
    }
  }

  public void addListener(EventListener<?> listener) {
    List<String> supportedEvents = listener.getSupportedEvents();
    if (!CollectionUtils.isEmpty(supportedEvents)) {
      supportedEvents.forEach(eventName -> addListener(eventName, listener));
    }
  }

  public void addListener(String eventName, EventListener<?> listener) {
    getListeners().computeIfAbsent(eventName, key -> new ArrayList<>()).add(listener);
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
      RedisAsyncCommands<String, String> async = getPublicationConnection().async();
      String eventJsonString = serializeObjectToJson(event);
      RedisFuture<Long> publish = async.publish(getChannelName(), eventJsonString);
      if (publish == null) {
        throw new IllegalStateException("Redis returned null publication event");
      }
      if (StringUtils.isNotBlank(publish.getError())) {
        throw new IllegalStateException("Redis returned an error while publishing event: " + publish.getError());
      }
    } catch (Exception e) {
      LOG.info("Redis connection failure, event {} will not be triggered remotely, try to trigger it locally only",
               event.getEventName());

      // Trigger events locally and using ES
      publishEventFallback(event);
    }
  }

  @SuppressWarnings("deprecation")
  public void triggerEvent(Event event) {
    String eventName = event.getEventName();
    Object data = event.getData();

    List<EventListener<?>> listenerList = getListeners().get(eventName);
    if (!CollectionUtils.isEmpty(listenerList)) {
      listenerList.forEach(listener -> listener.handleEvent(eventName, data));
    }
  }

  protected void publishEventFallback(Event event) {
    triggerEvent(event);
    publishEventOnElasticsearch(event);
  }

  protected void publishEventOnElasticsearch(Event event) {
    String eventJsonString = serializeObjectToJson(event);
    boolean hasClientName = StringUtils.isNotBlank(getClientName());
    List<String> consumers = hasClientName ? Collections.singletonList(redisProperties.getClientName())
                                           : Collections.emptyList();
    DeedTenantEvent deedTenantEvent = new DeedTenantEvent(event.getEventName(),
                                                          eventJsonString,
                                                          consumers,
                                                          Instant.now());
    deedTenantEventRepository.save(deedTenantEvent);
  }

  public void triggerElasticSearchEvents() {
    Stream<DeedTenantEvent> events =
                                   deedTenantEventRepository.findByDateGreaterThanEqualAndConsumersNotOrderByDateAsc(lastEventScanDate,
                                                                                                                     getClientName());
    events.forEach(event -> {
      try {
        if (hasListeners(event.getEventName())) {
          triggerElasticsearchEvent(event);
          addElasticsearchEventCurrentConsumer(event);
        }
      } finally {
        if (lastEventScanDate.isBefore(event.getDate())) {
          lastEventScanDate = event.getDate();
        }
      }
    });
  }

  public void cleanupElasticsearchEvents() {
    Stream<DeedTenantEvent> oldEvents =
                                      deedTenantEventRepository.findByDateLessThan(lastEventScanDate.minus(12, ChronoUnit.HOURS));
    oldEvents.forEach(event -> {
      try {
        deedTenantEventRepository.deleteById(event.getId());
      } catch (Exception e) {
        LOG.warn("An error occurred while deleting event with id {}/{}", event.getId(), event.getEventName());
      }
    });
  }

  protected void initSubscription() {
    try {
      if (this.subscriptionConnection != null && this.subscriptionConnection.isOpen()) {
        return;
      }
      // Add Redis Pub/Sub Listener
      this.subscriptionConnection = redisClient.connectPubSub();
      subscriptionConnection.addListener(redisListener);

      // Subscribe to channel
      RedisPubSubAsyncCommands<String, String> async = subscriptionConnection.async();
      async.subscribe(getChannelName());
    } catch (Exception e) {
      LOG.warn("Redis connection failure: {}. Persistent ES Events System will be used instead.", e.getMessage());
    }
  }

  protected void initElasticsearchEventScanning() {
    Page<DeedTenantEvent> page = deedTenantEventRepository.findByConsumersNotOrderByDateDesc(getClientName(), Pageable.ofSize(1));
    if (page != null && page.getSize() > 0 && !page.getContent().isEmpty()) {
      DeedTenantEvent lastTriggeredEvent = page.getContent().get(0);
      lastEventScanDate = lastTriggeredEvent.getDate();
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

  protected StatefulRedisPubSubConnection<String, String> getPublicationConnection() {
    if (publicationConnection == null || !publicationConnection.isOpen()) {
      publicationConnection = redisClient.connectPubSub();
    }
    return publicationConnection;
  }

  private void triggerElasticsearchEvent(DeedTenantEvent event) {
    try {
      redisListener.message(getChannelName(), event.getObjectJson());
    } catch (Exception e) {
      LOG.warn("Error while triggering event from ES {}/{}", event.getId(), event.getEventName(), e);
    }
  }

  private void addElasticsearchEventCurrentConsumer(DeedTenantEvent event) {
    try {
      // Get a fresh event detail
      event = deedTenantEventRepository.findById(event.getId()).orElse(event);
      List<String> consumers = CollectionUtils.isEmpty(event.getConsumers()) ? new ArrayList<>()
                                                                             : new ArrayList<>(event.getConsumers());
      consumers.add(getClientName());
      event.setConsumers(consumers);
      deedTenantEventRepository.save(event);
    } catch (Exception e) {
      LOG.warn("Error saving client {} in consumers list of event {}/{}", getClientName(), event.getId(), event.getEventName());
    }
  }

  private boolean hasListeners(String eventName) {
    return !CollectionUtils.isEmpty(getListeners().get(eventName));
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

  private String getClientName() {
    return redisProperties.getClientName();
  }

}
