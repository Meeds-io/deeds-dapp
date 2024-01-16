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
import static io.meeds.deeds.common.service.ListenerService.ES_LAST_SCANNED_DATE_SETTING_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import io.meeds.deeds.common.elasticsearch.model.DeedTenantEvent;
import io.meeds.deeds.common.elasticsearch.storage.DeedTenantEventRepository;
import io.meeds.deeds.common.listener.EventListener;
import io.meeds.deeds.common.listerner.model.Event;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@SpringBootTest(classes = {
    ListenerService.class,
}, properties = {
    "meeds.elasticsearch.listener.clientName=" + ListenerServiceTest.CLIENT_NAME,
    "meeds.elasticsearch.listener.events.cleanupHoursPeriodicity=" + ListenerServiceTest.CLEANUP_HOURS_PERIODICITY,
})
class ListenerServiceTest {

  public static final String        CLIENT_NAME               = "dApp";

  public static final long          CLEANUP_HOURS_PERIODICITY = 1l;

  private static final String       EVENT_NAME                = "test.event";

  private static final String       OTHER_EVENT_NAME          = "test.otherevent";

  private static final String       LISENER_NAME              = "listener.event";

  @MockBean
  private SettingService            settingService;

  @MockBean
  private DeedTenantEventRepository deedTenantEventRepository;

  @Autowired
  private ListenerService           listenerService;

  long                              persistentEventId;

  EventData                         eventData;

  ListenerTest                      eventListener;

  @BeforeEach
  public void setup() {
    ListenerService.LISTENERS.clear();
    ListenerService.EVENT_LISTENERS.clear();
    ListenerService.persistentFeatureEnabled = true;

    eventData = newData(2);
    eventListener = new ListenerTest();
    listenerService.addListener(eventListener);
    when(deedTenantEventRepository.save(any())).thenAnswer(invocation -> {
      DeedTenantEvent persistedEvent = invocation.getArgument(0, DeedTenantEvent.class);
      if (persistedEvent.getId() == null) {
        persistedEvent.setId(String.valueOf(++persistentEventId));
      }
      return persistedEvent;
    });

  }

  @Test
  void testPublishEvent() {
    listenerService.publishEvent("otherEvent", eventData);
    assertEquals(0, eventListener.getEventCount());
    assertNull(eventListener.getEventData());
    assertNull(eventListener.getEventName());

    listenerService.publishEvent(EVENT_NAME, eventData);
    assertEquals(1, eventListener.getEventCount());
    assertEquals(eventData, eventListener.getEventData());
    assertEquals(EVENT_NAME, eventListener.getEventName());
  }

  @Test
  void testRemoveListener() {
    listenerService.removeListsner(LISENER_NAME);

    listenerService.publishEvent(EVENT_NAME, eventData);
    assertEquals(0, eventListener.getEventCount());
    assertNull(eventListener.getEventData());
    assertNull(eventListener.getEventName());

    listenerService.publishEvent(OTHER_EVENT_NAME, eventData);
    assertEquals(0, eventListener.getEventCount());
    assertNull(eventListener.getEventData());
    assertNull(eventListener.getEventName());
  }

  @Test
  void testRemoveListenerByEventName() {
    listenerService.removeListsner(EVENT_NAME, LISENER_NAME);

    listenerService.publishEvent(EVENT_NAME, eventData);
    assertEquals(0, eventListener.getEventCount());
    assertNull(eventListener.getEventData());
    assertNull(eventListener.getEventName());

    listenerService.publishEvent(OTHER_EVENT_NAME, eventData);
    assertEquals(1, eventListener.getEventCount());
    assertEquals(eventData, eventListener.getEventData());
    assertEquals(OTHER_EVENT_NAME, eventListener.getEventName());
  }

  @Test
  void testTriggerElasticSearchEvents() throws Exception {
    long currentTimeMillis = System.currentTimeMillis();
    Instant now = Instant.ofEpochMilli(currentTimeMillis);
    String lastScannedValue = String.valueOf(currentTimeMillis);
    when(settingService.get(ES_LAST_SCANNED_DATE_SETTING_NAME + "-" + CLIENT_NAME)).thenReturn(lastScannedValue);

    Instant eventDate = now.plusSeconds(5);
    List<String> consumers = Collections.emptyList();
    String objectJson = OBJECT_MAPPER.writeValueAsString(new Event(EVENT_NAME, eventData, eventData.getClass().getName()));
    List<DeedTenantEvent> events = Collections.singletonList(new DeedTenantEvent(EVENT_NAME, objectJson, consumers, eventDate));
    when(deedTenantEventRepository.findByDateGreaterThanEqualAndConsumersNotOrderByDateAsc(now, CLIENT_NAME)).thenReturn(events);

    assertEquals(0, eventListener.getEventCount());
    assertNull(eventListener.getEventData());
    assertNull(eventListener.getEventName());

    listenerService.triggerElasticSearchEvents();

    assertEquals(1, eventListener.getEventCount());
    assertEquals(eventData, eventListener.getEventData());
    assertEquals(EVENT_NAME, eventListener.getEventName());

    verify(deedTenantEventRepository, times(1)).save(argThat(new ArgumentMatcher<DeedTenantEvent>() {
      @Override
      public boolean matches(DeedTenantEvent deedTenantEvent) {
        return deedTenantEvent != null && deedTenantEvent.getConsumers().contains(CLIENT_NAME);
      }
    }));
  }

  @Test
  void testCleanupElasticsearchEvents() throws Exception {
    long currentTimeMillis = System.currentTimeMillis();
    Instant now = Instant.ofEpochMilli(currentTimeMillis);
    String lastScannedValue = String.valueOf(currentTimeMillis);
    when(settingService.get(ES_LAST_SCANNED_DATE_SETTING_NAME + "-" + CLIENT_NAME)).thenReturn(lastScannedValue);

    listenerService.cleanupElasticsearchEvents();

    verify(deedTenantEventRepository, times(1)).deleteByDateLessThan(now.minus(CLEANUP_HOURS_PERIODICITY,
                                                                               ChronoUnit.HOURS));
  }

  private EventData newData(int data) {
    return new EventData(data);
  }

  public static class ListenerTest implements EventListener<ListenerServiceTest.EventData> {

    @Getter
    private int       eventCount;

    @Getter
    private String    eventName;

    @Getter
    private EventData eventData;

    @Override
    public void onEvent(String name, EventData data) throws Exception {
      eventCount++;
      eventName = name;
      eventData = data;
    }

    @Override
    public List<String> getSupportedEvents() {
      return Arrays.asList(EVENT_NAME, OTHER_EVENT_NAME);
    }

    @Override
    public String getName() {
      return LISENER_NAME;
    }
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class EventData {
    private int data;
  }
}
