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
package io.meeds.deeds.listener;

import java.util.List;

import org.springframework.stereotype.Component;

import io.meeds.deeds.service.ListenerService;

/**
 * An API Interface that can be used to implement a listener that will be
 * triggered once an event is published using {@link ListenerService}.
 * In order to inject a new Listener instace, you could implement this interface
 * and add your listener as a Spring {@link Component}. The
 * {@link ListenerService}, at startup time, will iterate over all beans
 * implementing this UI to inject it automatically as a listener. else, if you
 * prefer add it manually, you can simply use
 * {@link ListenerService#addListener(EventListener)}.
 * 
 * @param <T> Event Data Class Type
 */
public interface EventListener<T> {

  /**
   * @return Unique name of a listener that will be needed to be able to
   *           identify the listener in the list of listeners to be able to
   *           remove it
   */
  String getName();

  /**
   * @return {@link List} of supported events that will be used to trigger the
   *           listener if one of thos events had been published
   */
  List<String> getSupportedEvents();

  /**
   * Handle a published event, must not be overriden, use onEvent instead
   * 
   * @param eventName Event name
   * @param data Event data published at the same time by event producer
   * @deprecated used for internal casting and must not be overridden
   */
  @SuppressWarnings("unchecked")
  @Deprecated
  default void handleEvent(String eventName, Object data) { // NOSONAR
    onEvent(eventName, (T) data);
  }

  /**
   * Handle a published event
   * 
   * @param eventName Event name
   * @param data Event data published at the same time by event producer
   */
  void onEvent(String eventName, T data);
}
