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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.apache.commons.codec.binary.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.test.context.TestPropertySource;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.pubsub.RedisPubSubListener;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands;
import io.meeds.deeds.listener.EventListener;
import io.meeds.deeds.model.DeedTenantEvent;
import io.meeds.deeds.redis.Listener;
import io.meeds.deeds.redis.RedisConfigurationProperties;
import io.meeds.deeds.redis.model.Event;
import io.meeds.deeds.service.ListenerServiceTest.FakeEventListener;
import io.meeds.deeds.service.ListenerServiceTest.ListenerServiceTestConfiguration;
import io.meeds.deeds.storage.DeedTenantEventRepository;
import lombok.Getter;
import lombok.Setter;

@SpringBootTest(classes = {
    ListenerServiceTestConfiguration.class,
    ListenerService.class,
    FakeEventListener.class,
})
@EnableConfigurationProperties(value = RedisConfigurationProperties.class)
@TestPropertySource(properties = {
    "meeds.redis.channelName=" + ListenerServiceTest.CHANNEL_NAME_VALUE,
    "meeds.redis.clientName=" + ListenerServiceTest.CLIENT_NAME,
})
class ListenerServiceTest {

  public static final String                       CHANNEL_NAME_VALUE       = "CHANNEL_NAME";

  public static final String                       CLIENT_NAME              = "CLIENT_NAME";

  public static final String                       FAKE_EVENT_LISTENER_NAME = "fakeEventListener";

  @MockBean
  private DeedTenantEventRepository                deedTenantEventRepository;

  @Autowired
  private ListenerService                          listenerService;

  @Autowired
  private Listener                                 listener;

  @Autowired
  @Qualifier("asyncSub")
  private RedisPubSubAsyncCommands<String, String> asyncSub;

  @Autowired
  @Qualifier("asyncPub")
  private RedisPubSubAsyncCommands<String, String> asyncPub;

  @BeforeEach
  public void init() {
    listenerService.redisListener = listener;
    listenerService.getListeners().clear();
  }

  @Test
  void testAddListener() {
    assertNotNull(listenerService);

    String eventListenerName = "eventListenerName";

    String eventName = "eventName";
    String eventName2 = "eventName2";

    EventListener<String> eventListener = new EventListener<String>() {
      @Override
      public String getName() {
        return eventListenerName;
      }

      @Override
      public void onEvent(String eventName, String data) {
        // Nothing to Test
      }

      @Override
      public List<String> getSupportedEvents() {
        return Arrays.asList(eventName, eventName2);
      }
    };

    listenerService.addListener(eventListener);

    listenerService.initListeners();
    Map<String, List<EventListener<?>>> listeners = listenerService.getListeners();
    assertNotNull(listeners);
    assertEquals(3, listeners.size());
    assertNotNull(listener);
  }

  @Test
  void testRemoveListener() {
    assertNotNull(listenerService);

    String eventListenerName = "eventListenerName";

    String eventName = "eventName";
    String eventName2 = "eventName2";

    EventListener<String> eventListener = new EventListener<String>() {
      @Override
      public String getName() {
        return eventListenerName;
      }

      @Override
      public void onEvent(String eventName, String data) {
        // Nothing to Test
      }

      @Override
      public List<String> getSupportedEvents() {
        return Arrays.asList(eventName, eventName2);
      }
    };

    listenerService.addListener(eventListener);

    listenerService.initListeners();
    Map<String, List<EventListener<?>>> listeners = listenerService.getListeners();
    assertNotNull(listeners);
    assertEquals(3, listeners.size());
    assertNotNull(listener);

    listenerService.removeListsner(eventListenerName);
    listeners = listenerService.getListeners();
    assertNotNull(listeners);
    assertEquals(1, listeners.size());

    listenerService.removeListsner(eventListenerName);
    listeners = listenerService.getListeners();
    assertNotNull(listeners);
    assertEquals(1, listeners.size());

    listenerService.removeListsner(FAKE_EVENT_LISTENER_NAME);
    listeners = listenerService.getListeners();
    assertNotNull(listeners);
    assertEquals(0, listeners.size());

    listenerService.addListener(eventListener);
    listeners = listenerService.getListeners();
    assertNotNull(listeners);
    assertEquals(2, listeners.size());

    listenerService.removeListsner(eventName2, eventListenerName);
    listeners = listenerService.getListeners();
    assertNotNull(listeners);
    assertEquals(1, listeners.size());
  }

