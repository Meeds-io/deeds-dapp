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

import static io.meeds.deeds.listerner.model.EventSerialization.OBJECT_MAPPER;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.StampedLock;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.collections.MapUtils;
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

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.meeds.deeds.elasticsearch.model.DeedTenantEvent;
import io.meeds.deeds.listener.EventListener;
import io.meeds.deeds.listerner.model.Event;
import io.meeds.deeds.redis.RedisConfigurationProperties;
import io.meeds.deeds.scheduling.task.ListenerEventCleanupTask;
import io.meeds.deeds.scheduling.task.ListenerEventTriggerTask;
import io.meeds.deeds.storage.DeedTenantEventRepository;

@Component
public class ListenerService implements ApplicationContextAware {

  public static final String                              ES_LAST_SCANNED_DATE_SETTING_NAME = "ES-LAST-SCANNED-DATE";

  public static final Logger                              LOG                               =
                                                              LoggerFactory.getLogger(ListenerService.class);

  protected StatefulRedisPubSubConnection<String, String> subscriptionConnection;

  protected StatefulRedisPubSubConnection<String, String> publicationConnection;

  @Autowired
  private RedisConfigurationProperties                    redisProperties;

  @Autowired(required = false)
  private RedisClient                                     redisClient;

  @Autowired
  private SettingService                                  settingService;

  @Autowired
  private DeedTenantEventRepository                       deedTenantEventRepository;

  @Value("${meeds.elasticsearch.listener.events.cleanupHoursPeriodicity:}")
  private String                                          cleanupHoursPeriodicity;

  private ApplicationContext                              applicationContext;

  private List<EventListener<?>>                          eventListeners                    = new ArrayList<>();

  private Map<String, List<EventListener<?>>>             listeners;

  private StampedLock                                     elasticsearchEventReadingLock     = new StampedLock();

  private String                                          clientName;

  /**
   * Triggers an event locally and remotely using persistent event listening
   * based on a dedicated Elasticsearch index to persist the event and generates
   * a unique identifier for it. This event id will be published on Redis as
   * well in order to instantly inform all consumers that a new event has been
   * published in Elasticsearch. If redis is down or out of service, a Scheduled
   * Job {@link ListenerEventTriggerTask} on each consumer will retrieve events
   * periodically.
   * 
   * @param eventName Event Name as configured for listeners in
   *                    {@link EventListener#getSupportedEvents()}
   * @param data      Data Object to pass to listeners when executing
   *                    {@link EventListener#onEvent(String, Object)}
   */
  public void publishEvent(String eventName, Object data) {
    LOG.debug("{} - Publish event {} locally with data {}",
              getClientName(),
              eventName,
              data);

    Event event = new Event(eventName, data, data == null ? null : data.getClass().getName());
    triggerEventLocally(event);
    DeedTenantEvent persistentEvent = publishEventOnElasticsearch(event);
    publishEventOnRedis(persistentEvent.getId(), eventName);
  }

  /**
   * Add manually a listener for a dedicated event. This method is useless in
   * general case since the listeners are added automatically when defined as
   * any regular Spring Bean {@link Component} and that it inherits from
   * {@link EventListener}
   * 
   * @param listener local {@link EventListener}
   */
  public void addListener(EventListener<?> listener) {
    List<String> supportedEvents = listener.getSupportedEvents();
    if (!CollectionUtils.isEmpty(supportedEvents)) {
      supportedEvents.forEach(eventName -> getListeners().computeIfAbsent(eventName, key -> new ArrayList<>()).add(listener));
    }
  }

  /**
   * Removes all {@link EventListener} that have {@link EventListener#getName()}
   * the designated name in param
   * 
   * @param listenerName name of listeners to remove
   */
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

  /**
   * Removes all {@link EventListener} that have {@link EventListener#getName()}
   * the designated name in param and a
   * {@link EventListener#getSupportedEvents()} as designated in param name
   * 
   * @param eventName    {@link EventListener#getSupportedEvents()} event name
   * @param listenerName {@link EventListener#getName()} name
   */
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

  /**
   * An INTERNAL method that is used to be triggered periodically through the
   * Scheduled Task {@link ListenerEventTriggerTask}. In general case, this must
   * not be used outside this provided Task.
   */
  public void triggerElasticSearchEvents() {
    executeElasticSearchScanning(() -> {
      Instant lastEventScanDate = getLastELasticsearchScanDate();
      List<DeedTenantEvent> events;
      events = deedTenantEventRepository.findByDateGreaterThanEqualAndConsumersNotOrderByDateAsc(lastEventScanDate,
                                                                                                 getClientName());
      try {
        for (DeedTenantEvent persistentEvent : events) {
          if (hasListeners(persistentEvent.getEventName())) {
            triggerElasticsearchEvent(persistentEvent);
          }
          lastEventScanDate = maxInstant(lastEventScanDate, persistentEvent.getDate());
        }
      } finally {
        saveLastELasticsearchScanDate(lastEventScanDate);
      }
    });
  }

