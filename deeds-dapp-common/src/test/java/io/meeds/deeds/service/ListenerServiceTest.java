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

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.codec.binary.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.test.context.*;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.lettuce.core.RedisClient;
import io.lettuce.core.pubsub.RedisPubSubListener;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands;
import io.meeds.deeds.listener.EventListener;
import io.meeds.deeds.model.DeedMetadata;
import io.meeds.deeds.redis.RedisConfigurationProperties;
import io.meeds.deeds.redis.RedisConfigurationPropertiesTest;
import io.meeds.deeds.service.ListenerService.Listener;
import io.meeds.deeds.service.ListenerServiceTest.FakeEventListener;
import io.meeds.deeds.service.ListenerServiceTest.ListenerServiceTestConfiguration;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
    classes = {
        ListenerServiceTestConfiguration.class,
        ListenerService.class,
        FakeEventListener.class,
        RedisConfigurationProperties.class
    }
)
@TestPropertySource(
    properties = {
        "meeds.redis.channelName=" + RedisConfigurationPropertiesTest.CHANNEL_NAME_VALUE,
    }
)
public class ListenerServiceTest {

  public static final String       FAKE_EVENT_LISTENER_NAME = "fakeEventListener";

  public static final ObjectMapper OBJECT_MAPPER            = new ObjectMapper();

  static {
    // Workaround when Jackson is defined in shared library with different
    // version and without artifact jackson-datatype-jsr310
    OBJECT_MAPPER.registerModule(new JavaTimeModule());
  }

  @Autowired
  private ListenerService                          listenerService;

  @Autowired
  private Listener                                 listener;

  @Autowired
  private RedisPubSubAsyncCommands<String, String> async;

  @Before
  public void init() {
    if (listenerService != null && listenerService.getListeners() != null) {
      listenerService.getListeners().clear();
    }
  }

  @Test
  public void testAddListener() {
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
  public void testRemoveListener() {
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
  public void testPublishEvent() {
    assertNotNull(listenerService);
    assertNotNull(async);

    String eventListenerName = "eventListenerName";

    String eventName1 = "eventName1";
    String eventName2 = "eventName2";

    AtomicInteger event1TriggerCount = new AtomicInteger(0);
    AtomicInteger event2TriggerCount = new AtomicInteger(0);

    class EventData {
      Map<String, DeedMetadata> data;
    }

    EventData eventData;
    try {
      URL deedMetadatasResource = getClass().getClassLoader().getResource("metadatas.json");
      Map<String, DeedMetadata> eventDataMap = OBJECT_MAPPER.readerForMapOf(DeedMetadata.class).readValue(deedMetadatasResource);
      eventData = new EventData();
      eventData.data = eventDataMap;
    } catch (IOException e) {
      fail(e);
      return;
    }

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

    when(async.publish(anyString(), anyString())).thenThrow(new RuntimeException());

    listenerService.publishEvent("unkownEvent", eventData);
    assertEquals(0, event1TriggerCount.get());
    assertEquals(0, event2TriggerCount.get());

    listenerService.publishEvent(eventName1, eventData);
    assertEquals(1, event1TriggerCount.get());
    assertEquals(0, event2TriggerCount.get());

    reset(async);
    when(async.publish(anyString(), anyString())).thenAnswer(invocation -> {
      String channelName = invocation.getArgument(0, String.class);
      String message = invocation.getArgument(1, String.class);
      listener.message(channelName, message);
      return null;
    });

    listenerService.publishEvent(eventName1, eventData);
    assertEquals(2, event1TriggerCount.get());
    assertEquals(0, event2TriggerCount.get());

    reset(async);
    when(async.publish(anyString(), anyString())).thenAnswer(invocation -> {
      String channelName = invocation.getArgument(0, String.class);
      String message = invocation.getArgument(1, String.class);
      listener.message(null, channelName, message);
      return null;
    });

    listenerService.publishEvent(eventName2, eventData);
    assertEquals(2, event1TriggerCount.get());
    assertEquals(1, event2TriggerCount.get());

    reset(async);
    when(async.publish(anyString(), anyString())).thenAnswer(invocation -> {
      listener.message(null, null, null);
      return null;
    });

    listenerService.publishEvent(eventName2, eventData);
    assertEquals(2, event1TriggerCount.get());
    assertEquals(2, event2TriggerCount.get());

    reset(async);
    when(async.publish(anyString(), anyString())).thenAnswer(invocation -> {
      listener.message(null, null);
      return null;
    });

    listenerService.publishEvent(eventName2, eventData);
    assertEquals(2, event1TriggerCount.get());
    assertEquals(3, event2TriggerCount.get());
  }

  @Profile("test")
  @Configuration
  public static class ListenerServiceTestConfiguration {

    private Listener                                 listener;

    private RedisPubSubAsyncCommands<String, String> async;

    @Bean
    @Primary
    @SuppressWarnings("unchecked")
    public RedisClient redisClient() {
      RedisClient redisClient = mock(RedisClient.class);
      StatefulRedisPubSubConnection<String, String> connection = mock(StatefulRedisPubSubConnection.class);
      when(redisClient.connectPubSub()).thenReturn(connection);
      doAnswer(invocation -> {
        listener = invocation.getArgument(0, Listener.class);
        return null;
      }).when(connection).addListener(any(RedisPubSubListener.class));

      async = mock(RedisPubSubAsyncCommands.class);
      when(connection.async()).thenReturn(async);
      return redisClient;
    }

    @Bean
    public Listener listener() {
      return listener;
    }

    @Bean
    public RedisPubSubAsyncCommands<String, String> async() {
      return async;
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

}