  @SuppressWarnings("unchecked")
  @Test
  void testPublishEvent() throws IOException {
    assertNotNull(listenerService);

    String eventListenerName = "eventListenerName";

    String eventName1 = "eventName1";
    String eventName2 = "eventName2";

    AtomicInteger event1TriggerCount = new AtomicInteger(0);
    AtomicInteger event2TriggerCount = new AtomicInteger(0);

    EventData eventData = new EventData();
    eventData.data = 5;

    EventListener<EventData> eventListener = new EventListener<>() {
      @Override
      public String getName() {
        return eventListenerName;
      }

      @Override
      public void onEvent(String eventName, EventData data) {
        if (StringUtils.equals(eventName, eventName1)) {
          event1TriggerCount.incrementAndGet();
        } else if (StringUtils.equals(eventName, eventName2)) {
          event2TriggerCount.incrementAndGet();
        }
        assertEquals(eventData.data, data.data);
      }

      @Override
      public List<String> getSupportedEvents() {
        return Arrays.asList(eventName1, eventName2);
      }
    };

    listenerService.addListener(eventListener);

    when(asyncPub.publish(anyString(), anyString())).thenThrow(new RuntimeException());

    listenerService.publishEvent("unkownEvent", eventData);
    assertEquals(0, event1TriggerCount.get());
    assertEquals(0, event2TriggerCount.get());

    listenerService.publishEvent(eventName1, eventData);
    assertEquals(1, event1TriggerCount.get());
    assertEquals(0, event2TriggerCount.get());

    reset(asyncPub);
    RedisFuture<Long> publish = mock(RedisFuture.class);
    when(asyncPub.publish(anyString(), anyString())).thenReturn(publish);

    listenerService.publishEvent(eventName1, eventData);
    assertEquals(1, event1TriggerCount.get());
    assertEquals(0, event2TriggerCount.get());

    when(publish.getError()).thenReturn("FAKE PUBLICATION ERROR");
    listenerService.publishEvent(eventName1, eventData);
    assertEquals(2, event1TriggerCount.get());
    assertEquals(0, event2TriggerCount.get());

    listenerService.publishEvent(eventName2, eventData);
    assertEquals(2, event1TriggerCount.get());
    assertEquals(1, event2TriggerCount.get());

    listenerService.publishEvent(eventName2, eventData);
    assertEquals(2, event1TriggerCount.get());
    assertEquals(2, event2TriggerCount.get());

    listenerService.publishEvent(eventName2, eventData);
    assertEquals(2, event1TriggerCount.get());
    assertEquals(3, event2TriggerCount.get());
  }

  @SuppressWarnings("unchecked")
  @Test
  void testTriggerEvent() {
    String eventListenerName = "eventListenerName";
    String eventName1 = "eventName1";
    AtomicInteger triggerCount = new AtomicInteger(0);

    EventListener<EventData> eventListener = new EventListener<>() {
      @Override
      public void onEvent(String eventName, EventData data) {
        triggerCount.incrementAndGet();
      }

      @Override
      public String getName() {
        return eventListenerName;
      }

      @Override
      public List<String> getSupportedEvents() {
        return Arrays.asList(eventName1);
      }
    };
    reset(asyncPub);

    listenerService.addListener(eventListener);
    listenerService.triggerEvent(new Event("anotherEvent", new EventData(), EventData.class.getName()));
    assertEquals(0, triggerCount.get());

    listenerService.triggerEvent(new Event(eventName1, new EventData(), EventData.class.getName()));
    assertEquals(1, triggerCount.get());
  }

  @Test
  void testInitElasticsearchEventScanning() throws JsonProcessingException {
    String id = "eventId";
    String eventName = "eventName";
    String existingConsumer = "anotherClient";

    DeedTenantEvent deedTenantEvent = createDeedTenantEvent(id, eventName, new EventData(), existingConsumer);
    @SuppressWarnings("unchecked")
    Page<DeedTenantEvent> page = mock(Page.class);
    when(deedTenantEventRepository.findByConsumersNotOrderByDateDesc(CLIENT_NAME, Pageable.ofSize(1))).thenReturn(page);
    when(page.getContent()).thenReturn(Collections.singletonList(deedTenantEvent));
    when(page.getSize()).thenReturn(1);

    assertNotEquals(deedTenantEvent.getDate(), listenerService.getLastEventScanDate());
    listenerService.initElasticsearchEventScanning();
    assertEquals(deedTenantEvent.getDate(), listenerService.getLastEventScanDate());
  }