  /**
   * An INTERNAL method that is used to be triggered periodically through the
   * Scheduled Task {@link ListenerEventCleanupTask}. In general case, this must
   * not be used outside this provided Scheduled Task. This cleanup will be
   * triggered on one single instance which has made a configuration of its
   * periodicity, which is by default, the dApp only.
   */
  public void cleanupElasticsearchEvents() {
    if (StringUtils.isNotBlank(cleanupHoursPeriodicity)) {
      Instant lastEventScanDate = getLastELasticsearchScanDate();
      deedTenantEventRepository.deleteByDateLessThan(lastEventScanDate.minus(Long.parseLong(cleanupHoursPeriodicity.trim()),
                                                                             ChronoUnit.HOURS));
    }
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  @PostConstruct
  protected void init() {
    destroy();
    clientName = redisProperties.getClientName();
    initRedisSubscription();
  }

  @PreDestroy
  protected void destroy() {
    if (this.subscriptionConnection != null && this.subscriptionConnection.isOpen()) {
      this.subscriptionConnection.close();
      this.subscriptionConnection = null;
    }
    if (this.publicationConnection != null && this.publicationConnection.isOpen()) {
      this.publicationConnection.close();
      this.publicationConnection = null;
    }
  }

  /**
   * An internal method to trigger events coming from Redis through subscription
   * listener
   * 
   * @param redisEvent {@link Map} of events that are published on Redis. This
   *                     Map will have as value, the persistent event Identifier
   *                     (ES Id) and as value, the eventName.
   */
  private void triggerEventFromRedis(Map<String, String> redisEvent) {
    if (!MapUtils.isEmpty(redisEvent)) {
      executeElasticSearchScanning(() -> redisEvent.forEach((eventId, eventName) -> {
        if (hasListeners(eventName)) {
          triggerElasticsearchEvent(eventId);
        }
      }));
    }
  }

  /**
   * This method is used to seek for instant events triggering remotely on other
   * consumers.
   * 
   * @param eventId   persistent Elasticsearch unique Identifier
   * @param eventName event name used by
   *                    {@link EventListener#getSupportedEvents()}
   */
  private void publishEventOnRedis(String eventId, String eventName) {
    // Trigger Event Remotely
    try {
      RedisAsyncCommands<String, String> async = getPublicationConnection().async();
      String eventJsonString = serializeObjectToJson(Collections.singletonMap(eventId, eventName));
      RedisFuture<Long> publish = async.publish(getChannelName(), eventJsonString);
      if (publish == null) {
        throw new IllegalStateException("Redis returned null publication event");
      }
      if (StringUtils.isNotBlank(publish.getError())) {
        throw new IllegalStateException("Redis returned an error while publishing event: " + publish.getError());
      }
    } catch (Exception e) {
      LOG.trace("{} - Redis connection failure, event {}/{} will not be triggered remotely instantly",
                getClientName(),
                eventName,
                eventId,
                e);
    }
  }

  /**
   * Publish event on Elasticsearch by simply persisting it in a dedicated index
   * after adding the current consumer name in list of consumers which already
   * triggered the event locally.
   * 
   * @param  event {@link Event} to publish on ElasticSearch
   * @return       persisted {@link DeedTenantEvent}
   */
  private DeedTenantEvent publishEventOnElasticsearch(Event event) {
    String eventJsonString = serializeObjectToJson(event);
    List<String> consumers = Collections.singletonList(getClientName());
    return deedTenantEventRepository.save(new DeedTenantEvent(event.getEventName(),
                                                              eventJsonString,
                                                              consumers,
                                                              Instant.now()));
  }

  /**
   * Invoke local event listeners
   * 
   * @param event {@link Event} to trigger locally
   */
  @SuppressWarnings("deprecation")
  private void triggerEventLocally(Event event) {
    String eventName = event.getEventName();
    Object data = event.getData();

    List<EventListener<?>> listenerList = getListeners().get(eventName);
    if (!CollectionUtils.isEmpty(listenerList)) {
      LOG.debug("{} - Trigger event {} locally with data {}",
                getClientName(),
                eventName,
                data);

      listenerList.forEach(listener -> {
        LOG.debug("  {} - Trigger listener {}  with event {} with data {}",
                  getClientName(),
                  listener.getName(),
                  eventName,
                  data);
        listener.handleEvent(eventName, data);
      });
    }
  }

  protected Map<String, List<EventListener<?>>> getListeners() {// NOSONAR
    if (listeners == null) {
      registerListeners();
    }
    return listeners;
  }

  /**
   * Retrieves and registers automatically the list of listeners registered in
   * Spring {@link ApplicationContext} as a {@link Component} and that extends
   * {@link EventListener}
   */
  @SuppressWarnings("rawtypes")
  private synchronized void registerListeners() {
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

  private void triggerElasticsearchEvent(String persistentEventId) {
    DeedTenantEvent persistentEvent = deedTenantEventRepository.findById(persistentEventId).orElse(null);
    if (persistentEvent != null) {
      triggerElasticsearchEvent(persistentEvent);
    }
  }

  private void triggerElasticsearchEvent(DeedTenantEvent persistentEvent) {
    if (persistentEvent.getConsumers().contains(getClientName())) {
      return;
    }
    try {
      Event event = OBJECT_MAPPER.readValue(persistentEvent.getObjectJson(), Event.class);
      if (event == null) {
        LOG.debug("Can't parse Event Class of name {}. Ignore the event, it should be meant to another type of clients",
                  persistentEvent.getEventName());
      } else {
        triggerEventLocally(event);
      }
      addElasticsearchEventCurrentConsumer(persistentEvent);
    } catch (Exception e) {
      LOG.warn("{} - Error while triggering event from ES {}",
               getClientName(),
               persistentEvent,
               e);
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
      LOG.warn("{} - Error saving consumer name of event {}/{}",
               getClientName(),
               event.getId(),
               event.getEventName());
    }
  }

  private Instant getLastELasticsearchScanDate() {
    String settingName = getSettingName(ES_LAST_SCANNED_DATE_SETTING_NAME);
    String lastEventScanDate = settingService.get(settingName);
    if (StringUtils.isBlank(lastEventScanDate)) {
      return Instant.now();
    } else {
      return Instant.ofEpochMilli(Long.parseLong(lastEventScanDate));
    }
  }

  private void saveLastELasticsearchScanDate(Instant persistedEventDate) {
    String settingName = getSettingName(ES_LAST_SCANNED_DATE_SETTING_NAME);
    settingService.save(settingName, String.valueOf(persistedEventDate.toEpochMilli()));
  }

  private StatefulRedisPubSubConnection<String, String> getPublicationConnection() {
    if (publicationConnection == null || !publicationConnection.isOpen()) {
      publicationConnection = redisClient.connectPubSub();
    }
    return publicationConnection;
  }

  private void initRedisSubscription() {
    try {
      if (this.subscriptionConnection != null && this.subscriptionConnection.isOpen()) {
        return;
      }
      // Add Redis Pub/Sub Listener
      this.subscriptionConnection = redisClient.connectPubSub();
      this.subscriptionConnection.addListener(new RedisPubSubAdapter<String, String>() {
        @Override
        @SuppressWarnings("unchecked")
        public void message(String channel, String message) {
          try {
            Map<String, String> event = OBJECT_MAPPER.readValue(message, Map.class);
            triggerEventFromRedis(event);
          } catch (Exception e) {
            LOG.warn("{} - An error occurred while triggering an event published from Redis: {}",
                     getClientName(),
                     message,
                     e);
          }
        }

        @Override
        public void message(String pattern, String channel, String message) {
          message(channel, message);
        }
      });

      // Subscribe to channel
      this.subscriptionConnection.async().subscribe(getChannelName());
    } catch (Exception e) {
      LOG.warn("{} - Redis connection failure: {}. Events will not be triggered remotely instantly.",
               getClientName(),
               e.getMessage());
    }
  }

  private boolean hasListeners(String eventName) {
    return !CollectionUtils.isEmpty(getListeners().get(eventName));
  }

  private String serializeObjectToJson(Object event) {
    try {
      return OBJECT_MAPPER.writeValueAsString(event);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("An error occurred while parsing POJO object to JSON:" + event, e);
    }
  }

  /**
   * This will avoid to trigger the same event twice coming from Redis &
   * Periodic ES scan tasks This task can be blocker by using a lock because
   * it's triggered asynchronously through Redis or a Scheduled Job.
   * 
   * @param task Elasticsearch persistent event processing task
   */
  private void executeElasticSearchScanning(Runnable task) {
    try {
      long stamp = elasticsearchEventReadingLock.tryWriteLock(3, TimeUnit.SECONDS);
      try {
        task.run();
      } finally {
        elasticsearchEventReadingLock.unlock(stamp);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  private Instant maxInstant(Instant instant1, Instant instant2) {
    if (instant1.isBefore(instant2)) {
      instant1 = instant2;
    }
    return instant1;
  }

  private String getSettingName(String settingPrefix) {
    return settingPrefix + "-" + getClientName();
  }

  private String getChannelName() {
    return redisProperties.getChannelName();
  }

  private String getClientName() {
    return clientName;
  }

}
