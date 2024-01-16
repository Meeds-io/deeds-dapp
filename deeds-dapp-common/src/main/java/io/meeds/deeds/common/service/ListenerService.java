/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
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
package io.meeds.deeds.common.service;

import static io.meeds.deeds.common.listerner.model.EventSerialization.OBJECT_MAPPER;

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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.meeds.deeds.common.elasticsearch.model.DeedTenantEvent;
import io.meeds.deeds.common.elasticsearch.storage.DeedTenantEventRepository;
import io.meeds.deeds.common.listener.EventListener;
import io.meeds.deeds.common.listerner.model.Event;
import io.meeds.deeds.common.scheduling.task.ListenerEventCleanupTask;
import io.meeds.deeds.common.scheduling.task.ListenerEventTriggerTask;

@Component
public class ListenerService {

  public static final String                                 ES_LAST_SCANNED_DATE_SETTING_NAME = "ES-LAST-SCANNED-DATE";

  public static final Logger                                 LOG                               =
                                                                 LoggerFactory.getLogger(ListenerService.class);

  protected static final List<EventListener<?>>              EVENT_LISTENERS                   = new ArrayList<>();

  protected static final Map<String, List<EventListener<?>>> LISTENERS                         = new HashMap<>();

  protected static final StampedLock                         ELASTIC_SEARCH_EVENT_READING_LOCK = new StampedLock();

  protected static boolean                                   persistentFeatureEnabled          = true;

  @Autowired(required = false)
  private SettingService                                     settingService;

  @Autowired(required = false)
  private DeedTenantEventRepository                          deedTenantEventRepository;

  @Value("${meeds.elasticsearch.listener.events.cleanupHoursPeriodicity:}")
  private String                                             cleanupHoursPeriodicity;

  @Value("${meeds.elasticsearch.listener.clientName}")
  protected String                                           esClientName;

  /**
   * Triggers an event locally and remotely using persistent event listening
   * based on a dedicated Elasticsearch index to persist the event and generates
   * a unique identifier for it. A Scheduled Job
   * {@link ListenerEventTriggerTask} on each consumer will retrieve events
   * periodically.
   * 
   * @param eventName Event Name as configured for listeners in
   *                    {@link EventListener#getSupportedEvents()}
   * @param data      Data Object to pass to listeners when executing
   *                    {@link EventListener#onEvent(String, Object)}
   */
  public void publishEvent(String eventName, Object data) {
    LOG.debug("{} - Publish event {} locally with data {}",
              esClientName,
              eventName,
              data);

    Event event = new Event(eventName, data, data == null ? null : data.getClass().getName());
    triggerEventLocally(event);
    publishEventOnElasticsearch(event);
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
      supportedEvents.forEach(eventName -> LISTENERS.computeIfAbsent(eventName, key -> new ArrayList<>()).add(listener));
    }
  }

  /**
   * Removes all {@link EventListener} that have {@link EventListener#getName()}
   * the designated name in param
   * 
   * @param listenerName name of listeners to remove
   */
  public void removeListsner(String listenerName) {
    Iterator<Entry<String, List<EventListener<?>>>> iterator = LISTENERS.entrySet().iterator();
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
    Iterator<Entry<String, List<EventListener<?>>>> iterator = LISTENERS.entrySet().iterator();
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
    if (!this.isUsePerisistentEvents()) {
      return;
    }
    executeElasticSearchScanning(() -> {
      Instant lastEventScanDate = getLastELasticsearchScanDate();
      List<DeedTenantEvent> events;
      events = deedTenantEventRepository.findByDateGreaterThanEqualAndConsumersNotOrderByDateAsc(lastEventScanDate, esClientName);
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
    if (!this.isUsePerisistentEvents()) {
      return;
    }
    if (StringUtils.isNotBlank(cleanupHoursPeriodicity)) {
      Instant lastEventScanDate = getLastELasticsearchScanDate();
      deedTenantEventRepository.deleteByDateLessThan(lastEventScanDate.minus(Long.parseLong(cleanupHoursPeriodicity.trim()),
                                                                             ChronoUnit.HOURS));
    }
  }

  private boolean isUsePerisistentEvents() {
    return persistentFeatureEnabled
        && (StringUtils.contains(esClientName, "dApp")
            || StringUtils.contains(esClientName, "tenant-provisioning"));
  }

  /**
   * Publish event on Elasticsearch by simply persisting it in a dedicated index
   * after adding the current consumer name in list of consumers which already
   * triggered the event locally.
   * 
   * @param event {@link Event} to publish on ElasticSearch
   */
  private void publishEventOnElasticsearch(Event event) {
    if (!this.isUsePerisistentEvents()) {
      return;
    }
    String eventJsonString = serializeObjectToJson(event);
    List<String> consumers = Collections.singletonList(esClientName);
    deedTenantEventRepository.save(new DeedTenantEvent(event.getEventName(),
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

    List<EventListener<?>> listenerList = LISTENERS.get(eventName);
    if (!CollectionUtils.isEmpty(listenerList)) {
      LOG.debug("{} - Trigger event {} locally with data {}",
                esClientName,
                eventName,
                data);

      listenerList.forEach(listener -> {
        LOG.debug("  {} - Trigger listener {}  with event {} with data {}",
                  esClientName,
                  listener.getName(),
                  eventName,
                  data);
        listener.handleEvent(eventName, data);
      });
    }
  }

  private void triggerElasticsearchEvent(DeedTenantEvent persistentEvent) {
    if (persistentEvent.getConsumers().contains(esClientName)) {
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
               esClientName,
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
      consumers.add(esClientName);
      event.setConsumers(consumers);
      deedTenantEventRepository.save(event);
    } catch (Exception e) {
      LOG.warn("{} - Error saving consumer name of event {}/{}",
               esClientName,
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

  private boolean hasListeners(String eventName) {
    return !CollectionUtils.isEmpty(LISTENERS.get(eventName));
  }

  private String serializeObjectToJson(Object event) {
    try {
      return OBJECT_MAPPER.writeValueAsString(event);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("An error occurred while parsing POJO object to JSON:" + event, e);
    }
  }

  /**
   * @param task Elasticsearch persistent event processing task
   */
  private void executeElasticSearchScanning(Runnable task) {
    try {
      long stamp = ELASTIC_SEARCH_EVENT_READING_LOCK.tryWriteLock(3, TimeUnit.SECONDS);
      try {
        task.run();
      } finally {
        ELASTIC_SEARCH_EVENT_READING_LOCK.unlock(stamp);
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
    return settingPrefix + "-" + esClientName;
  }

}
