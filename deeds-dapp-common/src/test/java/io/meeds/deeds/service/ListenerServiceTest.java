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
import static io.meeds.deeds.service.ListenerService.ES_LAST_SCANNED_DATE_SETTING_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import io.lettuce.core.RedisClient;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands;
import io.meeds.deeds.elasticsearch.model.DeedTenantEvent;
import io.meeds.deeds.listener.EventListener;
import io.meeds.deeds.listerner.model.Event;
import io.meeds.deeds.redis.RedisConfigurationProperties;
import io.meeds.deeds.storage.DeedTenantEventRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@SpringBootTest(classes = {
    ListenerService.class,
    RedisConfigurationProperties.class,
}, properties = {
    "meeds.redis.clientName=" + ListenerServiceTest.CLIENT_NAME,
    "meeds.elasticsearch.listener.events.cleanupHoursPeriodicity=" + ListenerServiceTest.CLEANUP_HOURS_PERIODICITY,
})
class ListenerServiceTest {

  public static final String                    CLIENT_NAME               = "TestClientName";

  public static final long                      CLEANUP_HOURS_PERIODICITY = 1l;

  private static final String                   EVENT_NAME                = "test.event";

  private static final String                   OTHER_EVENT_NAME          = "test.otherevent";

  private static final String                   LISENER_NAME              = "listener.event";

  @MockBean
  private RedisClient                           redisClient;

  @MockBean
  private SettingService                        settingService;

  @MockBean
  private DeedTenantEventRepository             deedTenantEventRepository;

  @Autowired
  private ListenerService                       listenerService;

  long                                          persistentEventId;

  EventData                                     eventData;

  ListenerTest                                  eventListener;

  StatefulRedisPubSubConnection<String, String> redisConnection;

  RedisPubSubAdapter<String, String>            redisListener;

  RedisPubSubAsyncCommands<String, String>      redisCommand;

  @BeforeEach
  public void setup() {
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
    initRedis();

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

  @Test
  void testTriggerEventFromRedis() throws Exception {
    initRedis();

    Instant eventDate = Instant.now();
    List<String> consumers = Collections.emptyList();
    String objectJson = OBJECT_MAPPER.writeValueAsString(new Event(EVENT_NAME, eventData, eventData.getClass().getName()));
    DeedTenantEvent deedTenantEvent = new DeedTenantEvent(EVENT_NAME, objectJson, consumers, eventDate);
    deedTenantEvent.setId("PersistentEventId");
    when(deedTenantEventRepository.findById(deedTenantEvent.getId())).thenReturn(Optional.of(deedTenantEvent));

    Map<String, String> redisEvent = Collections.singletonMap(deedTenantEvent.getId(), EVENT_NAME);
    redisListener.message(null, "channel", OBJECT_MAPPER.writeValueAsString(redisEvent));

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
  void testTriggerEventFromRedisAlreadyHandled() throws Exception {
    initRedis();

    Instant eventDate = Instant.now();
    List<String> consumers = Collections.singletonList(CLIENT_NAME);
    String objectJson = OBJECT_MAPPER.writeValueAsString(new Event(EVENT_NAME, eventData, eventData.getClass().getName()));
    DeedTenantEvent deedTenantEvent = new DeedTenantEvent(EVENT_NAME, objectJson, consumers, eventDate);
    deedTenantEvent.setId("PersistentEventId");
    lenient().when(deedTenantEventRepository.findById(deedTenantEvent.getId())).thenReturn(Optional.of(deedTenantEvent));

    Map<String, String> redisEvent = Collections.singletonMap(deedTenantEvent.getId(), EVENT_NAME);
    redisListener.message(null, "channel", OBJECT_MAPPER.writeValueAsString(redisEvent));

    assertEquals(0, eventListener.getEventCount());
    assertNull(eventListener.getEventData());
    assertNull(eventListener.getEventName());

    verify(deedTenantEventRepository, never()).save(any());
  }

  @Test
  void testDestroy() {
    initRedis();

    listenerService.destroy();
    verify(redisConnection, atLeast(1)).close();
  }

  @SuppressWarnings("unchecked")
  private void initRedis() {
    if (redisListener == null) {
      redisConnection = Mockito.mock(StatefulRedisPubSubConnection.class);
      when(redisClient.connectPubSub()).thenReturn(redisConnection);
      Mockito.doAnswer(new Answer<Object>() {
        @Override
        public Object answer(InvocationOnMock invocation) throws Throwable {
          redisListener = invocation.getArgument(0);
          return null;
        }
      }).when(redisConnection).addListener(any(RedisPubSubAdapter.class));

      redisCommand = Mockito.mock(RedisPubSubAsyncCommands.class);
      when(redisConnection.async()).thenReturn(redisCommand);
      when(redisConnection.isOpen()).thenReturn(true);
    }
    listenerService.init();
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