  @Test
  void testTriggerElasticSearchEvents() throws JsonProcessingException {
    String id = "eventId";
    String eventName = "eventName";
    String existingConsumer = "anotherClient";

    EventData eventData = new EventData();
    eventData.data = 2;
    DeedTenantEvent deedTenantEvent = createDeedTenantEvent(id, eventName, eventData, existingConsumer);
    when(deedTenantEventRepository.findByDateGreaterThanEqualAndConsumersNotOrderByDateAsc(listenerService.getLastEventScanDate(),
                                                                                           CLIENT_NAME)).thenReturn(Stream.of(deedTenantEvent));

    AtomicInteger triggerCount = new AtomicInteger(0);
    EventListener<EventData> eventListener = new EventListener<>() {
      @Override
      public void onEvent(String eventName, EventData data) {
        triggerCount.incrementAndGet();
      }

      @Override
      public String getName() {
        return "FAKE_EVENT";
      }

      @Override
      public List<String> getSupportedEvents() {
        return Arrays.asList(eventName);
      }
    };
    listenerService.addListener(eventListener);

    listenerService.triggerElasticSearchEvents();
    assertEquals(1, triggerCount.get());
    assertEquals(deedTenantEvent.getDate(), listenerService.getLastEventScanDate());
  }

  @Test
  void testCleanupElasticSearchEvents() throws JsonProcessingException {
    String id = "eventId";
    String eventName = "eventName";
    String existingConsumer = "anotherClient";

    EventData eventData = new EventData();
    eventData.data = 2;
    DeedTenantEvent deedTenantEvent = createDeedTenantEvent(id, eventName, eventData, existingConsumer);
    when(deedTenantEventRepository.findByDateLessThan(listenerService.getLastEventScanDate()
                                                                     .minus(12, ChronoUnit.HOURS)))
                                                                                                   .thenReturn(Stream.of(deedTenantEvent));

    listenerService.cleanupElasticsearchEvents();
    verify(deedTenantEventRepository, times(1)).deleteById(deedTenantEvent.getId());
  }

  private DeedTenantEvent createDeedTenantEvent(String id, String eventName, EventData eventData,
                                                String existingConsumer) throws JsonProcessingException {
    Event event = new Event(eventName, eventData, EventData.class.getName());
    String objectJson = OBJECT_MAPPER.writeValueAsString(event);
    List<String> consumers = Arrays.asList(existingConsumer);
    Instant date = listenerService.getLastEventScanDate().plus(2, ChronoUnit.SECONDS);
    DeedTenantEvent deedTenantEvent = new DeedTenantEvent(id, eventName, objectJson, consumers, date);
    return deedTenantEvent;
  }

  @Configuration
  public static class ListenerServiceTestConfiguration {

    private RedisClient                              redisClient;

    private Listener                                 listener;

    private RedisPubSubAsyncCommands<String, String> asyncSub;

    private RedisPubSubAsyncCommands<String, String> asyncPub;

    @Bean
    @Primary
    @SuppressWarnings("unchecked")
    public RedisClient redisClient() {
      redisClient = mock(RedisClient.class);
      StatefulRedisPubSubConnection<String, String> connection = mock(StatefulRedisPubSubConnection.class);
      when(redisClient.connectPubSub()).thenReturn(connection);
      doAnswer(invocation -> {
        listener = invocation.getArgument(0, Listener.class);
        return null;
      }).when(connection).addListener(any(RedisPubSubListener.class));

      asyncSub = mock(RedisPubSubAsyncCommands.class);
      when(connection.async()).thenReturn(asyncSub);

      return redisClient;
    }

    @Bean
    public Listener listener() {
      return listener;
    }

    @Bean("asyncSub")
    public RedisPubSubAsyncCommands<String, String> asyncSub() {
      return asyncSub;
    }

    @Bean("asyncPub")
    @SuppressWarnings("unchecked")
    public RedisPubSubAsyncCommands<String, String> asyncPub(ListenerService listenerService) {
      asyncPub = mock(RedisPubSubAsyncCommands.class);
      listenerService.publicationConnection = mock(StatefulRedisPubSubConnection.class);
      when(listenerService.publicationConnection.isOpen()).thenReturn(true);
      when(listenerService.publicationConnection.async()).thenReturn(asyncPub);
      return asyncPub;
    }

  }

  @Component
  public static class FakeEventListener implements EventListener<String> {

    @Override
    public void onEvent(String eventName, String data) {
      // Nothing to change
    }

    @Override
    public List<String> getSupportedEvents() {
      return Arrays.asList("fakeEvent");
    }

    @Override
    public String getName() {
      return FAKE_EVENT_LISTENER_NAME;
    }
  }

  public static class EventData {
    @Getter
    @Setter
    int data;
  }
}
